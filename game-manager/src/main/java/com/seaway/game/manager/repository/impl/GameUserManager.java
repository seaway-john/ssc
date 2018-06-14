package com.seaway.game.manager.repository.impl;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.manager.wechat.WechatUserInfo;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserBalance;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.repository.GameUsersRepository;

@Slf4j
@Component
public class GameUserManager {

	private final GameUsersRepository gameUsersRepository;

	private final GameUserBalanceManager gameUserBalanceManager;

	private final GameUserLoginReportManager gameUserLoginReportManager;

	@Autowired
	public GameUserManager(GameUsersRepository gameUsersRepository,
			GameUserBalanceManager gameUserBalanceManager,
			GameUserLoginReportManager gameUserLoginReportManager) {
		this.gameUsersRepository = gameUsersRepository;
		this.gameUserBalanceManager = gameUserBalanceManager;
		this.gameUserLoginReportManager = gameUserLoginReportManager;
	}

	public GameUsers getByWxUid(String wxUid) {
		return gameUsersRepository.findByWxUidAndEnabled(wxUid, true);
	}

	public GameUsers getByWxSid(String wxSid) {
		return gameUsersRepository.findByWxSid(wxSid);
	}

	public GameUsers upsert(GameRoom gameRoom, WechatUserInfo wechatUserInfo,
			String code) {
		GameUsers gameUser = gameUsersRepository.findByWxUid(wechatUserInfo
				.getOpenId());
		if (gameUser == null) {
			gameUser = new GameUsers(wechatUserInfo.getOpenId(), code);
		} else if (!gameUser.isEnabled()) {
			return null;
		}

		gameUser.setWxSid(code);
		gameUser.setWxNickName(wechatUserInfo.getNickName());
		gameUser.setWxSex(wechatUserInfo.getSex() == 1);
		gameUser.setLastUpdate(new Date());

		GameUsers dbGameUser = gameUsersRepository.saveAndFlush(gameUser);

		GameUserBalance gameUserBalance = gameUserBalanceManager.upsert(
				gameRoom.getAgentWxUid(), dbGameUser);
		if (!gameUserBalance.isEnabled()) {
			return null;
		}

		gameUserLoginReportManager.insert(gameRoom, dbGameUser);
		return dbGameUser;
	}

	public void toggleTone(String wxUid) {
		GameUsers gameUser = getByWxUid(wxUid);
		if (gameUser == null) {
			return;
		}

		gameUser.setTone(!gameUser.isTone());
		gameUser.setLastUpdate(new Date());
		gameUsersRepository.saveAndFlush(gameUser);

		log.info("User {} toggle tone to {}", gameUser.getWxNickName(),
				gameUser.isTone());
	}

}
