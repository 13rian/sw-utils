package ch.wenkst.sw_utils.db.sqlite;

import java.sql.ResultSet;

public interface IQueryCallback {
	public void processResult(ResultSet rs);
}
