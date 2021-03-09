package ch.wenkst.sw_utils.date.holiday;

import java.util.Calendar;
import java.util.List;

import ch.wenkst.sw_utils.date.DateUtils;

public abstract class Holidays {	
	public abstract List<Calendar> createHolidayList(int year);
	
	
	public boolean isHoliday(Calendar date) {
		int year = date.get(Calendar.YEAR);
		for (Calendar holidayDate : createHolidayList(year)) {
			if (DateUtils.areDatesEqual(holidayDate, date)) {
				return true;
			}
		}
		
		return false;
	}
}
