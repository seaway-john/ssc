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
import com.seaway.game.common.entity.game.api.AgentInfo;
import com.seaway.game.common.entity.manager.UserManage;
import com.seaway.game.common.entity.manager.UserRechargeApply;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameLotteryWinStatus;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.common.utils.FileUtils;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.repository.impl.GameLotteryWinManager;
import com.seaway.game.manager.repository.impl.GameRoomManager;
import com.seaway.game.manager.repository.impl.GameUserBalanceManager;
import com.seaway.game.manager.repository.impl.GameUserBalanceReportManager;
import com.seaway.game.manager.repository.impl.GameUserBetManager;
import com.seaway.game.manager.repository.impl.GameUtilManager;
import com.seaway.game.manager.schedule.CqsscSchedule;
import com.seaway.game.manager.websocket.WebSocketEndPoint;

@Component
public class AgentApiManager extends FileUtils {

	private final GameUtilManager gameUtilManager;

	private final GameLotteryWinManager gameLotteryWinManager;

	private final GameRoomManager gameRoomManager;

	private final GameUserBetManager gameUserBetManager;

	private final GameUserBalanceManager gameUserBalanceManager;

	private final GameUserBalanceReportManager gameUserBalanceReportManager;

	private final GameConfigManager gameConfigManager;

	private final WebSocketEndPoint webSocketEndPoint;

	@Autowired
	public AgentApiManager(GameUtilManager gameUtilManager,
			GameLotteryWinManager gameLotteryWinManager,
			GameRoomManager gameRoomManager,
			GameUserBetManager gameUserBetManager,
			GameUserBalanceManager gameUserBalanceManager,
			GameUserBalanceReportManager gameUserBalanceReportManager,
			GameConfigManager gameConfigManager,
			WebSocketEndPoint webSocketEndPoint) {
		this.gameUtilManager = gameUtilManager;
		this.gameLotteryWinManager = gameLotteryWinManager;
		this.gameRoomManager = gameRoomManager;
		this.gameUserBetManager = gameUserBetManager;
		this.gameUserBalanceManager = gameUserBalanceManager;
		this.gameUserBalanceReportManager = gameUserBalanceReportManager;
		this.gameConfigManager = gameConfigManager;
		this.webSocketEndPoint = webSocketEndPoint;
	}

	public ResponseEntity logout(String uid, String sid) {
		ResponseEntity response = new ResponseEntity();
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return response;
		}

		// gameAgentManager.cleanSid();
		webSocketEndPoint.closeByUid(gameAgent.getWxUid());

		response.setStatus(true);
		return response;
	}

	public AgentInfo getAgentInfo(String rid, String uid, String sid) {
		AgentInfo agentInfo = new AgentInfo();
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return agentInfo;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return agentInfo;
		}

		agentInfo.setStatus(true);
		agentInfo.setWxUid(gameAgent.getWxUid());
		agentInfo.setWxNickName(gameAgent.getWxNickName());
		agentInfo.setWxSex(gameAgent.isWxSex());
		agentInfo.setBalance(gameAgent.getBalance());
		agentInfo.setRoomName(gameRoom.getName());

		String currentLotterySequence = "请稍后...";
		GameLotteryWin gameLotteryWin = gameLotteryWinManager.getLatest();
		if (gameLotteryWin != null) {
			currentLotterySequence = gameLotteryWin.getSequence().substring(8);
			if (GameLotteryWinStatus.NEW.toString().equals(
					gameLotteryWin.getStatus())) {
				agentInfo
						.setDeadlineSeconds(CqsscSchedule.getDeadlineSeconds());
			}
		}
		agentInfo.setCurrentLotterySequence(currentLotterySequence);

		return agentInfo;
	}

	public Map<String, Object> getUserManageMap(String rid, String uid,
			String sid, int page, int limit) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("msg", "验证失败");
		map.put("count", 0);
		map.put("data", new ArrayList<>());

		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return map;
		}

		return gameUserBalanceManager.getUserManageMap(rid,
				gameAgent.getWxUid(), page, limit);
	}

	public void updateUserRemarkname(String uid, String sid,
			UserManage userManage) {
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return;
		}

		gameUtilManager.updateRemarkname(userManage);
	}

	public void blacklisUsers(String uid, String sid, boolean cancel,
			List<UserManage> userManages) {
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return;
		}

		userManages.forEach(userManage -> {
			gameUserBalanceManager.blacklisUser(userManage, cancel);
		});
	}

	public Map<String, Object> getJournalMap(String uid, String sid, int day) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("msg", "验证失败");
		map.put("count", 0);
		map.put("data", new ArrayList<>());

		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return map;
		}

		float rebateRate = 0;
		GameConfig gameConfig = gameConfigManager.getAgent(
				gameAgent.getWxUid(), "rebateRate");
		if (gameConfig != null) {
			rebateRate = Float.parseFloat(gameConfig.getValue());
		}

		return gameUserBetManager.getJournalMap(gameAgent.getWxUid(), day,
				rebateRate);
	}

	public AgentConfig getConfig(String uid, String sid) {
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return null;
		}

		GameRoom gameRoom = gameRoomManager.getByAgentWxUid(uid);
		if (gameRoom == null) {
			return null;
		}

		List<String> attributes = new ArrayList<>();
		attributes.add("trialPlayBalance");
		attributes.add("rebateRate");
		attributes.add("wealRate");
		attributes.add("limit2");
		attributes.add("limit3");
		attributes.add("limit4");

		Map<String, GameConfig> limitMap = gameConfigManager.getAgentMap(uid,
				attributes);

		AgentConfig agentConfig = new AgentConfig();
		agentConfig.setWxUsername(gameAgent.getWxUsername());
		agentConfig.setBankerUsername(gameAgent.getBankerUsername());
		agentConfig.setBankerPassword(gameAgent.getBankerPassword());
		agentConfig.setRoomName(gameRoom.getName());
		agentConfig.setMinEachBetInvest(gameRoom.getMinEachBetInvest());
		agentConfig.setMaxEachBetInvest(gameRoom.getMaxEachBetInvest());
		if (limitMap.containsKey("trialPlayBalance")) {
			agentConfig.setTrialPlayBalance(Integer.parseInt(limitMap.get(
					"trialPlayBalance").getValue()));
		}
		if (limitMap.containsKey("rebateRate")) {
			agentConfig.setRebateRate(Float.parseFloat(limitMap.get(
					"rebateRate").getValue()));
		}
		if (limitMap.containsKey("wealRate")) {
			agentConfig.setWealRate(Integer.parseInt(limitMap.get("wealRate")
					.getValue()));
		}
		if (limitMap.containsKey("limit2")) {
			agentConfig.setLimit2(Integer.parseInt(limitMap.get("limit2")
					.getValue()));
		}
		if (limitMap.containsKey("limit3")) {
			agentConfig.setLimit3(Integer.parseInt(limitMap.get("limit3")
					.getValue()));
		}
		if (limitMap.containsKey("limit4")) {
			agentConfig.setLimit4(Integer.parseInt(limitMap.get("limit4")
					.getValue()));
		}

		return agentConfig;
	}

	public void updateConfig(String uid, String sid, AgentConfig agentConfig) {
		if (agentConfig == null
				|| StringUtils.isEmpty(agentConfig.getBankerUsername())
				|| StringUtils.isEmpty(agentConfig.getBankerPassword())) {
			return;
		}

		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return;
		}

		gameUtilManager.updateAgentAsAgent(gameAgent, agentConfig);

		gameRoomManager.updateConfig(uid, agentConfig);

		gameConfigManager.upsertAgent(uid, "trialPlayBalance", "试玩粮草",
				agentConfig.getTrialPlayBalance() + "");
		gameConfigManager.upsertAgent(uid, "rebateRate", "回水返点率",
				agentConfig.getRebateRate() + "");
		gameConfigManager.upsertAgent(uid, "wealRate", "一万流水返福利",
				agentConfig.getWealRate() + "");
		gameConfigManager.upsertAgent(uid, "limit2", "二定赔率",
				agentConfig.getLimit2() + "");
		gameConfigManager.upsertAgent(uid, "limit3", "三定赔率",
				agentConfig.getLimit3() + "");
		gameConfigManager.upsertAgent(uid, "limit4", "四定赔率",
				agentConfig.getLimit4() + "");
	}

	public Map<String, Object> getUserRechargeMap(String uid, String sid,
			int page, int limit) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("msg", "验证失败");
		map.put("count", 0);
		map.put("data", new ArrayList<>());

		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return map;
		}

		return gameUserBalanceReportManager.getRechargeMap(
				gameAgent.getWxUid(), page, limit);
	}

	public ResponseEntity ignoreAllUserRecharges(String uid, String sid) {
		ResponseEntity response = new ResponseEntity();
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return response;
		}

		response.setStatus(true);
		gameUserBalanceReportManager.ignoreAllUserRecharges(uid);
		return response;
	}

	public void agreenUserRecharges(String uid, String sid,
			List<UserRechargeApply> recharges, boolean cancel) {
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return;
		}

		GameRoom gameRoom = gameRoomManager.getByAgentWxUid(uid);
		if (gameRoom == null) {
			return;
		}

		gameUserBalanceManager.agreenUserRecharges(uid, gameRoom, recharges,
				cancel);
	}

	public ResponseEntity syncBalance(String uid, String sid) {
		ResponseEntity response = new ResponseEntity();
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return response;
		}

		gameUtilManager.syncBalance(gameAgent.getWxUid());

		response.setStatus(true);
		response.setMessage("同步余粮成功");
		return response;
	}

	public ResponseEntity uploadQrcode(String uid, String sid, String name,
			byte[] bytes) {
		ResponseEntity response = new ResponseEntity();
		if (!"wechat".equals(name) && !"alipay".equals(name)) {
			response.setMessage("Invalid name");
			return response;
		}

		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return response;
		}

		String filePath = Constants.HEAD_IMAGE_FOLDER + uid + "." + name
				+ ".png";
		return uploadFile(filePath, bytes);
	}

	public void updateRoomRule(String rid, String uid, String sid, String rule) {
		GameAgents gameAgent = gameUtilManager.getAgentByUidAndSid(uid, sid);
		if (gameAgent == null) {
			return;
		}

		GameRoom gameRoom = gameRoomManager.getByAgentWxUid(uid);
		if (gameRoom == null) {
			return;
		}

		gameConfigManager.upsertRoom(gameAgent.getWxUid(), gameRoom.getWxRid(),
				"roomRule", "房间押粮规则", rule);
	}

}
