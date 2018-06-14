package com.seaway.game.manager.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserLoginReport;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.repository.GameUserLoginReportRepository;

@Component
public class GameUserLoginReportManager {

	private final GameUserLoginReportRepository gameUserLoginReportRepository;

	@Autowired
	public GameUserLoginReportManager(
			GameUserLoginReportRepository gameUserLoginReportRepository) {
		this.gameUserLoginReportRepository = gameUserLoginReportRepository;
	}

	public Page<GameUserLoginReport> getPageable(Pageable pageable) {
		return gameUserLoginReportRepository.findAll(pageable);
	}

	public void insert(GameRoom gameRoom, GameUsers gameUser) {
		GameUserLoginReport userLoginReport = new GameUserLoginReport(
				gameUser.getWxUid(), gameUser.getWxSid(),
				gameUser.getWxNickName());
		userLoginReport.setAgent(false);
		userLoginReport.setRoomWxRid(gameRoom.getWxRid());
		userLoginReport.setRoomName(gameRoom.getName());

		gameUserLoginReportRepository.save(userLoginReport);
	}

	public void insert(GameAgents gameAgent) {
		GameUserLoginReport userLoginReport = new GameUserLoginReport(
				gameAgent.getWxUid(), gameAgent.getWxSid(),
				gameAgent.getWxNickName());
		userLoginReport.setAgent(true);

		gameUserLoginReportRepository.save(userLoginReport);
	}

}
