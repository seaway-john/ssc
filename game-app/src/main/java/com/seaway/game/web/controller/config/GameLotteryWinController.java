package com.seaway.game.web.controller.config;

import java.util.Date;

import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.repository.impl.GameLotteryWinManager;
import com.seaway.game.manager.schedule.CqsscSchedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/lottery-win")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class GameLotteryWinController {

	private final CqsscSchedule cqsscSchedule;

	private final GameLotteryWinManager gameLotteryWinManager;

	@Autowired
	public GameLotteryWinController(CqsscSchedule cqsscSchedule,
			GameLotteryWinManager gameLotteryWinManager) {
		this.cqsscSchedule = cqsscSchedule;
		this.gameLotteryWinManager = gameLotteryWinManager;
	}

	@RequestMapping(value = "/page", method = RequestMethod.GET)
	public Page<GameLotteryWin> getPage(Pageable pageable) {
		return gameLotteryWinManager.getPageable(pageable);
	}

	@PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
	@RequestMapping(value = "/publish", method = RequestMethod.POST)
	public void publish(@RequestBody GameLotteryWin gameLotteryWin) {
		cqsscSchedule.publish(gameLotteryWin.getSequence(),
				gameLotteryWin.getAwardNumber(), new Date());
	}

}
