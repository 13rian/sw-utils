package ch.wenkst.sw_utils.db.mongodb;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

public class DbConnectOptions {
	private String host;							// host of the db server
	private int port = -1;							// port of the db server
	private int timeout = 10;						// connect timeout in seconds
	private String username;						// username, if the db is authenticated
	private String password;						// password, if the db is authenticated
	private String dbName;							// the name of the db to connect to
	private String[] packageNames = new String[0];	// the package names of the db entities
	private String connectString; 					// defines the connect string for mongodb
	
	private CodecRegistry pojoCodecRegistry;		// codes to read and write pojos to the db
	
	private CompletableFuture<Throwable> connectionFuture; 	// future that is completed when the connection is established
	
	
	/**
	 * creates the connect string to connect to mongodb
	 * @return 				the connect string which can be used to open the connection to the db
	 * @throws DbConnectException 
	 */
	protected String createConnectString() throws DbConnectException {
		if (host == null || port < 0) {
			throw new DbConnectException("host or port are not specified in the connect options");
		}
		
		// create the connect string from the values in the configuration file
		StringBuilder sb = new StringBuilder();
		sb.append("mongodb://");
		if (username != null && password != null) {
			sb.append(username).append(":");
			sb.append(password).append("@");
		}
		sb.append(host).append(":").append(port);
		connectString = sb.toString();
		return connectString;
	}
	
	
	/**
	 * creates the code registry that is needed to insert and read pojos from mongodb
	 * before using the driver with java objects a CodecRegistry needs to be configured. This includes codecs that
	 * handle the translation to and form bson for the java objects. 
	 * This combines the default codec registry, with the PojoCodecProvider configured to automatically create PojoCodecs
	 * @return 		the codec registry that is needed for pojo
	 */
	protected CodecRegistry createCodecRegistry() {
		CodecProvider provider;
		if (packageNames == null || packageNames.length == 0) {
			// use the default codec provider
			provider = PojoCodecProvider.builder().automatic(true).build();

		} else {
			// use the registered db models of the passed packages. initially the codec uses reflection but later
			// the setter and getters are used if the exist
			provider = PojoCodecProvider.builder().register(packageNames).build();
		}
		
		pojoCodecRegistry = CodecRegistries.fromRegistries(
				CodecRegistries.fromProviders(provider),
				MongoClients.getDefaultCodecRegistry());
		
		return pojoCodecRegistry;
	}
	
	
	/**
	 * creates the mongo client
	 * @return
	 */
	protected MongoClient createMongClient() {
		createCodecRegistry();


		// create the listener for the mongo server description
		connectionFuture = new CompletableFuture<>();
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

		return MongoClients.create(settings);
	}
	
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public String[] getPackageNames() {
		return packageNames;
	}
	
	public void setPackageNames(String[] packageNames) {
		this.packageNames = packageNames;
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public CodecRegistry getPojoCodecRegistry() {
		return pojoCodecRegistry;
	}

	public CompletableFuture<Throwable> getConnectionFuture() {
		return connectionFuture;
	}
}
