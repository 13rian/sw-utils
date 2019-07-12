package ch.wenkst.sw_utils.db.mongodb;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;

import ch.wenkst.sw_utils.db.mongodb.base.BaseEntity;
import ch.wenkst.sw_utils.db.mongodb.base.EntityInfo;

public class MongoDBHandler {
	private static final Logger logger = LoggerFactory.getLogger(MongoDBHandler.class);
	
	private static MongoDBHandler instance = null; 	// instance for the singleton access
	
	private MongoClient mongoClient = null;					// the client to the mongo db
	private MongoDatabase database = null; 					// name of the database to use
	
	// map of the databases, key: name of the db, value: the db object
	private HashMap<String, MongoDatabase> dbMap = null;
	
	
	/**
	 * handles the interface to the mongodb
	 */
	protected MongoDBHandler() {
		dbMap = new HashMap<>();
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
	
	/**
	 * establishes a connection to the mongodb
	 * @param host 				the host to connect to
	 * @param port 				the port of the mongo db
	 * @param timeout 			the time in seconds how long to wait for the connection establishment (more than 30secs makes
	 * 							no sense as the timeout of the mongo client is 30 seconds)
	 * @param dbName 			the name of the db to use
	 * @param packageNames 		package names that contain the db entities
	 * @return 					true if the connection was successfully established, false otherwise
	 */
	public boolean connecToDB(String host, int port, int timeout, String dbName, String[] packageNames) {
		return connecToDB(host, port, timeout, null, null, dbName, packageNames);
	}
	
	
	/**
	 * establishes a connection to the mongodb
	 * @param host 				the host to connect to
	 * @param port 				the port of the mongo db
	 * @param timeout 			the time in seconds how long to wait for the connection establishment (more than 30secs makes
	 * 							no sense as the timeout of the mongo client is 30 seconds)
	 * @param username			the user name if the db is authenticated, null otherwise
	 * @param password  		the password if the db is authenticated, null otherwise
	 * @param dbName 			the name of the db to use
	 * @param packageNames 		package names that contain the db entities
	 * @return 			true if the connection was successfully established, false otherwise
	 */
	public boolean connecToDB(String host, int port, int timeout, String username, String password, String dbName, String[] packageNames) {
		String connString = createConnectString(host, port, username, password);
		logger.info("connect to db " + dbName + ", connection string: " + connString);
		
		boolean isConnected = connecToDB(connString, timeout, packageNames);
		if (isConnected) {
			database = mongoClient.getDatabase(dbName);
		}
		return isConnected;
	}
	
	
	/**
	 * establishes a connection to the mongodb
	 * @param connectString 	mongodb connection string to connect to the db
	 * @param timeout 			the time in seconds how long to wait for the connection establishment (more than 30secs makes no sense)
	 * 							as the timeout of the mongo client is 30 seconds
	 * @param packageNames 		package names that contain the db entities
	 * @return 					true if the connection was successfully established, false otherwise
	 */
	public boolean connecToDB(String connectString, int timeout, String... packageNames) {
		// before using the driver with java objects a CodecRegistry needs to be configured. This includes codecs that
		// handle the translation to and form bson for the java objects. 
		// This combines the default codec registry, with the PojoCodecProvider configured to automatically create PojoCodecs
		CodecProvider provider;
		if (packageNames == null || packageNames.length == 0) {
			// use the default codec provider
			provider = PojoCodecProvider.builder().automatic(true).build();
			
		} else {
			// use the registered db models of the passed packages. initially the codec uses reflection but later
			// the setter and getters are used if the exist
			provider = PojoCodecProvider.builder().register(packageNames).build();
		}
		
		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
				CodecRegistries.fromProviders(provider),
				MongoClients.getDefaultCodecRegistry());
		
		// create the listener for the mongo server description
		CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
		MongoStatusListener statusListener = new MongoStatusListener(connectionFuture);
		
		
		// configure the mongo client	
		MongoClientSettings settings = MongoClientSettings.builder()
				.codecRegistry(pojoCodecRegistry)
				.applyConnectionString(new ConnectionString(connectString))
//                .applyToClusterSettings(builder -> {
//	                builder.hosts(hosts); 		
//	             })
				.applyToSocketSettings(builder -> {
					builder.connectTimeout(timeout, TimeUnit.SECONDS);
				})
				.applyToServerSettings(builder -> {
					builder.addServerListener(statusListener);
				})
				.build();
		
		mongoClient = MongoClients.create(settings);		
		
		// wait for the db to establish the connection
		try {
			boolean connEstablished = connectionFuture.get(timeout, TimeUnit.SECONDS);
			if (connEstablished) {
				logger.info("connection to mongo db successfully established");
			} else {
				logger.info("failed to establish a connection to mongo db");
			}
			
			return connEstablished;
			
		} catch (Exception e) {
			logger.error("failed to establish a connection to mongo db: ", e);
			return false;
		}
	}
	
	
	/**
	 * creates the connect string to connect to mongodb
	 * @param host 			the host of the db
	 * @param port 			the port of the db
	 * @param username 		user name, if the db is authenticated, null otherwise 
	 * @param password 		password, if the db is authenticated, null otherwise 
	 * @return 				the connect string which can be used to open the connection to the db
	 */
	private String createConnectString(String host, int port, String username, String password) {
		// create the connect string from the values in the configuration file
		StringBuilder sb = new StringBuilder();
		sb.append("mongodb://");
		if (username != null && password != null) {
			sb.append(username).append(":");
			sb.append(password).append("@");
		}
		sb.append(host).append(":").append(port);
		return sb.toString();
	}
	

// 	not necessary because the connect method will fail if the db is not reachable 
//	/**
//	 * sends a ping to the db in order to check the connection
//	 * @param dbName 	the name of the db to use
//	 * @return 			true if the connection is open, false otherwise
//	 */
//	public boolean testConnection(String dbName) {
//		MongoDatabase db = getDatabase(dbName);
//		return testConnection(db);
//	}
//	
//	/**
//	 * sends a ping to the db in order to check the connection
//	 * @return 			true if the connection is open, false otherwise
//	 */
//	public boolean testConnection() {
//		return testConnection(database);
//	}
//	
//	
//	/**
//	 * sends a ping to the db in order to check the connection
//	 * @param databse 	the db to use	
//	 * @return 			true if the connection is open, false otherwise
//	 */
//	private boolean testConnection(MongoDatabase database) {
//		Publisher<String> publisher = mongoClient.listDatabaseNames();
//		BlockingSubscriber<String> strSubscriber = new BlockingSubscriber<>();
//		publisher.subscribe(strSubscriber);
//		
//		try {
//			List<String> strAnswer = strSubscriber.get(10, TimeUnit.SECONDS);
//			logger.debug("successfully sent the ping to mongodb: " + strAnswer);
//			return true;
//		} catch (Exception e) {
//			logger.error("failed to send the ping to mongdb: ", e);
//			return false;
//		}		
//	}
	
	
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
	 * @param subscriber 	subscriber to the insert one publisher
	 */
	public void insertOne(BaseEntity entity, Subscriber<Success> subscriber) {
		// get the collection
		MongoCollection<BaseEntity> collection = getCollection(entity.getClass());
		
		// insert the document
		Publisher<Success> publisher = collection.insertOne(entity);
		publisher.subscribe(subscriber);
	}
	
	
	
	
	/**
	 * asynchronously inserts many documents to the db
	 * @param entities 		the list of entities to save to the db
	 * @param subscriber 	subscriber to the insert many publisher
	 */
	public void insertMany(List<BaseEntity> entities, Subscriber<Success> subscriber) {
		if (entities.size() < 1) {
			return;
		}
		
		// get the collection
		MongoCollection<BaseEntity> collection = getCollection(entities.get(0).getClass());
		
		// insert the documents
		Publisher<Success> publisher = collection.insertMany(entities);
		publisher.subscribe(subscriber);
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													find	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * queries all entities in the db
	 * @param classObj 		the entity to retrieve
	 * @param subscriber 	subscriber to the find publisher
	 */
	@SuppressWarnings("unchecked")
	public void find(Class<?> classObj, Subscriber<? extends BaseEntity> subscriber) {
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		
		FindPublisher<BaseEntity> publisher = collection.find();
		publisher.subscribe((Subscriber<BaseEntity>) subscriber);	
	}	
	
	
	/**
	 * queries entities in the db based on the passed query
	 * @param classObj 		the entity to retrieve
	 * @param query 		the query, can be null
	 * @param sort 			the sort information, can be null
	 * @param subscriber 	subscriber to the find publisher
	 */
	@SuppressWarnings("unchecked")
	public void find(Class<?> classObj, Bson query, Bson sort, Subscriber<? extends BaseEntity> subscriber) {
		if (query == null) {
			query = new Document();
		}
		if (sort == null) {
			sort = new Document();
		}
		
		// get the collection
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		
		// retrieve the entities from the db
		FindPublisher<BaseEntity> publisher = collection.find(query).sort(sort);
		publisher.subscribe((Subscriber<BaseEntity>) subscriber);		
	}	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													update	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * updates entities in the db
	 * @param classObj 		the class of the entity to update
	 * @param query 		the query to for the update, can be null
	 * @param update 		the values for the update
	 * @param subscriber 	subscriber to the update publisher
	 */
	public void update(Class<?> classObj, Bson query, Bson update, Subscriber<UpdateResult> subscriber) {
		if (query == null) {
			query = new Document();
		}
		
		// get the collection
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		
		// execute the update
		Publisher<UpdateResult> publisher = collection.updateMany(query, update);
		publisher.subscribe(subscriber);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													delete	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * deletes entities in the db
	 * @param classObj 		the class of the entity to delete
	 * @param query 		the query for the deletion, can be null
	 * @param subscriber 	subscriber to the delete publisher
	 */
	public void delete(Class<?> classObj, Bson query, Subscriber<DeleteResult> subscriber) {
		if (query == null) {
			query = new Document();
		}
		
		// get the collection
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		
		// execute the update
		Publisher<DeleteResult> publisher = collection.deleteMany(query);
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * drops the collection in the db of the passed entity class
	 * @param classObj 		the class of the entity to delete
	 * @param subscriber 	subscriber to the drop collection publisher
	 */
	public void dropCollection(Class<?> classObj, Subscriber<Success> subscriber) {
		MongoCollection<BaseEntity> collection = getCollection(classObj);
		Publisher<Success> publisher = collection.drop();
		publisher.subscribe(subscriber);
	}
	
	
	
	/**
	 * drops the used database
	 * @param subscriber 	subscriber to the drop database publisher
	 */
	public void dropDatabase(Subscriber<Success> subscriber) {
		Publisher<Success> publisher = database.drop();
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * drops the db with the passed name
	 * @param dbName 		the name of the db to drop
	 * @param subscriber 	subscriber to the drop database publisher
	 */
	public void dropDatabase(String dbName, Subscriber<Success> subscriber) {
		MongoDatabase db = getDatabase(dbName);
		Publisher<Success> publisher = db.drop();
		publisher.subscribe(subscriber);
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
	 * @param subscriber 		subscriber to the find publisher
	 */
	public void findJson(String collectionName, Subscriber<Document> subscriber) {
		MongoCollection<Document> collection = getJsonCollection(collectionName);
		findJson(collection, subscriber);
	}
	
	
	/**
	 * queries all entities in the db
	 * @param collectionName 	the name of the collection to query
	 * @param dbName 			the name of the database to use
	 * @param subscriber 		subscriber to the find publisher
	 */
	public void findJson(String collectionName, String dbName, Subscriber<Document> subscriber) {
		MongoCollection<Document> collection = getJsonCollection(collectionName);
		findJson(collection, subscriber);
	}
	
	
	/**
	 * queries all entities in the db
	 * @param collection 	the collection to perform the query
	 * @param subscriber 	subscriber to the find publisher
	 */
	private void findJson(MongoCollection<Document> collection, Subscriber<Document> subscriber) {
		FindPublisher<Document> publisher = collection.find();
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * queries the entities in the db based on the passed query
	 * @param collectionName 	the name of the collection to query
	 * @param query 			the query to execute, can be null
	 * @param sort 				the sort information, can be null
	 * @param projection 		the projection information, can be null
	 * @param subscriber 		subscriber to the find publisher
	 */
	public void findJson(String collectionName, Bson query, Bson sort, Bson projection, Subscriber<Document> subscriber) {
		MongoCollection<Document> collection = getJsonCollection(collectionName);
		findJson(collection, query, sort, projection, subscriber);
	}
	
	
	/**
	 * queries the entities in the db based on the passed query
	 * @param collectionName 	the name of the collection to query
	 * @param dbName			the name of the db to use
	 * @param query 			the query to execute, can be null
	 * @param sort 				the sort information, can be null
	 * @param projection 		the projection information, can be null
	 * @param subscriber 		subscriber to the find publisher
	 */
	public void findJson(String collectionName, String dbName, Bson query, Bson sort, Bson projection, Subscriber<Document> subscriber) {
		MongoCollection<Document> collection = getJsonCollection(collectionName, dbName);
		findJson(collection, query, sort, projection, subscriber);
	}
	
	
	
	/**
	 * queries entities in the db based on the passed query
	 * @param collection 		the collection to perform the query
	 * @param query 			the query to execute, can be null
	 * @param sort 				the sort information, can be null
	 * @param projection 		the projection information, can be null
	 * @param subscriber 		subscriber to the find publisher
	 */
	private void findJson(MongoCollection<Document> collection, Bson query, Bson sort, Bson projection, Subscriber<Document> subscriber) {
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
		publisher.subscribe(subscriber);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 													delete	 								   			  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * drops the collection in the db
	 * @param collectionName 	the name of the collection to drop
	 * @param dbName 			the name of the db to use
	 * @param subscriber 		subscriber to the drop collection publisher
	 */
	public void dropCollection(String collectionName, String dbName, Subscriber<Success> subscriber) {
		MongoDatabase db = getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		Publisher<Success> publisher = collection.drop();
		publisher.subscribe(subscriber);
	}
	
	/**
	 * drops the collection in the db
	 * @param collectionName 	the name of the collection to drop
	 * @param subscriber 		subscriber to the drop collection publisher
	 */
	public void dropCollection(String collectionName, Subscriber<Success> subscriber) {
		MongoCollection<Document> collection = database.getCollection(collectionName);
		Publisher<Success> publisher = collection.drop();
		publisher.subscribe(subscriber);
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
	 * @param subscriber 		subscriber to the index publisher
	 */
	public void createIndex(String collectionName, String dbName, Bson index, IndexOptions indexOptions, Subscriber<String> subscriber) {
		MongoDatabase db = getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		createIndex(collection, index, indexOptions, subscriber);
	}
	
	
	/**
	 * creates an index in the db to accelerate queries, the same index is only created once and never 
	 * @param collectionName 	the name of the collection for which the index is created
	 * @param index 			the definition of the index
	 * @param indexOptions 		the options for the index, can be null
	 * @param subscriber 		subscriber to the index publisher
	 */
	public void createIndex(String collectionName, Bson index, IndexOptions indexOptions, Subscriber<String> subscriber) {
		MongoCollection<Document> collection = database.getCollection(collectionName);
		createIndex(collection, index, indexOptions, subscriber);
	}
	
	
	/**
	 * creates an index in the db to accelerate queries, the same index is only created once and never 
	 * @param collection 		the collection for which the index is created
	 * @param index 			the definition of the index
	 * @param indexOptions 		the options for the index, can be null
	 * @param subscriber 		subscriber to the index publisher
	 */
	private void createIndex(MongoCollection<Document> collection, Bson index, IndexOptions indexOptions, Subscriber<String> subscriber) {
		Publisher<String> publisher;
		if (indexOptions == null) {
			publisher = collection.createIndex(index);
		} else {
			publisher = collection.createIndex(index, indexOptions);
		}
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * drops all indexes of this collection
	 * @param collectionName 	the name of the collection for which all indexes are deleted
	 * @param subscriber 		subscriber to the delete indexes publisher
	 */
	public void deleteIndexes(String collectionName, Subscriber<Success> subscriber) {
		MongoCollection<Document> collection = database.getCollection(collectionName);
		Publisher<Success> publisher = collection.dropIndexes();
		publisher.subscribe(subscriber);
	}
	
	
	/**
	 * drops all indexes of this collection
	 * @param collectionName 	the name of the collection for which all indexes are deleted
	 * @param dbName 			the name of the database to use
	 * @param subscriber 		subscriber to the delete indexes publisher
	 */
	public void deleteIndexes(String collectionName, String dbName, Subscriber<Success> subscriber) {
		MongoDatabase db = getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		Publisher<Success> publisher = collection.dropIndexes();
		publisher.subscribe(subscriber);
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
		MongoCollection<BaseEntity> collection = database.getCollection(collectionName, classObj); 	
		
		return collection;
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
	
}
