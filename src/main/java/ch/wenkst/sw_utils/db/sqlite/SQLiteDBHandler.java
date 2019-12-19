package ch.wenkst.sw_utils.db.sqlite;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles the connections to all needed dbs
 */
public class SQLiteDBHandler {
	private static final Logger logger = LoggerFactory.getLogger(SQLiteDBHandler.class);
	
	private static SQLiteDBHandler instance = null; 	// instance for the singleton access
	
	// holds the sql-handlers with the connection to the db
	private ConcurrentHashMap<String, SQLiteConnector> dbMap = null;
	
	
	/**
	 * handles the sql interface to the sqlite files
	 */
	protected SQLiteDBHandler() {
		dbMap = new ConcurrentHashMap<>();
	}
	
	
	/**
	 * returns the instance of the dbHandler
	 * @return
	 */
	public static SQLiteDBHandler getInstance() {
		if (instance == null) {
			instance = new SQLiteDBHandler();
		}	      
		return instance;
	}
	
	
	/**
	 * opens a new connection to a db
	 * @param dbPath 	path to the db to connect to
	 * @return 			true if successfully connected, false if an error occurred
	 */
	public boolean openConnection(String dbPath) {
		// check if the db is already connected
		if (dbMap.containsKey(dbPath)) {
			logger.debug(dbPath + ": db is already connected");
			return true;
		}
		
		// create a new connection to the db
		SQLiteConnector sqlHandler = new SQLiteConnector();
		boolean connected = sqlHandler.connect(dbPath);
		if (!connected) {
			return false;
		}
		
		// add the db connection to the db map
		dbMap.put(dbPath, sqlHandler);
		return true;
	}
	
	
	/**
	 * closes a connection to the db
	 * @param dbPath 	path to the db to which the connection is closed
	 */
	public void closeConnection(String dbPath) {
		if (!dbMap.containsKey(dbPath)) {
			logger.debug(dbPath + ": connection is already closed");
			return;
		}
		
		SQLiteConnector sqlHandler = dbMap.get(dbPath);
		sqlHandler.disconnect();
		logger.info("successfully disconnected form the db: " + dbPath);
	}
	
	
	/**
	 * performs a read operation on one db
	 * @param dbPath 		 	the path of the db file
	 * @param dbOperation 	 	the callback that defines the read operation
	 */
	public void readOperation(String dbPath, IDBOperation dbOperation) {
		SQLiteConnector sqlHandler = dbMap.get(dbPath);
		if (sqlHandler == null) {
			logger.error("db with path: " + dbPath + " not found in the db-map");
		}
		dbOperation.executeDBOperation(sqlHandler);
	}
	
	
	/**
	 * performs a write operation on the db, only one thread at a time is allowed to write to the same db
	 * @param dbPath 			the path of the db file
	 * @param dbOperation 		the callback that defines the read operation
	 */
	public void writeOperation(String dbPath, IDBOperation dbOperation) {
		SQLiteConnector sqlHandler = dbMap.get(dbPath);
		if (sqlHandler == null) {
			logger.error("db with path: " + dbPath + " not found in the db-map");
			return;
		}
		
		// allow write operations only for one thread at a time
		synchronized (sqlHandler) {
			dbOperation.executeDBOperation(sqlHandler);
		}
	}
}
