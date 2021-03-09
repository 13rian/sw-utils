package ch.wenkst.sw_utils.date.holiday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ch.wenkst.sw_utils.date.DateFormats;
import ch.wenkst.sw_utils.date.DateUtils;

public abstract class EuropeanHolidays extends Holidays {
	
	protected List<Calendar> easterHolidays(int year) {
		List<Calendar> holidayList = new ArrayList<>();
		Calendar osterSonnatg = DateUtils.easterDate(year);
		holidayList.add(osterSonnatg);

		Calendar ostermontag = (Calendar) osterSonnatg.clone();
		ostermontag.add(Calendar.DATE, 1);
		holidayList.add(ostermontag);

		Calendar karfreitag = (Calendar) osterSonnatg.clone();;
		karfreitag.add(Calendar.DATE, -2);
		holidayList.add(karfreitag);

		Calendar himmelfahrt = (Calendar) osterSonnatg.clone();;
		himmelfahrt.add(Calendar.DATE, 39);
		holidayList.add(himmelfahrt);

		Calendar pfingstmontag = (Calendar) osterSonnatg.clone();;
		pfingstmontag.add(Calendar.DATE, 50);
		holidayList.add(pfingstmontag);
		
		return holidayList;
	}

	
	protected Calendar christmas(int year) {
		return DateUtils.strToDate("25.12." + year, DateFormats.europeanDate);
	}
	
	
	protected Calendar tagDerArbeit(int year) {
		return DateUtils.strToDate("01.05." + year, DateFormats.europeanDate);
	}
	
	
	protected Calendar newYear(int year) {
		return DateUtils.strToDate("01.01." + year, DateFormats.europeanDate);
	}
}
