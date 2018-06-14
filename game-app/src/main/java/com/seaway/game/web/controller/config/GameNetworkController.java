package com.seaway.game.web.controller.config;

import java.util.List;

import com.seaway.game.common.entity.manager.GameNetwork;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.manager.GameNetworkManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/network")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class GameNetworkController {

	private final GameNetworkManager gameNetworkManager;

	@Autowired
	public GameNetworkController(GameNetworkManager gameNetworkManager) {
		this.gameNetworkManager = gameNetworkManager;
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public List<GameNetwork> getList() {
		return gameNetworkManager.getList();
	}

	@RequestMapping(value = "/poll", method = RequestMethod.GET)
	public void poll() {
		gameNetworkManager.poll();
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public void update(@RequestBody GameNetwork network) {
		gameNetworkManager.update(network);
	}

}
