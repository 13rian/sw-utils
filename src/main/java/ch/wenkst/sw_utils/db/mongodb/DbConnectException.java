package ch.wenkst.sw_utils.db.mongodb;

public class DbConnectException extends Exception {
	private static final long serialVersionUID = 1L;

	public DbConnectException(String errorMessage) {
        super(errorMessage);
    }
}
