package com.seaway.game.manager.api;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.game.api.LoginQrcode;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.manager.manager.WechatManager;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.repository.impl.GameRoomManager;

@Slf4j
@Component
public class WechatApiManager {

	private final GameRoomManager gameRoomManager;

	private final GameConfigManager gameConfigManager;

	private final WechatManager wechatManager;

	@Autowired
	public WechatApiManager(GameRoomManager gameRoomManager,
			GameConfigManager gameConfigManager, WechatManager wechatManager) {
		this.gameRoomManager = gameRoomManager;
		this.gameConfigManager = gameConfigManager;
		this.wechatManager = wechatManager;
	}

	public String agentLogin(String invitedCode, String code) {
		if (StringUtils.isEmpty(invitedCode)) {
			return null;
		}

		GameConfig gameConfig = gameConfigManager.get("invitedCode");
		if (gameConfig == null || !gameConfig.getValue().equals(invitedCode)) {
			log.warn("The Invited Code {} is invalid", invitedCode);
			return null;
		}

		return wechatManager.agentLogin(code);
	}

	public String userLogin(String rid, String code) {
		if (StringUtils.isEmpty(rid)) {
			return null;
		}

		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		return wechatManager.userLogin(gameRoom, code);
	}

	public LoginQrcode getRoomLoginUrl(String rid) {
		GameRoom gameRoom = gameRoomManager.getByWxRid(rid);
		if (gameRoom == null) {
			return null;
		}

		return wechatManager.getUserLoginUrl(gameRoom.getWxRid());
	}

}
