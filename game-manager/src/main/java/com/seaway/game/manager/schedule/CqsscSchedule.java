package com.seaway.game.manager.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.seaway.game.common.entity.manager.AwardApiData;
import com.seaway.game.common.entity.manager.AwardApiInfo;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameLotteryWinStatus;
import com.seaway.game.common.utils.ScriptHelper;
import com.seaway.game.common.utils.TimeDuration;
import com.seaway.game.manager.manager.BetManager;
import com.seaway.game.manager.repository.impl.GameLotteryWinManager;
import com.seaway.game.manager.repository.impl.GameUtilManager;

@Slf4j
@Component
public class CqsscSchedule implements ApplicationRunner {

	private final GameLotteryWinManager gameLotteryWinManager;

	private final GameUtilManager gameUtilManager;

	private final BetManager betManager;

	private final BetterNetworkSchedule betterNetworkSchedule;

	private static final int deadlineToAwardSeconds = 60;

	private static final int maxSequenceNumber = 120;

	private final String apiUrl = "http://a.apiplus.net/TC6CA1F30A503BCCFK/cqssc-1.json";

	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			10);

	@Autowired
	public CqsscSchedule(GameLotteryWinManager gameLotteryWinManager,
			GameUtilManager gameUtilManager, BetManager betManager,
			BetterNetworkSchedule betterNetworkSchedule) {
		this.gameLotteryWinManager = gameLotteryWinManager;
		this.gameUtilManager = gameUtilManager;
		this.betManager = betManager;
		this.betterNetworkSchedule = betterNetworkSchedule;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		start(true);
	}

	private void start(boolean first) {
		long delay = 0;
		try {
			delay = getDeadlineSeconds();
			log.info("Delay {} seconds to deadline", delay);
		} catch (Exception e) {
		}

		Runnable r = () -> {
			deadline(first);
		};

		executor.schedule(r, delay, TimeUnit.SECONDS);
	}

	private void deadline(boolean first) {
		TimeDuration td = new TimeDuration();
		try {
			gameLotteryWinManager.saveDeadline();

			ScriptHelper.execScript(ScriptHelper.getScriptPath("clean.sh"));

			betterNetworkSchedule.pollNetworks();
		} catch (Exception e) {
		}

		Runnable r = () -> {
			String sequence = null;
			try {
				if (first) {
					AwardApiData awardApiData = getAwardApiData();
					if (awardApiData != null) {
						sequence = gameLotteryWinManager.getNextSequence(
								awardApiData.getExpect(), maxSequenceNumber);
					}
				} else {
					GameLotteryWin latest = gameLotteryWinManager.getLatest();
					if (latest != null) {
						sequence = latest.getSequence();
					}
				}
			} finally {
				getAward(sequence);
			}
		};

		int delaySeconds = deadlineToAwardSeconds
				- Math.round(td.stop() / 1000);
		executor.schedule(r, delaySeconds, TimeUnit.SECONDS);
	}

	private void getAward(String sequence) {
		try {
			try {
				log.info("getAward sleep 60 s");
				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
			}

			boolean timeout = false;
			TimeDuration td = new TimeDuration();
			while (true) {
				if (publishRobot(sequence)) {
					break;
				}

				if (td.cost() > 1.5 * 60 * 1000) {
					timeout = true;
					break;
				}

				try {
					log.info("Not publish yet and sleep 10 s");
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}
			}

			if (timeout) {
				log.info("Timeout in getAward publish");
				gameLotteryWinManager.awardTimeout();

				Runnable r = () -> {
					publishRobot(sequence);
				};
				executor.schedule(r, 60, TimeUnit.SECONDS);
			}

			gameLotteryWinManager.nextSequence(maxSequenceNumber);
		} catch (Exception e) {
			log.warn("Exception in getAward, reason {}", e.getMessage());
		}

		start(false);
	}

	public void publish(String sequence, String awardNumber, Date publishDate) {
		if (StringUtils.isEmpty(sequence) || sequence.length() != 11) {
			log.warn("Invalid sequence {}", sequence);
			return;
		}
		if (StringUtils.isEmpty(awardNumber)
				|| !awardNumber.matches("^\\d{5}$")) {
			log.warn("Invalid awardNumber {}", awardNumber);
			return;
		}

		synchronized (sequence) {
			GameLotteryWin gameLotteryWin = gameLotteryWinManager
					.getBySequence(sequence);
			if (gameLotteryWin != null) {
				if (GameLotteryWinStatus.PUBLISH.toString().equals(
						gameLotteryWin.getStatus())) {
					log.warn("Sequence {} has publish, undo it", sequence);
					return;
				}
			}
		}

		log.info("Publish lottery, sequence {}, award number {}", sequence,
				awardNumber);

		GameLotteryWin dbGameLotteryWin = gameLotteryWinManager.publish(
				sequence, awardNumber, publishDate);
		betManager.encash(dbGameLotteryWin);
		gameUtilManager.publishAward(dbGameLotteryWin);
	}

	private boolean publishRobot(String sequence) {
		GameLotteryWin gameLotteryWin = gameLotteryWinManager
				.getBySequence(sequence);
		if (gameLotteryWin != null) {
			if (GameLotteryWinStatus.PUBLISH.toString().equals(
					gameLotteryWin.getStatus())) {
				log.info("Sequence {} has publish by manual",
						gameLotteryWin.getSequence());
				return true;
			}
		}

		AwardApiData awardApiData = getAwardApiData();
		if (awardApiData == null) {
			return false;
		}

		if (sequence == null) {
			long currentTs = System.currentTimeMillis();
			long publishTs = awardApiData.getOpenTimestamp() * 1000;
			long timeoutTs = 2 * 60 * 1000;
			if (Math.abs(currentTs - publishTs) > timeoutTs) {
				return false;
			}
		} else {
			if (!awardApiData.getExpect().equals(sequence)) {
				return false;
			}
		}

		String awardNumber = awardApiData.getOpenCode().replaceAll(",", "");
		Date publishDate = new Date();
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			publishDate = format.parse(awardApiData.getOpenTime());
		} catch (ParseException e) {
		}

		publish(awardApiData.getExpect(), awardNumber, publishDate);

		return true;
	}

	private AwardApiData getAwardApiData() {
		try {
			TimeDuration td = new TimeDuration();
			Callable<String> callable = new Callable<String>() {
				@Override
				public String call() throws Exception {
					return ScriptHelper.execScript("curl", apiUrl);
				}
			};

			ExecutorService singleThreadExecutor = Executors
					.newSingleThreadExecutor();
			Future<String> future = singleThreadExecutor.submit(callable);

			String json = null;
			try {
				json = future.get(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("Exception in getAwardApiData future, reason {}",
						e.getMessage());
			} finally {
				singleThreadExecutor.shutdown();

				if (StringUtils.isEmpty(json)) {
					return null;
				}
			}

			long execTs = td.stop();
			log.info("queryApi execScript cost {} ms, json {}", execTs, json);

			Gson gson = new Gson();
			AwardApiInfo awardApiInfo = gson.fromJson(json, AwardApiInfo.class);
			if (awardApiInfo == null) {
				return null;
			}
			if (awardApiInfo.getData() == null) {
				return null;
			}
			if (awardApiInfo.getData().size() != 1) {
				return null;
			}

			return awardApiInfo.getData().get(0);
		} catch (Exception e) {
			log.warn("Exception in getAwardApiData, reason {}", e.getMessage());
		}

		return null;
	}

	private static int getSeconds() {
		LocalTime localTime = LocalTime.now();
		int hours = localTime.getHour();
		int minutes = localTime.getMinute();
		int seconds = localTime.getSecond();

		return hours * 3600 + minutes * 60 + seconds;
	}

	private static int getIntervalMinutes() {
		int seconds = getSeconds();
		boolean isAfter1h55m = seconds > 1 * 3600 + 55 * 60;
		boolean isAfter10h = seconds > 10 * 3600;
		boolean isAfter22h = seconds > 22 * 3600;

		if (isAfter1h55m && !isAfter10h) {
			return 8 * 60 + 5;
		}

		if (isAfter22h || !isAfter1h55m) {
			return 5;
		}

		return 10;
	}

	public static int getDeadlineSeconds() {
		LocalTime localTime = LocalTime.now();
		int hours = localTime.getHour();
		int minutes = localTime.getMinute();
		int seconds = localTime.getSecond();

		int delay = 0;
		int intervalMinutes = getIntervalMinutes();
		if (intervalMinutes == 8 * 60 + 5) {
			delay = (10 * 60 - (hours * 60 + minutes)) * 60 - seconds;
		} else {
			delay = (intervalMinutes - minutes % intervalMinutes) * 60
					- seconds;
		}

		return delay - deadlineToAwardSeconds;
	}
}
