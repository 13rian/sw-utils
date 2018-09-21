import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.event.EventBoard;
import ch.wenkst.sw_utils.event.managers.AsyncEventManager;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.http.builder.HttpRequestBuilder;
import ch.wenkst.sw_utils.http.builder.HttpResponseBuilder;
import ch.wenkst.sw_utils.http.parser.HttpRequestParser;
import ch.wenkst.sw_utils.http.parser.HttpResponseParser;
import ch.wenkst.sw_utils.scheduler.Scheduler;
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
	
	
	private static final Logger logger = LoggerFactory.getLogger(Main_SWUtils.class);

	public static void main(String[] args) {
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// make sure to use the right file in lib/security (as described in the folder file_for_ecyption) 		   	   //
		// Java 9: Security.setProperty("crypto.policy", "unlimited"); for the same effect 						   	   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 										test Security Utils									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n SECURITY UTILS"); 
		
		// certificate handling method
		String certDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "certs";
		String keyPath = FileUtils.findFileByPattern(certDir, "TLS", "pem");
		String certPath = FileUtils.findFileByPattern(certDir, "TLS", "cer");
		
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
