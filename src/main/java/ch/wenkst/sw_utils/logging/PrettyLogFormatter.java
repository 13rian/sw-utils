package ch.wenkst.sw_utils.logging;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import ch.wenkst.sw_utils.conversion.Conversion;

public class PrettyLogFormatter extends Formatter {
	private boolean logLineNumber = false;
	private String dateFormat;
	
	
	/**
	 * formats the log output
	 * @param logLineNumber 	true if the line number of the logger should be logged
	 * @param dateFormat 		format string of the date
	 */
	public PrettyLogFormatter(boolean logLineNumber, String dateFormat) {
		this.logLineNumber = logLineNumber;
		this.dateFormat = dateFormat;
	}
	
	
	@Override
    public String format(LogRecord rec) {
    	StringBuffer sb = new StringBuffer();
    	addDate(sb, rec.getMillis());
    	addLevel(sb, rec.getLevel().toString());
    	addClass(sb, rec.getLoggerName());
    	addLineNumber(sb);
    	addMessage(sb, rec.getMessage());
        return sb.toString();
    }
	
	
	private void addDate(StringBuffer sb, long millis) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.systemDefault());
    	sb.append(formatter.format(Instant.ofEpochMilli(millis)));
	}
	
	
	private void addLevel(StringBuffer sb, String level) {
    	sb.append(" ");
    	sb.append(Conversion.padRight(level, ' ', 7));
	}
	
	
	private void addClass(StringBuffer sb, String className) {
    	sb.append(" ");
    	sb.append(Conversion.padRight(className, ' ', 22));
	}
	
	
	private void addLineNumber(StringBuffer sb) {
    	if (logLineNumber) {
    		StackTraceElement ste = Thread.currentThread().getStackTrace()[10];
    		
    		sb.append(" (");
    		sb.append(Conversion.padRight(String.valueOf(ste.getLineNumber()), ' ', 4));
    		sb.append(")");
    	}
	}
	
	
	private void addMessage(StringBuffer sb, String message) {
    	sb.append(" - ");
    	sb.append(message);
    	sb.append("\n");
	}
}
