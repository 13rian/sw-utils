package ch.wenkst.sw_utils.db.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultQueryCallback {
	public void onFinish(ResultSet rs) throws SQLException;
}
