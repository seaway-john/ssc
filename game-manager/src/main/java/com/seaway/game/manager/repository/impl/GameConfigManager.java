package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.utils.FormatUtils;
import com.seaway.game.manager.repository.GameConfigRepository;

@Slf4j
@Component
public class GameConfigManager {

	private final GameConfigRepository gameConfigRepository;

	@Autowired
	public GameConfigManager(GameConfigRepository gameConfigRepository) {
		this.gameConfigRepository = gameConfigRepository;
	}

	public GameConfig get(String attribute) {
		return getAgent(null, attribute);
	}

	public GameConfig getAgent(String agentWxUid, String attribute) {
		return getRoom(agentWxUid, null, attribute);
	}

	public Map<String, GameConfig> getMap(List<String> attributes) {
		return getAgentMap(null, attributes);
	}

	public Map<String, GameConfig> getAgentMap(String agentWxUid,
			List<String> attributes) {
		return getRoomMap(agentWxUid, null, attributes);
	}

	public void upsert(String attribute, String name, String value) {
		upsertAgent(null, attribute, name, value);
	}

	public void upsertAgent(String agentWxUid, String attribute, String name,
			String value) {
		upsertRoom(agentWxUid, null, attribute, name, value);
	}

	public GameConfig getRoom(String agentWxUid, String roomWxRid,
			String attribute) {
		List<String> agentWxUids = new ArrayList<>();
		List<String> roomWxRids = new ArrayList<>();
		agentWxUids.add("");
		roomWxRids.add("");

		if (!StringUtils.isEmpty(agentWxUid)) {
			agentWxUids.add(agentWxUid);

			if (!StringUtils.isEmpty(roomWxRid)) {
				roomWxRids.add(roomWxRid);
			}
		}

		GameConfig gameConfig = gameConfigRepository
				.findFirstByAgentWxUidInAndRoomWxRidInAndAttributeOrderByRoomWxRidDescAgentWxUidDesc(
						agentWxUids, roomWxRids, attribute);

		if (gameConfig == null) {
			log.error(
					"Lack of GameConfig, agentWxUid {}, roomWxRid {}, attribute {}",
					agentWxUid, roomWxRid, attribute);
		}

		return gameConfig;
	}

	public Map<String, GameConfig> getRoomMap(String agentWxUid,
			String roomWxRid, List<String> attributes) {
		List<String> agentWxUids = new ArrayList<>();
		List<String> roomWxRids = new ArrayList<>();
		agentWxUids.add("");
		roomWxRids.add("");

		if (!StringUtils.isEmpty(agentWxUid)) {
			agentWxUids.add(agentWxUid);

			if (!StringUtils.isEmpty(roomWxRid)) {
				roomWxRids.add(roomWxRid);
			}
		}

		Map<String, GameConfig> map = new HashMap<>();
		List<GameConfig> dbGameConfigs = gameConfigRepository
				.findByAgentWxUidInAndRoomWxRidInAndAttributeInOrderByRoomWxRidDescAgentWxUidDesc(
						agentWxUids, roomWxRids, attributes);
		if (dbGameConfigs != null) {
			dbGameConfigs.forEach(dbGameConfig -> {
				if (!map.containsKey(dbGameConfig.getAttribute())) {
					map.put(dbGameConfig.getAttribute(), dbGameConfig);
				}
			});
		}

		attributes
				.forEach(attribute -> {
					if (!map.containsKey(attribute)) {
						log.error(
								"Lack of GameConfig, agentWxUid {}, roomWxRid {}, attribute {}",
								agentWxUid, roomWxRid, attribute);
					}
				});

		return map;
	}

	public void upsertRoom(String agentWxUid, String roomWxRid,
			String attribute, String name, String value) {
		agentWxUid = FormatUtils.trim(agentWxUid);
		if (StringUtils.isEmpty(agentWxUid)) {
			roomWxRid = "";
		}
		roomWxRid = FormatUtils.trim(roomWxRid);

		GameConfig gameConfig = gameConfigRepository
				.findByAgentWxUidAndRoomWxRidAndAttribute(agentWxUid,
						roomWxRid, attribute);
		if (gameConfig == null) {
			gameConfig = new GameConfig(attribute, name, value);
			gameConfig.setAgentWxUid(agentWxUid);
			gameConfig.setRoomWxRid(roomWxRid);
			gameConfig.setAttribute(attribute);
		} else {
			gameConfig.setLastUpdate(new Date());
		}

		gameConfig.setName(name);
		gameConfig.setValue(value);

		gameConfigRepository.save(gameConfig);
	}

	@Transactional
	public void deleteByAgentWxUid(String agentWxUid) {
		gameConfigRepository.deleteByAgentWxUid(agentWxUid);
	}
}
