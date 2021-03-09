package ch.wenkst.sw_utils.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.date.holiday.CountryCode;

public class DateUtilsTest {
	
	/**
	 * convert a date object to a string
	 */
	@Test
	@DisplayName("date to string")
	public void dateToStrTest() {
		// create a calendar object with the time Thursday, 15.09.2005 04:15:00 GMT+02:00
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(1126750500000L); 		
		
		Assertions.assertEquals("15.09.2005 - 04:15:00.000", DateUtils.dateToStr(date, "dd.MM.yyyy - HH:mm:ss.SSS"), "date to string");
	}
	
	
	/**
	 * convert a string to a date object
	 */
	@Test
	@DisplayName("string to date")
	public void strToDateTest() {
		// get a date from a String
		Calendar date = DateUtils.strToDate("17.3.1999 - 16:21:35.786", "dd.MM.yyyy - HH:mm:ss.SSS");
		
		// date
		Assertions.assertEquals(1999, date.get(Calendar.YEAR), "year of parsed date");
		Assertions.assertEquals(2, date.get(Calendar.MONTH), "month of parsed date");
		Assertions.assertEquals(17, date.get(Calendar.DAY_OF_MONTH), "day of month of parsed date");
		
		// time
		Assertions.assertEquals(16, date.get(Calendar.HOUR_OF_DAY), "hour of parsed date");
		Assertions.assertEquals(21, date.get(Calendar.MINUTE), "minute of parsed date");
		Assertions.assertEquals(35, date.get(Calendar.SECOND), "second of parsed date");
		Assertions.assertEquals(786, date.get(Calendar.MILLISECOND), "millisecond of parsed date");
	}
	
	
	/**
	 * convert a unix timestamp to a human readable string
	 */
	@Test
	@DisplayName("unix to readable string")
	public void unixToStrTest() {
		String dateStr = "21.05.1998 07:22:53";
		Assertions.assertEquals(dateStr, DateUtils.unixToDateStr(895728173000l), "unix to date string");
	}
	
	
	
	
	
	/**
	 * Easter Sunday test
	 */
	@Test
	@DisplayName("Easter Sunday")
	public void easterSundayTest() {
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
	
	
	/**
	 * check if tow dates are the same test
	 */
	@Test
	@DisplayName("same dates")
	public void sameDatesTestTest() {
		// test date of today and tomorrow
		Calendar now = Calendar.getInstance();
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DATE, 1);
		Assertions.assertFalse(DateUtils.areDatesEqual(now, tomorrow), "compare same dates");
		
		// test two dates of today, one now and one 3 seconds earlier
		Calendar earlierToday = Calendar.getInstance();
		earlierToday.add(Calendar.SECOND, -3);
		Assertions.assertTrue(DateUtils.areDatesEqual(now, earlierToday), "compare different dates");
	}
	
	
	/**
	 * check if a date is form today
	 */
	@Test
	@DisplayName("timestamp form today")
	public void dateFromTodayTest() {
		// test the current timestamp
		long now = System.currentTimeMillis();
		Assertions.assertTrue(DateUtils.isTimestampToday(now), "now is timestamp of today");
				
		// test the same timestamp form yesterday
		long yesterday = now - 24*60*60*1000;
		Assertions.assertFalse(DateUtils.isTimestampToday(yesterday), "timestamp of yesterday is not timestamp of today");
	}
	
	
	/**
	 * timestamp from multiple formats
	 */
	@Test
	@DisplayName("timestamp form multiple string formats")
	public void timestampFromMultipleFormatsTest() {
		// create two patterns that should be recognized
		SimpleDateFormat[] knownPatterns = {
				new SimpleDateFormat("dd.MM.yyyy - HH:mm"), 
				new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss")
		};
		
		// test pattern one
		String dateStr = "13.05.2000 - 17:37";
		long timestamp = DateUtils.parseDate(dateStr, knownPatterns);
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timestamp);
		
		Assertions.assertEquals(2000, date.get(Calendar.YEAR), "year of multiple formats parsed date");
		Assertions.assertEquals(4, date.get(Calendar.MONTH), "month of multiple formats parsed date");
		Assertions.assertEquals(13, date.get(Calendar.DAY_OF_MONTH), "day of month of multiple formats parsed date");
		Assertions.assertEquals(17, date.get(Calendar.HOUR_OF_DAY), "hour of multiple formats parsed date");
		Assertions.assertEquals(37, date.get(Calendar.MINUTE), "minute of multiple formats parsed date");
		
		
		// test pattern tow
		dateStr = "1999-07-30 - 18:22:59";
		timestamp = DateUtils.parseDate(dateStr, knownPatterns);
		date.setTimeInMillis(timestamp);
		
		Assertions.assertEquals(1999, date.get(Calendar.YEAR), "year of multiple formats parsed date");
		Assertions.assertEquals(06, date.get(Calendar.MONTH), "month of multiple formats parsed date");
		Assertions.assertEquals(30, date.get(Calendar.DAY_OF_MONTH), "day of month of multiple formats parsed date");
		Assertions.assertEquals(18, date.get(Calendar.HOUR_OF_DAY), "hour of multiple formats parsed date");
		Assertions.assertEquals(22, date.get(Calendar.MINUTE), "minute of multiple formats parsed date");
		Assertions.assertEquals(59, date.get(Calendar.SECOND), "second of multiple formats parsed date");
	}
	
	
	/**
	 * holidays in Switzerland
	 */
	@Test
	@DisplayName("holiday in switzerland")
	public void holidayInSwitzerlandTest() {
		// test all holidays 
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.01.2017", "dd.MM.yyyy")), "01.01.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("02.01.2017", "dd.MM.yyyy")), "02.01.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("14.04.2017", "dd.MM.yyyy")), "14.04.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("17.04.2017", "dd.MM.yyyy")), "17.04.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.05.2017", "dd.MM.yyyy")), "01.05.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("25.05.2017", "dd.MM.yyyy")), "25.05.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("05.06.2017", "dd.MM.yyyy")), "06.06.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.08.2017", "dd.MM.yyyy")), "01.08.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("25.12.2017", "dd.MM.yyyy")), "25.12.2017 is ch holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("26.12.2017", "dd.MM.yyyy")), "26.12.2017 is ch holiday");
		
		// test some non-holidays
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("03.03.2017", "dd.MM.yyyy")), "03.03.2017 is not ch holiday");
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("15.08.2017", "dd.MM.yyyy")), "15.08.2017 is not ch holiday");
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("01.11.2017", "dd.MM.yyyy")), "03.10.2017 is not ch holiday");
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.CH, DateUtils.strToDate("24.12.2017", "dd.MM.yyyy")), "24.12.2017 is not ch holiday");
	}
	
	
	/**
	 * holidays in Germany
	 */
	@Test
	@DisplayName("holiday in germany")
	public void holidayInGermanyTest() {
		// test all holidays 	
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.01.2017", "dd.MM.yyyy")), "01.01.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("14.04.2017", "dd.MM.yyyy")), "14.04.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("17.04.2017", "dd.MM.yyyy")), "17.04.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.05.2017", "dd.MM.yyyy")), "01.05.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("05.06.2017", "dd.MM.yyyy")), "05.06.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("25.05.2017", "dd.MM.yyyy")), "25.05.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("03.10.2017", "dd.MM.yyyy")), "03.10.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("24.12.2017", "dd.MM.yyyy")), "24.12.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("25.12.2017", "dd.MM.yyyy")), "25.12.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("26.12.2017", "dd.MM.yyyy")), "26.12.2017 is de holiday");
		Assertions.assertTrue(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("31.12.2017", "dd.MM.yyyy")), "31.12.2017 is de holiday");
		
		// test some non-holidays
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("03.03.2017", "dd.MM.yyyy")), "03.03.2017 is not de holiday");
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.08.2017", "dd.MM.yyyy")), "01.08.2017 is not de holiday");
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("01.11.2017", "dd.MM.yyyy")), "01.11.2017 is not de holiday");
		Assertions.assertFalse(DateUtils.isHoliday(CountryCode.DE, DateUtils.strToDate("06.12.2017", "dd.MM.yyyy")), "06.12.2017 is not de holiday");
	}
}
