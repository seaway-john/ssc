package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.game.api.AgentConfig;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.utils.FormatUtils;
import com.seaway.game.manager.repository.GameRoomRepository;

@Slf4j
@Component
public class GameRoomManager {

	private final GameRoomRepository gameRoomRepository;

	private final GameRoomChatManager gameRoomChatManager;

	private final GameConfigManager gameConfigManager;

	@Autowired
	public GameRoomManager(GameRoomRepository gameRoomRepository,
			GameRoomChatManager gameRoomChatManager,
			GameConfigManager gameConfigManager) {
		this.gameRoomRepository = gameRoomRepository;
		this.gameRoomChatManager = gameRoomChatManager;
		this.gameConfigManager = gameConfigManager;
	}

	public List<GameRoom> getList() {
		return gameRoomRepository.findByEnabled(true);
	}

	public GameRoom getByWxRid(String wxRid) {
		if (StringUtils.isEmpty(wxRid)) {
			log.warn("Invalid parameters!, wxRid {}", wxRid);
			return null;
		}

		return gameRoomRepository.findByWxRidAndEnabled(wxRid, true);
	}

	public GameRoom getByAgentWxUid(String agentWxUid) {
		return gameRoomRepository.findFirstByAgentWxUidAndEnabled(agentWxUid,
				true);
	}

	public void insert(GameAgents gameAgent) {
		String name = "聚乐时时彩";
		String wxRid = generateWxRid();

		GameRoom gameRoom = new GameRoom(gameAgent, wxRid);
		gameRoom.setName(name);
		gameRoom.setEnabled(true);

		List<String> attributes = new ArrayList<>();
		attributes.add("minEachBetInvest");
		attributes.add("maxEachBetInvest");

		Map<String, GameConfig> configMap = gameConfigManager.getAgentMap(
				gameAgent.getWxUid(), attributes);

		if (configMap.containsKey("minEachBetInvest")) {
			gameRoom.setMinEachBetInvest(Integer.parseInt(configMap.get(
					"minEachBetInvest").getValue()));
		}
		if (configMap.containsKey("maxEachBetInvest")) {
			gameRoom.setMaxEachBetInvest(Integer.parseInt(configMap.get(
					"maxEachBetInvest").getValue()));
		}

		gameRoomRepository.save(gameRoom);
	}

	private String generateWxRid() {
		String wxRid = FormatUtils.random(16);

		GameRoom gameRoom = getByWxRid(wxRid);
		if (gameRoom == null) {
			return wxRid;
		}

		return generateWxRid();
	}

	public void updateConfig(String agentWxUid, AgentConfig agentConfig) {
		GameRoom gameRoom = getByAgentWxUid(agentWxUid);
		if (gameRoom == null) {
			return;
		}

		gameRoom.setName(agentConfig.getRoomName());
		gameRoom.setMinEachBetInvest(agentConfig.getMinEachBetInvest());
		gameRoom.setMaxEachBetInvest(agentConfig.getMaxEachBetInvest());
		gameRoom.setLastUpdate(new Date());

		gameRoomRepository.save(gameRoom);
	}

	public void broadcastAll(String message) {
		List<GameRoom> gameRooms = getList();
		if (gameRooms == null || gameRooms.isEmpty()) {
			return;
		}

		gameRooms.forEach(gameRoom -> {
			gameRoomChatManager.insertRobot(gameRoom, message);
		});
	}

	@Transactional
	public void deleteByAgentWxUid(String agentWxUid) {
		gameRoomRepository.deleteByAgentWxUid(agentWxUid);
	}
}
