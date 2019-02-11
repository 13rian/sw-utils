package ch.wenkst.sw_utils.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holds some methods to handle dates and holidays
 */
public class DateUtils {
	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

	// static list of holiday dates (is only initialized once when it it is used for the first time)
	private static ArrayList<Calendar> chHolidayList = null;
	private static ArrayList<Calendar> deHolidayList = null;
	private static int holidayListYearCH = 0; 							// the year of the ch holiday list
	private static int holidayListYearDE = 0; 							// the year of the de holiday list

	//	// ch holidays
	//	public static enum HolidaysCH {OSTERMONTAG, KARFREITAG, HIMMELFAHRT, PFINGSTMONTAG, NEUJAHRSTAG, 
	//								   BERCHTOLDSTAG, TAG_DER_ARBEIT, NATIONALFEIERTAG, ERSTER_WEIHNACHTSTAG,
	//								   STEPHANSTAG};
	//	
	//	// de holidays 
	//	public static enum HolidaysDE {OSTERMONTAG, KARFREITAG, HIMMELFAHRT, PFINGSTMONTAG, NEUJAHRSTAG, 
	//		   						   TAG_DER_ARBEIT, 	TAG_DER_DEUTSCHEN_EINHEIT, WEIHNACHTSTAG,
	//		   						   ERSTER_WEIHNACHTSTAG, ZWEITER_WEIHNACHTSTAG, SILVESTER};



	/////////////////////////////////////////////////////////////////////////////////////////////
	// 						String to Date conversion management 							   //
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
			String sDate= sdf.format(date.getTime());
			return sDate;

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
			jdf.setTimeZone(TimeZone.getDefault()); 		// get the time zone of the computer
			result = jdf.format(date);

		} catch (Exception e) {
			logger.error("failed to parse the unix timestamp: ", e);
		}
		
		return result;
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
		boolean areDatesEqual = 
				date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) &&
				date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
				date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR);

		return areDatesEqual;
	}
	
	
	/**
	 * returns true if the passed timestamp is from today
	 * @param timestamp 	timestamp in unix time ms
	 * @return 				true if the passed timestamp is from today
	 */
	public static boolean isTimestampToday(long timestamp) {
		// create a calendar instance for today
		Calendar today = Calendar.getInstance();     
		today.setTime(new Date());
		
		// create the calendar instance of the passed timestamp
		Calendar dateToTest = Calendar.getInstance();
		dateToTest.setTimeInMillis(timestamp);
		
		return areDatesEqual(today, dateToTest);
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
		// check if the passed pattern contain legal arguments
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
		// try to  parse the passed date
		for (SimpleDateFormat sdf : knownSdf) {
			try {
				// try to parse the date
				return sdf.parse(dateStr).getTime();

			} catch (ParseException pe) {
				// do nothing and test the next pattern
			}
		}

		// no pattern matches
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
		// declare the needed varaibles
		int a, b, c, d, e, f, g, h, i, k, l, m;   	

		a = year % 19;
		b = (int)Math.floor(year/100.0);
		c = year % 100;
		d = (int)Math.floor(b/4.0);
		e = b % 4;
		f = (int)Math.floor((b+8)/25.0);
		g = (int)Math.floor((b-f+1)/3.0);
		h = (19*a + b - d - g + 15) % 30;
		i = (int)Math.floor(c/4.0);
		k = c % 4;
		l = (32 + 2*e + 2*i - h - k) % 7;
		m = (int)Math.floor((a + 11*h + 22*l)/451.0);

		// get the day and the month
		int month = (int)Math.floor((h + l - 7*m + 114)/31.0); 
		int day = ((h + l - 7*m + 114) % 31) + 1;


		// get a date instance with the Easter Sunday
		String dateStr = day + "." + month + "." + year;
		Calendar easterDate = strToDate(dateStr, "dd.MM.yyyy");

		return easterDate;		
	}


	/**
	 * checks if the passed date is a holiday in Switzerland or Germany
	 * @param countryCode 	ch or de
	 * @param date			the date that is tested
	 * @return 				true if the passed date is a holiday, false otherwise
	 */
	public static synchronized boolean isHoliday(String countryCode, Calendar date) {
		// create the holiday list if not already done
		boolean isSuccessfullyCreated = createHolidayList(countryCode, date.get(Calendar.YEAR));
		if (!isSuccessfullyCreated) {
			logger.error("failed to create the holiday list");
			return false;
		}

		// get the correct to use for the comparison depending on the country code
		ArrayList<Calendar> holidayList = null;
		if (countryCode.toLowerCase().equals("ch")) {
			holidayList = chHolidayList;
		} else if (countryCode.toLowerCase().equals("de")) {
			holidayList = deHolidayList;
		} else {
			logger.error("country code "+ countryCode + " not implemented");
			return false;
		}

		// loop to the list to compare the passed date
		for (Calendar holidayDate : holidayList) {
			if(areDatesEqual(holidayDate, date)) {
				return true;
			};
		}

		// no holiday from the list matches
		return false;

	}


	/**
	 * fills the static holiday lists of Switzerland and Germany, if it was not built yet.
	 * returns true if the list was successfully created
	 * @param countryCode 	ch (Switzerland), de (Germany)
	 * @param year  		the year of the holiday
	 * @return 				true if the holiday list was created successfully, false if it could not be created
	 */
	private static boolean createHolidayList(String countryCode, int year) {
		// check if the desired holiday list was already calculated
		if (countryCode.toLowerCase().equals("ch")) {
			if (chHolidayList != null && year == holidayListYearCH) {
				return true;
			}

		} else if (countryCode.toLowerCase().equals("de")) {
			if (deHolidayList != null && year == holidayListYearDE) {
				return true;
			}

		} else {
			logger.error("country code " + countryCode + " not implemeted");
			return false;
		}


		// Moving holidays
		// Ostersonntag
		Calendar osterSonnatg = easterDate(year);

		// Ostermontag (Ostersonntag+1)
		Calendar ostermontag = Calendar.getInstance();
		ostermontag.setTime(osterSonnatg.getTime());
		ostermontag.add(Calendar.DATE, 1);

		// Karfreitag (Ostersonntag-2)
		Calendar karfreitag = Calendar.getInstance();
		karfreitag.setTime(osterSonnatg.getTime());
		karfreitag.add(Calendar.DATE, -2);

		// Himmelfahrt (Ostersonntag+39)
		Calendar himmelfahrt = Calendar.getInstance();
		himmelfahrt.setTime(osterSonnatg.getTime());
		himmelfahrt.add(Calendar.DATE, 39);

		// Pfingstmontag (Ostersonntag+50)
		Calendar pfingstmontag = Calendar.getInstance();
		pfingstmontag.setTime(osterSonnatg.getTime());
		pfingstmontag.add(Calendar.DATE, 50);


		// fix holidays
		// Neujahrstag
		Calendar neujahrstag = strToDate("01.01."+year, "dd.MM.yyyy");

		// Tag der Arbeit
		Calendar tagDerArbeit = strToDate("01.05."+year, "dd.MM.yyyy");

		// Erster Weihnachtstag
		Calendar ersterWeihnachtstag = strToDate("25.12."+year, "dd.MM.yyyy");


		// specific for ch
		if (countryCode.toLowerCase().equals("ch")) {
			// Stephanstag
			Calendar stephanstag = strToDate("26.12."+year, "dd.MM.yyyy");

			// Berchtholdstag
			Calendar berchtholdstag = strToDate("02.01."+year, "dd.MM.yyyy");

			// Nationalfeiertag
			Calendar nationalfeiertag = strToDate("01.08."+year, "dd.MM.yyyy");


			// add all holiday dates to the list
			chHolidayList = new ArrayList<>();
			chHolidayList.add(osterSonnatg);
			chHolidayList.add(ostermontag);
			chHolidayList.add(karfreitag);
			chHolidayList.add(himmelfahrt);
			chHolidayList.add(pfingstmontag);
			chHolidayList.add(neujahrstag);
			chHolidayList.add(tagDerArbeit);
			chHolidayList.add(ersterWeihnachtstag);
			chHolidayList.add(stephanstag);
			chHolidayList.add(berchtholdstag);
			chHolidayList.add(nationalfeiertag);

			// set the year
			holidayListYearCH = year;
		}

		// specific for de
		else if (countryCode.toLowerCase().equals("de")) {
			// Tag der Deutschen Einheit
			Calendar tagDerDeutschenEinheit = strToDate("03.10."+year, "dd.MM.yyyy");	

			// Weihnachtstag
			Calendar weihnachtstag = strToDate("24.12."+year, "dd.MM.yyyy");

			// Zweiter Weihnachtstag
			Calendar zweiterWeihnachtstag = strToDate("26.12."+year, "dd.MM.yyyy");

			// Silvester
			Calendar silvester = strToDate("31.12."+year, "dd.MM.yyyy");


			// add all holiday dates to the list
			deHolidayList = new ArrayList<>();
			deHolidayList.add(osterSonnatg);
			deHolidayList.add(ostermontag);
			deHolidayList.add(karfreitag);
			deHolidayList.add(himmelfahrt);
			deHolidayList.add(pfingstmontag);
			deHolidayList.add(neujahrstag);
			deHolidayList.add(tagDerArbeit);
			deHolidayList.add(ersterWeihnachtstag);
			deHolidayList.add(tagDerDeutschenEinheit);
			deHolidayList.add(weihnachtstag);
			deHolidayList.add(zweiterWeihnachtstag);
			deHolidayList.add(silvester);

			// set the year
			holidayListYearDE = year;
		}

		return true;
	}



}
