package ch.wenkst.sw_utils.date.holiday;

import java.util.Calendar;
import java.util.List;

import ch.wenkst.sw_utils.date.DateFormats;
import ch.wenkst.sw_utils.date.DateUtils;

public class CH_Holidays extends EuropeanHolidays {
	
	@Override
	public List<Calendar> createHolidayList(int year) {
		List<Calendar> holidayList = easterHolidays(year);
		holidayList.add(christmas(year));
		holidayList.add(tagDerArbeit(year));
		holidayList.add(newYear(year));
		holidayList.add(stephansTag(year));
		holidayList.add(berchtholdsTag(year));
		holidayList.add(nationalfeiertag(year));
		return holidayList;
	}

	
	protected Calendar stephansTag(int year) {
		return  DateUtils.strToDate("26.12." + year, DateFormats.europeanDate);
	}
	
	
	protected Calendar berchtholdsTag(int year) {
		return DateUtils.strToDate("02.01." + year, DateFormats.europeanDate);
	}
	
	
	protected Calendar nationalfeiertag(int year) {
		return DateUtils.strToDate("01.08." + year, DateFormats.europeanDate);
	}
}
