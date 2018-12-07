import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
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
		
		CryptoUtils.registerBC();
		
		String sep = File.separator;
		String certDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep + "certs" + sep;
		
		// cert
		String pemCertPath = certDir + "server.cert.pem";
		String derCertPath = certDir + "server.cert.cer";
		
		// load the object from the file
		X509Certificate cert1 = CryptoUtils.certFromFile(pemCertPath);
		X509Certificate cert2 = CryptoUtils.certFromFile(derCertPath);
		
		// load the b64 encoded der certificate
		String b64Cert1 = CryptoUtils.derFromCertFile(pemCertPath, CryptoUtils.FORMAT_PEM);
		String b64Cert2 = CryptoUtils.derFromCertFile(derCertPath, CryptoUtils.FORMAT_DER);
		
		X509Certificate cert11 = CryptoUtils.certFromDer(Conversion.base64StrToByteArray(b64Cert1));
		X509Certificate cert22 = CryptoUtils.certFromDer(Conversion.base64StrToByteArray(b64Cert2));
		
		
		
		
		
		// key
		String pemKeyPath = certDir + "server.key.pem";
		String derKeyPath = certDir + "server.key.der";
		

		// load the key object from the file
		PrivateKey key1 = CryptoUtils.keyFromFile(pemKeyPath, CryptoUtils.FORMAT_PEM);
		PrivateKey key2 = CryptoUtils.keyFromFile(derKeyPath, CryptoUtils.FORMAT_DER);
		
		
		
		
		// load the b64 encoded der key
		String b64Key1 = CryptoUtils.derFromKeyFile(pemKeyPath, CryptoUtils.FORMAT_PEM);
		String b64Key2 = CryptoUtils.derFromKeyFile(derKeyPath, CryptoUtils.FORMAT_DER);
		
		PrivateKey pk11 = CryptoUtils.keyFromDer(Conversion.base64StrToByteArray(b64Key1));
		PrivateKey pk22 = CryptoUtils.keyFromDer(Conversion.base64StrToByteArray(b64Key2));
		
		
		
		// print all registered providers
		logger.info(CryptoUtils.getRegisteredProviders());
		
		// print the default providers
		logger.info(CryptoUtils.getDefaultProviders());
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											utils 													   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////
		// startup message
		Utils.logStartupMessage();


		
		logger.info("end of main test routine reached");
	}
	
	
	
}
