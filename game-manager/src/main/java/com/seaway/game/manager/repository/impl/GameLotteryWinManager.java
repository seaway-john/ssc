package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.game.api.AwardInfo;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameLotteryWinStatus;
import com.seaway.game.common.utils.DateUtils;
import com.seaway.game.manager.entity.WebSocketMessage;
import com.seaway.game.manager.repository.GameLotteryWinRepository;
import com.seaway.game.manager.schedule.CqsscSchedule;
import com.seaway.game.manager.websocket.WebSocketEndPoint;
import com.seaway.game.manager.websocket.WebSocketType;

@Slf4j
@Component
public class GameLotteryWinManager {

	private final GameLotteryWinRepository gameLotteryWinRepository;

	private final GameRoomManager gameRoomManager;

	private final WebSocketEndPoint webSocketEndPoint;

	@Autowired
	public GameLotteryWinManager(
			GameLotteryWinRepository gameLotteryWinRepository,
			GameRoomManager gameRoomManager, WebSocketEndPoint webSocketEndPoint) {
		this.gameLotteryWinRepository = gameLotteryWinRepository;
		this.gameRoomManager = gameRoomManager;
		this.webSocketEndPoint = webSocketEndPoint;
	}

	private GameLotteryWin save(GameLotteryWin gameLotteryWin) {
		return gameLotteryWinRepository.saveAndFlush(gameLotteryWin);
	}

	public GameLotteryWin getLatest() {
		return gameLotteryWinRepository.findTopByOrderBySequenceDesc();
	}

	public GameLotteryWin getLatestPublish() {
		return gameLotteryWinRepository
				.findFirstByStatusOrderBySequenceDesc(GameLotteryWinStatus.PUBLISH
						.toString());
	}

	public GameLotteryWin getBySequence(String sequence) {
		if (StringUtils.isEmpty(sequence)) {
			return null;
		}

		return gameLotteryWinRepository.findBySequence(sequence);
	}

	public Page<GameLotteryWin> getPageable(Pageable pageable) {
		return gameLotteryWinRepository.findAll(pageable);
	}

	public List<AwardInfo> getHistory() {
		List<AwardInfo> awardInfos = new ArrayList<>();

		String sequenceLike = DateUtils.getYmd() + "%";

		List<GameLotteryWin> gameLotteryWins = gameLotteryWinRepository
				.findByStatusAndSequenceLike(
						GameLotteryWinStatus.PUBLISH.toString(), sequenceLike);
		if (gameLotteryWins == null || gameLotteryWins.isEmpty()) {
			return awardInfos;
		}

		gameLotteryWins.forEach(gameLotteryWin -> {
			AwardInfo awardInfo = new AwardInfo();
			awardInfo.setSequence(gameLotteryWin.getSequence());
			awardInfo.setAwardNumber(gameLotteryWin.getAwardNumber());
			awardInfo.setPublishDate(gameLotteryWin.getPublishDate());

			awardInfos.add(awardInfo);
		});

		return awardInfos;
	}

	public GameLotteryWin publish(String sequence, String awardNumber,
			Date publishDate) {
		GameLotteryWin gameLotteryWin = getBySequence(sequence);
		if (gameLotteryWin == null) {
			gameLotteryWin = new GameLotteryWin(sequence,
					GameLotteryWinStatus.PUBLISH);
		} else {
			gameLotteryWin.setLastUpdate(new Date());
		}

		gameLotteryWin.setStatus(GameLotteryWinStatus.PUBLISH.toString());
		gameLotteryWin.setAwardNumber(awardNumber);
		gameLotteryWin.setPublishDate(publishDate);

		return save(gameLotteryWin);
	}

	public void nextSequence(int maxSequenceNumber) {
		GameLotteryWin latest = getLatest();
		if (latest == null) {
			return;
		}

		String nextSequence = getNextSequence(latest.getSequence(),
				maxSequenceNumber);
		if (nextSequence == null) {
			return;
		}

		GameLotteryWin gameLotteryWin = getBySequence(nextSequence);
		if (gameLotteryWin != null) {
			return;
		}

		gameLotteryWin = new GameLotteryWin(nextSequence,
				GameLotteryWinStatus.NEW);

		save(gameLotteryWin);

		StringBuilder sb = new StringBuilder();
		sb.append("🏁");
		sb.append(nextSequence.substring(8));
		sb.append("期<img src='../images/start-bet.gif'/>");
		gameRoomManager.broadcastAll(sb.toString());

		Map<String, Object> map = new HashMap<>();
		map.put("currentLotterySequence", nextSequence.substring(8));
		map.put("deadlineSeconds", CqsscSchedule.getDeadlineSeconds());

		WebSocketMessage wsMessage = new WebSocketMessage(
				WebSocketType.LOTTERY_SEQUENCE_NEW, map);
		webSocketEndPoint.broadcast(wsMessage);
	}

	public void saveDeadline() {
		GameLotteryWin gameLotteryWin = getLatest();
		if (gameLotteryWin == null
				|| !GameLotteryWinStatus.NEW.toString().equals(
						gameLotteryWin.getStatus())) {
			return;
		}

		gameLotteryWin.setStatus(GameLotteryWinStatus.DEADLINE.toString());
		gameLotteryWin.setLastUpdate(new Date());

		save(gameLotteryWin);

		log.info("Deadline of sequence {}", gameLotteryWin.getSequence());

		StringBuilder sb = new StringBuilder();
		sb.append("🏁");
		sb.append(gameLotteryWin.getSequence().substring(8));
		sb.append("期<img src='../images/deadline.gif'/>");

		gameRoomManager.broadcastAll(sb.toString());

		Map<String, Object> map = new HashMap<>();
		map.put("currentLotterySequence", gameLotteryWin.getSequence()
				.substring(8));
		map.put("deadlineSeconds", CqsscSchedule.getDeadlineSeconds());

		WebSocketMessage wsMessage = new WebSocketMessage(
				WebSocketType.LOTTERY_SEQUENCE_DEADLINE, map);
		webSocketEndPoint.broadcast(wsMessage);
	}

	public void awardTimeout() {
		GameLotteryWin gameLotteryWin = getLatest();
		if (gameLotteryWin == null) {
			return;
		}

		if (GameLotteryWinStatus.PUBLISH.toString().equals(
				gameLotteryWin.getStatus())) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("🏁");
		sb.append(gameLotteryWin.getSequence().substring(8));
		sb.append("期，开奖超时<br/>稍后会手动开奖，请继续押粮🌹");

		gameRoomManager.broadcastAll(sb.toString());

	}

	public String getNextSequence(String currentSequence, int maxSequenceNumber) {
		if (currentSequence == null || currentSequence.length() != 11) {
			return null;
		}
		int currentSequenceNumber = Integer.parseInt(currentSequence
				.substring(8));
		if (currentSequenceNumber < maxSequenceNumber) {
			return (Long.parseLong(currentSequence) + 1) + "";
		}

		return DateUtils.getYmd() + "001";
	}

}
