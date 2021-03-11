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
	private String host;
	private int port = 27017;
	private int connectTimeoutInSecs = 30;
	private String username;
	private String password;
	private String dbName;
	private String[] packageNames = new String[0];
	private String connectString;
	
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
//		sb.append("/?retryWrites=false");
		connectString = sb.toString();
		return connectString;
	}
	
	
	/**
	 * creates the mongo client
	 * @return
	 */
	protected MongoClient createMongClient() {
		createCodecRegistry();
		MongoStatusListener statusListener = createStatusListener();	
		MongoClientSettings settings = createMongoClientSettings(statusListener);
		return MongoClients.create(settings);
	}
	
	
	/**
	 * creates the code registry that is needed to insert and read pojos from mongodb
	 * before using the driver with java objects a CodecRegistry needs to be configured. This includes codecs that
	 * handle the translation to and form bson for the java objects. 
	 * This combines the default codec registry, with the PojoCodecProvider configured to automatically create PojoCodecs
	 * @return 		the codec registry that is needed for pojo
	 */
	private CodecRegistry createCodecRegistry() {
		CodecProvider provider = createcodecProvider();
				
		pojoCodecRegistry = CodecRegistries.fromRegistries(
				CodecRegistries.fromProviders(provider),
				MongoClients.getDefaultCodecRegistry());
		
		return pojoCodecRegistry;
	}
	
	
	private CodecProvider createcodecProvider() {
		if (!packagesSet()) {
			return PojoCodecProvider.builder().automatic(true).build();

		} else {
			// use the registered db models of the passed packages. initially the codec uses reflection but later
			// the setter and getters are used if the exist
			return PojoCodecProvider.builder().register(packageNames).build();
		}
	}
	
	
	private boolean packagesSet() {
		return packageNames != null && packageNames.length != 0;
	}
	
	
	private MongoStatusListener createStatusListener() {
		connectionFuture = new CompletableFuture<>();
		return new MongoStatusListener(connectionFuture);
	}
	
	
	private MongoClientSettings createMongoClientSettings(MongoStatusListener statusListener) {
		return MongoClientSettings.builder()
				.codecRegistry(pojoCodecRegistry)
				.applyConnectionString(new ConnectionString(connectString))
				//                .applyToClusterSettings(builder -> {
				//	                builder.hosts(hosts); 		
				//	             })
				.applyToSocketSettings(builder -> {
					builder.connectTimeout(connectTimeoutInSecs, TimeUnit.SECONDS);
				})
				.applyToServerSettings(builder -> {
					builder.addServerListener(statusListener);
				})
				.build();
	}
	
	
	public DbConnectOptions host(String host) {
		this.host = host;
		return this;
	}
	
	public DbConnectOptions port(int port) {
		this.port = port;
		return this;
	}
	
	public DbConnectOptions connectTimeoutInSecs(int connectTimeoutInSecs) {
		this.connectTimeoutInSecs = connectTimeoutInSecs;
		return this;
	}
	
	public DbConnectOptions username(String username) {
		this.username = username;
		return this;
	}
	
	public DbConnectOptions password(String password) {
		this.password = password;
		return this;
	}
	
	public DbConnectOptions dbName(String dbName) {
		this.dbName = dbName;
		return this;
	}
	
	public DbConnectOptions packageNames(String[] packageNames) {
		this.packageNames = packageNames;
		return this;
	}
	
	public DbConnectOptions connectString(String connectString) {
		this.connectString = connectString;
		return this;
	}
	
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getConnectTimeoutInSecs() {
		return connectTimeoutInSecs;
	}
	
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	public String[] getPackageNames() {
		return packageNames;
	}

	public String getConnectString() {
		return connectString;
	}

	public CodecRegistry getPojoCodecRegistry() {
		return pojoCodecRegistry;
	}

	public CompletableFuture<Throwable> getConnectionFuture() {
		return connectionFuture;
	}
}
