package ch.wenkst.sw_utils.logging;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class StreamHandlerFlush extends StreamHandler {
	/**
	 * defines a stream handler that is flushed immediately after the a new record is added to the log
	 */
	public StreamHandlerFlush() {
		
	}
	
	public StreamHandlerFlush(OutputStream out, Formatter formatter) {
		super(out, formatter);
	}
	
	/**
	 * flush after every log in order to immediately print the log message to the console
	 */
	@Override
    public synchronized void publish(final LogRecord record) {
		super.publish(record);
		flush();
    }
}
