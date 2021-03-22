package ch.wenkst.sw_utils.db.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface QueryCallback {
	public void processResult(ResultSet rs) throws SQLException;
}
