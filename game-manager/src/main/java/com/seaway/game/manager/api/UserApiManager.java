package com.seaway.game.manager.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.game.api.AgentConfig;
import com.seaway.game.common.entity.game.api.UserInfo;
import com.seaway.game.common.entity.game.api.UserRecharge;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameLotteryWinStatus;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserBalance;
import com.seaway.game.common.entity.mysql.GameUserBalanceReportType;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.repository.impl.GameAgentManager;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.repository.impl.GameLotteryWinManager;
import com.seaway.game.manager.repository.impl.GameRoomManager;
import com.seaway.game.manager.repository.impl.GameUserBalanceManager;
import com.seaway.game.manager.repository.impl.GameUserBalanceReportManager;
import com.seaway.game.manager.repository.impl.GameUtilManager;
import com.seaway.game.manager.schedule.CqsscSchedule;
import com.seaway.game.manager.websocket.WebSocketEndPoint;

@Component
public class UserApiManager {

	private final GameUtilManager gameUtilManager;

	private final GameLotteryWinManager gameLotteryWinManager;

	private final GameAgentManager gameAgentManager;

	private final GameRoomManager gameRoomManager;

	private final GameUserBalanceManager gameUserBalanceManager;

	private final GameUserBalanceReportManager gameUserBalanceReportManager;

	private final GameConfigManager gameConfigManager;

	private final WebSocketEndPoint webSocketEndPoint;

	@Autowired
	public UserApiManager(GameUtilManager gameUtilManager,
			GameLotteryWinManager gameLotteryWinManager,
			GameAgentManager gameAgentManager, GameRoomManager gameRoomManager,
			GameUserBalanceManager gameUserBalanceManager,
			GameUserBalanceReportManager gameUserBalanceReportManager,
			GameConfigManager gameConfigManager,
			WebSocketEndPoint webSocketEndPoint) {
		this.gameUtilManager = gameUtilManager;
		this.gameLotteryWinManager = gameLotteryWinManager;
		this.gameAgentManager = gameAgentManager;
		this.gameRoomManager = gameRoomManager;
		this.gameUserBalanceManager = gameUserBalanceManager;
		this.gameUserBalanceReportManager = gameUserBalanceReportManager;
		this.gameConfigManager = gameConfigManager;
		this.webSocketEndPoint = webSocketEndPoint;
	}

	public ResponseEntity logout(String rid, String uid, String sid) {
		ResponseEntity response = new ResponseEntity();
		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return response;
		}

		// gameUserManager.cleanSid();
		webSocketEndPoint.closeByUid(gameUser.getWxUid());

		response.setStatus(true);
		return response;
	}

	public UserInfo getUserInfo(String rid, String uid, String sid) {
		UserInfo userInfo = new UserInfo();
		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return userInfo;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return userInfo;
		}

		GameUserBalance gameUserBalance = gameUserBalanceManager
				.getByAgentWxUidAndUserWxUid(gameRoom.getAgentWxUid(),
						gameUser.getWxUid());
		if (gameUserBalance == null) {
			return userInfo;
		}

		userInfo.setStatus(true);
		userInfo.setWxNickName(gameUser.getWxNickName());
		userInfo.setWxUid(gameUser.getWxUid());
		userInfo.setWxSex(gameUser.isWxSex());
		userInfo.setTone(gameUser.isTone());
		userInfo.setAgentWxUid(gameRoom.getAgentWxUid());
		userInfo.setRoomName(gameRoom.getName());

		userInfo.setTrialPlay(gameUserBalance.isTrialPlay());
		if (userInfo.isTrialPlay()) {
			userInfo.setBalance(gameUserBalance.getTrialPlayBalance());
		} else {
			userInfo.setBalance(gameUserBalance.getBalance());
		}

		GameConfig gameConfig = gameConfigManager.getAgent(
				gameRoom.getAgentWxUid(), "wealRate");
		if (gameConfig != null) {
			int wealRate = 0;
			if (!StringUtils.isEmpty(gameConfig.getValue())) {
				wealRate = Integer.parseInt(gameConfig.getValue());
			}

			if (wealRate > 0) {
				userInfo.setEnabelWeal(true);
				userInfo.setWeal((int) Math.floor(gameUserBalance
						.getAvailableBetInvest() * wealRate / 10000));
			}
		}

		String currentLotterySequence = "请稍后...";
		GameLotteryWin gameLotteryWin = gameLotteryWinManager.getLatest();
		if (gameLotteryWin != null) {
			currentLotterySequence = gameLotteryWin.getSequence().substring(8);
			if (GameLotteryWinStatus.NEW.toString().equals(
					gameLotteryWin.getStatus())) {
				userInfo.setDeadlineSeconds(CqsscSchedule.getDeadlineSeconds());
			}
		}
		userInfo.setCurrentLotterySequence(currentLotterySequence);

		return userInfo;
	}

	public ResponseEntity toggleTone(String rid, String uid, String sid) {
		ResponseEntity response = new ResponseEntity();

		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return response;
		}

		gameUtilManager.toggleTone(gameUser.getWxUid());

		response.setStatus(true);
		return response;
	}

	public ResponseEntity toggleTrialPlay(String rid, String uid, String sid) {
		ResponseEntity response = new ResponseEntity();

		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return response;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return response;
		}

		GameUserBalance gameUserBalance = gameUserBalanceManager
				.toggleTrialPlay(gameRoom.getAgentWxUid(), gameUser);
		if (gameUserBalance == null) {
			return response;
		}

		int balance = gameUserBalance.isTrialPlay() ? gameUserBalance
				.getTrialPlayBalance() : gameUserBalance.getBalance();

		response.setStatus(true);
		response.setMessage(balance + "");
		return response;
	}

	public ResponseEntity exchangeWeal(String rid, String uid, String sid,
			UserRecharge exchangeWeal) {
		if (!GameUserBalanceReportType.WEAL.equals(exchangeWeal.getType())) {
			return null;
		}

		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return null;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		return gameUserBalanceManager.exchangeWeal(gameRoom,
				gameUser.getWxUid(), exchangeWeal.getNumber());
	}

	public ResponseEntity recharge(String rid, String uid, String sid,
			UserRecharge userRecharge) {
		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return null;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		GameUserBalance gameUserBalance = gameUserBalanceManager
				.getByAgentWxUidAndUserWxUid(gameRoom.getAgentWxUid(),
						gameUser.getWxUid());
		if (gameUserBalance == null || gameUserBalance.isTrialPlay()) {
			return null;
		}

		return gameUserBalanceReportManager.insert(gameUserBalance,
				userRecharge);
	}

	public Map<String, Object> getRecordMap(String rid, String uid, String sid,
			int page, int limit, int day) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("msg", "验证失败");
		map.put("count", 0);
		map.put("data", new ArrayList<>());

		GameUsers gameUser = gameUtilManager.getUserByUidAndSid(uid, sid);
		if (gameUser == null) {
			return map;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return map;
		}

		return gameUserBalanceReportManager
				.getRecordMap(gameRoom.getAgentWxUid(), gameUser.getWxUid(),
						page, limit, day);
	}

	public AgentConfig supply(String rid, String uid, String sid) {
		AgentConfig agentConfig = new AgentConfig();
		if (!gameUtilManager.checkUser(uid, sid)) {
			return agentConfig;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return agentConfig;
		}

		GameAgents gameAgent = gameAgentManager.getByWxUid(gameRoom
				.getAgentWxUid());
		if (gameAgent == null) {
			return agentConfig;
		}

		agentConfig.setWxUsername(gameAgent.getWxUsername());
		agentConfig.setRoomName(gameRoom.getName());
		agentConfig.setMinEachBetInvest(gameRoom.getMinEachBetInvest());
		agentConfig.setMaxEachBetInvest(gameRoom.getMaxEachBetInvest());

		List<String> attributes = new ArrayList<>();
		attributes.add("limit2");
		attributes.add("limit3");
		attributes.add("limit4");

		Map<String, GameConfig> gameConfigMap = gameConfigManager.getRoomMap(
				gameAgent.getWxUid(), gameRoom.getWxRid(), attributes);
		if (gameConfigMap.containsKey("limit2")) {
			agentConfig.setLimit2(Integer.parseInt(gameConfigMap.get("limit2")
					.getValue()));
		}
		if (gameConfigMap.containsKey("limit3")) {
			agentConfig.setLimit3(Integer.parseInt(gameConfigMap.get("limit3")
					.getValue()));
		}
		if (gameConfigMap.containsKey("limit4")) {
			agentConfig.setLimit4(Integer.parseInt(gameConfigMap.get("limit4")
					.getValue()));
		}

		return agentConfig;
	}
}
