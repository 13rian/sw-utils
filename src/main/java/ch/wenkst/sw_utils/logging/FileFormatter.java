package ch.wenkst.sw_utils.logging;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FileFormatter extends Formatter {
	private boolean logLineNumber = false;

	/**
	 * formats the log output that is written into the file
	 * @param logLineNumber 	true if the line number of the logger should be logged
	 */
	public FileFormatter(boolean logLineNumber) {
		super();
		this.logLineNumber = logLineNumber;
	}
	
	
	/**
	 * this method is called for every log record
	 */
	@Override
    public String format(LogRecord rec) {
    	StringBuffer sb = new StringBuffer();
    	
    	// the date
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		sb.append(sdf.format(rec.getMillis()));
		
		// the level
    	sb.append(" ");
    	sb.append(padRight(rec.getLevel().toString(), ' ', 7));
    	
    	// the name of the class
    	sb.append(" ");
    	sb.append(padRight(rec.getLoggerName(), ' ', 22));
    	
    	// the line
    	if (logLineNumber) {
    		StackTraceElement ste = Thread.currentThread().getStackTrace()[9];
    		
    		sb.append(" (");
    		sb.append(padRight(String.valueOf(ste.getLineNumber()), ' ', 4));
    		sb.append(")");
    	}
    	
    	// the message
    	sb.append(" - ");
    	sb.append(rec.getMessage());
    	sb.append("\n");
    	
        return sb.toString();
    }


    
    
	/**
	 * adds a padding on the right side of the passed string if it is shorter than the passed length
	 * @param str 		the string that is padded
	 * @param padChar 	character to pad the string with
	 * @param length 	desired length
	 * @return
	 */
	public String padRight(String str, char padChar, int length) {
		int strLength = str.length();
		if (strLength >= length) {
			return str;
		}
		
		String paddedStr = str + new String(new char[length - strLength]).replace('\0', padChar);
		return paddedStr;
	}
}