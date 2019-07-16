package ch.wenkst.sw_utils.crypto;

public class KeyParsingException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public KeyParsingException(String message) {
		super(message);
	}
	
	public KeyParsingException(Throwable throwable) {
		super(throwable);
	}
	
	public KeyParsingException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
