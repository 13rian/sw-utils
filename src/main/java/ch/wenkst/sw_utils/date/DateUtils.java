package ch.wenkst.sw_utils.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.date.holiday.CountryCode;
import ch.wenkst.sw_utils.date.holiday.Holidays;
import ch.wenkst.sw_utils.date.holiday.HolidaysFactory;

public class DateUtils {
	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
	
	
	private DateUtils() {
		
	}



	/////////////////////////////////////////////////////////////////////////////////////////////
	// 								date conversion management 							   	   //
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * parses the passed date in string format to a Calendar instance, or null if it could not be parsed
	 * @param dateStr 	the date as string
	 * @param format 	the format of the date, e.g. dd.MM.yyyy
	 * @return 			calendar instance with the passed date
	 */
	public static Calendar strToDate(String dateStr, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Calendar parsedDate = Calendar.getInstance();
			parsedDate.setTime(sdf.parse(dateStr));
			return parsedDate;

		} catch (Exception e) {
			logger.error("error parsing the passed date: " + dateStr + " with format " + format + ": ", e);
			return null;
		}
	}


	/**
	 * converts the passed date to a String
	 * @param date		the date to convert
	 * @param format 	the format of the date, e.g. dd.MM.yyyy
	 * @return 			string representation of the passed date
	 */
	public static String dateToStr(Calendar date, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date.getTime());

		} catch (Exception e) {
			logger.error("error converting the date to a String: ", e);
			return null;
		}
	}
	
	
	/**
	 * converts the passed unix timestamp to a human readable string
	 * @param timestamp		timestamp in ms
	 * @return 				date as string with the format dd.MM.yyyy HH:mm:ss
	 */
	public static String unixToDateStr(long timestamp) {
		String result = timestamp+"";
		
		try {
			Date date = new Date(timestamp); 
			SimpleDateFormat jdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			jdf.setTimeZone(TimeZone.getDefault());
			result = jdf.format(date);

		} catch (Exception e) {
			logger.error("failed to parse the unix timestamp: ", e);
		}
		
		return result;
	}
	
	
	/**
	 * converts the passed unix time in ms to a LocalDateTime, utc is used for the zone
	 * @param unixTime	unix time in ms
	 * @return
	 */
	public static LocalDateTime unixToLocalDateTime(long unixTime) {
    	return unixToLocalDateTime(unixTime, ZoneId.of("UTC"));
	}
	
	
	/**
	 * converts the passed unix time in ms to a LocalDateTime
	 * @param unixTime	unix time in ms
	 * @param zoneId	the time zone to use for the conversion
	 * @return
	 */
	public static LocalDateTime unixToLocalDateTime(long unixTime, ZoneId zoneId) {
    	return Instant.ofEpochMilli(unixTime)
    			.atZone(zoneId)
    			.toLocalDateTime();
	}
	
	
	/**
	 * converts the passed localDateTime to unix time in ms, utc is used for the zone
	 * @param localDateTime		the local date time 
	 * @return
	 */
	public static long localDateTimeToUnix(LocalDateTime localDateTime) {
		return localDateTimeToUnix(localDateTime, ZoneId.of("UTC"));		
	}
	
	
	/**
	 * converts the passed localDateTime to unix time in ms
	 * @param localDateTime		the local date time 
	 * @param zoneId			the time zone that is used for the conversion
	 * @return
	 */
	public static long localDateTimeToUnix(LocalDateTime localDateTime, ZoneId zoneId) {
		return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
	}
	
	

	/////////////////////////////////////////////////////////////////////////////////////////////
	// 									comparing methods 							   		   //
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns true if the two passed dates represent the same day (the time of the dates is ignored)
	 * @param date1 	date1
	 * @param date2 	date2
	 * @return 			true if the passed dates are equal, false otherwise
	 */
	public static boolean areDatesEqual(Calendar date1, Calendar date2) {
		return 	date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) &&
				date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
				date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR);
	}
	
	
	/**
	 * returns true if the passed timestamp is from today
	 * @param timestamp 	timestamp in unix time ms
	 * @return 				true if the passed timestamp is from today
	 */
	public static boolean isTimestampToday(long timestamp) {
		Calendar today = Calendar.getInstance();     
		today.setTime(new Date());
		
		Calendar dateToTest = Calendar.getInstance();
		dateToTest.setTimeInMillis(timestamp);
		
		return areDatesEqual(today, dateToTest);
	}
	
	
	/**
	 * returns the duration in ms between the two passed dates
	 * @param startLocalDateTime		the start time
	 * @param endLocalDateTime			the end time
	 * @return
	 */
	public static long durationInMilli(LocalDateTime startLocalDateTime, LocalDateTime endLocalDateTime) {
		return Duration.between(startLocalDateTime, endLocalDateTime).toMillis();
	}



	/////////////////////////////////////////////////////////////////////////////////////////////
	// 										date parser 								   	   //
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * parses the passed date string with the passed patterns
	 * @param dateStr 		the date string to parse
	 * @param patterns 		the pattern to parse
	 * @return				unix time in ms of the passed date string or 0 if an error occurred
	 */
	public static long parseDate(String dateStr, String[] patterns) {
		SimpleDateFormat[] knownSdf = new SimpleDateFormat[patterns.length];
		try {
			for (int i=0; i<patterns.length; i++) {
				knownSdf[i] = new SimpleDateFormat(patterns[i]);
			}
		} catch (Exception e) {
			logger.error("error creating the simple date formats: ", e);
			return 0;
		}

		
		return parseDate(dateStr, knownSdf);
	}
	
	
	/**
	 * parses the passed date string with the passed patterns
	 * @param dateStr 		the date string to parse
	 * @param knownSdf 		the simple date formats to parse
	 * @return				unix time in ms of the passed date string or 0 if an error occurred
	 */
	public static long parseDate(String dateStr, SimpleDateFormat[] knownSdf) {
		for (SimpleDateFormat sdf : knownSdf) {
			try {
				return sdf.parse(dateStr).getTime();

			} catch (ParseException pe) {

			}
		}

		logger.error("date string could not be parsed, just return 0");
		return 0;
	}




	/////////////////////////////////////////////////////////////////////////////////////////////
	// 									Holiday management 								   	   //
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * calculates the Gregorian Easter Sunday using the Anonymous Gregorian algorithm
	 * @param year  	the year of the Easter Sunday
	 * @return 			calendar instance with the easter date of the passed year
	 */
	public static Calendar easterDate(int year) {
		int a = year % 19;
		int b = (int) Math.floor(year/100.0);
		int c = year % 100;
		int d = (int) Math.floor(b/4.0);
		int e = b % 4;
		int f = (int) Math.floor((b+8)/25.0);
		int g = (int) Math.floor((b-f+1)/3.0);
		int h = (19*a + b - d - g + 15) % 30;
		int i = (int) Math.floor(c/4.0);
		int k = c % 4;
		int l = (32 + 2*e + 2*i - h - k) % 7;
		int m = (int) Math.floor((a + 11*h + 22*l)/451.0);

		int month = (int) Math.floor((h + l - 7*m + 114)/31.0); 
		int day = ((h + l - 7*m + 114) % 31) + 1;
		String dateStr = day + "." + month + "." + year;
		return strToDate(dateStr, DateFormats.europeanDate);
	}


	/**
	 * checks if the passed date is a holiday in Switzerland or Germany
	 * @param countryCode 	ch or de
	 * @param date			the date that is tested
	 * @return 				true if the passed date is a holiday, false otherwise
	 */
	public static synchronized boolean isHoliday(CountryCode countryCode, Calendar date) {
		Holidays holidays = HolidaysFactory.getHolidays(countryCode);
		return holidays.isHoliday(date);
	}
}
