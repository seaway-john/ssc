package com.seaway.game.manager.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.manager.GameNetwork;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.schedule.BetterNetworkSchedule;
import com.seaway.game.manager.schedule.BetterNetworkTask;

@Component
public class GameNetworkManager {

	private final GameConfigManager gameConfigManager;

	private final BetterNetworkSchedule betterNetworkSchedule;

	private final BetterNetworkTask betterNetworkTask;

	@Autowired
	public GameNetworkManager(GameConfigManager gameConfigManager,
			BetterNetworkSchedule betterNetworkSchedule,
			BetterNetworkTask betterNetworkTask) {
		this.gameConfigManager = gameConfigManager;
		this.betterNetworkSchedule = betterNetworkSchedule;
		this.betterNetworkTask = betterNetworkTask;
	}

	public List<GameNetwork> getList() {
		List<String> attributes = new ArrayList<>();
		for (int i = 1; i <= 9; i++) {
			attributes.add("network" + i);
		}

		Map<String, GameConfig> networkMap = gameConfigManager
				.getMap(attributes);

		List<GameNetwork> networks = new ArrayList<>();
		for (GameConfig gameConfig : networkMap.values()) {
			GameNetwork network = new GameNetwork();
			network.setAttribute(gameConfig.getAttribute());
			network.setName(gameConfig.getName());
			network.setUrl(gameConfig.getValue());
			network.setDelay(betterNetworkTask.getDelayByAttribute(gameConfig
					.getAttribute()));

			networks.add(network);
		}

		networks.sort((network1, networks2) -> network1.getAttribute()
				.compareTo(networks2.getAttribute()));

		return networks;
	}

	public void update(GameNetwork network) {
		if (network == null) {
			return;
		}

		gameConfigManager.upsert(network.getAttribute(), network.getName(),
				network.getUrl());
	}

	public void poll() {
		betterNetworkSchedule.pollNetworks();
	}

}
