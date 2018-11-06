import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.event.managers.AsyncEventManager;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.logging.Log;
import ch.wenkst.sw_utils.tests.events.Event;
import ch.wenkst.sw_utils.tests.events.Listener;

public class Main_SWUtils {
//	// define a class initializer that is executed before any other properties and classes are loaded (since it is the 
//	// initializer for the main program). It sets the needed system property for the logger
//	// no need to set the JVM arguments anymore in the Debug Configurations
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Main_SWUtils.class);

	public static void main(String[] args) {
		/////////////////////////////////////////////////////////////////////////////////////////////
		// 										test the logger 								   //
		/////////////////////////////////////////////////////////////////////////////////////////////
		System.err.println("gg");
		Log log = Log.getLogger(Main_SWUtils.class);
		log.config("config log");
		log.info("info log");
		log.severe("severe test");
	
		String loggerConfig = "resource" + File.separator + "log" + File.separator + "log_config.properties";
		Log.initLogger(loggerConfig);
		log.config("config log");
		log.info("info log");
		log.severe("severe test");
		
		
		
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
		

		
		// wait and shutdown the thread-pool
		Utils.sleep(3500);
		threadPool.shutdown();

		
		logger.info("end of main test routine reached");
	}
	
	
	
}
