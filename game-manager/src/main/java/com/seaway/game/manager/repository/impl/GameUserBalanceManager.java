package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.manager.UserManage;
import com.seaway.game.common.entity.manager.UserRechargeApply;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameRoom;
import com.seaway.game.common.entity.mysql.GameUserBalance;
import com.seaway.game.common.entity.mysql.GameUserBalanceReport;
import com.seaway.game.common.entity.mysql.GameUserBalanceReportType;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.entity.mysql.GameUsers;
import com.seaway.game.manager.entity.WebSocketMessage;
import com.seaway.game.manager.repository.GameUserBalanceRepository;
import com.seaway.game.manager.repository.GameUsersRepository;
import com.seaway.game.manager.websocket.WebSocketEndPoint;
import com.seaway.game.manager.websocket.WebSocketType;

@Slf4j
@Component
public class GameUserBalanceManager {

	private final GameUserBalanceRepository gameUserBalanceRepository;

	private final GameUsersRepository gameUsersRepository;

	private final GameAgentManager gameAgentManager;

	private final GameUserBalanceReportManager gameUserBalanceReportManager;

	private final GameRoomChatManager gameRoomChatManager;

	private final GameConfigManager gameConfigManager;

	private final WebSocketEndPoint webSocketEndPoint;

	@Autowired
	public GameUserBalanceManager(
			GameUserBalanceRepository gameUserBalanceRepository,
			GameUsersRepository gameUsersRepository,
			GameAgentManager gameAgentManager,
			GameUserBalanceReportManager gameUserBalanceReportManager,
			GameRoomChatManager gameRoomChatManager,
			GameConfigManager gameConfigManager,
			WebSocketEndPoint webSocketEndPoint) {
		this.gameUserBalanceRepository = gameUserBalanceRepository;
		this.gameUsersRepository = gameUsersRepository;
		this.gameAgentManager = gameAgentManager;
		this.gameUserBalanceReportManager = gameUserBalanceReportManager;
		this.gameRoomChatManager = gameRoomChatManager;
		this.gameConfigManager = gameConfigManager;
		this.webSocketEndPoint = webSocketEndPoint;
	}

	public GameUserBalance getByAgentWxUidAndUserWxUid(String agentWxUid,
			String userWxUid) {
		return gameUserBalanceRepository
				.findByAgentWxUidAndUserWxUidAndEnabled(agentWxUid, userWxUid,
						true);
	}

	private GameUserBalance save(GameUserBalance gameUserBalance) {
		return gameUserBalanceRepository.saveAndFlush(gameUserBalance);
	}

	public GameUserBalance toggleTrialPlay(String agentWxUid, GameUsers gameUser) {
		GameUserBalance gameUserBalance = getByAgentWxUidAndUserWxUid(
				agentWxUid, gameUser.getWxUid());
		if (gameUserBalance == null) {
			return null;
		}

		gameUserBalance.setTrialPlay(!gameUserBalance.isTrialPlay());
		GameUserBalance dbGameUserBalance = save(gameUserBalance);

		log.info("User {} toggle trial to {}", gameUser.getWxNickName(),
				gameUserBalance.isTrialPlay());

		return dbGameUserBalance;
	}

	public GameUserBalance upsert(String agentWxUid, GameUsers dbGameUser) {
		GameUserBalance gameUserBalance = gameUserBalanceRepository
				.findByAgentWxUidAndUserWxUid(agentWxUid, dbGameUser.getWxUid());
		if (gameUserBalance != null) {
			return gameUserBalance;
		}

		gameUserBalance = new GameUserBalance(agentWxUid, dbGameUser);
		GameConfig gameConfig = gameConfigManager.getAgent(agentWxUid,
				"trialPlayBalance");
		if (gameConfig != null) {
			gameUserBalance.setTrialPlayBalance(Integer.parseInt(gameConfig
					.getValue()));
		}

		return gameUserBalanceRepository.saveAndFlush(gameUserBalance);
	}

	@Transactional
	public GameUserBalance update(GameUserBet gameUserBet,
			GameUserBalanceReportType type) {
		String agentWxUid = gameUserBet.getAgentWxUid();
		String userWxUid = gameUserBet.getUserWxUid();

		int gain = 0;
		switch (type) {
		case BET:
			gain = 0 - gameUserBet.getBetInvest();
			break;
		case BET_ENCASH:
			gain = gameUserBet.getBetIncome();
			break;
		case BET_CANCEL:
			gain = gameUserBet.getBetInvest();
			break;
		default:
			break;
		}

		if (gameUserBet.isTrialPlay()) {
			gameUserBalanceRepository
					.updateTrialPlayBalanceByAgentWxUidAndUserWxUid(agentWxUid,
							userWxUid, gain);
		} else {
			switch (type) {
			case BET:
			case BET_CANCEL:
				gameUserBalanceRepository
						.updateBetInvestByAgentWxUidAndUserWxUid(agentWxUid,
								userWxUid, -gain);
				break;
			case BET_ENCASH:
				gameUserBalanceRepository
						.updateBetIncomeByAgentWxUidAndUserWxUid(agentWxUid,
								userWxUid, gain);
				break;
			default:
				break;
			}
		}

		GameUserBalance dbGameUserBalance = getByAgentWxUidAndUserWxUid(
				agentWxUid, userWxUid);
		if (gain != 0) {
			if (!gameUserBet.isTrialPlay()) {
				if (!gameUserBet.isNpc()) {
					gameAgentManager.update(agentWxUid, type, gain);
				}
				gameUserBalanceReportManager.insert(dbGameUserBalance,
						gameUserBet, type, gain);
			}

			notifyBalanceChange(agentWxUid, userWxUid);
		}

		return dbGameUserBalance;
	}

	@Transactional
	public void agreenUserRecharges(String agentWxUid, GameRoom gameRoom,
			List<UserRechargeApply> recharges, boolean cancel) {
		for (UserRechargeApply recharge : recharges) {
			GameUserBalanceReport gameUserBalanceReport = gameUserBalanceReportManager
					.getById(recharge.getId());
			if (gameUserBalanceReport == null) {
				continue;
			}

			GameUserBalanceReportType type = Enum.valueOf(
					GameUserBalanceReportType.class,
					gameUserBalanceReport.getType());

			if (cancel) {
				if (!gameUserBalanceReport.isEncash()) {
					continue;
				}
			} else {
				if (gameUserBalanceReport.isEncash()) {
					continue;
				}

				if (GameUserBalanceReportType.CASH_DOWN.equals(type)) {
					GameUserBalance gameUserBalance = gameUserBalanceRepository
							.findByAgentWxUidAndUserWxUid(agentWxUid,
									gameUserBalanceReport.getUserWxUid());
					if (gameUserBalance == null) {
						continue;
					}

					if (gameUserBalance.getBalance() < Math
							.abs(gameUserBalanceReport.getGain())) {
						continue;
					}
				}
			}

			int gain = cancel ? -gameUserBalanceReport.getGain()
					: gameUserBalanceReport.getGain();
			gameUserBalanceRepository.updateBalanceByAgentWxUidAndUserWxUid(
					agentWxUid, gameUserBalanceReport.getUserWxUid(), gain);
			notifyBalanceChange(agentWxUid,
					gameUserBalanceReport.getUserWxUid());

			gameUserBalanceReportManager.agreen(gameUserBalanceReport, cancel);

			if (!cancel) {
				StringBuilder sb = new StringBuilder();
				sb.append("🌹");

				switch (type) {
				case CASH_UP:
					sb.append("上粮+");
					break;
				case CASH_DOWN:
					sb.append("下粮-");
					break;
				default:
					continue;
				}

				sb.append(Math.abs(gain));
				sb.append("成功🌹，请查收");

				String wxNickName = gameUsersRepository.findByWxUid(
						gameUserBalanceReport.getUserWxUid()).getWxNickName();

				List<String> mesages = new ArrayList<>();
				mesages.add(sb.toString());
				gameRoomChatManager.insertRobot(gameRoom, mesages, wxNickName,
						false);
			}
		}
	}

	public Map<String, Object> getUserManageMap(String rid, String agentWxUid,
			int page, int limit) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 0);
		map.put("msg", "");

		Pageable pageable = new PageRequest(page - 1, limit);

		Page<GameUserBalance> userBalancePage = gameUserBalanceRepository
				.findByAgentWxUidOrderByBetInvestDesc(pageable, agentWxUid);

		List<GameUserBalance> userBalances = userBalancePage.getContent();

		List<UserManage> userManages1 = new ArrayList<>();
		List<UserManage> userManages2 = new ArrayList<>();
		List<UserManage> userManages3 = new ArrayList<>();
		if (userBalances != null && !userBalances.isEmpty()) {
			userBalances
					.forEach(userBalance -> {
						UserManage userManage = new UserManage();
						userManage.setId(userBalance.getId());
						userManage.setRemarkName(userBalance.getRemarkName());
						userManage.setBalance(userBalance.getBalance());
						userManage.setBetInvest(userBalance.getBetInvest());
						userManage.setBetIncome(userBalance.getBetIncome());

						if (userBalance.isEnabled()) {
							if (webSocketEndPoint.onlineRoom(rid,
									userBalance.getUserWxUid())) {
								String status = "<span style=\"color: green;\">在线</span>";
								userManage.setStatus(status);

								userManages1.add(userManage);
							} else {
								String status = "<span style=\"color: gray;\">离线</span>";
								userManage.setStatus(status);

								userManages2.add(userManage);
							}
						} else {
							String status = "<span style=\"color: red;\">冻结</span>";
							userManage.setStatus(status);

							userManages3.add(userManage);
						}
					});
		}

		List<UserManage> userManages = new ArrayList<>();
		userManages.addAll(userManages1);
		userManages.addAll(userManages2);
		userManages.addAll(userManages3);

		map.put("count", userBalancePage.getTotalElements());
		map.put("data", userManages);

		return map;
	}

	@Transactional
	public GameUserBalance updateRemarkname(int id, String remarkName) {
		GameUserBalance gameUserBalance = gameUserBalanceRepository.findOne(id);
		if (gameUserBalance == null) {
			return null;
		}

		gameUserBalanceRepository.updateRemarknameById(id, remarkName);

		return gameUserBalance;
	}

	@Transactional
	public ResponseEntity exchangeWeal(GameRoom gameRoom, String userWxUid,
			int weal) {
		GameUserBalance gameUserBalance = getByAgentWxUidAndUserWxUid(
				gameRoom.getAgentWxUid(), userWxUid);
		if (gameUserBalance == null || gameUserBalance.isTrialPlay()) {
			return null;
		}

		GameConfig gameConfig = gameConfigManager.getAgent(
				gameRoom.getAgentWxUid(), "wealRate");
		if (gameConfig == null) {
			return null;
		}

		int wealRate = 0;
		if (!StringUtils.isEmpty(gameConfig.getValue())) {
			wealRate = Integer.parseInt(gameConfig.getValue());
		}

		if (wealRate == 0) {
			return null;
		}

		ResponseEntity response = new ResponseEntity();
		int exchangeBetInvest = (int) Math.ceil(weal * 10000 / wealRate);
		if (gameUserBalance.getAvailableBetInvest() < exchangeBetInvest) {
			response.setMessage("您的兑换福利超过了可兑换值");
			return response;
		}

		gameUserBalanceRepository.exchangeWealById(gameUserBalance.getId(),
				weal, exchangeBetInvest);

		GameUserBalance dbGameUserBalance = gameUserBalanceRepository
				.findOne(gameUserBalance.getId());
		gameUserBalanceReportManager.insert(dbGameUserBalance, null,
				GameUserBalanceReportType.WEAL, weal);

		notifyBalanceChange(gameRoom.getAgentWxUid(), userWxUid);

		StringBuilder sb = new StringBuilder();
		sb.append("🌹兑换福利+");
		sb.append(weal);
		sb.append("成功🌹，请查收");

		String wxNickName = gameUsersRepository.findByWxUid(userWxUid)
				.getWxNickName();

		List<String> mesages = new ArrayList<>();
		mesages.add(sb.toString());
		gameRoomChatManager.insertRobot(gameRoom, mesages, wxNickName, false);

		response.setStatus(true);
		response.setMessage("成功兑换福利，请查收");
		return response;
	}

	@Transactional
	public void blacklisUser(UserManage userManage, boolean cancel) {
		GameUserBalance gameUserBalance = gameUserBalanceRepository
				.findOne(userManage.getId());
		if (gameUserBalance == null) {
			return;
		}

		gameUserBalanceRepository.updateEnableById(userManage.getId(), cancel);

		if (!cancel) {
			webSocketEndPoint.closeByUid(gameUserBalance.getUserWxUid());
		}
	}

	private void notifyBalanceChange(String agentWxUid, String userWxUid) {
		GameUserBalance gameUserBalance = getByAgentWxUidAndUserWxUid(
				agentWxUid, userWxUid);
		if (gameUserBalance == null) {
			return;
		}

		int balance = gameUserBalance.isTrialPlay() ? gameUserBalance
				.getTrialPlayBalance() : gameUserBalance.getBalance();

		WebSocketMessage wsMessage = new WebSocketMessage(
				WebSocketType.BALANCE_CHANGE, balance);
		webSocketEndPoint.broadcastUid(userWxUid, wsMessage);
	}

	@Transactional
	public void deleteByAgentWxUid(String agentWxUid) {
		gameUserBalanceRepository.deleteByAgentWxUid(agentWxUid);
	}

}
