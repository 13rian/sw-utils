package ch.wenkst.sw_utils;


import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.AggregatePublisher;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.db.mongodb.DbConnectOptions;
import ch.wenkst.sw_utils.db.mongodb.MongoDBHandler;
import ch.wenkst.sw_utils.db.mongodb.base.BaseEntity;
import ch.wenkst.sw_utils.db.mongodb.subscriber.PrintResultCallback;
import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallbackSubscriber;

public class MainAsyncMongoDB {
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
	
	final static Logger logger = LoggerFactory.getLogger(Main_ZMQ_Messaging.class);

	public static void main(String[] args) throws Exception {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		MongoDBHandler dbHandler = MongoDBHandler.getInstance();
		
		// connect to the db
		String[] packageNames = {Person.class.getPackage().getName()};
		
		try {
			DbConnectOptions options = new DbConnectOptions();
			options.setHost("192.168.5.128");
			options.setPort(27017);
			options.setTimeout(10);
			options.setDbName("AsyncTest");
			options.setPackageNames(packageNames);
			
			dbHandler.connectToDB(options);
			
		} catch (Exception e) {
			logger.error("failed to connect to the db: ", e);
		}
		
//		// send the ping
//		boolean isPingSent = dbHandler.testConnection();
//		
//		logger.info("connection test finished, isConnected: " + isConnected + ", isPingSent: " + isPingSent);
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											with java objects 													//
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// insert one document
		Person ada = new Person("Ada Byron", 20, new Address("St James Square", "London", "W1"));		
		
		ada.saveToDB((result, error) ->  {
			if (error == null) {
				logger.debug("one document successfully inserted");
			
			} else {
				logger.error("failed to insert one document to the db: ", error);
			}
		});
		
		
		// insert many documents
		Utils.sleep(200);
		List<BaseEntity> peopleList = Arrays.asList(
				new Person("Charles Babbage", 45, new Address("5 Devonshire Street", "London", "W11")),
				new Person("Alan Turing", 28, new Address("Bletchley Hall", "Bletchley Park", "MK12")),
				new Person("Timothy Berners-Lee", 61, new Address("Colehill", "Wimborne", null))
		);
		
		dbHandler.insert(peopleList, (result, error) ->  {
			if (error == null) {
				logger.debug("many documents successfully inserted");
			
			} else {
				logger.error("failed to insert the entity list to the db: ", error);
			}
		});
		
		
		
		// make a query to get all persons
		Utils.sleep(200);
		
		dbHandler.find(Person.class, (result, error) ->  {
			List<Person> persons = (List<Person>) result;
			
			if (error != null) {
				logger.error("query failed: ", error);
			} else {
				logger.info("query successful, length: " + result.size());
			}
		});
		
		
		// make a query using the generic method to only get one person
		FindPublisher<BaseEntity> publisher = dbHandler
				.getCollection(Person.class)
				.find(new Document())
				.sort(new Document())
				.limit(1);
		
		dbHandler.find(publisher, (result, error) ->  {
			List<Person> persons = (List<Person>) result;
			
			if (error != null) {
				logger.error("query failed: ", error);
			} else {
				logger.info("query successful, length: " + result.size());
			}
		});
		
		
		// make a query to retrieve specific persons
		Utils.sleep(200);
		Bson query = Filters.and(Filters.eq("address.city", "London"), Filters.lte("age", 30));
		Bson sort = Sorts.ascending("age");
		
		dbHandler.find(Person.class, query, sort, (result, error) ->  {
			if (error != null) {
				logger.error("query failed: ", error);
			} else {
				logger.info("query successful, length: " + result.size());
			}
		});
		
		
		
		// update entities in the db	
		Utils.sleep(200);
		query = Filters.not(Filters.eq("address.zip", null));
		Bson update = Updates.set("address.zip", null);
		
		// example to combine updates
		// Updates.combine(Updates.set("address.zip", null), Updates.set("age", 23));		
		dbHandler.update(Person.class, query, update, (result, error) ->  {
			if (error == null) {
				logger.info("many persons altered, modify count: " + result.getModifiedCount());
			} else {
				logger.error("error updating many person in the db: ", error);
			}
		});
		
		
		
		// delete entities in the db
		Utils.sleep(200);
		query = Filters.eq("address.city", "London");
		
		dbHandler.delete(Person.class, query, (result, error) ->  {
			if (error == null) {
				logger.info("delete many person, deleted count: " + result.getDeletedCount());
			} else {
				logger.error("error deleting many persons: ", error);
			}
		});
		
		
		// drop the collection
		Utils.sleep(200);
		
		dbHandler.dropCollection(Person.class, (result, error) ->  {
			if (error == null) {
				logger.info("collection successfully dropped");
			} else {
				logger.error("failed to drop the collection: ", error);
			}
		});
		
		
		
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 												json find 														//
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// insert a few documents for the test
		Utils.sleep(200);		
		peopleList = Arrays.asList(
				new Person("Paul Morphy", 37, new Address("5 Devonshire Street", "Brooklyn", "W15")),
				new Person("Robert Fischer", 36, new Address("Bletchley Hall", "Paris", "MK12")),
				new Person("Roy Handsome", 57, new Address("Colehill", "Budapest", "MI34"))
		);
		
		dbHandler.insert(peopleList, (result, error) ->  {
			if (error == null) {
				logger.debug("many documents successfully inserted");
			} else {
				logger.error("failed to insert the entity list to the db: ", error);
			}
		});
		
		
		// find all entities in the db
		Utils.sleep(200);
		
		dbHandler.findJson("Person", (result, error) ->  {
			if (error != null) {
				logger.error("json-query failed: ", error);
				
			} else {
				logger.info("json-query successful: " + result);
			}
		});
		
		
		// use more complex method, to find all entities
		Utils.sleep(200);
		
		dbHandler.findJson("Person", null, null, null, (result, error) ->  {
			if (error != null) {
				logger.error("json-query failed: ", error);
				
			} else {
				logger.info("json-query successful: " + result);
			}
		});
		
		
		
		// use query, sort and projection
		Utils.sleep(200);
		
		Bson projection = Projections.fields(
				Projections.include("name"),
				Projections.include("age"),
				Projections.excludeId());
		
//		Document projection = new Document()
//				.append("name", 1)
//	            .append("age",1)
//	            .append("_id", 0); 		// 0: exclude, 1: include
		
		
		dbHandler.findJson("Person", null, Sorts.ascending("age"), projection, (result, error) ->  {
			if (error != null) {
				logger.error("json-projection-query failed: ", error);
				
			} else {
				logger.info("json-projection-query successful: " + result);
			}
		});
		
		
		// create indexes
		Utils.sleep(200);
		
		// single ascending index
		Bson index1 = Indexes.ascending("name");
		dbHandler.createIndex("Person", index1, null, new PrintResultCallback<String>("ascending-index"));

		// compound ascending index (index of multiple fields)
		Bson index2 = Indexes.ascending("name", "age");
		dbHandler.createIndex("Person", index2, null, new PrintResultCallback<String>("ascending-compound-index"));

		// single descending index
		Bson index3 = Indexes.descending("name");
		dbHandler.createIndex("Person", index3, null, new PrintResultCallback<String>("descending-index"));

		// compound descending index
		Bson index4 = Indexes.descending("stars", "age");
		dbHandler.createIndex("Person", index4, null, new PrintResultCallback<String>("descending-compound-index"));

		// compound descending and ascending index 
		// here it is saved in the db like this
		// age : 	92	85	65	65	65	28	28	28	17 		first order descending
		// name:	ca  ab  aa  ab  ac  bc	bd  s   a 		second order ascending name if age is equal
		Bson index5 = Indexes.compoundIndex(Indexes.descending("age"), Indexes.ascending("name"));
		dbHandler.createIndex("Person", index5, null, new PrintResultCallback<String>("ascending-and-descending-compound-index"));
		
		// create a unique index, the field age and name cannot be the same
		IndexOptions indexOptions = new IndexOptions().unique(true);
		Bson index6 = Indexes.ascending("name", "stars");
		dbHandler.createIndex("Person", index6, indexOptions, new PrintResultCallback<String>("unique-index"));
		
		
		// delete all indexes
		Utils.sleep(200);
		
		dbHandler.deleteIndexes("Person", (result, error) ->  {
			if (error == null) {
				logger.info("all indexes successfully deleted");
			} else {
				logger.info("failed to delete all indexs: ", error);
			}
		});
		
		
		
		// use the aggregation framework
		Utils.sleep(200);
		
		// get the json collection
		MongoCollection<Document> collection = dbHandler.getJsonCollection("Person");
		
		// get the count of the different ages
		AggregatePublisher<Document> diffAgeCountPub = collection.aggregate(Arrays.asList(
				Aggregates.match(new Document()), 											// match all documents
				Aggregates.group("$age", Accumulators.sum("count", 1)))
		);
		
		ValueCallbackSubscriber<Document> diffAgeCountSub = new ValueCallbackSubscriber<>((result, error) ->  {
			if (error == null) {
				logger.info("aggregation finished, summed age");
			} else {
				logger.error("aggregation finished, summed age error: ", error);
			}
		});
		diffAgeCountPub.subscribe(diffAgeCountSub);
		
		
		// sum up the age grouped by city
		Utils.sleep(100);
		
		Publisher<Document> agePerCityPub = collection.aggregate(Arrays.asList(
				Aggregates.match(new Document()), 											// match all documents
				Aggregates.group("$address.city", Accumulators.sum("sum", "$age")))
		);
		
		ValueCallbackSubscriber<Document> agePerCitySub = new ValueCallbackSubscriber<>((result, error) ->  {
			if (error == null) {
				logger.info("aggregation finished, summed age");
			} else {
				logger.error("aggregation finished, summed age error: ", error);
			}
		});
		agePerCityPub.subscribe(agePerCitySub);


		
		// sum up all the age fields
		Utils.sleep(100);
		
		Publisher<Document> summedAgePub = collection.aggregate(Arrays.asList(
				Aggregates.match(new Document()), 											// match all documents
				Aggregates.group("", Accumulators.sum("sum", "$age")))
		);
		
		ValueCallbackSubscriber<Document> summedAgeSub = new ValueCallbackSubscriber<>((result, error) ->  {
			if (error == null) {
				logger.info("aggregation finished, summed age");
			} else {
				logger.error("aggregation finished, summed age error: ", error);
			}
		});
		summedAgePub.subscribe(summedAgeSub);
		
		
		
		// drop the collection
		Utils.sleep(200);
		dbHandler.dropCollection("Person", new PrintResultCallback<Void>("drop-collection"));
		
		
		// drop the database
		Utils.sleep(200);

        dbHandler.dropDatabase(new PrintResultCallback<Void>("drop-db"));
        
        
        Utils.sleep(200);
        logger.info("end of test reached");
	}
}
