package ch.wenkst.sw_utils.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.date.holiday.CountryCode;

public class DateUtilsTest extends BaseTest {
	
	@Test
	public void dateToString() {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(1126750500000L); 		
		Assertions.assertEquals("15.09.2005 - 04:15:00.000", DateUtils.dateToStr(date, "dd.MM.yyyy - HH:mm:ss.SSS"));
	}
	
	
	@Test
	public void stringToDate() {
		Calendar date = DateUtils.strToDate("17.3.1999 - 16:21:35.786", "dd.MM.yyyy - HH:mm:ss.SSS");
		
		Assertions.assertEquals(1999, date.get(Calendar.YEAR));
		Assertions.assertEquals(2, date.get(Calendar.MONTH));
		Assertions.assertEquals(17, date.get(Calendar.DAY_OF_MONTH));
		
		Assertions.assertEquals(16, date.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(21, date.get(Calendar.MINUTE));
		Assertions.assertEquals(35, date.get(Calendar.SECOND));
		Assertions.assertEquals(786, date.get(Calendar.MILLISECOND));
	}
	
	
	@Test
	public void unixToDateString() {
		String dateStr = "21.05.1998 07:22:53";
		Assertions.assertEquals(dateStr, DateUtils.unixToDateStr(895728173000l));
	}
	
	
	@Test
	public void easterSunday() {
		// easter in 2017
		Calendar easterSunday = DateUtils.easterDate(2017);
		Assertions.assertEquals(2017, easterSunday.get(Calendar.YEAR), "year of easter in 2017");
		Assertions.assertEquals(3, easterSunday.get(Calendar.MONTH), "month of easter in 2017");
		Assertions.assertEquals(16, easterSunday.get(Calendar.DAY_OF_MONTH), "day of month of easter in 2017");
		
		// easter in 1997
		easterSunday = DateUtils.easterDate(1997);
		Assertions.assertEquals(1997, easterSunday.get(Calendar.YEAR), "year of easter in 1997");
		Assertions.assertEquals(2, easterSunday.get(Calendar.MONTH), "month of easter in 1997");
		Assertions.assertEquals(30, easterSunday.get(Calendar.DAY_OF_MONTH), "day of month of easter in 1997");
		
		// easter in 1975
		easterSunday = DateUtils.easterDate(1975);
		Assertions.assertEquals(1975, easterSunday.get(Calendar.YEAR), "year of easter in 1975");
		Assertions.assertEquals(2, easterSunday.get(Calendar.MONTH), "month of easter in 1975");
		Assertions.assertEquals(30, easterSunday.get(Calendar.DAY_OF_MONTH), "day of month of easter in 1975");
	}
	
	
	@Test
	public void datesAreEqual() {
		Calendar now = Calendar.getInstance();
		Calendar earlierToday = Calendar.getInstance();
		earlierToday.add(Calendar.SECOND, -3);
		Assertions.assertTrue(DateUtils.areDatesEqual(now, earlierToday));
	}
	
	
	@Test
	public void datesAreNotEqual() {
		Calendar now = Calendar.getInstance();
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DATE, 1);
		Assertions.assertFalse(DateUtils.areDatesEqual(now, tomorrow));
	}
	
	
	@Test
	public void timestampIsFromToday() {
		long now = System.currentTimeMillis();
		Assertions.assertTrue(DateUtils.isTimestampToday(now));
	}
	
	
	@Test
	public void timestampIsNotFromToday() {
		long now = System.currentTimeMillis();
		long yesterday = now - 24*60*60*1000;
		Assertions.assertFalse(DateUtils.isTimestampToday(yesterday));
	}		
	

	@Test
	public void timestampFromMultiplePatterns() {
		SimpleDateFormat[] knownPatterns = {
				new SimpleDateFormat("dd.MM.yyyy - HH:mm"), 
				new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss")
		};
		
		
		String dateStr = "13.05.2000 - 17:37";
		long timestamp = DateUtils.parseDate(dateStr, knownPatterns);
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timestamp);
		
		Assertions.assertEquals(2000, date.get(Calendar.YEAR));
		Assertions.assertEquals(4, date.get(Calendar.MONTH));
		Assertions.assertEquals(13, date.get(Calendar.DAY_OF_MONTH));
		Assertions.assertEquals(17, date.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(37, date.get(Calendar.MINUTE));
	}
	
	
	@Test
	public void holidaysInSwitzerland() {
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.01.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("02.01.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("14.04.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("17.04.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.05.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("25.05.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("05.06.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.08.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("25.12.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("26.12.2017", "dd.MM.yyyy")));
	}
	
	
	@Test
	public void nonHolidaysInSwitzerland() {
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("03.03.2017", "dd.MM.yyyy")));
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("15.08.2017", "dd.MM.yyyy")));
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.11.2017", "dd.MM.yyyy")));
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("24.12.2017", "dd.MM.yyyy")));
	}
	
	
	@Test
	public void holidaysInGermany() {
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.01.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("14.04.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("17.04.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.05.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("05.06.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("25.05.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("03.10.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("24.12.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("25.12.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("26.12.2017", "dd.MM.yyyy")));
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("31.12.2017", "dd.MM.yyyy")));
	}
	
	
	@Test
	public void nonHolidaysInGermany() {
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("03.03.2017", "dd.MM.yyyy")));
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.08.2017", "dd.MM.yyyy")));
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.11.2017", "dd.MM.yyyy")));
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("06.12.2017", "dd.MM.yyyy")));
	}
}
