package ch.wenkst.sw_utils.db.sqlite;

import java.sql.SQLException;

public interface DBOperation {
	public void executeDBOperation(SQLiteConnector sqlConnector) throws SQLException;
}
