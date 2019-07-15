package ch.wenkst.sw_utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.db.sqlite.SQLiteDBHandler;

public class MainSQLite {
	private static final Logger logger = LoggerFactory.getLogger(MainRabbitMQ.class);
	
	public static void main(String[] args) {
		// define the path to the db-file, this db is called test
		String dbFilePath = Utils.getWorkDir() + File.separator + "db" + File.separator + "test.db";
		
		// define the name of the table
		String tableName = "Company";
		
		// setup the handler
		SQLiteDBHandler dbHandler = SQLiteDBHandler.getInstance();
		

		// connect to the db, if the db does not exist a new db-file will be created
		dbHandler.openConnection(dbFilePath);

		
		// create a table
		String[] columns = {"name", "age", "address", "salary"};
		String[] dataTypes = {"TEXT NOT NULL", "INT NOT NULL", "CHAR(50)", "REAL"};
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			dbConnector.createTable(tableName, columns, dataTypes);
		});
		


		// insert some data
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
		
		

		
		
		// select all without any condition
		dbHandler.readOperation(dbFilePath, (dbConnector) -> {
			dbConnector.selectAll(tableName, (rs) -> {
				logger.debug("values form the select all without a condition query: ");
				try {
					while (rs.next()) {
						logger.debug("name: " + rs.getString("name") + ", age: " + rs.getInt("age") + ", salary: " + rs.getFloat("salary"));
					}
				} catch (Exception e) {
					logger.error("error reading form the result set: ", e);
				}
			});
		});
		

		
		
		// select all with a condition
		dbHandler.readOperation(dbFilePath, (dbConnector) -> {
			dbConnector.selectAll(tableName, "name='Mark'", (rs) -> {
				logger.debug("values form the select all with a condition query: ");
				try {
					while (rs.next()) {
						logger.debug("name: " + rs.getString("name") + ", age: " + rs.getInt("age") + ", salary: " + rs.getFloat("salary"));
					}
				} catch (Exception e) {
					logger.error("error reading form the result set: ", e);
				}
			});
		});
		

		
		
		// select only two values
		dbHandler.readOperation(dbFilePath, (dbConnector) -> {
			String[] values = {"name", "age"};
			dbConnector.select(tableName, values, (rs) -> {
				logger.debug("values form the select query: ");
				try {
					while (rs.next()) {
						logger.debug("name: " + rs.getString("name") + ", age: " + rs.getInt("age"));
					}
				} catch (Exception e) {
					logger.error("error reading form the result set: ", e);
				}
			});
		});
		

		
		
		
		// update values in the db
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			String[] updateColumns = {"name", "salary"};
			Object[] updateValues = {"Theodore", 90000.00};
			dbConnector.update(tableName, updateColumns, updateValues, "name='Teddy'");
			
			
			// update one value in the db
			dbConnector.update(tableName, "name", "Brian", "name='Mark'");
		});
		

		
		// delete form the table
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			dbConnector.delete(tableName, "salary<60000.0");
		});
		
		
		// drop the table
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			dbConnector.dropTable(tableName);
		});
		
		
		// close the connection
		dbHandler.writeOperation(dbFilePath, (dbConnector) -> {
			dbConnector.dropTable(tableName);
		});
		
		dbHandler.closeConnection(dbFilePath);
	}
}
