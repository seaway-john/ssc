package com.seaway.game.test.bet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetDecode {
	public static void main(String[] args) {
		long startTs = System.currentTimeMillis();

		for (int i = 0; i < 1; i++) {
			test();
		}

		long endTs = System.currentTimeMillis();
		System.out.println("Cost " + (endTs - startTs) + "ms");
	}

	public static void test() {
		// String betStr =
		// "头12中23尾34各1,万123/十双各2排重, 千345-百单尾小不要三重各4, 尾小-百单千345去重各5，尾78 大各4排重";
		// String betStr = "头小单124689各3排三重,尾单双大各2除双重";

		// String betStr = "万千十123 145 266 238各2";

		// String betStr = "前豹子/2,对子各3";
		// String betStr = "千百对各2";
		// String betStr = "尾飞机各2";

		// String betStr =
		// "尾78 大各4排重，对子各3，万千十123 145 266 238各2，123 456 789 012 345各2";
		String betStr = "123 456 789 012 345各2,123二定各2";

		List<String> bets = formatList(betStr);
		List<BetDecodeDto> dtos = new ArrayList<>();
		for (String bet : bets) {
			BetDecodeDto dto = decode(bet);
			if (dto == null || !dto.isStatus()) {
				dtos.clear();
				break;
			}

			dtos.add(dto);
		}

		for (BetDecodeDto dto : dtos) {
			System.out.println("玩法: " + dto.getFormat());
			System.out.println("单注: " + dto.getMoney());
			System.out.println("数量: " + dto.getSize());
			System.out.println("码数: " + String.join(", ", dto.getData()));
			System.out.println("");
		}
	}

	private static BetDecodeDto decode(String bet) {
		String format = format(bet);
		if (format == null) {
			return null;
		}

		String[] arr = format.split("-");
		String mode = arr[arr.length - 2];
		int money = Integer.valueOf(arr[arr.length - 1]);
		List<String> data = new ArrayList<>();

		if ("单组码".equals(mode)) {
			List<String> bits = new ArrayList<>();
			bits.add(arr[0].contains("万") ? "O" : "X");
			bits.add(arr[0].contains("千") ? "O" : "X");
			bits.add(arr[0].contains("百") ? "O" : "X");
			bits.add(arr[0].contains("十") ? "O" : "X");
			bits.add(arr[0].contains("个") ? "O" : "X");

			int length = 0;
			for (String bit : bits) {
				if (!bit.equals("X")) {
					length++;
				}
			}

			String[] codes = arr[1].trim().split(" ");
			for (String code : codes) {
				char[] chars = code.toCharArray();
				if (length != chars.length) {
					return null;
				}

				int id = 0;
				for (int i = 0; i < bits.size(); i++) {
					if (!bits.get(i).equals("X")) {
						bits.set(i, String.valueOf(chars[id++]));
					}
				}

				data.add(String.join("", bits));
			}
		} else {
			List<String> bits1 = new ArrayList<>();
			List<String> bits2 = new ArrayList<>();
			List<String> bits3 = new ArrayList<>();
			List<String> bits4 = new ArrayList<>();
			List<String> bits5 = new ArrayList<>();
			bits1.add("X");
			bits2.add("X");
			bits3.add("X");
			bits4.add("X");
			bits5.add("X");

			int startId = 1;
			for (char c : arr[0].toCharArray()) {
				switch (c) {
				case '万':
					bits1 = getNumberList(arr[startId++]);
					break;
				case '千':
					bits2 = getNumberList(arr[startId++]);
					break;
				case '百':
					bits3 = getNumberList(arr[startId++]);
					break;
				case '十':
					bits4 = getNumberList(arr[startId++]);
					break;
				case '个':
					bits5 = getNumberList(arr[startId++]);
					break;
				default:
					break;
				}
			}

			boolean removeSame2 = "除二重".equals(mode);
			boolean removeSame3 = "除三重".equals(mode);
			boolean removeSame4 = "除四重".equals(mode);
			for (String bit1 : bits1) {
				for (String bit2 : bits2) {
					for (String bit3 : bits3) {
						for (String bit4 : bits4) {
							for (String bit5 : bits5) {
								String numbers = bit1 + bit2 + bit3 + bit4
										+ bit5;

								if (removeSame2) {
									if (isSame(numbers, 2)) {
										continue;
									}
								}

								if (removeSame3) {
									if (isSame(numbers, 3)) {
										continue;
									}
								}

								if (removeSame4) {
									if (isSame(numbers, 4)) {
										continue;
									}
								}

								if (!data.contains(numbers)) {
									data.add(numbers);
								}
							}
						}
					}
				}
			}
		}

		BetDecodeDto dto = new BetDecodeDto();
		dto.setStatus(true);
		dto.setMessage("成功");
		dto.setFormat(format);
		dto.setMoney(money);
		dto.setSize(data.size());
		dto.setData(data);

		return dto;
	}

	private static List<String> getNumberList(String numbers) {
		List<String> bits = new ArrayList<>();

		for (char c : numbers.toCharArray()) {
			String number = String.valueOf(c);
			if (!bits.contains(number)) {
				bits.add(number);
			}
		}

		return bits;
	}

	private static String format(String bet) {
		bet = bet.replaceAll("[萬万前]", "万");
		bet = bet.replaceAll("[仟千头]", "千");
		bet = bet.replaceAll("[佰百白中]", "百");
		bet = bet.replaceAll("[拾十]", "十");
		bet = bet.replaceAll("[个尾后末]", "个");
		bet = bet.replaceAll("(对子?)", "对子");
		bet = bet.replaceAll("(豹子)|(飞机)", "豹子");
		bet = bet.replaceAll("[去除排(不要?)][([2二]?重)(对子?)(双重?)]", "除二重");
		bet = bet.replaceAll("[去除排(不要?)][([3三]?重)(豹子?)]", "除三重");
		bet = bet.replaceAll("[去除排(不要?)][4四]?重", "除四重");
		bet = bet.replaceAll("[\\s]+", " ");

		Map<String, String> map = new LinkedHashMap<>();
		map.put("万", null);
		map.put("千", null);
		map.put("百", null);
		map.put("十", null);
		map.put("个", null);

		String regex = null;
		String group = null;
		String mode = null;

		regex = "^[万千百十个]";
		group = getGroup(bet, regex);
		if (group == null) {
			bet = "个" + bet;
		}

		bet = addPreFormat(bet);

		regex = "^(万|千|百|十|个){2,4}[\\s/-]?(\\d+[\\s])*\\d+";
		group = getGroup(bet, regex);
		if (group != null) {
			mode = "单组码";
			regex = "^(万|千|百|十|个){2,4}";
			String bitGroup = getGroup(bet, regex);
			if (bitGroup == null) {
				return null;
			}

			String code = group.substring(bitGroup.length()).trim();
			for (char c : bitGroup.toCharArray()) {
				String key = String.valueOf(c);
				String value = code == null ? "" : code;
				code = null;

				if (!map.containsKey(key) || map.get(key) != null) {
					return null;
				}

				map.put(key, value);
			}
		} else {
			bet = bet.replaceAll("万", "-万");
			bet = bet.replaceAll("千", "-千");
			bet = bet.replaceAll("百", "-百");
			bet = bet.replaceAll("十", "-十");
			bet = bet.replaceAll("个", "-个");

			bet = bet.replaceAll("小", "-01234-");
			bet = bet.replaceAll("大", "-56789-");
			bet = bet.replaceAll("单", "-13579-");
			bet = bet.replaceAll("双", "-02468-");
			bet = bet.replaceAll("全", "-0123456789-");

			bet = bet.replaceAll("[\\s/-]+", "-");
			bet = bet.replaceAll("万-", "万");
			bet = bet.replaceAll("千-", "千");
			bet = bet.replaceAll("百-", "百");
			bet = bet.replaceAll("十-", "十");
			bet = bet.replaceAll("个-", "个");
			bet = bet.replaceAll("^-", "");

			bet = addGroupFormat(bet);

			regex = "^((万|千|百|十|个)\\d+[\\s/-]?){2,4}";
			group = getGroup(bet, regex);
			if (group == null) {
				return null;
			}

			String[] arr = group.split("-");
			for (String str : arr) {
				String key = str.substring(0, 1);
				String value = str.substring(1);

				if (!map.containsKey(key) || map.get(key) != null) {
					return null;
				}

				map.put(key, value);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (String key : map.keySet()) {
			if (map.get(key) != null) {
				sb.append(key);
			}
		}
		sb.append("]");

		for (String value : map.values()) {
			if (value != null && value != "") {
				sb.append("-");
				sb.append(value);
			}
		}

		String suffix = bet.substring(group.length());
		if (mode == null) {
			mode = getMode(suffix);
		}
		int money = getMoney(suffix);

		sb.append("-");
		sb.append(mode);
		sb.append("-");
		sb.append(money);

		return sb.toString();
	}

	private static String addPreFormat(String bet) {
		String regex = null;
		String group = null;

		regex = "^[万千百十个][([2二]?对子)([3三]?豹子)]";
		group = getGroup(bet, regex);
		if (group != null) {
			regex = "^(万|千|百|十|个)";
			String bitGroup = getGroup(bet, regex);
			if (bitGroup.startsWith("个")) {
				if (bet.contains("对子")) {
					bet = "十" + bet;
				} else if (bet.contains("豹子")) {
					bet = "百十" + bet;
				}
			}

			if (bet.contains("对子")) {
				bet = bet.replaceAll("十[2二]?对子", "十个对子");
				bet = bet.replaceAll("百[2二]?对子", "百十对子");
				bet = bet.replaceAll("千[2二]?对子", "千百对子");
				bet = bet.replaceAll("万[2二]?对子", "万千对子");
			} else if (bet.contains("豹子")) {
				bet = bet.replaceAll("百[3三]?豹子", "百十个豹子");
				bet = bet.replaceAll("千[3三]?豹子", "千百十豹子");
				bet = bet.replaceAll("万[3三]?豹子", "万千百豹子");
			}

			bet = bet.replaceAll("对子", "00 11 22 33 44 55 66 77 88 99");
			bet = bet.replaceAll("豹子",
					"000 111 222 333 444 555 666 777 888 999");
		}

		regex = "^个[\\s/-]?(\\d+[\\s]){4}\\d+";
		group = getGroup(bet, regex);
		if (group != null) {
			regex = "[\\s/-]?(\\d+[\\s])";
			String numberGroup = getGroup(bet, regex);
			if (numberGroup == null) {
				return null;
			}

			String preReplace = "万千百十个".substring(5 - numberGroup.trim()
					.length());

			bet = bet.replaceAll("^个", preReplace);
		}

		regex = "[二三四][定现]";
		group = getGroup(bet, regex);
		if (group != null) {
			if (bet.startsWith("个")) {
				regex = "二[定现]";
				group = getGroup(bet, regex);
				if (group != null) {
					bet = bet.replaceAll("^个", "十个");
				}

				regex = "三[定现]";
				group = getGroup(bet, regex);
				if (group != null) {
					bet = bet.replaceAll("^个", "百十个");
				}

				regex = "四[定现]";
				group = getGroup(bet, regex);
				if (group != null) {
					bet = bet.replaceAll("^个", "千百十个");
				}
			}
		}

		System.out.println(bet);

		return bet;
	}

	private static String addGroupFormat(String format) {
		String regex = null;
		String group = null;

		regex = "\\d+-\\d+";
		group = getGroup(format, regex);
		if (group == null) {
			return format;
		}

		List<Character> bitList = new ArrayList<>();
		bitList.add('万');
		bitList.add('千');
		bitList.add('百');
		bitList.add('十');
		bitList.add('个');

		if (format.startsWith("个")) {
			int number = format.split("-").length - 1;

			format = bitList.get(4 - number) + format.substring(1);
		}

		StringBuilder sb = new StringBuilder();

		char[] chars = format.toCharArray();
		char preBit = chars[0];
		for (int i = 0; i < chars.length; i++) {
			sb.append(chars[i]);

			if (i < chars.length - 1) {
				if (bitList.contains(chars[i])) {
					preBit = chars[i];
				}

				if (chars[i] == '-' && !bitList.contains(chars[i + 1])) {
					preBit = bitList.get(bitList.indexOf(preBit) + 1);
					sb.append(preBit);
				}
			}
		}

		return addGroupFormat(sb.toString());
	}

	private static String getMode(String suffix) {
		String mode = "正常";

		if (suffix.contains("除二重")) {
			mode = "除二重";
			return mode;
		}

		if (suffix.contains("除三重")) {
			mode = "除三重";
			return mode;
		}

		if (suffix.contains("除四重")) {
			mode = "除四重";
			return mode;
		}

		if (suffix.contains("对子")) {
			mode = "对子";
			return mode;
		}

		if (suffix.contains("豹子")) {
			mode = "豹子";
			return mode;
		}

		return mode;
	}

	private static int getMoney(String suffix) {
		int money = 1;

		String regex = "\\d+";
		String group = getGroup(suffix, regex);
		if (group != null) {
			money = Integer.parseInt(group);
		}

		return money > 1 ? money : 1;
	}

	private static boolean isSame(String numbers, int count) {
		int sameCount = 0;
		char pre = 'X';

		for (char c : numbers.toCharArray()) {
			if (c == 'X') {
				continue;
			}

			if (pre != 'X') {
				if (pre == c) {
					sameCount++;
				}
			}

			pre = c;
		}

		return sameCount >= count - 1;
	}

	private static String getGroup(String line, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		if (!matcher.find()) {
			return null;
		}

		return matcher.group();
	}

	private static List<String> formatList(String betStr) {
		List<String> bets = new ArrayList<>();

		String[] arr = betStr.split("[,，;；]");
		for (String bet : arr) {
			bet = bet.trim();

			if (bet.length() > 0) {
				bets.add(bet);
			}

		}

		return bets;
	}
}
