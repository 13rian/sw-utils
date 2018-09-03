package ch.wenkst.sw_utils.crypto.tls.bp;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.wenkst.sw_utils.crypto.tls.TrustManagerTrustAny;

/**
 * generates the SSL context for elliptic curves (brainpool included), in order for this to work the BCJSSE
 * security provider is used.
 * Note: The JCE unlimited strength file needs to be installed
 */
public class SSLContextGenerator_BP {
	private static final Logger logger = LogManager.getLogger(SSLContextGenerator_BP.class);    // initialize the logger


	/**
	 * sets up the sslScontext for a secure connection, can be used for the server and the client
	 * @param keyStorePassword	keyStore password (must be the same as chosen to create the certificate)
	 * @param keyFilePath 		the path to the p12-file containing the private key and the certificate
	 * @param trustedCertPaths	a list of paths to crt-files of the trusted certificates (are added to the trustStore)
	 * @param protocol 			the used tls protocol
	 * @return	 				the SSLContext that is used to create the SSL socket
	 */
	public static SSLContext createSSLContext(String keyStorePassword, String keyFilePath, ArrayList<String> trustedCertPaths, String protocol) {
		try {
			SSLContext sslContext = SSLContext.getInstance(protocol, "BCJSSE");
			
			// Set up Keystore (class to save certificates and private key) and import the client certificate
			KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");		// the pkcs12 format stores the private key and the certificate together
			File keyFile = new File(keyFilePath);
			FileInputStream keyInput = new FileInputStream(keyFile);
			keyStore.load(keyInput, keyStorePassword.toCharArray());
			keyInput.close();
			
			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX", "BCJSSE");
			kmf.init(keyStore, keyStorePassword.toCharArray());

			// if any connection should be trusted
			if (trustedCertPaths == null) {
				TrustManager[] trustAllCerts = { new TrustManagerTrustAny() };
				sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
				return sslContext;

			} else {

				// add trusted Certificates (only needed if client authentication is used)
				TrustManagerTLS_BP trustManager = new TrustManagerTLS_BP();
				for (String certPath : trustedCertPaths) {
					trustManager.addCertificate(certPath);
				}

				// Initialize the SSLContext to work with our key managers.
				TrustManager[] trustManagers = {trustManager};
				sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());

				return sslContext;
			}

		} catch (Exception e) {
			logger.error("error creating ssl context: ", e);
			return null;
		}
	}




	/**
	 * sets up the sslScontext for a secure connection, can be used for the server and the client
	 * @param keyStorePassword	keyStore password (must be the same as chosen to create the certificate)
	 * @param privateKey 		the own private key
	 * @param ownCert	 		the certificate for the keystore (own certificate)
	 * @param caCert	 		certificate of the certificate authority
	 * @param protocol 			the used tls protocol
	 * @return	 				the SSLContext that is used to create the SSL socket
	 */
	public static SSLContext createSSLContext(String keyStorePassword, PrivateKey privateKey, Certificate ownCert, Certificate caCert, String protocol) {
		try {
			SSLContext sslContext = SSLContext.getInstance(protocol, "BCJSSE");


			// Set up Keystore with the key manager factory
			KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");		// create the keystore that save the key and certificates, KeyStore.getInstance("BKS", "BC");
			keyStore.load(null); 

			// add the certificate and entries
			Certificate[] chain = new Certificate[] { ownCert, caCert };
//			Certificate[] chain = new Certificate[] { ownCert };  				// does not work        
			keyStore.setKeyEntry("importkey", privateKey, keyStorePassword.toCharArray(), chain);	// store the private key


			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX", "BCJSSE");
			kmf.init(keyStore, keyStorePassword.toCharArray());

			// add trusted Certificates (only needed if client authentication is used)
			TrustManagerTLS_BP trustManager = new TrustManagerTLS_BP();
			trustManager.addCertificate(caCert);
			

			// Initialize the SSLContext to work with our key managers.
			TrustManager[] trustManagers = {trustManager};
			sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());

			return sslContext;


		} catch (Exception e) {
			logger.error("error creating ssl context: ", e);
			return null;
		}
	}


}
