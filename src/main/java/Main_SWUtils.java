import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.query.Query;
import com.mongodb.ServerAddress;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.db.DBHandler;
import ch.wenkst.sw_utils.db.EntityBase;
import ch.wenkst.sw_utils.event.EventBoard;
import ch.wenkst.sw_utils.event.managers.AsyncEventManager;
import ch.wenkst.sw_utils.file.FileHandler;
import ch.wenkst.sw_utils.http.builder.HttpRequestBuilder;
import ch.wenkst.sw_utils.http.builder.HttpResponseBuilder;
import ch.wenkst.sw_utils.http.parser.HttpRequestParser;
import ch.wenkst.sw_utils.http.parser.HttpResponseParser;
import ch.wenkst.sw_utils.scheduler.Scheduler;
import ch.wenkst.sw_utils.tests.db.Car;
import ch.wenkst.sw_utils.tests.events.Event;
import ch.wenkst.sw_utils.tests.events.Listener;
import ch.wenkst.sw_utils.tests.scheduler.PrintTask;

public class Main_SWUtils {
//	// define a class initializer that is executed before any other properties and classes are loaded (since it is the 
//	// initializer for the main program). It sets the needed system property for the logger
//	// no need to set the JVM arguments anymore in the Debug Configurations
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
	
	
	final static Logger logger = LogManager.getLogger(Main_SWUtils.class);    // initialize the logger

	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) {
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// make sure to use the right file in lib/security (as described in the folder file_for_ecyption) 		   	   //
		// Java 9: Security.setProperty("crypto.policy", "unlimited"); for the same effect 						   	   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	
		Number test = 0.000000000001;
		System.out.println(test.doubleValue() == 0);
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 										test Security Utils									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n SECURITY UTILS"); 
		
		// certificate handling method
		String certDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "certs";
		String keyPath = FileHandler.findFileByPattern(certDir, "TLS", "pem");
		String certPath = FileHandler.findFileByPattern(certDir, "TLS", "cer");
		
		// load the key in der (tested, it is working)
		String keyDER = CryptoUtils.loadDERPrivateKey(keyPath);
		System.out.println(keyDER);
		
		// load the cert in der format (tested it is working)
		String certDER = CryptoUtils.loadDERCertificate(certPath);
		System.out.println(certDER);
		
		
		
		// print all registered providers
		logger.info(CryptoUtils.getRegisteredProviders());
		
		// print the default providers
		logger.info(CryptoUtils.getDefaultProviders());
		
		

		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										FileHandler									 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n FILE HANDLER TEST");

		// read the last few lines of a file
		String filePath = Utils.getWorkDir() + File.separator + "resource" + File.separator + "xmlWriteTest.xml";
		// path, number of lines, nCharacters to read in each iteration (should be at least as big as the largest expected line)
		ArrayList<String> lines = FileHandler.readLastLines(filePath, 5);
		logger.info("extracted lines: \n" + lines); 
		

		// get the content of a file as String
		String filePath2 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile.txt";
		logger.info("read from file: \n" + FileHandler.readStrFromFile(filePath2));

		// write a String to a file
		String filePath3 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile" + File.separator + "newFile.txt";
		logger.info("new file written?: " + FileHandler.writeStrToFile(filePath3, "test content written by Java8"));

		// copy a file
		String fileToCopy = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile.txt";
		String dest = Utils.getWorkDir() + File.separator + "resource" + File.separator + "copyDir" + File.separator + "testFile.txt";
		logger.info("file copied?: " + FileHandler.copyFile(fileToCopy, dest, true));

		// move a file
		// create a test file to rename and to move
		String fileToCopy2 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile.txt";
		String dest2 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileRename.txt";
		logger.info("created testFile2.txt to remove?: " + FileHandler.moveFile(fileToCopy2, dest2, true));
		
		// rename the file
		String fileToRename = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileRename.txt";
		String renameFile = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileMove.txt";
		logger.info("file renamed?: " + FileHandler.moveFile(fileToRename, renameFile, true));
		
		// copy the file again in order to use it the next time
		FileHandler.copyFile(renameFile, fileToCopy, true);
		
		// move the file
		String fileToMove = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileMove.txt";
		String moveDest = Utils.getWorkDir() + File.separator + "resource" + File.separator + "moveDir" + File.separator + "testFileMove.txt";
		logger.info("file moved?: " + FileHandler.moveFile(fileToMove, moveDest, true));
		
		// delete a file that does not exist
		logger.info("file deleted?: " + FileHandler.deleteFile("testi"));
		
		// delete a file that does exist
		logger.info("file deleted?: " + FileHandler.deleteFile(dest));
		logger.info("file deleted?: " + FileHandler.deleteFile(moveDest));
		
		
		// recursively copy a directory
		String dirToCopy = Utils.getWorkDir() + File.separator + "resource" + File.separator + "dirToCopy";
		String copiedDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "copiedDir";
		logger.info("directory recursively copied: " + FileHandler.copyDir(dirToCopy, copiedDir, FileHandler.MERGE_NO_REPLACE));
		
		
		// recursively delete a directory
		logger.info("directory recursively deleted: " + FileHandler.deleteDir(copiedDir));
		
		

		// get all files from a directory
		String dir = Utils.getWorkDir() + File.separator + "resource" + File.separator;
		logger.info("Number of files in " + dir + ": " + FileHandler.getFilesFromDir(dir).length);
		
		// read the last line of a file
		String filePath4 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "xmlReadTest.xml";
		logger.info("first line of the file: " + FileHandler.readFirstLine(filePath4));
		
		// read the nth line number of a file
		logger.info("23th line of the file: " + FileHandler.readNthLine(filePath4, 23));

		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 							test the http builder and parser								   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n HTTP TEST");
		
		// test the http request builder
		logger.info("test the http request builder");
		HttpRequestBuilder reqBuilder = new HttpRequestBuilder();
		
		reqBuilder.preparePost("https://i-api.eon.de/gwa/ClsManagementAdapter_In/v1/")
		.setHeaderProperty("Content-Type", "text/plain")
		.setHeaderProperty("Authorization", "Some secret code")
		.setBody("This is the html request body\n with no deep meaning");
		
		logger.info("http request: \n" + reqBuilder.toString());
		
		
		
		// test the http response builder
		logger.info("test the http response builder");
		HttpResponseBuilder respBuilder = new HttpResponseBuilder();

		respBuilder.status(200)
		.setHeaderProperty("Content-Type", "text/plain")
		.setHeaderProperty("Authorization", "Some secret code")
		.setBody("Some http response body \n with no deep meaning");

		logger.info("http response: \n" + respBuilder.toString());
		
		
		
		// test the http request parser
		logger.info("test the http request parser");
		HttpRequestParser reqParser = new HttpRequestParser();
		
		// feed chunk 1
		String requestChunk =  
				"POST /test HTTP/1.1\n" +
				"Authorization: Bearer 2dd4f332cad2705bb89a209e407bc636\n" +
				"Host: localhost:8001\n";
		
		
		reqParser.addData(requestChunk.getBytes(StandardCharsets.US_ASCII));
		logger.info("successfully parsed: " + reqParser.isComplete());
		
		// feed chunk 2
		requestChunk = 
				"User-Agent: AHC/1.0\n" +
				"Connection: keep-alive\n" +
				"Accept: */*\n" +
				"Content-Type: text/xml\n" +
				"Content-Length: 17\n" +
				"\n" +
				"aergf.qa";
		reqParser.addData(requestChunk.getBytes(StandardCharsets.US_ASCII));
		logger.info("successfully parsed: " + reqParser.isComplete());
		
		// feed chunk 3
		requestChunk = "e4t.435zg";
		reqParser.addData(requestChunk.getBytes(StandardCharsets.US_ASCII));
		logger.info("successfully parsed: " + reqParser.isComplete());
		
		
		logger.info("http-method: " + reqParser.getHttpMethod());
		logger.info("status-txt: " + reqParser.getRequestURI());
		logger.info("body: " + reqParser.getBodyStr());
		
		
		
		// test the http response parser
		logger.info("test the http response parser");
		logger.info("normal http response");
		HttpResponseParser respParser = new HttpResponseParser();
		String crlf = "\r\n";
		
		// test a normal http response
		String httpResponse = 
				"HTTP/1.1 200 OK\n" +
				"Access-Control-Allow-Origin: *\n" +
				"Access-Control-Allow-Headers: origin, content-type, accept, authorization\n" +
				"Access-Control-Allow-Credentials: true\n" +
				"Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD\n" +
				"Content-Type: text/plain\n" +
				"Content-Length: 37\n" +
				"\n" +
				"{\"params\":\"d81\",\"command\":\"testpost\"}";

		respParser.addData(httpResponse.getBytes(StandardCharsets.US_ASCII));

		logger.info("status: " + respParser.getStatus());
		logger.info("status-txt: " + respParser.getStatusTxt());
		logger.info("body: " + respParser.getBodyStr());

		
		// test a http response without a content length (the server closes the connection to indicate that the response is complete)
		logger.info("http without a content length");
		String httpResponse2 = 
				"HTTP/1.1 200 OK\n" +
				"Access-Control-Allow-Origin: *\n" +
				"Access-Control-Allow-Headers: origin, content-type, accept, authorization\n" +
				"Access-Control-Allow-Credentials: true\n" +
				"Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD\n" +
				"Content-Type: text/plain\n" +
				"\n" +
				"{\"params\":\"d81\",\"command\":\"testpost\"}";

		respParser.clearAfterFullMessage();
		respParser.addData(httpResponse2.getBytes(StandardCharsets.US_ASCII));
		respParser.fullMessageReceived();

		logger.info("status: " + respParser.getStatus());
		logger.info("status-txt: " + respParser.getStatusTxt());
		logger.info("body: " + respParser.getBodyStr());
		
		
		// test a chunked http response
		logger.info("chunked http response");
		String httpResponse3 = 
				"HTTP/1.1 200 OK" + crlf +
				"Access-Control-Allow-Origin: *" + crlf +
				"Access-Control-Allow-Headers: origin, content-type, accept, authorization" + crlf +
				"Access-Control-Allow-Credentials: true" + crlf +
				"Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD" + crlf +
				"Transfer-Encoding: chunked" + crlf +
				"Content-Type: text/plain" + crlf +
				crlf +
				"3" + crlf +
				"lol" + crlf +
				"3" + crlf + 
				"man" + crlf + 
				"0"
				+ crlf;

		respParser.clearAfterFullMessage();
		respParser.addData(httpResponse3.getBytes(StandardCharsets.US_ASCII));
		

		logger.info("status: " + respParser.getStatus());
		logger.info("status-txt: " + respParser.getStatusTxt());
		logger.info("body: " + respParser.getBodyStr());
		
		
	
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 						test scheduler									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n SCHEDULER TEST");
		
		
		
		// start the thread pool
		int nThreads = 10;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(nThreads); 
		
		// create the scheduler with a poll interval of 100ms
		Scheduler scheduler = new Scheduler(100, executor);
		scheduler.start();
		
		// create two tasks and schedule them into the future
		PrintTask printTask1 = new PrintTask(System.currentTimeMillis() + 2000, "PrintTask_1");
		PrintTask printTask2 = new PrintTask(System.currentTimeMillis() + 1000, "PrintTask_2");
		
		logger.info("schedule tasks");
		scheduler.addToTasks(printTask1);
		scheduler.addToTasks(printTask2);
		
		
		// stop the scheduler
		scheduler.stopScheduler();
		
		// shutdown the executor
		logger.info("shutdown executor");
		executor.shutdown();
		

		
		
		

		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										MongoDB										 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////	
		System.out.println("\n MONGO DB TEST");
		
		// note: to make the query most effective always query the one that restricts the results the most first

		// define the parameters to connect to the mongoDB, the name of the collection is indicated as annotation in the entity Car
		String mongoHost = "192.168.1.183";
		int mongoPort = 27017;
		String dbName = "test_db";

		// test the connection
		ArrayList<ServerAddress> hosts = new ArrayList<>();
		hosts.add(new ServerAddress(mongoHost, mongoPort));
		// List<ServerAddress> hosts = Arrays.asList(new ServerAddress(mongoHost, mongoPort));
		boolean isReachable = DBHandler.testMongoDBConnection(hosts, 5000);
		logger.info("connectin to mongoDB? " + isReachable);

		if (isReachable) {

			// connect to the database
			DBHandler.connectToDB(mongoHost, mongoPort);

			// delete all collections in the database (use only here for test cases!!!)
			DBHandler.dropDatabase(dbName);

			// create objects to save in the database
			Car ford = new Car("Ford", 1600);
			Car ford2 = new Car("Ford", 2000);
			Car audi = new Car("Audi", 2500);
			Car vw = new Car("VW", 2500);
			Car mercedes = new Car("Mercedes", 1800);

			// save objects o the database
			DBHandler.saveToDB(ford, dbName);
			DBHandler.saveToDB(ford2, dbName);
			DBHandler.saveToDB(audi, dbName);
			DBHandler.saveToDB(vw, dbName);
			DBHandler.saveToDB(mercedes, dbName);

			// retrieve entities from the database (single constraint)
			Query<?> weightQuery = DBHandler.createFilterQuery(Car.class, "weight <=", 1900, null, dbName);
			List<?> lightCars = DBHandler.getListFromQuery(weightQuery);
			for (Object item: lightCars) {
				Car car = (Car)item;
				logger.info("low weight: " + car.getName());
			}

			// retrieve entities from the database (multiple constraints)
			String[] constraints = {"weight <=", "name ="};
			Object[] constraintVals = {1900, "Ford"};
			Query<?> weightNameQuery = DBHandler.createAndQuery(Car.class, constraints, constraintVals, null, dbName);
			List<?> lightFordCars = DBHandler.getListFromQuery(weightNameQuery);
			for (Object item: lightFordCars) {
				Car car = (Car)item;
				logger.info("light Ford car: " + car.getName());
			}

			// update value
			Query<?> updateValueQuery = DBHandler.createFilterQuery(Car.class, "weight =", 2500, null, dbName);
			Query<EntityBase> updateValueQueryBE = (Query<EntityBase>)updateValueQuery; 						// query needs to be casted
			DBHandler.updateEntityInDB("weight", 3000, dbName, updateValueQueryBE);


			// update values
			Query<?> updateValuesQuery = DBHandler.createFilterQuery(Car.class, "weight =", 3000, null, dbName);
			Query<EntityBase> updateValuesQueryBE = (Query<EntityBase>)updateValuesQuery; 						// query needs to be casted
			String[] fields = {"name", "weight"};
			Object[] values = {"Tesla", 2900};
			DBHandler.updateEntityInDB(fields, values, dbName, updateValuesQueryBE);


			// delete objects from the database
			Query<?> deleteEntityQuery = DBHandler.createFilterQuery(Car.class, "name =", "Tesla", null, dbName);
			Query<EntityBase> deleteEntityQueryBE = (Query<EntityBase>)deleteEntityQuery; 										// query needs to be casted
			DBHandler.deleteFromDB(dbName, deleteEntityQueryBE);

			
			// delete all collections in the database (use only here for test cases!!!)
			DBHandler.dropDatabase(dbName);
			

			// disconnect from the database
			DBHandler.disconnectFromDB();
		} else {
			System.out.println();
		}
		
		

		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										EventFramework								 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////	
		System.out.println("\n EVENT FRAMEWORK TEST");

		
		// asynchronous events (without the event board) //
		///////////////////////////////////////////////////
		
		// create the thread pool needed to send the asynchronous events
		logger.info("start the thread pool");
		int corePooSize = 10;
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	    threadPool.setCorePoolSize(corePooSize); 
		
		// create two EventManager to send two events
		AsyncEventManager asyncEventManager1 = new AsyncEventManager("new Email", threadPool);
		AsyncEventManager asyncEventManager2 = new AsyncEventManager("new SMSss", threadPool);
		
		// create the listeners and register it for the 2 events defined in the event board
		Listener listener1 = new Listener("listener1", 2000);
		Listener listener2 = new Listener("listener2", 3000);
		asyncEventManager1.register(listener1);
		asyncEventManager1.register(listener2);
		asyncEventManager2.register(listener1);
		asyncEventManager2.register(listener2);

		// fire the 4 events
		System.out.println("async without event board");
		// two different events will be processed asynchronously
		asyncEventManager1.fire(new Event("mail", "mail 1 received"));   
		asyncEventManager2.fire(new Event("sms", "sms 1 received")); 
		Utils.sleep(20);
		
		// fire the same events again before the handleEvent method has finished, this will buffer the events
		// until the first ones are finished
		asyncEventManager1.fire(new Event("mail", "mail 2 received"));
		asyncEventManager2.fire(new Event("sms", "sms 2 received"));
		
		// wait
		Utils.sleep(8000);
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// synchronous events with event board //
		/////////////////////////////////////////
		
		EventBoard eventBoard = new EventBoard();	
		
		// create the listeners and register them
		listener1 = new Listener("listener1", 500);
		listener2 = new Listener("listener2", 300);
		eventBoard.registerListerner(listener1, "new Email");
		eventBoard.registerListerner(listener1, "new SMSss");
		eventBoard.registerListerner(listener2, "new Email");
		eventBoard.registerListerner(listener2, "new SMSss");
		
		// fire 4 events
		System.out.println("sync with event board");
		eventBoard.fireEvent("new Email", new Event("mail", "mail 1 received"));
		eventBoard.fireEvent("new Email", new Event("mail", "mail 2 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 1 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 2 received"));
		
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// same events synchronous, different events asynchronous with event board //
		/////////////////////////////////////////////////////////////////////////////
		
		eventBoard = new EventBoard(threadPool, false);	
		
		// create the listeners and register them
		listener1 = new Listener("listener1", 2000);
		listener2 = new Listener("listener2", 3000);
		eventBoard.registerListerner(listener1, "new Email");
		eventBoard.registerListerner(listener1, "new SMSss");
		eventBoard.registerListerner(listener2, "new Email");
		eventBoard.registerListerner(listener2, "new SMSss");
		
		// fire 4 events
		System.out.println("same events sync, different events async with event board");
		eventBoard.fireEvent("new Email", new Event("mail", "mail 1 received"));
		eventBoard.fireEvent("new Email", new Event("mail", "mail 2 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 1 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 2 received"));
		
		// wait
		Utils.sleep(8000);
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// asynchronous events with event board //
		//////////////////////////////////////////
		
		eventBoard = new EventBoard(threadPool, true);	
		
		// create the listeners and register them
		listener1 = new Listener("listener1", 2000);
		listener2 = new Listener("listener2", 3000);
		eventBoard.registerListerner(listener1, "new Email");
		eventBoard.registerListerner(listener1, "new SMSss");
		eventBoard.registerListerner(listener2, "new Email");
		eventBoard.registerListerner(listener2, "new SMSss");
		
		// fire 4 events
		System.out.println("async with event board");
		eventBoard.fireEvent("new Email", new Event("mail", "mail 1 received"));
		eventBoard.fireEvent("new Email", new Event("mail", "mail 2 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 1 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 2 received"));

		
		// wait and shutdown the thread-pool
		Utils.sleep(3500);
		threadPool.shutdown();

		
		logger.info("end of main test routine reached");
	}
	
	
	


}
