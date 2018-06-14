package com.seaway.game.web.controller.statistics;

import java.util.ArrayList;
import java.util.List;
import com.seaway.game.common.entity.manager.AgentJournal;
import com.seaway.game.common.entity.mysql.GameAgents;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.utils.Constants;
import com.seaway.game.manager.repository.impl.GameAgentManager;
import com.seaway.game.manager.repository.impl.GameConfigManager;
import com.seaway.game.manager.repository.impl.GameUserBetManager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics/agent-journal-account")
@PreAuthorize("hasRole('" + Constants.ROLE_RO + "')")
public class AgentJournalAccountController {

	private final GameAgentManager gameAgentManager;

	private final GameUserBetManager gameUserBetManager;

	private final GameConfigManager gameConfigManager;

	@Autowired
	public AgentJournalAccountController(GameAgentManager gameAgentManager,
			GameUserBetManager gameUserBetManager,
			GameConfigManager gameConfigManager) {
		this.gameAgentManager = gameAgentManager;
		this.gameUserBetManager = gameUserBetManager;
		this.gameConfigManager = gameConfigManager;
	}

	@RequestMapping(value = "/table/refresh", method = RequestMethod.GET)
	public List<AgentJournal> getAgentJournalAccount(
			@RequestParam(value = "dayStart", defaultValue = "-1") int dayStart,
			@RequestParam(value = "dayEnd", defaultValue = "-1") int dayEnd) {
		List<GameAgents> gameAgents = gameAgentManager.getList();
		if (gameAgents == null || gameAgents.isEmpty()) {
			return new ArrayList<>();
		}

		List<AgentJournal> agentJournals = new ArrayList<>();
		gameAgents.forEach(gameAgent -> {
			AgentJournal agentJournal = new AgentJournal(gameAgent.getWxUid(),
					gameAgent.getWxSid());
			BeanUtils.copyProperties(gameAgent, agentJournal);

			GameConfig gameConfig = gameConfigManager.getAgent(
					gameAgent.getWxUid(), "rebateRate");
			if (gameConfig != null) {
				agentJournal.setRebateRate(Float.parseFloat(gameConfig
						.getValue()));
			}

			agentJournal.setBetInvest(0);
			agentJournal.setBetIncome(0);

			List<GameUserBet> gameUserBets = gameUserBetManager
					.getAgentUserBets(gameAgent.getWxUid(), dayStart, dayEnd);
			if (gameUserBets != null && !gameUserBets.isEmpty()) {
				int betInvest = 0;
				int betIncome = 0;
				for (GameUserBet gameUserBet : gameUserBets) {
					betInvest += gameUserBet.getBetInvest();
					betIncome += gameUserBet.getBetIncome();
				}

				agentJournal.setBetInvest(betInvest);
				agentJournal.setBetIncome(betIncome);
				agentJournal.setBetRebate(Math.round(agentJournal
						.getRebateRate() * agentJournal.getBetInvest() / 100));
			}

			agentJournals.add(agentJournal);
		});

		return agentJournals;
	}
}
