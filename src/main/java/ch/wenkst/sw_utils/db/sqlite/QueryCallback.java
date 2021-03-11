package ch.wenkst.sw_utils.db.sqlite;

import java.sql.ResultSet;

public interface QueryCallback {
	public void processResult(ResultSet rs);
}
