package com.seaway.game.web.controller.config;

import com.seaway.game.common.entity.game.api.LoginQrcode;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.manager.GameQrCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/qrcode")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class GameQrCodeController {

	private final GameQrCodeManager gameQrCodeManager;

	@Autowired
	public GameQrCodeController(GameQrCodeManager gameQrCodeManager) {
		this.gameQrCodeManager = gameQrCodeManager;
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.GET)
	public LoginQrcode getAgentLoginUrl() {
		return gameQrCodeManager.getAgentLoginUrl();
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/re-generate", method = RequestMethod.GET)
	public void reGenerate() {
		gameQrCodeManager.reGenerate();
	}

}
