package ch.wenkst.sw_utils.db.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.file.FileUtils;

public class DbTestManager {
	private String dbFilePath = Utils.getWorkDir() + File.separator + "resource" + File.separator + "db" + File.separator + "test.db";
	private String tableName = "Company";
	private SQLiteDBHandler dbHandler = SQLiteDBHandler.getInstance();
		
	
	public void connectToDb() throws ClassNotFoundException, SQLException {
		dbHandler.openConnection(dbFilePath);
	}
	
	
	public void createTable() throws SQLException {
		String[] columns = {"name", "age", "address", "salary"};
		String[] dataTypes = {"TEXT NOT NULL", "INT NOT NULL", "CHAR(50)", "REAL"};
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			dbConnector.createTable(tableName, columns, dataTypes);
		});
	}
	
	
	public void dropTable() throws SQLException {
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			dbConnector.dropTable(tableName);
		});
	}
	
	
	public void insertMany() throws SQLException {
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			String[] columns1 = {"name", "age", "address", "salary"};
			Object[] values1 = {"Paul", 32, "California", 20000.00};
			dbConnector.insertRow(tableName, columns1, values1);
			
			Object[] values2 = {"Allen", 56, "Texas", 15000.00};
			dbConnector.insertRow(tableName, columns1, values2);
			
			Object[] values3 = {"Teddy", 23, "Norway", 35000.00};
			dbConnector.insertRow(tableName, columns1, values3);
			
			Object[] values4 = {"Mark", 25, "Rich-Mond", 65000.00};
			dbConnector.insertRow(tableName, columns1, values4);
		});
	}
	
	
	public void closeAndDeleteDbFile() throws IOException {
		dbHandler.closeConnection(dbFilePath);
		FileUtils.deleteFile(dbFilePath);
	}
	
	
	
	public String getDbFilePath() {
		return dbFilePath;
	}
	
	
	public String getTableName() {
		return tableName;
	}
}
