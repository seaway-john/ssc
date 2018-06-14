package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.seaway.game.common.entity.game.api.AgentJournalStat;
import com.seaway.game.common.entity.manager.BetDecode;
import com.seaway.game.common.entity.manager.banker.BankerResponse;
import com.seaway.game.common.entity.manager.banker.BetSendResponse;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameLotteryWinStatus;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserBalance;
import com.seaway.game.common.entity.mysql.GameUserBalanceReportType;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.entity.mysql.GameUserBetStatus;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.common.utils.DateUtils;
import com.seaway.game.manager.handler.BetHandler;
import com.seaway.game.manager.repository.GameUserBetRepository;

@Slf4j
@Component
public class GameUserBetManager {

	private final GameUserBetRepository gameUserBetRepository;

	private final GameRoomChatManager gameRoomChatManager;

	private final GameUserBalanceManager gameUserBalanceManager;

	private final BetHandler betHandler;

	private static Queue<String> queue = new ConcurrentLinkedQueue<>();

	@Autowired
	public GameUserBetManager(GameUserBetRepository gameUserBetRepository,
			GameRoomChatManager gameRoomChatManager,
			GameUserBalanceManager gameUserBalanceManager, BetHandler betHandler) {
		this.gameUserBetRepository = gameUserBetRepository;
		this.gameRoomChatManager = gameRoomChatManager;
		this.gameUserBalanceManager = gameUserBalanceManager;
		this.betHandler = betHandler;
	}

	public List<GameUserBet> awaitEncashByLotteryWinSequence(
			String lotteryWinSequence) {
		return gameUserBetRepository.findByLotteryWinSequenceAndBetStatus(
				lotteryWinSequence, GameUserBetStatus.BET.toString());
	}

	public List<GameUserBet> encashByLotteryWinSequence(
			String lotteryWinSequence) {
		return gameUserBetRepository.findByLotteryWinSequenceAndBetStatus(
				lotteryWinSequence, GameUserBetStatus.BET_ENCASH.toString());
	}

	private GameUserBet save(GameUserBet gameUserBet) {
		return gameUserBetRepository.saveAndFlush(gameUserBet);
	}

	public void insert(GameLotteryWin gameLotteryWin, GameUsers gameUser,
			GameRoom gameRoom, String betString, BetDecode betDecode) {
		GameUserBalance gameUserBalance = gameUserBalanceManager
				.getByAgentWxUidAndUserWxUid(gameRoom.getAgentWxUid(),
						gameUser.getWxUid());
		if (gameUserBalance == null) {
			return;
		}

		if (!check(betDecode, gameUser, gameRoom, gameUserBalance)) {
			return;
		}

		int cancelBetCode = 0;
		if (!gameUserBalance.isTrialPlay() && !gameUser.isNpc()) {
			BetSendResponse betResponse = null;
			try {
				queue.add(gameUser.getWxUid());
				betResponse = betHandler.sendAgentBet(gameRoom.getAgentWxUid(),
						betDecode);
			} finally {
				queue.remove(gameUser.getWxUid());
			}

			if (betResponse != null) {
				if (!betResponse.isStatus()) {
					log.error("sendAgentBet Failed, Agent {}, reason {}",
							gameRoom.getAgentWxUid(), betResponse.getInfo());

					List<String> messages = new ArrayList<>();
					messages.add("💔押粮失败💔");
					messages.add("原因：" + betResponse.getInfo());

					gameRoomChatManager.insertRobot(gameRoom, messages,
							gameUser.getWxNickName(),
							gameUserBalance.isTrialPlay());

					return;
				}

				if (betResponse.getCmdObject() != null) {
					if (betResponse.getCmdObject().getBetInfo() != null
							&& betResponse.getCmdObject().getBetInfo().size() >= 1) {
						cancelBetCode = Integer.parseInt(betResponse
								.getCmdObject().getBetInfo().get(1).trim());
					}
				}
			}
		}

		GameUserBet gameUserBet = new GameUserBet(gameLotteryWin.getSequence(),
				gameRoom.getAgentWxUid(), gameRoom.getWxRid(),
				gameUser.getWxUid(), GameUserBetStatus.BET);
		gameUserBet.setRemarkName(gameUserBalance.getRemarkName());
		gameUserBet.setBetString(betString);
		gameUserBet.setTrialPlay(gameUserBalance.isTrialPlay());
		gameUserBet.setCancelBetCode(cancelBetCode);
		gameUserBet.setBetFormat(betDecode.getFormat());

		Gson gson = new Gson();
		gameUserBet.setBetDecode(gson.toJson(betDecode));

		int betInvest = betDecode.getMoney() * betDecode.getSize();
		gameUserBet.setBetInvest(betInvest);
		gameUserBet.setBetIncome(0);
		gameUserBet.setNpc(gameUser.isNpc());

		GameUserBet dbGameUserBet = save(gameUserBet);

		GameUserBalance dbGameUserBalance = gameUserBalanceManager.update(
				dbGameUserBet, GameUserBalanceReportType.BET);

		int dbBalance = gameUserBet.isTrialPlay() ? dbGameUserBalance
				.getTrialPlayBalance() : dbGameUserBalance.getBalance();

		List<String> messages = new ArrayList<>();
		messages.add("🌹押粮成功🌹");
		messages.add("玩法：" + betDecode.getFormat());

		messages.add("粮草：押" + betInvest + " 余" + dbBalance);
		messages.add("撤退：<button class=\"layui-btn layui-btn-xs layui-btn\" onclick=\"cancelBet('"
				+ dbGameUserBet.getLotteryWinSequence().substring(8)
				+ "','"
				+ gameUser.getWxUid()
				+ "',"
				+ dbGameUserBet.getId()
				+ ")\">↩️点我撤回</button>");
		gameRoomChatManager.insertRobot(gameRoom, messages,
				gameUser.getWxNickName(), gameUserBet.isTrialPlay());
	}

	private boolean check(BetDecode betDecode, GameUsers gameUser,
			GameRoom gameRoom, GameUserBalance gameUserBalance) {
		String message = null;

		if (queue.contains(gameUser.getWxUid())) {
			message = "您有未完成的请求，请稍后再试";
		}

		if (message == null) {
			if (!betDecode.isStatus()) {
				message = betDecode.getMessage();
			} else {
				if (betDecode.getData() == null
						|| betDecode.getData().size() == 0) {
					message = "押粮匹配为空";
				} else {
					int size = 0;
					for (List<String> betNumbers : betDecode.getData().values()) {
						size += betNumbers.size();
					}

					if (size != betDecode.getSize()) {
						message = "押粮个数不匹配";
					}
				}
			}
		}

		if (message == null) {
			if (betDecode.getMoney() < gameRoom.getMinEachBetInvest()) {
				message = "单注不得低于" + gameRoom.getMinEachBetInvest() + "粮草";
			} else if (betDecode.getMoney() > gameRoom.getMaxEachBetInvest()) {
				message = "单注不得高于" + gameRoom.getMaxEachBetInvest() + "粮草";
			} else {
				int betInvest = betDecode.getMoney() * betDecode.getSize();
				int balance = gameUserBalance.isTrialPlay() ? gameUserBalance
						.getTrialPlayBalance() : gameUserBalance.getBalance();
				if (betInvest > balance) {
					message = "粮草不足，需" + betInvest + " 余" + balance;
				}
			}
		}

		if (message != null) {
			List<String> messages = new ArrayList<>();
			messages.add("💔押粮失败💔");
			messages.add("原因：" + message);
			gameRoomChatManager.insertRobot(gameRoom, messages,
					gameUser.getWxNickName(), gameUserBalance.isTrialPlay());

			return false;
		}

		return true;
	}

	public boolean cancelBet(GameLotteryWin gameLotteryWin, GameRoom gameRoom,
			GameUsers gameUser, int betId) {
		GameUserBet gameUserBet = gameUserBetRepository.findOne(betId);
		if (gameUserBet == null) {
			return false;
		}

		String message = null;
		if (queue.contains(gameUser.getWxUid())) {
			message = "您有未完成的请求，请稍后再试";
		} else {
			if (!GameLotteryWinStatus.NEW.toString().equals(
					gameLotteryWin.getStatus())) {
				message = "已经封盘";
			} else if (!gameLotteryWin.getSequence().equals(
					gameUserBet.getLotteryWinSequence())) {
				message = "不能撤回以前期数的押粮";
			} else if (!gameUserBet.getUserWxUid().equals(gameUser.getWxUid())) {
				message = "您不能撤回他人的押粮😂";
			} else {
				GameUserBetStatus betType = Enum.valueOf(
						GameUserBetStatus.class, gameUserBet.getBetStatus());
				switch (betType) {
				case BET:
					if (!gameUserBet.isTrialPlay() && !gameUser.isNpc()) {
						BankerResponse bankerResponse = null;
						try {
							queue.add(gameUser.getWxUid());
							bankerResponse = betHandler.cancelAgentBet(
									gameRoom.getAgentWxUid(), gameUserBet);
						} finally {
							queue.remove(gameUser.getWxUid());
						}

						if (bankerResponse != null
								&& !bankerResponse.isStatus()) {
							log.error(
									"calcelAgentBet Failed, Agent {}, reason {}",
									gameRoom.getAgentWxUid(),
									bankerResponse.getInfo());

							message = bankerResponse.getInfo();
						}
					}
					break;
				case BET_CANCEL:
					message = "已经撤回，请不要重复发送😥";
					break;
				case BET_ENCASH:
					message = "该押粮已经结算😂";
					break;
				default:
					break;
				}
			}
		}

		List<String> messages = new ArrayList<>();
		if (message != null) {
			messages.add("💔撤回失败💔");
			messages.add("原因：" + message);
		} else {
			gameUserBet.setBetStatus(GameUserBetStatus.BET_CANCEL.toString());
			gameUserBet.setLastUpdate(new Date());

			GameUserBet dbGameUserBet = save(gameUserBet);

			GameUserBalance dbGameUserBalance = gameUserBalanceManager.update(
					dbGameUserBet, GameUserBalanceReportType.BET_CANCEL);

			int dbBalance = gameUserBet.isTrialPlay() ? dbGameUserBalance
					.getTrialPlayBalance() : dbGameUserBalance.getBalance();

			messages.add("🌹撤回成功🌹");
			messages.add("粮草：退" + gameUserBet.getBetInvest() + " 余" + dbBalance);
		}

		gameRoomChatManager.insertRobot(gameRoom, messages,
				gameUser.getWxNickName(), gameUserBet.isTrialPlay());

		return message == null;
	}

	public void encash(GameUserBet gameUserBet, int betWinTicket, int betIncome) {
		gameUserBet.setBetWinTicket(betWinTicket);
		gameUserBet.setBetIncome(betIncome);
		gameUserBet.setBetStatus(GameUserBetStatus.BET_ENCASH.toString());
		gameUserBet.setLastUpdate(new Date());
		GameUserBet dbGameUserBet = save(gameUserBet);

		gameUserBalanceManager.update(dbGameUserBet,
				GameUserBalanceReportType.BET_ENCASH);
	}

	@Transactional
	public void updateRemarkname(String agentWxUid, String userWxUid,
			String remarkName) {
		gameUserBetRepository.updateRemarknameByAgentWxUidAndUserWxUid(
				agentWxUid, userWxUid, remarkName);
	}

	public List<GameUserBet> getAgentUserBets(String agentWxUid, int dayStart,
			int dayEnd) {
		if (dayStart == -1) {
			dayStart = 365 * 100;
		}

		if (dayEnd == -1) {
			dayEnd = 0;
		}

		Date[] dates = DateUtils.getDateRange(dayStart, dayEnd);

		List<String> betStatusIn = new ArrayList<>();
		betStatusIn.add(GameUserBalanceReportType.BET.toString());
		betStatusIn.add(GameUserBalanceReportType.BET_ENCASH.toString());

		return gameUserBetRepository
				.findByAgentWxUidAndCreatedBetweenAndBetStatusInAndTrialPlayAndNpc(
						agentWxUid, dates[0], dates[1], betStatusIn, false,
						false);
	}

	public Map<String, Object> getJournalMap(String agentWxUid, int day,
			float rebateRate) {
		List<AgentJournalStat> agentJournalStats = new ArrayList<>();
		if (day == 0) {
			Date[] dates = DateUtils.getDateRange(0, 0);

			List<String> betStatusIn = new ArrayList<>();
			betStatusIn.add(GameUserBalanceReportType.BET.toString());
			betStatusIn.add(GameUserBalanceReportType.BET_ENCASH.toString());
			List<GameUserBet> gameUserBets = gameUserBetRepository
					.findByAgentWxUidAndCreatedBetweenAndBetStatusInAndTrialPlayAndNpcOrderByIdDesc(
							agentWxUid, dates[0], dates[1], betStatusIn, false,
							false);

			Map<String, AgentJournalStat> gameUserBetMap = new LinkedHashMap<>();
			if (gameUserBets != null && !gameUserBets.isEmpty()) {
				gameUserBets
						.forEach(gameUserBet -> {
							String key = gameUserBet.getLotteryWinSequence();

							if (!gameUserBetMap.containsKey(key)) {
								AgentJournalStat agentJournalStat = new AgentJournalStat();
								agentJournalStat.setName(gameUserBet
										.getLotteryWinSequence().substring(8)
										+ "期");

								gameUserBetMap.put(key, agentJournalStat);
							}

							gameUserBetMap.get(key).setBetInvest(
									gameUserBetMap.get(key).getBetInvest()
											+ gameUserBet.getBetInvest());
							gameUserBetMap.get(key).setBetIncome(
									gameUserBetMap.get(key).getBetIncome()
											+ gameUserBet.getBetIncome());
						});
			}

			AgentJournalStat totalAgentJournalStat = new AgentJournalStat();
			totalAgentJournalStat.setName("合计");
			gameUserBetMap.forEach((key, agentJournalStat) -> {
				agentJournalStat.setBetGain(agentJournalStat.getBetIncome()
						- agentJournalStat.getBetInvest());
				agentJournalStat.setBetRebate(Math.round(rebateRate
						* agentJournalStat.getBetInvest() / 100));

				totalAgentJournalStat.setBetInvest(totalAgentJournalStat
						.getBetInvest() + agentJournalStat.getBetInvest());
				totalAgentJournalStat.setBetIncome(totalAgentJournalStat
						.getBetIncome() + agentJournalStat.getBetIncome());
			});

			totalAgentJournalStat.setBetGain(totalAgentJournalStat
					.getBetIncome() - totalAgentJournalStat.getBetInvest());
			totalAgentJournalStat.setBetRebate(Math.round(rebateRate
					* totalAgentJournalStat.getBetInvest() / 100));

			agentJournalStats.add(totalAgentJournalStat);
			gameUserBetMap.forEach((key, agentJournalStat) -> {
				agentJournalStats.add(agentJournalStat);
			});
		} else if (day == 7) {
			for (int i = 1; i <= 7; i++) {
				Date[] dates = DateUtils.getDateRange(i, i);
				List<String> betStatusIn = new ArrayList<>();
				betStatusIn.add(GameUserBalanceReportType.BET.toString());
				betStatusIn
						.add(GameUserBalanceReportType.BET_ENCASH.toString());

				AgentJournalStat totalAgentJournalStat = new AgentJournalStat();
				totalAgentJournalStat.setName("前" + i + "天");

				List<GameUserBet> gameUserBets = gameUserBetRepository
						.findByAgentWxUidAndCreatedBetweenAndBetStatusInAndTrialPlayAndNpc(
								agentWxUid, dates[0], dates[1], betStatusIn,
								false, false);
				if (gameUserBets != null && !gameUserBets.isEmpty()) {
					gameUserBets.forEach(gameUserBet -> {
						totalAgentJournalStat
								.setBetInvest(totalAgentJournalStat
										.getBetInvest()
										+ gameUserBet.getBetInvest());
						totalAgentJournalStat
								.setBetIncome(totalAgentJournalStat
										.getBetIncome()
										+ gameUserBet.getBetIncome());
					});
				}

				totalAgentJournalStat.setBetGain(totalAgentJournalStat
						.getBetIncome() - totalAgentJournalStat.getBetInvest());
				totalAgentJournalStat.setBetRebate(Math.round(rebateRate
						* totalAgentJournalStat.getBetInvest() / 100));

				agentJournalStats.add(totalAgentJournalStat);
			}

		}

		Map<String, Object> map = new HashMap<>();
		map.put("code", 0);
		map.put("msg", "");
		map.put("count", agentJournalStats.size());
		map.put("data", agentJournalStats);

		return map;
	}

	@Transactional
	public void deleteByAgentWxUid(String agentWxUid) {
		gameUserBetRepository.deleteByAgentWxUid(agentWxUid);
	}
}
