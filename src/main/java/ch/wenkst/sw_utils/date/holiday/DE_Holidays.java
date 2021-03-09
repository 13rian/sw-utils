package ch.wenkst.sw_utils.date.holiday;

import java.util.Calendar;
import java.util.List;

import ch.wenkst.sw_utils.date.DateFormats;
import ch.wenkst.sw_utils.date.DateUtils;

public class DE_Holidays extends EuropeanHolidays {

	@Override
	public List<Calendar> createHolidayList(int year) {
		List<Calendar> holidayList = easterHolidays(year);
		holidayList.add(christmas(year));
		holidayList.add(tagDerArbeit(year));
		holidayList.add(newYear(year));
		holidayList.add(tagDerDeutschenEinheit(year));
		holidayList.add(weihnachtstag(year));
		holidayList.add(zweiterWeihnachtstag(year));
		holidayList.add(silvester(year));
		
		return holidayList;
	}
	
	
	protected Calendar tagDerDeutschenEinheit(int year) {
		return DateUtils.strToDate("03.10." + year, DateFormats.europeanDate);	
	}


	protected Calendar weihnachtstag(int year) {
		return DateUtils.strToDate("24.12." + year, DateFormats.europeanDate);
	}


	protected Calendar zweiterWeihnachtstag(int year) {
		return DateUtils.strToDate("26.12." + year, DateFormats.europeanDate);
	}


	protected Calendar silvester(int year) {
		return DateUtils.strToDate("31.12." + year, DateFormats.europeanDate);
	}
}
