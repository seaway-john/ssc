package com.seaway.game.common.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

	public static String getMysqlDateString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

		return dateFormat.format(date);
	}

	public static String getYmd() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		return format.format(new Date());
	}

	public static Date getDateBeforeDay(int day) {
		LocalDate localDateBefore = LocalDate.now().minusDays(day);

		return Date.from(localDateBefore.atStartOfDay(ZoneId.systemDefault())
				.toInstant());
	}

	public static Date[] getDateRange(int dayStart, int dayEnd) {
		LocalTime localtime = LocalTime.now();
		if (localtime.getHour() < 2) {
			dayStart++;
		} else {
			dayEnd--;
		}

		long plusSeconds = 2 * 3600;
		LocalDate localDate = LocalDate.now();
		LocalDate localDateStart = localDate.minusDays(dayStart);
		Date dateStart = Date.from(localDateStart
				.atStartOfDay(ZoneId.systemDefault()).toInstant()
				.plusSeconds(plusSeconds));

		LocalDate localDateEnd = localDate.minusDays(dayEnd);
		Date dateEnd = Date.from(localDateEnd
				.atStartOfDay(ZoneId.systemDefault()).toInstant()
				.plusSeconds(plusSeconds));

		return new Date[] { dateStart, dateEnd };
	}
}
