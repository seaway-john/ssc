package com.seaway.game;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import com.seaway.game.common.utils.ScriptHelper;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = { "com.seaway.game.admin.repository",
		"com.seaway.game.manager.repository" })
@EnableScheduling
public class GameApplication {

	public static void main(String[] args) {
		if (!check()) {
			return;
		}

		SpringApplication.run(GameApplication.class, args);
	}

	private static boolean check() {
		List<String> macWhiteList = new ArrayList<>();

		// 172.0.1.163
		macWhiteList.add("00:0C:29:39:89:DA");
		// 193.112.97.200
		macWhiteList.add("52:54:00:42:E7:17");

		return macWhiteList.contains(getLocalMac());
	}

	private static String getLocalMac() {
		String mac = null;

		try {
			String strHwAddr = "HWaddr";
			String ifConfig = ScriptHelper.execScript("ifconfig", "eth0");
			if (!StringUtils.isEmpty(ifConfig) && ifConfig.contains(strHwAddr)) {
				String[] arr = ifConfig.split("\\s+");

				for (int i = 0; i < arr.length; i++) {
					if (strHwAddr.endsWith(arr[i])) {
						if (i < arr.length) {
							mac = arr[i + 1].trim().toUpperCase();
						}
						break;
					}
				}

			}
		} catch (Exception e) {
		}

		return mac;
	}
}
