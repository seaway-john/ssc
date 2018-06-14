package com.seaway.game.manager.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.seaway.game.common.entity.ResponseEntity;
import com.seaway.game.common.entity.game.api.UserRecharge;
import com.seaway.game.common.entity.manager.BetRecord;
import com.seaway.game.common.entity.manager.UserRechargeApply;
import com.seaway.game.common.entity.mysql.GameUserBalance;
import com.seaway.game.common.entity.mysql.GameUserBalanceReport;
import com.seaway.game.common.entity.mysql.GameUserBalanceReportType;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.utils.DateUtils;
import com.seaway.game.manager.repository.GameUserBalanceReportRepository;

@Component
public class GameUserBalanceReportManager {

	private final GameUserBalanceReportRepository gameUserBalanceReportRepository;

	@Autowired
	public GameUserBalanceReportManager(
			GameUserBalanceReportRepository gameUserBalanceReportRepository) {
		this.gameUserBalanceReportRepository = gameUserBalanceReportRepository;
	}

	public GameUserBalanceReport getById(int id) {
		return gameUserBalanceReportRepository.findOne(id);
	}

	private void save(GameUserBalanceReport gameUserBalanceReport) {
		gameUserBalanceReportRepository.save(gameUserBalanceReport);
	}

	public ResponseEntity insert(GameUserBalance gameUserBalance,
			UserRecharge userRecharge) {
		if (userRecharge == null || userRecharge.getNumber() <= 0) {
			return null;
		}

		if (!GameUserBalanceReportType.CASH_UP.equals(userRecharge.getType())
				&& !GameUserBalanceReportType.CASH_DOWN.equals(userRecharge
						.getType())) {
			return null;
		}

		ResponseEntity response = new ResponseEntity();
		int unExchageCount = gameUserBalanceReportRepository
				.countByAgentWxUidAndUserWxUidAndTypeAndEncash(gameUserBalance
						.getAgentWxUid(), gameUserBalance.getUserWxUid(),
						userRecharge.getType().toString(), false);
		if (unExchageCount > 0) {
			response.setMessage("操作失败，您有未处理的请求");
			return response;
		}

		if (GameUserBalanceReportType.CASH_DOWN.equals(userRecharge.getType())) {
			if (gameUserBalance.getBalance() < userRecharge.getNumber()) {
				response.setMessage("下粮失败，没有足够的粮草");
				return response;
			}
		}

		GameUserBalanceReport gameUserBalanceReport = new GameUserBalanceReport(
				gameUserBalance.getAgentWxUid(),
				gameUserBalance.getUserWxUid(), userRecharge.getType());
		gameUserBalanceReport.setRemarkName(gameUserBalance.getRemarkName());
		gameUserBalanceReport.setEncash(false);

		int gain = 0;
		if (GameUserBalanceReportType.CASH_UP.equals(userRecharge.getType())) {
			gain = userRecharge.getNumber();
		} else if (GameUserBalanceReportType.CASH_DOWN.equals(userRecharge
				.getType())) {
			gain = 0 - userRecharge.getNumber();
		}

		gameUserBalanceReport.setGain(gain);
		gameUserBalanceReport.setBalance(gameUserBalance.getBalance() + gain);

		save(gameUserBalanceReport);

		response.setStatus(true);
		response.setMessage("申请成功，请联系房管确认");

		return response;
	}

	public void insert(GameUserBalance gameUserBalance,
			GameUserBet gameUserBet, GameUserBalanceReportType type, int gain) {
		GameUserBalanceReport gameUserBalanceReport = new GameUserBalanceReport(
				gameUserBalance.getAgentWxUid(),
				gameUserBalance.getUserWxUid(), type);
		gameUserBalance.setRemarkName(gameUserBalance.getRemarkName());
		if (gameUserBet != null) {
			gameUserBalanceReport.setLotteryWinSequence(gameUserBet
					.getLotteryWinSequence());
			gameUserBalanceReport.setBetFormat(gameUserBet.getBetFormat());
		}
		gameUserBalanceReport.setEncash(true);
		gameUserBalanceReport.setGain(gain);
		gameUserBalanceReport.setBalance(gameUserBalance.getBalance());

		save(gameUserBalanceReport);

	}

	public Map<String, Object> getRechargeMap(String agentWxUid, int page,
			int limit) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 0);
		map.put("msg", "");

		Pageable pageable = new PageRequest(page - 1, limit);

		List<String> types = new ArrayList<>();
		types.add(GameUserBalanceReportType.CASH_UP.toString());
		types.add(GameUserBalanceReportType.CASH_DOWN.toString());

		Page<GameUserBalanceReport> userBalanceReportPage = gameUserBalanceReportRepository
				.findByAgentWxUidAndTypeInOrderByCreatedDesc(pageable,
						agentWxUid, types);

		List<GameUserBalanceReport> userBalanceReports = userBalanceReportPage
				.getContent();

		List<UserRechargeApply> userRechargeApplys = new ArrayList<>();
		if (userBalanceReports != null && !userBalanceReports.isEmpty()) {
			for (GameUserBalanceReport userBalanceReport : userBalanceReports) {
				UserRechargeApply userRechargeApply = new UserRechargeApply();
				String cnType = null;

				GameUserBalanceReportType type = Enum.valueOf(
						GameUserBalanceReportType.class,
						userBalanceReport.getType());
				switch (type) {
				case CASH_UP:
					cnType = "上粮+" + userBalanceReport.getGain();
					break;
				case CASH_DOWN:
					cnType = "下粮-" + Math.abs(userBalanceReport.getGain());
					break;
				default:
					break;
				}

				userRechargeApply.setId(userBalanceReport.getId());
				userRechargeApply.setRemarkName(userBalanceReport
						.getRemarkName());
				userRechargeApply.setType(cnType);
				userRechargeApply
						.setStatus(userBalanceReport.isEncash() ? "<span style=\"color: green;\">已同意</span>"
								: "<span style=\"color: orange;\">未处理</span>");
				userRechargeApply.setDate(DateUtils
						.getMysqlDateString(userBalanceReport.getCreated()));

				userRechargeApplys.add(userRechargeApply);
			}
		}

		map.put("count", userBalanceReportPage.getTotalElements());
		map.put("data", userRechargeApplys);

		return map;
	}

	public Map<String, Object> getRecordMap(String agentWxUid,
			String userWxUid, int page, int limit, int day) {
		Map<String, Object> map = new HashMap<>();
		map.put("code", 0);
		map.put("msg", "");

		Pageable pageable = new PageRequest(page - 1, limit);

		Date dateStart = DateUtils.getDateBeforeDay(day);
		Date dateEnd = new Date();
		if (day == 1) {
			dateEnd = DateUtils.getDateBeforeDay(0);
		}

		Page<GameUserBalanceReport> userBalanceReportPage = gameUserBalanceReportRepository
				.findByAgentWxUidAndUserWxUidAndCreatedBetweenAndEncash(
						pageable, agentWxUid, userWxUid, dateStart, dateEnd,
						true);

		List<GameUserBalanceReport> userBalanceReports = userBalanceReportPage
				.getContent();

		List<BetRecord> betRecodrs = new ArrayList<>();
		if (userBalanceReports != null && !userBalanceReports.isEmpty()) {
			for (GameUserBalanceReport userBalanceReport : userBalanceReports) {
				BetRecord betRecod = new BetRecord();
				String cnType = null;

				GameUserBalanceReportType type = Enum.valueOf(
						GameUserBalanceReportType.class,
						userBalanceReport.getType());
				switch (type) {
				case WEAL:
					cnType = "福利";
					break;
				case BET:
					cnType = "押粮";
					break;
				case BET_CANCEL:
					cnType = "退粮";
					break;
				case BET_ENCASH:
					cnType = "中粮";
					break;
				case CASH_UP:
					cnType = "上粮";
					break;
				case CASH_DOWN:
					cnType = "下粮";
					break;
				default:
					break;
				}

				betRecod.setType(cnType);
				betRecod.setBetFormat(userBalanceReport.getBetFormat());
				betRecod.setBalanceGain(userBalanceReport.getGain());
				betRecod.setBalanceRemain(userBalanceReport.getBalance());
				betRecod.setDate(DateUtils.getMysqlDateString(userBalanceReport
						.getCreated()));

				betRecodrs.add(betRecod);
			}
		}

		map.put("count", userBalanceReportPage.getTotalElements());
		map.put("data", betRecodrs);

		return map;
	}

	@Transactional
	public void ignoreAllUserRecharges(String agentWxUid) {
		List<String> types = new ArrayList<>();
		types.add(GameUserBalanceReportType.CASH_UP.toString());
		types.add(GameUserBalanceReportType.CASH_DOWN.toString());

		gameUserBalanceReportRepository.deleteByAgentWxUidAndTypeInAndEncash(
				agentWxUid, types, false);
	}

	public void agreen(GameUserBalanceReport gameUserBalanceReport,
			boolean cancel) {
		gameUserBalanceReport.setEncash(!cancel);
		gameUserBalanceReport.setLastUpdate(new Date());

		save(gameUserBalanceReport);
	}

	@Transactional
	public void updateRemarkname(String agentWxUid, String userWxUid,
			String remarkName) {
		gameUserBalanceReportRepository
				.updateRemarknameByAgentWxUidAndUserWxUid(agentWxUid,
						userWxUid, remarkName);
	}

	@Transactional
	public void deleteByAgentWxUid(String agentWxUid) {
		gameUserBalanceReportRepository.deleteByAgentWxUid(agentWxUid);
	}

}
