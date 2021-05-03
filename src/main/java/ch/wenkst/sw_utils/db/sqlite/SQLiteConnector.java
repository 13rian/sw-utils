package ch.wenkst.sw_utils.db.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteConnector {
	private static final Logger logger = LoggerFactory.getLogger(SQLiteConnector.class);    // initialize the logger


	private Connection connection = null; 		// the connection to the db


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											connection handling 													 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * establishes a connection to the sqlite db
	 * @param dbFilePath 	path to the db-file
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public void connect(String dbFilePath) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");

		// create the directory of the db file if it does not exist
		String dbFileDir = new File(dbFilePath).getParent();
		new File(dbFileDir).mkdirs();

		connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
		logger.info("successfully connected to the db " + dbFilePath);
	}


	/**
	 * disconnect form the db
	 */
	public void disconnect() {
		try {
			connection.close();
			logger.info("successfully disconnected form the db");

		} catch (Exception e) {
			logger.error("failed to disconnect form the db: ", e);
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											convenience methods 													  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * creates a table if it does not already exist with an int as primary key that corresponds to the row number
	 * auto-increment needs a lot of cpu power and is almost always not necessary
	 * @param tableName 	the name of the table
	 * @param columns 		the name of the columns without the id
	 * @param dataTypes 	the data types of the columns, e.g. CHAR(50) or TEXT NOT NULL
	 * @throws SQLException 
	 */
	public void createTableWithPK(String tableName, String[] columns, String[] dataTypes) throws SQLException {
		if (columns.length != dataTypes.length) {
			throw new SQLException("columns and dataTypes have not the same length");
		}

		// create the sql statement to create the table
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName);
		sb.append(" (id INTEGER PRIMARY KEY");
		for (int i=0; i<columns.length; i++) {
			sb.append(", ").append(columns[i]).append(" ").append(dataTypes[i]);
		}
		sb.append(");");

		// execute the update
		String sql = sb.toString();
		executeUpdate(sql);
	}


	/**
	 * creates a table if it does not already exist
	 * @param tableName 	the name of the table
	 * @param columns 		the name of the columns without the id
	 * @param dataTypes 	the data types of the columns, e.g. CHAR(50) or TEXT NOT NULL
	 * @param callback 		the callback that is called when the db operation has finsihed
	 * @throws SQLException 
	 */
	public void createTable(String tableName, String[] columns, String[] dataTypes) throws SQLException {
		if (columns.length != dataTypes.length) {
			throw new SQLException("columns and dataTypes have not the same length");
		}

		// create the sql statement to create the table
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
		for (int i=0; i<columns.length; i++) {
			sb.append(columns[i]).append(" ").append(dataTypes[i]).append(",");
		}
		sb.replace(sb.length()-1, sb.length(), ");"); 		// get rid of the last comma

		// execute the update
		String sql = sb.toString();
		executeUpdate(sql);
	}


	/**
	 * drops the table with the passed name if it exists
	 * @param tableName 	the name of the table to drop
	 */
	public void dropTable(String tableName) throws SQLException {
		String sql = "DROP TABLE IF EXISTS " + tableName;
		executeUpdate(sql);
	}


	/**
	 * inserts a row into the table
	 * @param tableName 	the name of the table
	 * @param columns 		the columns of the new row
	 * @param values 		the values of the new row
	 * @throws SQLException 
	 */
	public void insertRow(String tableName, String[] columns, Object[] values) throws SQLException {
		if (columns.length != values.length) {
			throw new SQLException("columns and values have not the same length");
		}

		// create the sql
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName).append(" (");
		for (int i=0; i<columns.length; i++) {
			sb.append(columns[i]).append(",");
		}
		sb.replace(sb.length()-1, sb.length(), ")"); 	// get rid of the last comma

		sb.append(" VALUES (");
		for (int i=0; i<values.length; i++) {
			if (values[i] instanceof String) {
				sb.append("'").append(values[i]).append("'").append(",");
			} else {
				sb.append(values[i]).append(",");
			}
		}
		sb.replace(sb.length()-1, sb.length(), ");"); 	// get rid of the last comma

		// execute the update
		String sql = sb.toString(); 
		executeUpdate(sql);
	}


	/**
	 * loads all rows with all columns from the table
	 * @param tableName 	the name of the table
	 * @param callback 		the callback that is called when the result is here
	 * @throws SQLException 
	 */
	public void selectAll(String tableName, ResultQueryCallback callback) throws SQLException {
		selectAll(tableName, null, callback);
	}


	/**
	 * loads all rows with all columns from the table that fulfill the passed condition
	 * @param tableName 	the name of the table
	 * @param condition 	the query condition. e.g. salary<20000 can be null if it should be omitted
	 * @param callback 		the callback that is called when the result is here
	 * @throws SQLException 
	 */
	public void selectAll(String tableName, String condition, ResultQueryCallback callback) throws SQLException {
		// create the sql
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(tableName);

		if (condition != null) {
			sb.append(" WHERE ").append(condition);
		}
		sb.append(";");

		// execute the query
		String sql = sb.toString();
		executeQuery(sql, callback);
	}


	/**
	 * loads all rows but only the passed columns from the table
	 * @param tableName 	the name of the table
	 * @param columns 		the name of the columns to load from the db
	 * @param callback 		the callback that is called when the result is here
	 * @throws SQLException 
	 */
	public void select(String tableName, String[] columns, ResultQueryCallback callback) throws SQLException {
		select(tableName, columns, null, callback);
	}



	/**
	 * loads all rows but only the passed columns from the table that fulfill the passed condition
	 * @param tableName 	the name of the table
	 * @param columns 		the name of the columns to load from the db
	 * @param condition 	the query condition. e.g. salary<20000
	 * @param callback 		the callback that is called when the result is here
	 * @throws SQLException 
	 */
	public void select(String tableName, String[] columns, String condition, ResultQueryCallback callback) throws SQLException {
		// create the sql
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (String column : columns) {
			sb.append(column).append(",");
		}
		sb.replace(sb.length()-1, sb.length(), " "); 		// get rid of the last comma
		sb.append("FROM ").append(tableName);

		if (condition != null) {
			sb.append(" WHERE ").append(condition);
		}
		sb.append(";");

		// execute the query
		String sql = sb.toString();
		executeQuery(sql, callback);
	}



	/**
	 * loads all rows with the passed filter form the table
	 * @param tableName 	the name of the table
	 * @param filter 		the selection filter, i.e. SELECT filter FROM ... 
	 * @param callback 		the callback that is called when the result is here
	 * @throws SQLException 
	 */
	public void select(String tableName, String filter, ResultQueryCallback callback) throws SQLException {
		select(tableName, filter, null, callback);
	}



	/**
	 * loads all rows with the passed filter that fulfill the passed condition
	 * @param tableName 	the name of the table
	 * @param filter 		the selection filter, i.e. SELECT filter FROM ...
	 * @param condition 	the query condition. e.g. salary<20000
	 * @param callback 		the callback that is called when the result is here		the result set of the query
	 * @throws SQLException 
	 */
	public void select(String tableName, String filter, String condition, ResultQueryCallback callback) throws SQLException {
		// create the sql
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(filter);
		sb.append(" FROM ").append(tableName);

		if (condition != null) {
			sb.append(" WHERE ").append(condition);
		}
		sb.append(";");

		// execute the query
		String sql = sb.toString();
		executeQuery(sql, callback);
	}


	/**
	 * updates one value in the table
	 * @param tableName 		name of the table
	 * @param column 			column to update
	 * @param value 			value to update
	 * @param condition 		the query condition, e.g. weight=63.5 or name='Brian'
	 * @throws SQLException 
	 */
	public void update(String tableName, String column, Object value, String condition) throws SQLException {
		String[] columns = {column};
		Object[] values = {value};
		update(tableName, columns, values, condition);
	}



	/**
	 * updates values in the table
	 * @param tableName 		name of the table
	 * @param columns 			columns to update
	 * @param values 			values to update
	 * @param condition 		the query condition, e.g. weight=63.5 or name='Brian'
	 * @throws SQLException 
	 */
	public void update(String tableName, String[] columns, Object[] values, String condition) throws SQLException {
		if (columns.length != values.length) {
			throw new SQLException("columns and values have not the same length");
		}

		// create the sql
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(tableName).append(" SET");
		for (int i=0; i<columns.length; i++) {
			if (values[i] instanceof String) {
				sb.append(" ").append(columns[i]).append("='").append(values[i]).append("',");
			} else {
				sb.append(" ").append(columns[i]).append("=").append(values[i]).append(",");
			}
		}
		sb.replace(sb.length()-1, sb.length(), " "); 			// get rid of the last comma
		sb.append("WHERE ").append(condition).append(";");

		// execute the sql
		String sql = sb.toString();
		executeUpdate(sql);
	}



	/**
	 * deletes rows from a table
	 * @param tableName 	the table form which rows are deleted
	 * @param condition 	the condition, e.g. name='Brian'
	 * @throws SQLException 
	 */
	public void delete(String tableName, String condition) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(tableName).append(" WHERE ").append(condition).append(";");
		String sql = sb.toString();
		executeUpdate(sql);
	}





	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											execute sqls 														  	  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * executes an update operation, usually used for insert, create, update
	 * @param sql 			the sql string that is executed
	 * @throws SQLException 
	 */
	public void executeUpdate(String sql) throws SQLException {
		logger.trace("executed the update-sql: " + sql);

		Statement stmt = connection.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();
	}


	/**
	 * executes an query operation, usually used for select
	 * @param sql 			the sql string that is executed
	 * @param callback 		the callback that is called when the db operation has finsihed
	 * @throws SQLException 
	 */
	public void executeQuery(String sql, ResultQueryCallback callback) throws SQLException {
		logger.trace("executed the query-sql: " + sql);
		
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		callback.onFinish(rs);		
		stmt.close();
	}
}
