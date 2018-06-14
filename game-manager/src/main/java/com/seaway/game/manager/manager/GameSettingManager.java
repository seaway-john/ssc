package com.seaway.game.manager.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.seaway.game.common.entity.manager.GameSetting;
import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.manager.repository.impl.GameConfigManager;

@Component
public class GameSettingManager {

	private final GameConfigManager gameConfigManager;

	@Autowired
	public GameSettingManager(GameConfigManager gameConfigManager) {
		this.gameConfigManager = gameConfigManager;
	}

	public GameSetting get() {
		List<String> attributes = new ArrayList<>();
		attributes.add("roomName");
		attributes.add("trialPlayBalance");
		attributes.add("minEachBetInvest");
		attributes.add("maxEachBetInvest");
		attributes.add("limit2");
		attributes.add("limit3");
		attributes.add("limit4");
		attributes.add("limit5");

		Map<String, GameConfig> settingMap = gameConfigManager
				.getMap(attributes);

		GameSetting setting = new GameSetting();
		if (settingMap.containsKey("roomName")) {
			setting.setRoomName(settingMap.get("roomName").getValue());
		}
		if (settingMap.containsKey("trialPlayBalance")) {
			setting.setTrialPlayBalance(Integer.parseInt(settingMap.get(
					"trialPlayBalance").getValue()));
		}
		if (settingMap.containsKey("minEachBetInvest")) {
			setting.setMinEachBetInvest(Integer.parseInt(settingMap.get(
					"minEachBetInvest").getValue()));
		}
		if (settingMap.containsKey("maxEachBetInvest")) {
			setting.setMaxEachBetInvest(Integer.parseInt(settingMap.get(
					"maxEachBetInvest").getValue()));
		}
		if (settingMap.containsKey("limit2")) {
			setting.setLimit2(Integer.parseInt(settingMap.get("limit2")
					.getValue()));
		}
		if (settingMap.containsKey("limit3")) {
			setting.setLimit3(Integer.parseInt(settingMap.get("limit3")
					.getValue()));
		}
		if (settingMap.containsKey("limit4")) {
			setting.setLimit4(Integer.parseInt(settingMap.get("limit4")
					.getValue()));
		}
		if (settingMap.containsKey("limit5")) {
			setting.setLimit5(Integer.parseInt(settingMap.get("limit5")
					.getValue()));
		}

		return setting;
	}

	public void update(GameSetting setting) {
		if (setting == null) {
			return;
		}

		gameConfigManager.upsert("roomName", "房间名称", setting.getRoomName());
		gameConfigManager.upsert("trialPlayBalance", "试玩粮草",
				setting.getTrialPlayBalance() + "");
		gameConfigManager.upsert("minEachBetInvest", "最小押注",
				setting.getMinEachBetInvest() + "");
		gameConfigManager.upsert("maxEachBetInvest", "最大押注",
				setting.getMaxEachBetInvest() + "");
		gameConfigManager.upsert("limit2", "二定赔率", setting.getLimit2() + "");
		gameConfigManager.upsert("limit3", "三定赔率", setting.getLimit3() + "");
		gameConfigManager.upsert("limit4", "四定赔率", setting.getLimit4() + "");
		gameConfigManager.upsert("limit5", "五定赔率", setting.getLimit5() + "");
	}

}
