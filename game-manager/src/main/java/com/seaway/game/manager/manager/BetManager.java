package com.seaway.game.manager.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.seaway.game.common.entity.manager.BetDecode;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameLotteryWin;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.common.utils.TimeDuration;
import com.seaway.game.manager.handler.BetHandler;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.repository.impl.GameUserBetManager;

@Slf4j
@Component
public class BetManager {

	private final GameUserBetManager gameUserBetManager;

	private final GameConfigManager gameConfigManager;

	private final BetHandler betHandler;

	@Autowired
	public BetManager(GameUserBetManager gameUserBetManager,
			GameConfigManager gameConfigManager, BetHandler betHandler) {
		this.gameUserBetManager = gameUserBetManager;
		this.gameConfigManager = gameConfigManager;
		this.betHandler = betHandler;
	}

	public void send(GameLotteryWin gameLotteryWin, GameUsers gameUser,
			GameRoom gameRoom, String message) {
		if (!Pattern.compile("\\d+").matcher(message).find()) {
			return;
		}

		List<BetDecode> betDecodes = betHandler.formatBet(message);
		if (betDecodes == null || betDecodes.isEmpty()) {
			return;
		}

		betDecodes.forEach(betDecode -> {
			gameUserBetManager.insert(gameLotteryWin, gameUser, gameRoom,
					message, betDecode);
		});
	}

	public boolean cancel(GameLotteryWin gameLotteryWin, GameRoom gameRoom,
			GameUsers gameUser, int betId) {
		return gameUserBetManager.cancelBet(gameLotteryWin, gameRoom, gameUser,
				betId);
	}

	public void encash(GameLotteryWin gameLotteryWin) {
		TimeDuration td = new TimeDuration();

		List<GameUserBet> gameUserBets = gameUserBetManager
				.awaitEncashByLotteryWinSequence(gameLotteryWin.getSequence());
		if (gameUserBets != null && !gameUserBets.isEmpty()) {
			List<String> attributes = new ArrayList<>();
			attributes.add("limit1");
			attributes.add("limit2");
			attributes.add("limit3");
			attributes.add("limit4");
			attributes.add("limit5");

			Map<String, Map<String, GameConfig>> roomConfigMap = new HashMap<>();
			gameUserBets.forEach(gameUserBet -> {
				if (!roomConfigMap.containsKey(gameUserBet.getRoomWxRid())) {
					Map<String, GameConfig> map = gameConfigManager.getRoomMap(
							gameUserBet.getAgentWxUid(),
							gameUserBet.getRoomWxRid(), attributes);

					roomConfigMap.put(gameUserBet.getRoomWxRid(), map);
				}
			});

			for (GameUserBet gameUserBet : gameUserBets) {
				int[] winInfo = betHandler.getWinInfo(
						gameLotteryWin.getAwardNumber(),
						gameUserBet.getBetDecode(),
						roomConfigMap.get(gameUserBet.getRoomWxRid()));
				gameUserBetManager.encash(gameUserBet, winInfo[0], winInfo[1]);
			}
			log.info("encash cost {} ms", td.stop());
		} else {
			log.info("Nobody bet the lotteryWin sequence {}",
					gameLotteryWin.getSequence());
		}

	}

}
