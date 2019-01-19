package ch.wenkst.sw_utils.db.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * handles the database operations with mongoDB and morphia as ORM
 * the queries are synchronous which means that the method is blocked until the query is finished
 * things to know:
 * query.asList(new FindOptions().limit(1));   	to add an additional options to the query
 * note: to make the query most effective always query the one that restricts the results the most first
 */
public class MongoDbHandlerSync {
	private static final Logger logger = LoggerFactory.getLogger(MongoDbHandlerSync.class);

	// maps the dbName to the morphia's datastore (key: dbName, value: datastore) 
	private HashMap<String,Datastore> dataStoreMap = new HashMap<>(); 

	private MongoClient mongo = null;						// instance of the mongoDB driver
	private Morphia morphia = null; 			// instance of morphia that is used as a orm	

	/////////////////////////////////////////////////////////////////////////////////////////////
	// 								Connection to the database 								   //
	/////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * establishes a connection to mongoDB
	 * @param host 		host to connect to
	 * @param port 		mongo-port
	 * @return 			true if the connection was successfully established, false if an error occurred
	 */
	public boolean connectToDB(String host, int port) {
		try {
			// initialize morphia and connect to to mongoDB
			morphia = new Morphia();

			String connectStr = "mongodb://" + host + ":" + port;
			MongoClientURI clientURI = new MongoClientURI(connectStr);
			mongo = new MongoClient(clientURI);
				
			logger.info("successfully connected to mongoDB");
			return true;

		} catch (Exception e) {
			logger.error("failed to connect to the database connecting to the database", e);
			return false;
		}
	}
	
	
	/**
	 * establishes an authenticated connection to mongoDB
	 * @param host 			the host of the mongoDB
	 * @param port 			the port of the mongoDB
	 * @param username 		the user name
	 * @param password 		the password
	 * @return 			true if the connection was successfully established, false if an error occurred
	 */
	public boolean connectToDB(String host, int port, String username, String password) {
		try {
			// initialize morphia and connect to to mongoDB
			morphia = new Morphia();

			String connectStr = "mongodb://" + username + ":" + password + "@" + host + ":" + port;
			MongoClientURI clientURI = new MongoClientURI(connectStr);
			mongo = new MongoClient(clientURI);
				
			logger.info("successfully connected to mongoDB");
			return true;

		} catch (Exception e) {
			logger.error("failed to connect to the database connecting to the database", e);
			return false;
		}
	}


	/**
	 * disconnects from the database
	 * @return 		true if the connection to mongodb was successfully close, false if an error occurred
	 */
	public boolean disconnectFromDB() {
		try {
			mongo.close(); 			// close the connection to the mongoDB
			dataStoreMap.clear(); 	// clear the datastore map
			logger.info("disconnected from mongoDB");
			return true;

		} catch (Exception e) {
			logger.error("error disconnecting form mongoDB: ", e);
			return false;
		}
	}


	/**
	 * returns the datastore of the passed dbName, if it is not saved in the dataStoreMap,
	 * a new datastore for this db is created
	 * @param dbName 	name of the database
	 * @return  		the datastore of the passed dbName
	 */
	private Datastore getDataStore(String dbName) {
		Datastore datastore = dataStoreMap.get(dbName);
		if (datastore == null) {
			// datastore not found create a new one and put it into the dataStoreMap
			datastore = morphia.createDatastore(mongo, dbName);
			dataStoreMap.put(dbName, datastore);
		}

		return datastore;
	}


	/**
	 * tests the mongoDB connection for the passed hosts
	 * @param host 					host to connect to
	 * @param port 					mongo-port
	 * @param connectionTimeout 	defines the timeout how long to wait for the db to connect in ms
	 * @return				 		true if the connection was successfully established, false otherwise
	 */
	public boolean isReachable(String host, int port, int connectionTimeout) {
		MongoClient mongo = null;

		try {		
			// connect to the mongoDB with the defined options
			String connectStr = new StringBuilder()
					.append("mongodb://")
					.append(host).append(":").append(port).append("/")
					.append("?connectTimeoutMS=").append(connectionTimeout)
					.append("&socketTimeoutMS=").append(connectionTimeout)
					.append("&serverSelectionTimeoutMS=").append(connectionTimeout)
					.toString();
			MongoClientURI clientURI = new MongoClientURI(connectStr);
			mongo = new MongoClient(clientURI);
			
			// ping
			Document ping = new Document("ping", 1);
			mongo.getDatabase("test").runCommand(ping);

			// close the connection
			mongo.close();			

			// establishing connection was successful
			return true;

		} catch (Exception e) {
			logger.info("error testing for the mongoDB connection: ", e);
			if (mongo != null) {
				// close the connection if one was established
				mongo.close();
			}
		}
		return false;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////
	// 									database queries 									   //
	/////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * creates the query to the database with just one filter value
	 * @param <T> 				 class of the db entity
	 * @param entityClass		 class name of the entity that is queried
	 * @param constraintStr 	 the constraint (e.g. fieldname &lt;=), can be null or empty
	 * @param constraintValue	 value for the filter, can be null
	 * @param orderTerm 		 value for the ordering, can be null
	 * @param dbName			 name of the database: the collection name is indicated as annotation in the db entity
	 * @return					 the query to the database
	 */
	public <T> Query<T> createFilterQuery(Class<T> entityClass, String constraintStr, Object constraintValue, String orderTerm, String dbName) {
		Query<T> query = null;

		// get the datastore
		Datastore datastore = getDataStore(dbName);

		// retrieve the object list from the db
		try {
			// define the query
			query = datastore.createQuery(entityClass);     	// create the generic query

			// add the filter for the constraint 
			if (constraintStr != null && !constraintStr.isEmpty() && constraintValue != null) {
				query = query.filter(constraintStr, constraintValue);	
			}

			// add the filter for the ordering if not null
			if (orderTerm != null) {
				query = query.order(orderTerm);
			}

		} catch (Exception e) {
			logger.error("error creating query: ", e);
		}

		return query;
	}


	/** 
	 * creates and AND query to the database, the length of the constraintStr and constraintValues
	 * need to be equal
	 * @param <T> 				 the class of the db entity
	 * @param entityClass		 class name of the entity that is queried
	 * @param constraintStrs 	 array of the constraints (e.g. fieldname &lt;=), can be null
	 * @param constraintValues	 array of values for the filter, can be null
	 * @param orderTerm 		 value for the ordering, can be null
	 * @param dbName			 name of the database: the collection name is indicated as annotation in the db entity
	 * @return  				 the query to the database
	 */
	public <T> Query<T> createAndQuery(Class<T> entityClass, String[] constraintStrs, Object[] constraintValues, String orderTerm, String dbName) {
		Query<T> query = null;

		// get the datastore
		Datastore datastore = getDataStore(dbName);

		// retrieve the object list from the db
		try {
			// define the query
			query = datastore.createQuery(entityClass);     	// create the generic query

			// add the filter for the constraint 
			if (constraintStrs != null && constraintValues != null && constraintStrs.length == constraintValues.length) {
				for (int i=0; i<constraintStrs.length; i++) {
					query.filter(constraintStrs[i], constraintValues[i]);
				}
			}

			// add the filter for the ordering if not null
			if (orderTerm != null) {
				query = query.order(orderTerm);
			}

		} catch (Exception e) {
			logger.error("error creating query: ", e);
		}

		return query;		
	}
	
	
	/** 
	 * creates an OR query to the database
	 * @param <T> 				 the class of the db entity
	 * @param entityClass		 class name of the entity that is queried
	 * @param field		 		 the field for the or query, can be null
	 * @param orValues	 		 array of values for the or filter, can be null
	 * @param orderTerm			 value for the ordering, can be null
	 * @param dbName			 name of the database: the collection name is indicated as annotation in the db entity
	 * @return	 				 the query to the database
	 */
	public <T> Query<T> createOrQuery(Class<T> entityClass, String field, Object[] orValues, String orderTerm, String dbName) {
		Query<T> query = null;

		// get the datastore
		Datastore datastore = getDataStore(dbName);

		// retrieve the object list from the db
		try {
			// define the query
			query = datastore.createQuery(entityClass);     	// create the generic query

			// add the or criteria 
			if (field != null && orValues != null) {
				CriteriaContainerImpl[] criteriaList = new CriteriaContainerImpl[orValues.length];
				for (int i=0; i<orValues.length; i++) {
					criteriaList[i] = query.criteria(field).equal(orValues[i]);
				}
				query.or(criteriaList); 			// add the or constraints
			}

			// add the filter for the ordering if not null
			if (orderTerm != null) {
				query = query.order(orderTerm);
			}

		} catch (Exception e) {
			logger.error("error creating query: ", e);
		}

		return query;		
	}


	/** 
	 * creates the query to the database to load all objects from the db
	 * @param <T> 				 class of the db entity
	 * @param entityClass		 class name of the entity that is queried
	 * @param dbName			 name of the database: the collection name is indicated as annotation in the db entity
	 * @return  				 the query to the database
	 */
	public <T> Query<T> createQuery(Class<T> entityClass, String dbName) {
		Query<T> query = null;

		// get the datastore
		Datastore datastore = getDataStore(dbName);

		// retrieve the object list from the db
		try {
			// define the query
			query = datastore.createQuery(entityClass);     	// create the generic query

		} catch (Exception e) {
			logger.error("error creating query: ", e);
		}

		return query;		
	}


	/**
	 * returns the collection name of the passed entity class, the name is read from the annotation
	 * @param entityClass 	class of the entity
	 * @return 				name of the collection
	 */
	public String getCollectionName(Class<?> entityClass) {
		String result = "";
		Entity ann = entityClass.getAnnotation(Entity.class);
		if (ann != null) {
			result = ann.value();
		} else {
			logger.error("annotation from the passed class is null");
		}

		return result;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////
	// 								database operations 									   //
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * saves the passed object to the database
	 * @param entity 	object to save in the database
	 * @param dbName 	name of the database in which the object is saved
	 * @return 			true if the entity was successfully saved, false if an error occurred
	 */
	public boolean saveToDB(EntityBase entity, String dbName) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);
		try {
			// save the object in the database, the name of the collection is saved as annotation in the entity object
			datastore.save(entity);
			return true;
			
		} catch (Exception e) {
			logger.error("error saving the document to the db: ", e);
			return false;
		}
	}


	/**
	 * saves the passed object to the database
	 * @param entityList 	list of object to save in the database
	 * @param dbName 		name of the database in which the object is saved
	 * @return 				true if the collections were successfully saved, false if an error occurred
	 */
	public boolean saveToDB(Iterable<?> entityList, String dbName) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);

		// save the object in the database, the name of the collection is saved as annotation in the entity object
		try {
			datastore.save(entityList);
			return true;
		
		} catch (Exception e) {
			logger.error("error saving the document to the db: ", e);
			return false;
		}
	}
	
	


	/**
	 * returns the list of database entities resulting from the passed query
	 * @param <T> 		class of the db entity
	 * @param query 	database query
	 * @return  		list of database entities 
	 */
	public <T> List<T> getListFromQuery(Query<T> query) {
		List<T> list = new ArrayList<>();

		try {
			list = query.asList();

		} catch (Exception e) {
			logger.error("error getting the list from the query: ",e );
		}

		return list;			
	}
	
	
	/**
	 * returns the list of database entities resulting from the passed query
	 * @param <T> 			class of the db entity
	 * @param query 		database query
	 * @param findOptions 	additional options for the query	
	 * @return  			list of database entities 
	 */
	public <T> List<T> getListFromQuery(Query<T> query, FindOptions findOptions) {
		List<T> list = new ArrayList<>();

		try {
			list = query.asList(findOptions);

		} catch (Exception e) {
			logger.error("error getting the list from the query: ",e );
		}

		return list;			
	}


	/**
	 * updates an entity in the database	
	 * @param <T> 				class of the db entity
	 * @param fieldToUpdate 	the field that should be updated
	 * @param newValue  		the new value for the field
	 * @param dbName 			the name of the database
	 * @param query 			query that defines the entities to update
	 * @return 					true if the update was successfully, false if an error occurred
	 */
	public <T> boolean updateEntityInDB(String fieldToUpdate, Object newValue, String dbName, Query<T> query) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);

		try {	
			UpdateOperations<T> updateOperations = datastore.createUpdateOperations(query.getEntityClass()).set(fieldToUpdate, newValue);
			datastore.update(query, updateOperations);
			return true;

		} catch (Exception e) {
			logger.error("error updating entities in the database: ", e);
			return false;
		}
	}


	/**
	 * updates an entity in the database, fieldsToUpdate and newValues need to have the same length	
	 * @param <T> 				class of the db entity
	 * @param fieldsToUpdate 	the field that should be updated
	 * @param newValues 		the new value for the field
	 * @param dbName 			the name of the database
	 * @param query 			query that defines the entities to update
	 * @return 					true if the update was successful, false if an error occurred
	 */
	public <T> boolean updateEntityInDB(String[] fieldsToUpdate, Object[] newValues, String dbName, Query<T> query) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);

		try {
			if (fieldsToUpdate.length == newValues.length) {
				UpdateOperations<T> updateOperations = datastore.createUpdateOperations(query.getEntityClass());

				// define all field to set
				for (int i=0; i<fieldsToUpdate.length; i++) {
					updateOperations.set(fieldsToUpdate[i], newValues[i]);
				}

				// update in the database
				datastore.update(query, updateOperations);

			} else {
				logger.error("failed to update the entity in the database: fieldsToUpdate and new Values have not the same length");
			}
			
			return true;

		} catch (Exception e) {
			logger.error("error updating entities in the database: ", e);
			return false;
		}
	}


	/**
	 * deletes the from the database
	 * @param <T> 		class of the db entity
	 * @param dbName 	name of the database
	 * @param query 	query that defines the entities to delete
	 * @return 			true if the delete operation was successful, false if an error occurred
	 */
	public <T> boolean deleteFromDB(String dbName, Query<T> query) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);

		try {
			datastore.delete(query);
			return true;

		} catch (Exception e) {
			logger.error("error deleting from database");
			return false;
		}
	}



	/**
	 * drops the whole collection in the db
	 * @param entityClass 	the class of the db entity for which the whole collection should be dropped
	 * @param dbName 		the name of the database
	 * @return 				true if the collection was successfully dropped, false otherwise
	 */
	public boolean dropCollection(Class<?> entityClass, String dbName) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);

		try {
			String collectionName = getCollectionName(entityClass);
			datastore.getDB().getCollection(collectionName).drop();
			return true;

		} catch (Exception e) {
			logger.error("error dropping the collection");
			return false;
		}
	}



	/**
	 * deletes all collections in the database, typically you don't want to call this method,
	 * only in the test phase
	 * @param dbName 	name of the database
	 * @return 			true if the database was successfully dropped, false if an error occurred
	 */
	public boolean dropDatabase(String dbName) {
		// get the datastore
		Datastore datastore = getDataStore(dbName);

		// deletes all collections iin the database
		try {
			logger.info("dropping all collections in: " + dbName);
			datastore.getDB().dropDatabase(); 	
			return true;
			
		} catch (Exception e) {
			logger.error("error deleting all collections in the database: ", e);
			return false;
		}
	}
}


