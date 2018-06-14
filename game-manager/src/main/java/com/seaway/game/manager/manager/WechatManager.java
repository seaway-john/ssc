package com.seaway.game.manager.manager;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.game.api.LoginQrcode;
import com.seaway.game.common.entity.manager.wechat.WechatUserInfo;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.handler.WechatHandler;
import com.seaway.game.manager.repository.impl.GameAgentManager;
import com.seaway.game.manager.repository.impl.GameRoomManager;
import com.seaway.game.manager.repository.impl.GameUserManager;

@Slf4j
@Component
public class WechatManager {

	private final GameAgentManager gameAgentManager;

	private final GameRoomManager gameRoomManager;

	private final GameUserManager gameUserManager;

	private final WechatHandler wechatHandler;

	@Value("${wechat.appid}")
	private String appId;

	@Value("${wechat.api.host}")
	private String apiHost;

	@Autowired
	public WechatManager(GameAgentManager gameAgentManager,
			GameRoomManager gameRoomManager, GameUserManager gameUserManager,
			WechatHandler wechatHandler) {
		this.gameAgentManager = gameAgentManager;
		this.gameRoomManager = gameRoomManager;
		this.gameUserManager = gameUserManager;
		this.wechatHandler = wechatHandler;
	}

	public String agentLogin(String code) {
		GameAgents gameAgent = gameAgentManager.getByWxSid(code);
		if (gameAgent == null) {
			WechatUserInfo wechatUserInfo = wechatHandler
					.getUserInfoByCode(code);
			if (wechatUserInfo == null) {
				log.warn("Failed to get agent wechatUserInfo");
				return null;
			}

			gameAgent = gameAgentManager.upsert(wechatUserInfo, code);
		}

		if (!gameAgent.isEnabled()) {
			log.info("Un-approve agent {} login, jump to un-approve.html",
					gameAgent.getWxNickName());
			return getUrl("/agent/un-approve.html");
		}

		GameRoom gameRoom = gameRoomManager.getByAgentWxUid(gameAgent
				.getWxUid());

		Map<String, String> params = new HashMap<>();
		params.put("rid", gameRoom.getWxRid());
		params.put("uid", gameAgent.getWxUid());
		params.put("sid", gameAgent.getWxSid());

		String url = getUrlByParams("/agent/index.html#!/lobby", params);

		log.info("Redirect agent {} to url {}", gameAgent.getWxNickName(), url);

		return url;
	}

	public String userLogin(GameRoom gameRoom, String code) {
		GameUsers gameUser = gameUserManager.getByWxSid(code);
		if (gameUser == null) {
			WechatUserInfo wechatUserInfo = wechatHandler
					.getUserInfoByCode(code);
			if (wechatUserInfo == null) {
				log.warn("Failed to get user wechatUserInfo");
				return null;
			}

			gameUser = gameUserManager.upsert(gameRoom, wechatUserInfo, code);
			if (gameUser == null) {
				log.info("Blacklist user {} login, reject it",
						wechatUserInfo.getNickName());
				return null;
			}
		}

		if (!gameUser.isEnabled()) {
			log.info("Blacklist user {} login", gameUser.getWxNickName());
			return null;
		}

		Map<String, String> params = new HashMap<>();
		params.put("rid", gameRoom.getWxRid());
		params.put("uid", gameUser.getWxUid());
		params.put("sid", gameUser.getWxSid());

		String url = getUrlByParams("/user/index.html#!/lobby", params);

		log.info("Redirect user {} to url {}", gameUser.getWxNickName(), url);

		return url;
	}

	public LoginQrcode getAgentLoginUrl(String invitedCode) {
		String agentLoginUrl = getUrl("/game-app/api/wechat/agent/login");

		LoginQrcode loginQrcode = new LoginQrcode();
		loginQrcode.setAppId(appId);
		loginQrcode.setUrl(agentLoginUrl);
		loginQrcode.setState(invitedCode);

		return loginQrcode;
	}

	public LoginQrcode getUserLoginUrl(String roomWxRid) {
		String userLoginUrl = getUrl("/game-app/api/wechat/user/login");

		LoginQrcode loginQrcode = new LoginQrcode();
		loginQrcode.setAppId(appId);
		loginQrcode.setUrl(userLoginUrl);
		loginQrcode.setState(roomWxRid);

		return loginQrcode;
	}

	private String getUrl(String url) {
		return getUrlByParams(url, null);
	}

	private String getUrlByParams(String url, Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append(apiHost);
		sb.append(url);
		sb.append("?");

		if (params != null && !params.isEmpty()) {
			params.forEach((key, value) -> {
				sb.append(key);
				sb.append("=");
				sb.append(value);
				sb.append("&");
			});
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}
}
