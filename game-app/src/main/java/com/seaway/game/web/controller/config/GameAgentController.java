package com.seaway.game.web.controller.config;

import java.util.List;

import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.repository.impl.GameAgentManager;
import com.seaway.game.manager.repository.impl.GameUtilManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/agents")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class GameAgentController {

	private final GameUtilManager gameUtilManager;

	private final GameAgentManager gameAgentManager;

	@Autowired
	public GameAgentController(GameUtilManager gameUtilManager,
			GameAgentManager gameAgentManager) {
		this.gameUtilManager = gameUtilManager;
		this.gameAgentManager = gameAgentManager;
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public List<GameAgents> getList() {
		return gameAgentManager.getList();
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public void update(@RequestBody GameAgents agent) {
		gameUtilManager.updateAgentAsAdmin(agent);
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/agreen", method = RequestMethod.POST)
	public void agreen(@RequestBody List<GameAgents> agents) {
		gameAgentManager.agreen(agents);
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public void delete(@RequestBody GameAgents agent) {
		gameUtilManager.deleteAgent(agent);
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/ignore", method = RequestMethod.POST)
	public void ignore() {
		gameAgentManager.ignore();
	}

}
