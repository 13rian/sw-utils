package ch.wenkst.sw_utils.db.mongodb;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.db.mongodb.entitiy.Person;
import ch.wenkst.sw_utils.miscellaneous.StatusResult;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoPojoTest extends BaseTest {
	private DbTestManager testManager = new DbTestManager();
	private MongoDBHandler dbHandler;

	
	
	@BeforeAll
	public void prepareDbHandler() throws DbConnectException, InterruptedException, ExecutionException, TimeoutException {
		dbHandler = MongoDBHandler.getInstance();
		testManager.connectToDb();
		dbHandler.dropDatabaseSync();
	}



	@Test
	public void insertOne() {
		testManager.insertOnePerson();
	}
	
	
	@Test
	public void insertMany() {
		testManager.insertManyPersons();
	}
	
	
	@Test
	public void findAll() {
		testManager.insertManyPersons();
		List<Person> personList = testManager.queryAllPersons();
		Assertions.assertEquals(testManager.personList().size(), personList.size());
	}
	
	
	@Test
	public void findBySimpleProperty() {
		testManager.insertManyPersons();
		
		Bson query = Filters.lte("age", 50);
		Bson sort = Sorts.ascending("age");
		StatusResult findResult = dbHandler.findSync(Person.class, query, sort);
		Assertions.assertTrue(findResult.isSuccess());
		List<Person> personList = findResult.getResult();
		Assertions.assertEquals(2, personList.size());
	}
	
	
	@Test
	public void findByNestedProperty() {
		testManager.insertManyPersons();
		
		Bson query = Filters.and(Filters.eq("address.city", "London"), Filters.lte("age", 45));
		Bson sort = Sorts.ascending("age");
		
		StatusResult findResult = dbHandler.findSync(Person.class, query, sort);
		Assertions.assertTrue(findResult.isSuccess());
		List<Person> personList = findResult.getResult();
		Person person = personList.get(0);
		Assertions.assertTrue(person.getAge() <= 45);
		Assertions.assertEquals("London", person.getAddress().getCity());
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void asyncFindTest() {
		testManager.insertManyPersons();
		
		Bson query = Filters.lte("age", 50);
		
		AtomicBoolean finished = new AtomicBoolean(false);
		
		dbHandler.find(Person.class, query, null, (result, error) ->  {
			List<Person> personList = (List<Person>) result;
			Assertions.assertEquals(2, personList.size());
			finished.set(true);
		});
		
		Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> {
			return finished.get();
		});
	}
	
	
	@Test
	public void updateSimplePropery() {
		testManager.insertOnePerson();
		Bson update = Updates.set("name", "Jack");	
		StatusResult updateResult = dbHandler.updateSync(Person.class, null, update);
		Assertions.assertTrue(updateResult.isSuccess());
		
		List<Person> personList = testManager.queryAllPersons();
		Person person = personList.get(0);
		Assertions.assertEquals("Jack", person.getName());
	}
	
	
	@Test
	public void updateNestedProperty() {
		testManager.insertOnePerson();
		Bson update = Updates.combine(
				Updates.set("address.city", "Paris"),
				Updates.set("age", 23));
		StatusResult updateResult = dbHandler.updateSync(Person.class, null, update);
		Assertions.assertTrue(updateResult.isSuccess());
		
		List<Person> personList = testManager.queryAllPersons();
		Person person = personList.get(0);
		Assertions.assertEquals("Paris", person.getAddress().getCity());
		Assertions.assertEquals(23, person.getAge());
	}
	
	
	@Test
	public void deleteByProperty() {
		testManager.insertManyPersons();
		
		Bson query = Filters.eq("address.city", "London");
		StatusResult deleteResult = dbHandler.deleteSync(Person.class, query);
		Assertions.assertTrue(deleteResult.isSuccess());
		List<Person> personList = testManager.queryAllPersons();
		Assertions.assertEquals(2, personList.size());
	}
	
	
	@Test
	public void dropCollection() {
		testManager.insertManyPersons();
		StatusResult dropResult = dbHandler.dropCollectionSync(Person.class);
		Assertions.assertTrue(dropResult.isSuccess());
		
		List<Person> personList = testManager.queryAllPersons();
		Assertions.assertTrue(personList.isEmpty());
	}
	
	
	@Test
	@Disabled
	public void makeTransaction() {
//		// make a transaction, only works in replica sets
//		StatusResult transactionResult = dbHandler.transactionSessionSync();
//		if (transactionResult.isSuccess()) {
//			ClientSession clientSession = transactionResult.getResult();
//			clientSession.startTransaction();
//			
//			MongoCollection<BaseEntity> collection = dbHandler.getCollection(Person.class);
//			Publisher<UpdateResult> updatePub = collection.updateMany(clientSession, Filters.eq("age", 37), Updates.set("age", 38));
//			dbHandler.executeOperation(updatePub);
//			
//			updatePub = collection.updateMany(clientSession, Filters.eq("age", 37), Updates.set("age", 38));
//			dbHandler.executeOperation(updatePub);
//			
//			updatePub = collection.updateMany(clientSession, Filters.eq("age",57), Updates.set("age", 60));
//			dbHandler.executeOperation(updatePub);
//			
//			clientSession.commitTransaction();
//			clientSession.close();
//			
//		} else {
//			logger.error("could not create client session for transaction");
//		}
	}
	
	
	@Test
	public void createIndex() {
		testManager.insertManyPersons();
		Bson index = Indexes.ascending("name");
		StatusResult indexResult = dbHandler.createIndexSync("Person", index, null);
		Assertions.assertTrue(indexResult.isSuccess());
	}
	
	
	@Test
	public void deleteAllIndexes() {
		testManager.insertManyPersons();
		Bson index = Indexes.ascending("name");
		StatusResult indexResult = dbHandler.createIndexSync("Person", index, null);
		Assertions.assertTrue(indexResult.isSuccess());
		StatusResult deleteResult = dbHandler.deleteIndexesSync("Person");
		Assertions.assertTrue(deleteResult.isSuccess());
	}

	
	@AfterEach
	public void dropTestDb() {
		dbHandler.dropDatabaseSync();
	}



	@AfterAll
	public void dropTestCollection() {
		dbHandler.dropDatabaseSync();
	}
}
