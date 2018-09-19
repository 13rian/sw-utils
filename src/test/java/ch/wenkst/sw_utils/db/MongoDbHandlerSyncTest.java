package ch.wenkst.sw_utils.db;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mongodb.morphia.query.Query;

public class MongoDbHandlerSyncTest {
	private static MongoDbHandlerSync dbHandler = null; 	 // the db handler object that handles the connection to the db
	
	// connection parameters for the db 
	private static String host = "192.168.1.183";
	private static int port = 27017;
	private static String dbName = "sw_utils_test_db";
	
	
	/**
	 * loads the resources that are needed for the test
	 */
	@BeforeAll
	public static void initializeExternalResources() {
		// create an instance of the dbHandler
		dbHandler = new MongoDbHandlerSync();
		
		// connect to the db
		boolean connected = dbHandler.connectToDB(host, port);
		if (!connected) {
			Assertions.fail("connection to the db could not be established");
		}
		
		// test if the db is reachable
		boolean reachable = dbHandler.isReachable(host, port, 10000);
		if (!reachable) {
			Assertions.fail("db is not reachable");
		}
	}
	
	/**
	 * make sure that the db collection is empty
	 */
	@BeforeEach
	public void dropCollection() {
		boolean dropped = dbHandler.dropCollection(Car.class, dbName);
		if (!dropped) {
			Assertions.fail("collection could not be dropped");
		}
	}
	
	
	/**
	 * test if the db is reachable
	 */
	@Test
	@DisplayName("db reachable")
	public void dbReachableTest() {
		boolean reachable = dbHandler.isReachable(host, port, 30000);
		Assertions.assertTrue(reachable, "db is reachable");
	}
	
	
	/**
	 * save one entity to the db
	 */
	@Test
	@DisplayName("save single entity")
	public void saveSingleEntityTest() {
		// create an entity that is save to the db
		Car car = new Car("Ford", 1600);
		
		// save objects o the database
		boolean saved = dbHandler.saveToDB(car, dbName);
		Assertions.assertTrue(saved, "single entity saved");
	}
	
	
	/**
	 * save multiple entities to the db
	 */
	@Test
	@DisplayName("save multiple entities")
	public void saveMultipleEntitiesTest() {
		// create an entity that is save to the db
		Car car1 = new Car("Ford", 1600);
		Car car2 = new Car("VW", 1900);
		Car car3 = new Car("Mercedes", 1400);
		
		// save objects o the database
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2, car3), dbName);
		Assertions.assertTrue(saved, "multiple entities saved");
	}
	
	
	/**
	 * load all entities form the db
	 */
	@Test
	@DisplayName("query all entites")
	public void queryAllEntitiesTest() {
		// create an entity that is save to the db
		Car car1 = new Car("Ford", 1600);
		Car car2 = new Car("VW", 1900);
		Car car3 = new Car("Mercedes", 1400);
		Car car4 = new Car("Audi", 2000);
		
		// save objects o the database
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2, car3, car4), dbName);
		Assertions.assertTrue(saved, "multiple entities saved");
		
		
		// query the entities form the db
		Query<Car> query = dbHandler.createQuery(Car.class, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(query);
		
		Assertions.assertEquals(4, queryResultList.size(), "all entities retrieved from the db");
	}
	
	
	/**
	 * query entities form the db with a filter-query
	 */
	@Test
	@DisplayName("filter-query")
	public void filterQueryTest() {
		// save a few entities in the db in order to make the query
		Car car1 = new Car("Ford", 1900);
		Car car2 = new Car("VW", 1500);
		Car car3 = new Car("Audi", 2000);
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2, car3), dbName);
		Assertions.assertTrue(saved, "test entities saved to db");
		
		
		// query the entities form the db
		Query<Car> query = dbHandler.createFilterQuery(Car.class, "weight >=", 1900, null, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(query);
		
		
		// check the size
		Assertions.assertEquals(2, queryResultList.size(), "number of retrieved entities matching");
		
		// check the weight
		List<Integer> actualWeights = Arrays.asList(queryResultList.get(0).getWeight(), queryResultList.get(1).getWeight());
		MatcherAssert.assertThat("weights match", actualWeights, Matchers.containsInAnyOrder(1900, 2000));
	}
	
	
	/**
	 * query entities form the db with an and-query
	 */
	@Test
	@DisplayName("and-query")
	public void andQueryTest() {
		// save a few entities in the db in order to make the query
		Car car1 = new Car("Mercedes", 1300);
		Car car2 = new Car("Ford", 1400);
		Car car3 = new Car("Ford", 1600);
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2, car3), dbName);
		Assertions.assertTrue(saved, "test entities saved to db");
		
		
		// query the entities form the db
		String[] constraints = {"weight <=", "name ="};
		Object[] constraintVals = {1500, "Ford"};
		Query<Car> query = dbHandler.createAndQuery(Car.class, constraints, constraintVals, null, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(query);

		
		// check the size
		Assertions.assertEquals(1, queryResultList.size(), "number of retrieved entities matching");
		
		// check the name and the weight
		Assertions.assertEquals("Ford", queryResultList.get(0).getName(), "name matches");
		Assertions.assertEquals(1400, queryResultList.get(0).getWeight(), "weight matches");
	}
	
	
	/**
	 * query entities form the db with an or-query
	 */
	@Test
	@DisplayName("or-query")
	public void orQueryTest() {
		// save a few entities in the db in order to make the query
		Car car1 = new Car("Mercedes", 1300);
		Car car2 = new Car("Audi", 1400);
		Car car3 = new Car("Ford", 1600);
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2, car3), dbName);
		Assertions.assertTrue(saved, "test entities saved to db");
		
		
		// query the entities form the db
		Object[] constraintVals = {"Mercedes", "Ford"};
		Query<Car> query = dbHandler.createOrQuery(Car.class, "name", constraintVals, null, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(query);

		
		// check the size
		Assertions.assertEquals(2, queryResultList.size(), "number of retrieved entities matching");
		
		// check the names
		List<String> actualNames = Arrays.asList(queryResultList.get(0).getName(), queryResultList.get(1).getName());
		MatcherAssert.assertThat("names match", actualNames, Matchers.containsInAnyOrder("Mercedes", "Ford"));
	}
	
	
	/**
	 * update one field in the db
	 */
	@Test
	@DisplayName("update one field")
	public void updateOneFieldTest() {
		// save a few entities in the db in order to make the query
		Car car1 = new Car("Mercedes", 1300);
		Car car2 = new Car("Audi", 1400);
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2), dbName);
		Assertions.assertTrue(saved, "test entities saved to db");
		
		
		// update the name field in the db
		Query<Car> query = dbHandler.createQuery(Car.class, dbName);					
		boolean updated = dbHandler.updateEntityInDB("weight", 3000, dbName, query);
		Assertions.assertTrue(updated, "entity updated");
		
		
		// check if the update was successful
		Query<Car> validationQuery = dbHandler.createQuery(Car.class, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(validationQuery);
		
		Assertions.assertEquals(3000, queryResultList.get(0).getWeight(), "weight updated");
		Assertions.assertEquals(3000, queryResultList.get(1).getWeight(), "weight updated");
	}
	
	
	/**
	 * update multiple fields in the db
	 */
	@Test
	@DisplayName("update multiple fields")
	public void updateMultipleFieldsTest() {
		// save a few entities in the db in order to make the query
		Car car1 = new Car("Mercedes", 1300);
		boolean saved = dbHandler.saveToDB(car1, dbName);
		Assertions.assertTrue(saved, "test entities saved to db");
		
		
		// update the the name and the weight field in the db
		Query<Car> query = dbHandler.createFilterQuery(Car.class, "name =", "Mercedes", null, dbName);	
		String[] fields = {"name", "weight"};
		Object[] values = {"Tesla", 2900};
		boolean updated = dbHandler.updateEntityInDB(fields, values, dbName, query);
		Assertions.assertTrue(updated, "entity updated");
		
		
		// check if both the name and the weight was updated
		Query<Car> validationQuery = dbHandler.createQuery(Car.class, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(validationQuery);
		
		Assertions.assertEquals("Tesla", queryResultList.get(0).getName(), "name updated");
		Assertions.assertEquals(2900, queryResultList.get(0).getWeight(), "weight updated");
	}
	
	
	/**
	 * delete entities form the db
	 */
	@Test
	@DisplayName("delete entities")
	public void deleteEntitiesTest() {
		// save a few entities in the db in order to make the query
		Car car1 = new Car("Mercedes", 1300);
		Car car2 = new Car("VW", 1400);
		Car car3 = new Car("Audi", 1500);
		boolean saved = dbHandler.saveToDB(Arrays.asList(car1, car2, car3), dbName);
		Assertions.assertTrue(saved, "test entities saved to db");
		
		
		// delete entities from the database
		Query<Car> query = dbHandler.createFilterQuery(Car.class, "weight <", 1500, null, dbName); 										
		boolean deleted = dbHandler.deleteFromDB(dbName, query);
		Assertions.assertTrue(deleted, "entities deleted");
		
		
		// check if the correct entities were deleted
		Query<Car> validationQuery = dbHandler.createQuery(Car.class, dbName);
		List<Car> queryResultList = dbHandler.getListFromQuery(validationQuery);
		Assertions.assertEquals(1, queryResultList.size(), "number of retrieved entities matching");
		
		Assertions.assertEquals("Audi", queryResultList.get(0).getName(), "name matching");
		Assertions.assertEquals(1500, queryResultList.get(0).getWeight(), "weight matching");
	}
	
	
	
	
	/**
	 * drops the test database and closes the connection
	 */
	@AfterAll
	public static void tearDownResources() {
		// drop the test db
		boolean dropped = dbHandler.dropDatabase(dbName);
		if (!dropped) {
			Assertions.fail("failed to drop the test db");
		}
		
		// close the connection
		boolean disconnected = dbHandler.disconnectFromDB();
		if (!disconnected) {
			Assertions.fail("failed to close the connection to the db");
		}
	}
}




