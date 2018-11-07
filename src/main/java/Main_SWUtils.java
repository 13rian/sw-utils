import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.logging.Log;

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


		
		logger.info("end of main test routine reached");
	}
	
	
	
}
