package com.seaway.game.manager.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.game.api.LoginQrcode;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.utils.FormatUtils;
import com.seaway.game.manager.repository.impl.GameConfigManager;

@Component
public class GameQrCodeManager {

	private final GameConfigManager gameConfigManager;

	private final WechatManager wechatManager;

	@Autowired
	public GameQrCodeManager(GameConfigManager gameConfigManager,
			WechatManager wechatManager) {
		this.gameConfigManager = gameConfigManager;
		this.wechatManager = wechatManager;
	}

	public LoginQrcode getAgentLoginUrl() {
		GameConfig gameConfig = gameConfigManager.get("invitedCode");
		if (gameConfig == null) {
			return null;
		}

		return wechatManager.getAgentLoginUrl(gameConfig.getValue());
	}

	public void reGenerate() {
		String invitedCode = FormatUtils.random(16);

		gameConfigManager.upsert("invitedCode", "邀请码", invitedCode);
	}

}
