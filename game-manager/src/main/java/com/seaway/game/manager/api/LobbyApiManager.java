package com.seaway.game.manager.api;

import java.util.ArrayList;
import java.util.List;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.game.api.AwardInfo;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameLotteryWinStatus;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameRoomChat;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.manager.BetManager;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.repository.impl.GameLotteryWinManager;
import com.seaway.game.manager.repository.impl.GameRoomChatManager;
import com.seaway.game.manager.repository.impl.GameRoomManager;
import com.seaway.game.manager.repository.impl.GameUtilManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LobbyApiManager {

	private final GameUtilManager gameUtilManager;

	private final GameLotteryWinManager gameLotteryWinManager;

	private final GameRoomManager gameRoomManager;

	private final GameRoomChatManager gameRoomChatManager;

	private final GameConfigManager gameConfigManager;

	private final BetManager betManager;

	@Autowired
	public LobbyApiManager(GameUtilManager gameUtilManager,
			GameLotteryWinManager gameLotteryWinManager,
			GameRoomManager gameRoomManager,
			GameRoomChatManager gameRoomChatManager,
			GameConfigManager gameConfigManager, BetManager betSendManager) {
		this.gameUtilManager = gameUtilManager;
		this.gameLotteryWinManager = gameLotteryWinManager;
		this.gameRoomManager = gameRoomManager;
		this.gameRoomChatManager = gameRoomChatManager;
		this.gameConfigManager = gameConfigManager;
		this.betManager = betSendManager;
	}

	public AwardInfo awardLatest() {
		AwardInfo awardInfo = new AwardInfo();
		GameLotteryWin gameLotteryWin = gameLotteryWinManager
				.getLatestPublish();
		if (gameLotteryWin == null) {
			return awardInfo;
		}

		awardInfo.setSequence(gameLotteryWin.getSequence());
		awardInfo.setAwardNumber(gameLotteryWin.getAwardNumber());
		awardInfo.setPublishDate(gameLotteryWin.getPublishDate());

		return awardInfo;
	}

	public List<AwardInfo> awardHistory(String rid) {
		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		return gameLotteryWinManager.getHistory();
	}

	public List<GameRoomChat> lastMessage(String rid) {
		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		return gameRoomChatManager.getLastMessage(gameRoom.getWxRid());
	}

	public List<GameRoomChat> previousMessage(String rid, int startId) {
		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		return gameRoomChatManager.getPreviousMessage(gameRoom.getWxRid(),
				startId);
	}

	public void sendAgentMessage(String rid, String uid, String sid,
			String message) {
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return;
		}

		gameRoomChatManager.insertAgent(gameAgent, gameRoom, message);
	}

	public ResponseEntity sendUserMessage(String rid, String uid, String sid,
			String message, boolean keyboard) {
		ResponseEntity response = new ResponseEntity();

		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return response;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return response;
		}

		String betMessage = null;
		String roomMessage = null;
		String keyboardPrefix = "【键选】";
		if (keyboard) {
			betMessage = message;
			roomMessage = keyboardPrefix + message;
		} else {
			if (message.startsWith(keyboardPrefix)) {
				message = message.substring(keyboardPrefix.length());
			}

			betMessage = message;
			roomMessage = message;
		}

		gameRoomChatManager.insertUser(gameUser, gameRoom, roomMessage);

		GameLotteryWin gameLotteryWin = gameLotteryWinManager.getLatest();
		if (gameLotteryWin == null
				|| !GameLotteryWinStatus.NEW.toString().equals(
						gameLotteryWin.getStatus())) {
			response.setMessage("发送失败，已经封盘！");
			return response;
		}

		betManager.send(gameLotteryWin, gameUser, gameRoom, betMessage);

		response.setMessage("发送成功，请去大厅查看押粮信息！");
		response.setStatus(true);
		return response;
	}

	public List<String> getRules(String rid) {
		List<String> rules = new ArrayList<>();
		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return rules;
		}

		GameConfig gameConfig = gameConfigManager.getRoom(
				gameRoom.getAgentWxUid(), gameRoom.getWxRid(), "roomRule");
		if (gameConfig == null) {
			return rules;
		}

		rules.add(gameConfig.getValue());
		return rules;
	}

	public ResponseEntity cancelBet(String rid, String uid, String sid,
			int betId) {
		ResponseEntity response = new ResponseEntity();

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return response;
		}

		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return response;
		}

		GameLotteryWin gameLotteryWin = gameLotteryWinManager.getLatest();
		if (gameLotteryWin == null) {
			return response;
		}

		response.setStatus(betManager.cancel(gameLotteryWin, gameRoom,
				gameUser, betId));

		return response;
	}

}
