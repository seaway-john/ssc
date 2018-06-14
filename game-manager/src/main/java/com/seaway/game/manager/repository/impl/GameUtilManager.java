package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.game.api.AgentConfig;
import com.seaway.game.common.entity.manager.UserManage;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserBalance;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.common.utils.FormatUtils;
import com.seaway.game.manager.handler.BankerHandler;

@Slf4j
@Component
public class GameUtilManager {

	private final GameAgentManager gameAgentManager;

	private final GameRoomManager gameRoomManager;

	private final GameUserManager gameUserManager;

	private final GameUserBalanceManager gameUserBalanceManager;

	private final GameUserBetManager gameUserBetManager;

	private final GameRoomChatManager gameRoomChatManager;

	private final GameUserBalanceReportManager gameUserBalanceReportManager;

	private final GameConfigManager gameConfigManager;

	private final BankerHandler bankerHandler;

	@Autowired
	public GameUtilManager(GameAgentManager gameAgentManager,
			GameRoomManager gameRoomManager, GameUserManager gameUserManager,
			GameUserBalanceManager gameUserBalanceManager,
			GameUserBetManager gameUserBetManager,
			GameRoomChatManager gameRoomChatManager,
			GameUserBalanceReportManager gameUserBalanceReportManager,
			GameConfigManager gameConfigManager, BankerHandler bankerHandler) {
		this.gameAgentManager = gameAgentManager;
		this.gameRoomManager = gameRoomManager;
		this.gameUserManager = gameUserManager;
		this.gameUserBalanceManager = gameUserBalanceManager;
		this.gameUserBetManager = gameUserBetManager;
		this.gameRoomChatManager = gameRoomChatManager;
		this.gameUserBalanceReportManager = gameUserBalanceReportManager;
		this.gameConfigManager = gameConfigManager;
		this.bankerHandler = bankerHandler;
	}

	public boolean checkAgent(String uid, String sid) {
		return getAgentByUidAndSid(uid, sid) != null;
	}

	public boolean checkUser(String uid, String sid) {
		return getUserByUidAndSid(uid, sid) != null;
	}

	public GameAgents getAgentByUidAndSid(String uid, String sid) {
		if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(sid)) {
			log.warn("Invalid parameters!, uid {}, sid {}", uid, sid);
			return null;
		}

		GameAgents agent = gameAgentManager.getByWxUid(uid);
		if (agent == null) {
			log.warn("Agent {} no exist!", uid);
			return null;
		}

		if (!sid.equals(agent.getWxSid())) {
			log.warn("Agent sid {} no match!", uid);
			return null;
		}

		return agent;
	}

	public GameUsers getUserByUidAndSid(String uid, String sid) {
		if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(sid)) {
			log.warn("Invalid parameters!, uid {}, sid {}", uid, sid);
			return null;
		}

		GameUsers user = gameUserManager.getByWxUid(uid);
		if (user == null) {
			log.warn("User {} no exist!", uid);
			return null;
		}

		if (!sid.equals(user.getWxSid())) {
			log.warn("User sid {} no match!", uid);
			return null;
		}

		return user;
	}

	public void updateRemarkname(UserManage userManage) {
		GameUserBalance gameUserBalance = gameUserBalanceManager
				.updateRemarkname(userManage.getId(),
						userManage.getRemarkName());
		if (gameUserBalance != null) {
			gameUserBetManager.updateRemarkname(
					gameUserBalance.getAgentWxUid(),
					gameUserBalance.getUserWxUid(), userManage.getRemarkName());

			gameUserBalanceReportManager.updateRemarkname(
					gameUserBalance.getAgentWxUid(),
					gameUserBalance.getUserWxUid(), userManage.getRemarkName());
		}
	}

	public void publishAward(GameLotteryWin gameLotteryWin) {
		List<GameRoom> gameRooms = gameRoomManager.getList();
		if (gameRooms == null || gameRooms.isEmpty()) {
			return;
		}

		Map<String, List<GameUserBet>> map = new LinkedHashMap<>();
		List<GameUserBet> gameUserBets = gameUserBetManager
				.encashByLotteryWinSequence(gameLotteryWin.getSequence());
		if (gameUserBets != null && !gameUserBets.isEmpty()) {
			gameUserBets.forEach(gameUserBet -> {
				if (!map.containsKey(gameUserBet.getRoomWxRid())) {
					map.put(gameUserBet.getRoomWxRid(), new ArrayList<>());
				}

				map.get(gameUserBet.getRoomWxRid()).add(gameUserBet);
			});
		}

		gameRooms.forEach(gameRoom -> {
			broadcastRoomAwardInfo(gameLotteryWin, gameRoom,
					map.get(gameRoom.getWxRid()));
		});
	}

	private void broadcastRoomAwardInfo(GameLotteryWin gameLotteryWin,
			GameRoom gameRoom, List<GameUserBet> gameUserBets) {
		StringBuilder sb = new StringBuilder();
		sb.append("🏁");
		sb.append(gameLotteryWin.getSequence().substring(8));
		sb.append("期，开  🏆<span style='color: #d44b4b; font-weight: bold; font-size: 24px;'>");
		sb.append(gameLotteryWin.getAwardNumber());
		sb.append("</span>🏆<br/>🌹🌹🌹🌹🌹🌹战果🌹🌹🌹🌹🌹🌹");
		// sb.append("<br/>*******战果*******");

		Map<String, List<GameUserBet>> map = new LinkedHashMap<>();
		if (gameUserBets != null && !gameUserBets.isEmpty()) {
			gameUserBets.forEach(gameUserBet -> {
				if (!map.containsKey(gameUserBet.getUserWxUid())) {
					map.put(gameUserBet.getUserWxUid(), new ArrayList<>());
				}

				map.get(gameUserBet.getUserWxUid()).add(gameUserBet);
			});
		}

		map.forEach((userWxUid, userBets) -> {
			GameUserBalance gameUserBalance = gameUserBalanceManager
					.getByAgentWxUidAndUserWxUid(gameRoom.getAgentWxUid(),
							userWxUid);
			if (gameUserBalance == null) {
				return;
			}

			GameUsers gameUser = gameUserManager.getByWxUid(userWxUid);
			if (gameUser == null) {
				return;
			}

			List<GameUserBet> realUserBets = new ArrayList<>();
			List<GameUserBet> trialPlayUserBets = new ArrayList<>();
			userBets.forEach(userBet -> {
				if (userBet.isTrialPlay()) {
					trialPlayUserBets.add(userBet);
				} else {
					realUserBets.add(userBet);
				}
			});

			sb.append("<br/><span style=\"color: #0066cc;\">@ ");
			sb.append(gameUser.getWxNickName());
			sb.append("</span>");
			if (!realUserBets.isEmpty()) {
				sb.append(getUserBetAwardInfo(gameLotteryWin.getAwardNumber(),
						realUserBets, false, gameUserBalance.getBalance()));
			}
			if (!trialPlayUserBets.isEmpty()) {
				sb.append(getUserBetAwardInfo(gameLotteryWin.getAwardNumber(),
						trialPlayUserBets, true,
						gameUserBalance.getTrialPlayBalance()));
			}
		});

		gameRoomChatManager.insertRobot(gameRoom, sb.toString());
	}

	private StringBuilder getUserBetAwardInfo(String awardNumber,
			List<GameUserBet> userBets, boolean isTrialPlay, int balance) {
		int invest = 0;
		int gain = 0;
		int winTicket = 0;

		for (GameUserBet userBet : userBets) {
			invest += userBet.getBetInvest();
			winTicket += userBet.getBetWinTicket();
			gain += (userBet.getBetIncome() - userBet.getBetInvest());
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<br/>");
		if (isTrialPlay) {
			sb.append("【试玩】");
		}
		sb.append("粮草：押");
		sb.append(invest);
		sb.append(" 中");
		sb.append(winTicket);

		if (gain < 0) {
			sb.append(" 跌");
			sb.append(Math.abs(gain));
			sb.append("🥀");
		} else if (gain > 0) {
			sb.append(" 涨");
			sb.append(Math.abs(gain));
			sb.append("😎");
		} else {
			sb.append(" 平");
			sb.append(Math.abs(gain));
			sb.append("😂");
		}

		sb.append(" 余");
		sb.append(balance);

		return sb;
	}

	public void updateAgentAsAdmin(GameAgents gameAgent) {
		GameAgents dbGameAgent = gameAgentManager.getByWxUid(gameAgent
				.getWxUid());
		gameAgentManager.updateAsAdmin(gameAgent);

		if (!FormatUtils.equals(dbGameAgent.getBankerUsername(),
				gameAgent.getBankerUsername())
				|| !FormatUtils.equals(dbGameAgent.getBankerPassword(),
						gameAgent.getBankerPassword())) {
			syncBalance(gameAgent.getWxUid());
		}
	}

	public void updateAgentAsAgent(GameAgents gameAgent, AgentConfig agentConfig) {
		gameAgentManager.updateAsAgent(gameAgent.getWxUid(), agentConfig);

		if (!FormatUtils.equals(gameAgent.getBankerPassword(),
				agentConfig.getBankerPassword())) {
			syncBalance(gameAgent.getWxUid());
		}
	}

	public void toggleTone(String userWxUid) {
		gameUserManager.toggleTone(userWxUid);
	}

	public void syncBalance(String agentWxUid) {
		bankerHandler.syncBalance(agentWxUid, null, true);
	}

	public void deleteAgent(GameAgents agent) {
		if (agent == null) {
			return;
		}
		String agentWxUid = agent.getWxUid();

		GameRoom gameRoom = gameRoomManager.getByAgentWxUid(agentWxUid);
		if (gameRoom == null) {
			return;
		}
		String roomWxRid = gameRoom.getWxRid();
		
		log.warn("Delete agent {}", agentWxUid);

		gameConfigManager.deleteByAgentWxUid(agentWxUid);
		gameUserBalanceReportManager.deleteByAgentWxUid(agentWxUid);
		gameUserBetManager.deleteByAgentWxUid(agentWxUid);
		gameUserBalanceManager.deleteByAgentWxUid(agentWxUid);
		gameRoomChatManager.deleteByRoomWxRid(roomWxRid);
		gameRoomManager.deleteByAgentWxUid(agentWxUid);
		gameAgentManager.deleteByWxUid(agentWxUid);
	}
}
