package com.seaway.game.web.controller.statistics;

import com.seaway.game.common.entity.mysql.GameUserLoginReport;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.repository.impl.GameUserLoginReportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics/game-login-report")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class GameLoginReportController {

	private final GameUserLoginReportManager gameUserLoginReportManager;

	@Autowired
	public GameLoginReportController(
			GameUserLoginReportManager gameUserLoginReportManager) {
		this.gameUserLoginReportManager = gameUserLoginReportManager;
	}

	@RequestMapping(value = "/page", method = RequestMethod.GET)
	public Page<GameUserLoginReport> getPage(Pageable pageable) {
		return gameUserLoginReportManager.getPageable(pageable);
	}

}
