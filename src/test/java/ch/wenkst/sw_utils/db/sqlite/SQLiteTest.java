package ch.wenkst.sw_utils.db.sqlite;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ch.wenkst.sw_utils.BaseTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SQLiteTest extends BaseTest {	
	private DbTestManager testManager = new DbTestManager();
	private SQLiteDBHandler dbHandler;

	
	@BeforeAll
	public void prepareDbHandler() throws ClassNotFoundException, SQLException {
		dbHandler = SQLiteDBHandler.getInstance();
		testManager.connectToDb();
		testManager.dropTable();
	}
	
	
	@BeforeEach
	public void resetTable() throws SQLException {
		testManager.dropTable();
		testManager.createTable();
	}



	@Test
	public void insert() throws SQLException {
		testManager.insertMany();
	}
	
	
	@Test
	public void findAll() throws SQLException {
		testManager.insertMany();
		
		dbHandler.readOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.selectAll(testManager.getTableName(), (rs) -> {
				correctlyInserted(rs);
			});
		});
	}
	
	
	private void correctlyInserted(ResultSet rs) throws SQLException {
		List<String> names =  new ArrayList<String>(); 
		while (rs.next()) {
			names.add(rs.getString("name"));
		}
		String[] downloadedNames = names.toArray(new String[4]);
		String[] expectedNames = {"Paul", "Allen", "Teddy", "Mark"};
		
		Assertions.assertEquals(4, names.size());
		Assertions.assertArrayEquals(expectedNames, downloadedNames);
	}
	
	
	@Test
	public void findByProperty() throws SQLException {
		testManager.insertMany();
		
		dbHandler.readOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.selectAll(testManager.getTableName(), "name='Mark'", (rs) -> {
				int count = 0;
				while (rs.next()) {
					Assertions.assertEquals("Mark", rs.getString("name"));
					Assertions.assertEquals(25, rs.getInt("age"));
					Assertions.assertEquals(65000F, rs.getFloat("salary"), 0.001);
					count++;
				}
				Assertions.assertEquals(1, count);
			});
		});
	}


	@Test
	public void findDefinedColumns() throws SQLException {
		testManager.insertMany();
		
		dbHandler.readOperation(testManager.getDbFilePath(), (dbConnector) -> {
			String[] values = {"name", "age"};
			dbConnector.select(testManager.getTableName(), values, (rs) -> {
				int count = 0;
				while (rs.next()) {
					Assertions.assertNotNull(rs.getString("name"));
					Assertions.assertNotNull(rs.getInt("age"));
					Assertions.assertThrows(SQLException.class, () -> rs.getFloat("address"));
					Assertions.assertThrows(SQLException.class, () -> rs.getFloat("salary"));
					count++;
				}
				Assertions.assertEquals(4, count);
			});
		});
	}
	
	
	@Test
	public void updateOneValue() throws SQLException {
		testManager.insertMany();
		
		dbHandler.writeOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.update(testManager.getTableName(), "name", "Brian", "name='Mark'");
		});
		
		dbHandler.readOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.selectAll(testManager.getTableName(), "name='Brian'", (rs) -> {
				int count = 0;
				while (rs.next()) {
					Assertions.assertEquals("Brian", rs.getString("name"));
					count++;
				}
				Assertions.assertEquals(1, count);
			});
		});
	}
	
	
	@Test
	public void updateManyValues() throws SQLException {
		testManager.insertMany();
		
		dbHandler.writeOperation(testManager.getDbFilePath(), (dbConnector) -> {
			String[] updateColumns = {"name", "salary"};
			Object[] updateValues = {"Theodore", 90000.00};
			dbConnector.update(testManager.getTableName(), updateColumns, updateValues, "name='Teddy'");
		});
		
		dbHandler.readOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.selectAll(testManager.getTableName(), "name='Theodore'", (rs) -> {
				int count = 0;
				while (rs.next()) {
					Assertions.assertEquals("Theodore", rs.getString("name"));
					Assertions.assertEquals(90000.00, rs.getFloat("salary"), 0.001);
					count++;
				}
				Assertions.assertEquals(1, count);
			});
		});
	}
	
	
	@Test
	public void deleteFromTable() throws SQLException {
		testManager.insertMany();
		
		dbHandler.writeOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.delete(testManager.getTableName(), "salary<34000.0");
		});
		
		dbHandler.readOperation(testManager.getDbFilePath(), (dbConnector) -> {
			dbConnector.selectAll(testManager.getTableName(), (rs) -> {
				int count = 0;
				while (rs.next()) {
					count++;
				}
				Assertions.assertEquals(2, count);
			});
		});
	}


	@AfterEach
	public void dropTestDb() throws SQLException {
		testManager.dropTable();
	}


	@AfterAll
	public void dropTestCollection() throws IOException {
		testManager.closeAndDeleteDbFile();
	}
}
