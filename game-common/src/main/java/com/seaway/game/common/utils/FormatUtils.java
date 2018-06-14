package com.seaway.game.common.utils;

import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

public class FormatUtils {

	public static String uuid() {
		UUID uuid = UUID.randomUUID();

		return uuid.toString().trim().replace("-", "");
	}

	public static String random(int length) {
		String uuid = uuid();

		return uuid.substring(0, length);
	}

	public static String mac(String mac) {
		if (StringUtils.isEmpty(mac)) {
			return null;
		}

		mac = mac.trim().toLowerCase().replaceAll("-", ":");

		String pattern = "^[a-f0-9]{2}(:[a-f0-9]{2}){5}$";
		if (!Pattern.compile(pattern).matcher(mac).find()) {
			return null;
		}

		return mac;
	}

	public static String trim(Object source) {
		if (StringUtils.isEmpty(source)) {
			return "";
		}

		return source.toString().trim();
	}

	public static boolean equals(String compare1, String compare2) {
		return trim(compare1).equals(trim(compare2));
	}
}
