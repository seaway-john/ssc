package com.seaway.game.manager.handler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.seaway.game.common.entity.manager.BetDecode;
import com.seaway.game.common.entity.manager.banker.BankerResponse;
import com.seaway.game.common.entity.manager.banker.BetCancelLsBetId;
import com.seaway.game.common.entity.manager.banker.BetSendResponse;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.entity.mysql.GameUserBet;
import com.seaway.game.common.utils.ScriptHelper;

@Slf4j
@Component
public class BetHandler {

	private final BankerHandler bankerHandler;

	@Autowired
	public BetHandler(BankerHandler bankerHandler) {
		this.bankerHandler = bankerHandler;
	}

	public List<BetDecode> formatBet(String message) {
		if (StringUtils.isEmpty(message)) {
			return null;
		}

		String betString = message.trim();
		try {
			String json = ScriptHelper.execScript("python",
					ScriptHelper.getScriptPath("ssc_format_parse.py"),
					betString);

			Gson gson = new Gson();
			Type type = new TypeToken<ArrayList<BetDecode>>() {
			}.getType();

			return gson.fromJson(json, type);
		} catch (Exception e) {
			log.error("Exception in formatBet, reason {}", e.getMessage());
		}

		return null;
	}

	public BetSendResponse sendAgentBet(String agentWxUid, BetDecode betDecode) {
		Gson gson = new Gson();
		Map<String, String> map = new LinkedHashMap<>();
		map.put("BetAmt", betDecode.getMoney() + "");
		map.put("BetNumber", gson.toJson(betDecode.getData()));
		map.put("_", System.currentTimeMillis() + "");

		return bankerHandler.sendBet(agentWxUid, map);
	}

	public BankerResponse cancelAgentBet(String agentWxUid,
			GameUserBet gameUserBet) {
		List<BetCancelLsBetId> lsBetIds = new ArrayList<>();

		Gson gson = new Gson();
		BetDecode betDecode = gson.fromJson(gameUserBet.getBetDecode(),
				BetDecode.class);
		for (Entry<String, List<String>> entry : betDecode.getData().entrySet()) {
			for (String betNumber : entry.getValue()) {
				BetCancelLsBetId betCancelLsBetId = new BetCancelLsBetId();
				betCancelLsBetId.setBetInfoId(gameUserBet.getCancelBetCode());
				betCancelLsBetId.setBetNumber(betNumber);

				lsBetIds.add(betCancelLsBetId);
			}
		}

		Map<String, String> map = new LinkedHashMap<>();
		map.put("LsBetIds", gson.toJson(lsBetIds));

		return bankerHandler.cancelBet(agentWxUid, map);
	}

	public int[] getWinInfo(String awardNumber, String json,
			Map<String, GameConfig> modeMap) {
		Gson gson = new Gson();
		BetDecode betDecode = gson.fromJson(json, BetDecode.class);

		int totalWinCount = 0;
		int totalOdds = 0;
		for (Entry<String, List<String>> entry : betDecode.getData().entrySet()) {
			List<String> winNumbers = new ArrayList<>();
			for (String betNumber : entry.getValue()) {
				if (match(awardNumber, betNumber)) {
					winNumbers.add(betNumber);
				}
			}

			if (winNumbers.isEmpty()) {
				continue;
			}

			String mode = entry.getKey();
			if (modeMap == null || !modeMap.containsKey(mode)) {
				int match = 0;
				char[] winNumberChars = winNumbers.get(0).toCharArray();
				for (int i = 0; i < winNumberChars.length; i++) {
					if (winNumberChars[i] == 'X') {
						continue;
					}

					match++;
				}

				mode = "limit" + match;
			}

			int odd = 0;
			if (modeMap != null && modeMap.containsKey(mode)) {
				odd = Integer.parseInt(modeMap.get(mode).getValue());
			} else {
				switch (mode) {
				case "limit1":
					odd = 9;
					break;
				case "limit2":
					odd = 97;
					break;
				case "limit3":
					odd = 960;
					break;
				case "limit4":
					odd = 9500;
					break;
				case "limit5":
					odd = 94000;
					break;
				}

				log.warn("Lack of mode {} in getIncome, set default odd {}",
						mode, odd);
			}

			int winCount = winNumbers.size();
			totalWinCount += winCount;
			totalOdds += winCount * odd;
		}

		int betWinTicket = totalWinCount * betDecode.getMoney();
		int betIncome = totalOdds * betDecode.getMoney();

		return new int[] { betWinTicket, betIncome };
	}

	private boolean match(String awardNumber, String betNumber) {
		char[] awardNumberChars = awardNumber.toCharArray();
		char[] betNumberChars = betNumber.toCharArray();

		boolean match = true;
		for (int i = 0; i < betNumberChars.length; i++) {
			if (betNumberChars[i] == 'X') {
				continue;
			}

			if (betNumberChars[i] != awardNumberChars[i]) {
				match = false;
				break;
			}
		}

		return match;
	}

}
