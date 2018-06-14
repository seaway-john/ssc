package com.seaway.game.web.controller.config;

import com.seaway.game.common.entity.manager.GameSetting;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.manager.GameSettingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/setting")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class GameSettingController {

	private final GameSettingManager gameSettingManager;

	@Autowired
	public GameSettingController(GameSettingManager gameSettingManager) {
		this.gameSettingManager = gameSettingManager;
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public GameSetting get() {
		return gameSettingManager.get();
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public void update(@RequestBody GameSetting setting) {
		gameSettingManager.update(setting);
	}

}
