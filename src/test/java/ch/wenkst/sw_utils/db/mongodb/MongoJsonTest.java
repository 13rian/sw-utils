package ch.wenkst.sw_utils.db.mongodb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.reactivestreams.client.MongoCollection;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.db.mongodb.subscriber.list.DocumentListCallbackSubscriber;
import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallbackSubscriber;
import ch.wenkst.sw_utils.miscellaneous.StatusResult;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("unchecked")
public class MongoJsonTest extends BaseTest {
	private DbTestManager testManager = new DbTestManager();
	private MongoDBHandler dbHandler;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	
	@BeforeAll
	public void prepareDbHandler() throws DbConnectException, InterruptedException, ExecutionException, TimeoutException {
		dbHandler = MongoDBHandler.getInstance();
		testManager.connectToDb();
		dbHandler.dropDatabaseSync();
	}

	

	@Test
	public void findAll() {
		testManager.insertManyPersons();
		StatusResult findResult = dbHandler.findJsonSync("Person");
		Assertions.assertTrue(findResult.isSuccess());
		String jsonResult = findResult.getResult();
		List<Object> personList = gson.fromJson(jsonResult, List.class);
		Assertions.assertEquals(3, personList.size());
	}
	
	
	@Test
	public void findByProperty() {
		testManager.insertManyPersons();
		Bson query = Filters.lte("age", 50);
		StatusResult findResult = dbHandler.findJsonSync("Person", query, null, null);
		Assertions.assertTrue(findResult.isSuccess());
		String jsonResult = findResult.getResult();
		List<Object> personList = gson.fromJson(jsonResult, List.class);
		Assertions.assertEquals(2, personList.size());
	}
	
	
	@Test
	public void findWithProjection() {
		testManager.insertManyPersons();
		
		Bson projection = Projections.fields(
				Projections.include("name"),
				Projections.include("age"),
				Projections.excludeId());
		
//		Document projection = new Document()
//				.append("name", 1)
//	            .append("age",1)
//	            .append("_id", 0); 		// 0: exclude, 1: include
		
		
		StatusResult findResult =  dbHandler.findJsonSync("Person", null, null, projection);
		Assertions.assertTrue(findResult.isSuccess());
		
		String jsonResult = findResult.getResult();
		
		List<Object> personList = gson.fromJson(jsonResult, List.class);
		Map<String, Object> personMap = (Map<String, Object>) personList.get(0);
		Assertions.assertTrue(personMap.containsKey("name"));
		Assertions.assertTrue(personMap.containsKey("age"));
		Assertions.assertFalse(personMap.containsKey("_id"));
	}
	
	
	@Test
	public void simpleAggregation() {
		testManager.insertManyPersons();
		MongoCollection<Document> collection = dbHandler.getJsonCollection("Person");
		
		// sums all age fields
		Publisher<Document> publisher = collection.aggregate(Arrays.asList(
				Aggregates.match(new Document()),
				Aggregates.group("", Accumulators.sum("sum", "$age")))
		);
		
		AtomicBoolean finished = new AtomicBoolean(false);
		ValueCallbackSubscriber<Document> subscriber = new ValueCallbackSubscriber<>((result, error) ->  {
			int summedAge = (int) result.get("sum");
			Assertions.assertEquals(134, summedAge);
			finished.set(true);
		});
		publisher.subscribe(subscriber);
		
		Awaitility.await().atMost(100, TimeUnit.SECONDS).until(() -> {
			return finished.get();
		});
	}
	
	
	@Test
	public void aggregationByGroup() {
		testManager.insertManyPersons();
		MongoCollection<Document> collection = dbHandler.getJsonCollection("Person");
		
		// sums the ages grouped by the city
		Publisher<Document> publisher = collection.aggregate(Arrays.asList(
				Aggregates.match(new Document()),
				Aggregates.group("$address.city", Accumulators.sum("sum", "$age")))
		);
		
		AtomicBoolean finished = new AtomicBoolean(false);
		DocumentListCallbackSubscriber subscriber = new DocumentListCallbackSubscriber((result, error) ->  {
			Assertions.assertEquals(3, result.size());
			finished.set(true);
		});
		publisher.subscribe(subscriber);
		
		Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> {
			return finished.get();
		});
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
