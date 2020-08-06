package ch.wenkst.sw_utils.db.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;

import ch.wenkst.sw_utils.db.mongodb.base.BaseEntity;
import ch.wenkst.sw_utils.db.mongodb.base.EntityInfo;
import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallback;
import ch.wenkst.sw_utils.db.mongodb.subscriber.value.ValueCallbackSubscriber;
import ch.wenkst.sw_utils.db.mongodb.subscriber.list.DocumentListCallback;
import ch.wenkst.sw_utils.db.mongodb.subscriber.list.DocumentListCallbackSubscriber;
import ch.wenkst.sw_utils.db.mongodb.subscriber.list.DocumentListSubscriber;
import ch.wenkst.sw_utils.db.mongodb.subscriber.list.PojoListCallback;
import ch.wenkst.sw_utils.db.mongodb.subscriber.list.PojoListCallbackSubscriber;
import ch.wenkst.sw_utils.future.TimeoutFuture;
import ch.wenkst.sw_utils.map.MapUtils;
import ch.wenkst.sw_utils.miscellaneous.StatusResult;

public class MongoDBHandler {
	private static final Logger logger = LoggerFactory.getLogger(MongoDBHandler.class);

	private static MongoDBHandler instance = null; 			// instance for the singleton access

	private MongoClient mongoClient = null;					// the client to the mongo db
	private MongoDatabase database = null; 					// name of the database to use

	// map of the databases, key: name of the db, value: the db object
	private Map<String, MongoDatabase> dbMap = null;

	protected long dbTimeout = 10000; 						// time in ms on how long to wait for sync db operations
	private Gson gson;										// gson to handle json


	/**
	 * handles the interface to the mongodb
	 */
	protected MongoDBHandler() {
		dbMap = new HashMap<>();
		gson = new Gson();
	}


	/**
	 * returns the instance of the dbHandler
	 * @return
	 */
	public static MongoDBHandler getInstance() {
		if (instance == null) {
			instance = new MongoDBHandler();
		}	      
		return instance;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												Connection to the database 								   			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void connectToDB(DbConnectOptions options) throws DbConnectException, InterruptedException, ExecutionException, TimeoutException {
		if (options.getConnectString() == null) {
			options.createConnectString();
		}		

		long timeout = options.getTimeout();
		String connectString = options.getConnectString();
		String dbName = options.getDbName();
		if (dbName == null) {
			throw new DbConnectException("no db name specified in the connect options");
		}

		logger.info("connect to db " + dbName + ", connection string: " + connectString);
		
		mongoClient = options.createMongClient();

		
		// wait for the db to establish the connection
		Throwable throwable = options.getConnectionFuture().get(timeout, TimeUnit.SECONDS);
		if (throwable != null) {
			throw new DbConnectException(throwable.getMessage());
		}		

		database = mongoClient.getDatabase(dbName);
	}


	/**
	 * disconnects from the database
	 */
	public void disconnectFromDB() {
		if (mongoClient != null) {
			mongoClient.close(); 						// close the connection to the mongoDB
			logger.info("disconnected from mongoDB");
		}
	}




	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 		 																											  //
	// 												OPERATIONS WITH POJO	 	 										  //
	// 											uses java objects to map the db  										  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													insert	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * asynchronously inserts one document to the db
	 * @param entity 		the entity to save to the db
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void insert(BaseEntity entity, ValueCallback<Success> resultCallback) {
		MongoCollection<BaseEntity> collection = getCollection(entity.getClass());
		Publisher<Success> publisher = collection.insertOne(entity);
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) ->  {
			resultCallback.onResult(result, error);
		});		
		publisher.subscribe(subscriber);
	}


	/**
	 * synchronously inserts a pojo into the db
	 * @param entityList 	the list with the entities to insert
	 * @return 				the result of the db operation
	 */
	public StatusResult insertSync(BaseEntity entity) {
		List<BaseEntity> entityList = new ArrayList<>();
		entityList.add(entity);
		return insertSync(entityList);
	}


	/**
	 * asynchronously inserts many documents to the db
	 * @param entityList 		the list of entities to save to the db
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void insert(List<? extends BaseEntity> entityList, ValueCallback<Success> resultCallback) {
		if (entityList.isEmpty()) {
			return;
		}

		MongoCollection<BaseEntity> collection = getCollection(entityList.get(0).getClass());
		Publisher<Success> publisher = collection.insertMany(entityList);
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) ->  {
			resultCallback.onResult(result, error);
		});		
		publisher.subscribe(subscriber);
	}


	/**
	 * synchronously inserts a pojo list into the db
	 * @param entityList 	the list with the entities to insert
	 * @return 				the result of the db operation
	 */
	public StatusResult insertSync(List<? extends BaseEntity> entityList) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<StatusResult>(dbTimeout);

		insert(entityList, (result, error) ->  {
			String entityName = "-";
			if (entityList != null && !entityList.isEmpty()) {
				entityName = entityList.get(0).getClass().getSimpleName();
			}

			if (error != null) {
				logger.error(entityName + ": failed to insert pojo entity list to the db: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug(entityName + ": many pojo documents successfully inserted");
				future.complete(StatusResult.success(result));
			}
		});
		
		return waitForFuture(future);
	}	



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													find	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * queries all entities in the db
	 * @param classObj 			the entity to retrieve
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void find(Class<?> classObj, PojoListCallback resultCallback) {
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		FindPublisher<BaseEntity> publisher = collection.find();
		
		PojoListCallbackSubscriber subscriber = new PojoListCallbackSubscriber((result, error) ->  {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe((Subscriber<BaseEntity>) subscriber);	
	}	


	/**
	 * queries entities in the db based on the passed query
	 * @param classObj 			the entity to retrieve
	 * @param query 			the query, can be null
	 * @param sort 				the sort information, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	public <T, U> void find(Class<?> classObj, Bson query, Bson sort, PojoListCallback resultCallback) {
		if (query == null) {
			query = new Document();
		}
		if (sort == null) {
			sort = new Document();
		}

		MongoCollection<BaseEntity> collection = getCollection(classObj);
		FindPublisher<BaseEntity> publisher = collection.find(query).sort(sort);
		
		PojoListCallbackSubscriber subscriber = new PojoListCallbackSubscriber((result, error) ->  {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe((Subscriber<BaseEntity>) subscriber);		
	}	


	/**
	 * synchronously retrieved all pojos from the db
	 * @param classObj 		the class of the object to find in the db
	 * @return 				the result of the db operation
	 */
	public StatusResult findSync(Class<?> classObj) {
		return findSync(classObj, null,  null);
	}	


	/**
	 * synchronously retrieved a pojo from the db
	 * @param classObj 		the class of the object to find in the db
	 * @param query 		the query for the db
	 * @param sort 			the sort information
	 * @return 				the result of the db operation
	 */
	public StatusResult findSync(Class<?> classObj, Bson query, Bson sort) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<StatusResult>(dbTimeout);

		find(classObj, query, sort, (result, error) ->  {
			if (error != null) {
				logger.error(classObj.getSimpleName() + " pojo db find query failed: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug(classObj.getSimpleName() + " pojo db find query successful, length: " + result.size());
				future.complete(StatusResult.success(result));
			}
		});

		return waitForFuture(future);
	}	



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													update	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * updates entities in the db
	 * @param classObj 			the class of the entity to update
	 * @param query 			the query to for the update, can be null
	 * @param update 			the values for the update
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void update(Class<?> classObj, Bson query, Bson update, ValueCallback<UpdateResult> resultCallback) {
		if (query == null) {
			query = new Document();
		}

		MongoCollection<BaseEntity> collection = getCollection(classObj);
		Publisher<UpdateResult> publisher = collection.updateMany(query, update);
		
		ValueCallbackSubscriber<UpdateResult> subscriber = new ValueCallbackSubscriber<>((result, error) ->  {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * synchronously updates a pojo in the db
	 * @param classObj 		the class of the object to find in the db
	 * @param query 		the query for the db
	 * @param update 		the db update
	 * @return 				the result of the db operation
	 */
	public StatusResult updateSync(Class<?> classObj, Bson query, Bson update) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<StatusResult>(dbTimeout);

		update(classObj, query, update, (result, error) ->  {
			if (error != null) {
				logger.error(classObj.getSimpleName() + " pojo db update query failed: ", error);
				future.complete(StatusResult.error("pojo update query failed"));

			} else {
				logger.debug(classObj.getSimpleName() + ": pojo db update query successful");
				future.complete(StatusResult.success(result));
			}
		});

		return waitForFuture(future);
	}
	
	
	/**
	 * saves the passed update to the db
	 * @param findQuery			query to find the alredy existing entity in the db
	 * @param updateQuery		the query to update
	 * @param classObj 			the db entity class to update or insert
	 * @return					the status result of the db operation
	 */
	public StatusResult insertOrUpdateSync(Bson findQuery, Map<String, Object> updateMap, Class<?> classObj) {
		// check if the device already exists in the db
		StatusResult result = findSync(classObj, findQuery, null);
		if (!result.isSuccess()) {
			logger.error("error executing find pojo, reason: " + result.getErrorMsg());
			return result;
		}


		// update or insert the entity
		List<BaseEntity> deviceEntities = result.getResult();
		if (deviceEntities.size() > 0) {
			Bson update = updateMapToBson(updateMap);			
			return updateSync(classObj, findQuery, update);

		} else {
			BaseEntity device = (BaseEntity) MapUtils.mapToObj(updateMap, classObj);
			if (device == null) {
				logger.error("failed to save the pojo to the db, entity class could not be created form the update map");
				return StatusResult.error("failed to save the pojo to the db, entity class could not be created form the update map");
			}			

			return insertSync(device);
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													delete	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * deletes entities in the db
	 * @param classObj 			the class of the entity to delete
	 * @param query 			the query for the deletion, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void delete(Class<?> classObj, Bson query, ValueCallback<DeleteResult> resultCallback) {
		if (query == null) {
			query = new Document();
		}

		MongoCollection<BaseEntity> collection = getCollection(classObj);
		Publisher<DeleteResult> publisher = collection.deleteMany(query);
		
		ValueCallbackSubscriber<DeleteResult> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * synchronously deletes a pojo from the db
	 * @param classObj 		the class of the object to find in the db
	 * @param query 		the query for the db
	 * @return 				the result of the db operation
	 */
	public StatusResult deleteSync(Class<?> classObj, Bson query) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<>(dbTimeout);

		delete(classObj, query, (result, error) ->  {
			if (error != null) {
				logger.debug(classObj.getSimpleName() + ": pojo db delete query failed: ", error);
				future.complete(StatusResult.error("pojo delete query failed"));

			} else {
				logger.debug(classObj.getSimpleName() + ": pojo delete query successful");
				future.complete(StatusResult.success(result));
			}
		});

		return waitForFuture(future);
	}


	/**
	 * drops the collection in the db of the passed entity class
	 * @param classObj 			the class of the entity to delete
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void dropCollection(Class<?> classObj, ValueCallback<Success> resultCallback) {
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		Publisher<Success> publisher = collection.drop();		
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * synchronously drops the collection in the db of the passed entity class
	 * @param classObj 			the class of the entity to delete
	 * @return
	 */
	public StatusResult dropCollectionSync(Class<?> classObj) {	
		TimeoutFuture<StatusResult> future = new TimeoutFuture<StatusResult>(dbTimeout);

		dropCollection(classObj, (result, error) ->  {
			if (error != null) {
				logger.error("collection of " + classObj.getName() + " could not be dropped: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug("successfully dropped collection of " + classObj.getName());
				StatusResult statusResult = StatusResult.success(result);
				future.complete(statusResult);
			}
		});

		return waitForFuture(future);
	}


	/**
	 * drops the used database
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void dropDatabase(ValueCallback<Success> resultCallback) {
		Publisher<Success> publisher = database.drop();
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * drops the db with the passed name
	 * @param dbName 			the name of the db to drop
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void dropDatabase(String dbName, ValueCallback<Success> resultCallback) {
		MongoDatabase db = getDatabase(dbName);
		Publisher<Success> publisher = db.drop();
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}

	
	/**
	 * synchronously drops the used database
	 * @return
	 */
	public StatusResult dropDatabaseSync() {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<>(dbTimeout);

		dropDatabase((result, error) ->  {
			if (error != null) {
				logger.error("database could not be dropped: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug("successfully dropped database");
				StatusResult statusResult = StatusResult.success(result);
				future.complete(statusResult);
			}
		});

		return waitForFuture(future);
	}
	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 		 																											  //
	// 												OPERATIONS WITH JSON	 	 										  //
	// 						directly loads json from the db, i.e. bson that is converted to json						  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												json-find	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * queries all entities in the db
	 * @param collectionName 	the name of the collection to query
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void findJson(String collectionName, ValueCallback<String> resultCallback) {
		MongoCollection<Document> collection = getJsonCollection(collectionName);
		findJson(collection, resultCallback);
	}


	/**
	 * queries all entities in the db
	 * @param collectionName 	the name of the collection to query
	 * @param dbName 			the name of the database to use
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void findJson(String collectionName, String dbName, ValueCallback<String> resultCallback) {
		MongoCollection<Document> collection = getJsonCollection(collectionName, dbName);
		findJson(collection, resultCallback);
	}


	/**
	 * queries all entities in the db
	 * @param collection 		the collection to perform the query
	 * @param resultCallback 	callback that is called with the db result
	 */
	private void findJson(MongoCollection<Document> collection, ValueCallback<String> resultCallback) {
		FindPublisher<Document> publisher = collection.find();
		
		ValueCallbackSubscriber<Document> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			String jsonList = gson.toJson(result);
			resultCallback.onResult(jsonList, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * queries the entities in the db based on the passed query
	 * @param collectionName 	the name of the collection to query
	 * @param query 			the query to execute, can be null
	 * @param sort 				the sort information, can be null
	 * @param projection 		the projection information, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void findJson(String collectionName, Bson query, Bson sort, Bson projection, ValueCallback<String> resultCallback) {
		MongoCollection<Document> collection = getJsonCollection(collectionName);
		findJson(collection, query, sort, projection, resultCallback);
	}


	/**
	 * queries the entities in the db based on the passed query
	 * @param collectionName 	the name of the collection to query
	 * @param dbName			the name of the db to use
	 * @param query 			the query to execute, can be null
	 * @param sort 				the sort information, can be null
	 * @param projection 		the projection information, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void findJson(String collectionName, String dbName, Bson query, Bson sort, Bson projection, ValueCallback<String> resultCallback) {
		MongoCollection<Document> collection = getJsonCollection(collectionName, dbName);
		findJson(collection, query, sort, projection, resultCallback);
	}


	/**
	 * queries entities in the db based on the passed query
	 * @param collection 		the collection to perform the query
	 * @param query 			the query to execute, can be null
	 * @param sort 				the sort information, can be null
	 * @param projection 		the projection information, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	private void findJson(MongoCollection<Document> collection, Bson query, Bson sort, Bson projection, ValueCallback<String> resultCallback) {
		if (query == null) {
			query = new Document();
		}
		if (sort == null) {
			sort = new Document();
		}
		if (projection == null) {
			projection = new Document();
		}

		FindPublisher<Document> publisher = collection.find(query).sort(sort).projection(projection);
		
		DocumentListCallbackSubscriber subscriber = new DocumentListCallbackSubscriber((result, error) ->  {
			String jsonList = gson.toJson(result);
			resultCallback.onResult(jsonList, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * synchronously queries all entities in the db
	 * @param collectionName 	the name of the collection to query
	 */
	public StatusResult findJsonSync(String collectionName) {
		return findJsonSync(collectionName, null, null, null);
	}


	/**
	 * returns json as a result of the db query
	 * @param collectionName	the name of the collection to query
	 * @param query				the query for the db
	 * @param sort				the sort information
	 * @param projection	    the projection information	
	 * @return
	 */
	public StatusResult findJsonSync(String collectionName, Bson query, Bson sort, Bson projection) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<>(dbTimeout);

		findJson(collectionName, query, sort, projection, (result, error) ->  {
			if (error != null) {
				logger.error(collectionName + " json find query failed: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug(collectionName + " json find query successfull");
				StatusResult statusResult = StatusResult.success(result);
				future.complete(statusResult);
			}
		});

		return waitForFuture(future);
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													delete	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * drops the collection in the db
	 * @param collectionName 	the name of the collection to drop
	 * @param dbName 			the name of the db to use
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void dropCollection(String collectionName, String dbName, ValueCallback<Success> resultCallback) {
		MongoDatabase db = getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		Publisher<Success> publisher = collection.drop();
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}

	
	/**
	 * drops the collection in the db
	 * @param collectionName 	the name of the collection to drop
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void dropCollection(String collectionName, ValueCallback<Success> resultCallback) {
		MongoCollection<Document> collection = database.getCollection(collectionName);
		Publisher<Success> publisher = collection.drop();
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * synchronously drops the collection in the db
	 * @param collectionName 	the name of the collection to drop
	 * @return
	 */
	public StatusResult dropCollectionSync(String collectionName) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<StatusResult>(dbTimeout);

		dropCollection(collectionName, (result, error) ->  {
			if (error != null) {
				logger.error(collectionName + ": error dropping the collection: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug(collectionName + ": successfully dropped collection");
				StatusResult statusResult = StatusResult.success(result);
				future.complete(statusResult);
			}
		});

		return waitForFuture(future);
	}

	



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													index	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * creates an index in the db to accelerate queries, the same index is only created once and never 
	 * @param collectionName 	the name of the collection for which the index is created
	 * @param dbName 			the name of the database to use
	 * @param index 			the definition of the index
	 * @param indexOptions 		the options for the index, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void createIndex(String collectionName, String dbName, Bson index, IndexOptions indexOptions, ValueCallback<String> resultCallback) {
		MongoDatabase db = getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		createIndex(collection, index, indexOptions, resultCallback);
	}


	/**
	 * creates an index in the db to accelerate queries, the same index is only created once and never 
	 * @param collectionName 	the name of the collection for which the index is created
	 * @param index 			the definition of the index
	 * @param indexOptions 		the options for the index, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void createIndex(String collectionName, Bson index, IndexOptions indexOptions, ValueCallback<String> resultCallback) {
		MongoCollection<Document> collection = database.getCollection(collectionName);		
		createIndex(collection, index, indexOptions, resultCallback);
	}


	/**
	 * creates an index in the db to accelerate queries, the same index is only created once and never 
	 * @param collection 		the collection for which the index is created
	 * @param index 			the definition of the index
	 * @param indexOptions 		the options for the index, can be null
	 * @param resultCallback 	callback that is called with the db result
	 */
	private void createIndex(MongoCollection<Document> collection, Bson index, IndexOptions indexOptions, ValueCallback<String> resultCallback) {
		Publisher<String> publisher;
		if (indexOptions == null) {
			publisher = collection.createIndex(index);
		} else {
			publisher = collection.createIndex(index, indexOptions);
		}
		
		ValueCallbackSubscriber<String> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});		
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * creates an index in the db to accelerate queries, the same index is only created once and never 
	 * @param collectionName 	the name of the collection for which the index is created
	 * @param index 			the definition of the index
	 * @param indexOptions 		the options for the index, can be null
	 * @return  				
	 */
	public StatusResult createIndexSync(String collectionName, Bson index, IndexOptions indexOptions) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<StatusResult>(dbTimeout);
		
		MongoCollection<Document> collection = database.getCollection(collectionName);		
		createIndex(collection, index, indexOptions, (result, error) -> {
			if (error != null) {
				logger.error(collectionName + " failed to create index: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug(collectionName + " index successfully created");
				StatusResult statusResult = StatusResult.success(result);
				future.complete(statusResult);
			}
		});

		return waitForFuture(future);
	}


	/**
	 * drops all indexes of this collection
	 * @param collectionName 	the name of the collection for which all indexes are deleted
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void deleteIndexes(String collectionName, ValueCallback<Success> resultCallback) {
		MongoCollection<Document> collection = database.getCollection(collectionName);
		Publisher<Success> publisher = collection.dropIndexes();
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * drops all indexes of this collection
	 * @param collectionName 	the name of the collection for which all indexes are deleted
	 * @param dbName 			the name of the database to use
	 * @param resultCallback 	callback that is called with the db result
	 */
	public void deleteIndexes(String collectionName, String dbName, ValueCallback<Success> resultCallback) {
		MongoDatabase db = getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		Publisher<Success> publisher = collection.dropIndexes();
		
		ValueCallbackSubscriber<Success> subscriber = new ValueCallbackSubscriber<>((result, error) -> {
			resultCallback.onResult(result, error);
		});
		publisher.subscribe(subscriber);
	}


	/**
	 * synchronously drops all indexes of this collection
	 * @param collectionName 	the name of the collection for which all indexes are deleted
	 * @return
	 */
	public StatusResult deleteIndexesSync(String collectionName) {
		TimeoutFuture<StatusResult> future = new TimeoutFuture<>(dbTimeout);
		
		deleteIndexes(collectionName, (result, error) -> {
			if (error != null) {
				logger.error(collectionName + " failed to delete all indexes: ", error);
				future.complete(StatusResult.error(error.getMessage()));

			} else {
				logger.debug(collectionName + " all indexes successfully created");
				StatusResult statusResult = StatusResult.success(result);
				future.complete(statusResult);
			}
		});

		return waitForFuture(future);
	}
	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													helper methods	 				 		   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns the database object with the passed name
	 * @param dbName 	name of the db that is returned
	 * @return
	 */
	private synchronized MongoDatabase getDatabase(String dbName) {
		if (!dbMap.containsKey(dbName)) {
			// database not used so far, create a new object
			MongoDatabase db = mongoClient.getDatabase(dbName);
			dbMap.put(dbName, db);
			return db;

		} else {
			return dbMap.get(dbName);
		}
	}


	/**
	 * returns the collection that can be used to make queries
	 * if the EntityInfo annotation is missing the db name from the connect method is used
	 * and the classname is used for the collection name
	 * @param classObj 		the class of the db entity
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MongoCollection<BaseEntity> getCollection(Class classObj) {
		EntityInfo entityInfo = (EntityInfo) classObj.getAnnotation(EntityInfo.class);
		String dbName = ""; 
		String collectionName = "";
		if (entityInfo != null) {
			dbName = entityInfo.db();
			collectionName = entityInfo.collection();
		}


		// if the collection name is not specified take the class name 
		if (collectionName.isEmpty()) {
			collectionName = classObj.getSimpleName();
		}

		// define the database to use
		MongoDatabase db = null;
		if (dbName.isEmpty()) {
			db = database;
		} else {
			db = getDatabase(dbName);
		}

		return getCollection(collectionName, classObj, db);
	}


	/**
	 * returns the collection that can be used to make queries
	 * @param classObj 		the class of the db entity
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MongoCollection<BaseEntity> getCollection(String collectionName, Class classObj, MongoDatabase database) {
		return database.getCollection(collectionName, classObj); 	
	}


	/**
	 * returns the collection that can be used to access the driver directly without a mapping class
	 * @param collectionName 	the name of the collection
	 */
	public MongoCollection<Document> getJsonCollection(String collectionName) {
		return database.getCollection(collectionName); 
	}


	/**
	 * returns the collection that can be used to access the driver directly without a mapping class
	 * @param collectionName 	the name of the collection
	 * @param dbName 			the name of the db
	 */
	public MongoCollection<Document> getJsonCollection(String collectionName, String dbName) {
		MongoDatabase db = getDatabase(dbName);
		return db.getCollection(collectionName); 
	}

	
	/**
	 * waits for the passed future to be completed
	 * @return	status result of the completed future
	 */
	protected StatusResult waitForFuture(TimeoutFuture<StatusResult> future) {
		try {
			StatusResult result = future.get();
			if (result == null) {
				return StatusResult.error("db operation timeout out after " + dbTimeout / 1000 + " seconds.");
			}
			
			return result;

		} catch (Exception e) {
			logger.error("error waiting on db operation future: ", e);
			return StatusResult.error("error waiting on db operation future");
		}
	}
	
	
	/**
	 * creates the bson update from the passed update map
	 * @param updateMap 	map that contains all the parameters to update
	 * @return 				bson update that is used for the db query
	 */
	public Bson updateMapToBson(Map<String, Object> updateMap) {
		// remove any db id fields
		updateMap.remove("id");
		updateMap.remove("_id");
		
		ArrayList<Bson> updateList = new ArrayList<>();		
		for (Map.Entry<String, Object> entry : updateMap.entrySet()) {
			Bson update = Updates.set(entry.getKey(), entry.getValue());
			updateList.add(update);
		}
		
		Bson update = Updates.combine(updateList);
		return update;
	}
}
