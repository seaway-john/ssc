package com.seaway.game.manager.repository.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.seaway.game.common.entity.game.api.AgentConfig;
import com.seaway.game.common.entity.manager.wechat.WechatUserInfo;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameUserBalanceReportType;
import com.seaway.game.manager.entity.WebSocketMessage;
import com.seaway.game.manager.repository.GameAgentsRepository;
import com.seaway.game.manager.websocket.WebSocketEndPoint;
import com.seaway.game.manager.websocket.WebSocketType;

@Component
public class GameAgentManager {

	private final GameAgentsRepository gameAgentsRepository;

	private final GameRoomManager gameRoomManager;

	private final GameUserLoginReportManager gameUserLoginReportManager;

	private final WebSocketEndPoint webSocketEndPoint;

	@Autowired
	public GameAgentManager(GameAgentsRepository gameAgentsRepository,
			GameRoomManager gameRoomManager,
			GameUserLoginReportManager gameUserLoginReportManager,
			WebSocketEndPoint webSocketEndPoint) {
		this.gameAgentsRepository = gameAgentsRepository;
		this.gameRoomManager = gameRoomManager;
		this.gameUserLoginReportManager = gameUserLoginReportManager;
		this.webSocketEndPoint = webSocketEndPoint;
	}

	public GameAgents getByWxUid(String wxUid) {
		return gameAgentsRepository.findByWxUidAndEnabled(wxUid, true);
	}

	public GameAgents getByWxSid(String wxSid) {
		return gameAgentsRepository.findByWxSid(wxSid);
	}

	public List<GameAgents> getList() {
		return gameAgentsRepository
				.findAll(new Sort(Sort.Direction.DESC, "id"));
	}

	public GameAgents upsert(WechatUserInfo wechatUserInfo, String code) {
		GameAgents gameAgent = gameAgentsRepository.findByWxUid(wechatUserInfo
				.getOpenId());
		if (gameAgent == null) {
			gameAgent = new GameAgents(wechatUserInfo.getOpenId(), code);
		}

		gameAgent.setWxSid(code);
		gameAgent.setWxNickName(wechatUserInfo.getNickName());
		gameAgent.setWxSex(wechatUserInfo.getSex() == 1);
		gameAgent.setLastUpdate(new Date());

		GameAgents dbGameAgent = gameAgentsRepository.saveAndFlush(gameAgent);

		gameUserLoginReportManager.insert(dbGameAgent);

		return dbGameAgent;
	}

	public void agreen(List<GameAgents> agents) {
		agents.forEach(agent -> {
			GameAgents gameAgent = gameAgentsRepository.findByWxUid(agent
					.getWxUid());
			if (gameAgent == null || gameAgent.isEnabled()) {
				return;
			}

			gameAgent.setEnabled(true);
			gameAgent.setLastUpdate(new Date());
			GameAgents dbGameAgent = gameAgentsRepository
					.saveAndFlush(gameAgent);

			gameRoomManager.insert(dbGameAgent);
		});
	}

	@Transactional
	public void update(String wxUid, GameUserBalanceReportType type, int gain) {
		switch (type) {
		case BET:
		case BET_CANCEL:
			gameAgentsRepository.updateBetInvestByWxUid(wxUid, -gain);
			break;
		case BET_ENCASH:
			gameAgentsRepository.updateBetIncomeByWxUid(wxUid, gain);
			break;
		default:
			break;
		}
	}

	@Transactional
	public void updateAsAgent(String wxUid, AgentConfig agentConfig) {
		gameAgentsRepository.updateByWxUidAsAgent(wxUid,
				agentConfig.getWxUsername(), agentConfig.getBankerUsername(),
				agentConfig.getBankerPassword());
	}

	@Transactional
	public void updateAsAdmin(GameAgents agent) {
		gameAgentsRepository.updateByWxUidAsAdmin(agent.getWxUid(),
				agent.getDescription(), agent.getBankerUsername(),
				agent.getBankerPassword());
	}

	@Transactional
	public void updateBalance(String wxUid, int balance) {
		gameAgentsRepository.updateBalanceByWxUid(wxUid, balance);

		WebSocketMessage wsMessage = new WebSocketMessage(
				WebSocketType.BALANCE_CHANGE, balance);
		webSocketEndPoint.broadcastUid(wxUid, wsMessage);
	}

	@Transactional
	public void ignore() {
		gameAgentsRepository.deleteByEnabled(false);
	}

	@Transactional
	public void deleteByWxUid(String wxUid) {
		gameAgentsRepository.deleteByWxUid(wxUid);
	}

}
