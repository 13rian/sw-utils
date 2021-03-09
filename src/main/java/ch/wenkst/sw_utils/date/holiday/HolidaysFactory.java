package ch.wenkst.sw_utils.date.holiday;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HolidaysFactory {
	private static final Logger logger = LoggerFactory.getLogger(HolidaysFactory.class);
	
	
	public static Holidays getHolidays(CountryCode countryCode) {
		switch (countryCode) {
			case CH:
				return new CH_Holidays();
				
			case DE:
				return new DE_Holidays();
				
			default:
				logger.error("no holidays implementation defined for country code " + countryCode.toString());
				return null;
		}
	}
}
