package ch.wenkst.sw_utils.crypto.tls;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * generates the SSL context if ec brainpool curves should be supported as well the BC and the BCJSSE
 * security provider need to be registered.
 */
public class SSLContextGenerator {
	private static final Logger logger = LoggerFactory.getLogger(SSLContextGenerator.class);


	/**
	 * sets up the sslScontext for a secure connection, can be used for the server and the client
	 * @param keyFilePath 		the path to the p12-file containing the private key and the certificate
	 * @param keyStorePassword	keyStore password (must be the same as chosen to create the certificate)
	 * @param trustedCertPaths	a list of paths to crt-files of the trusted certificates, can be null to trust all
	 * @param protocol 			the used tls protocol
	 * @return	 				the SSLContext that is used to create the SSL socket
	 */
	public static SSLContext createSSLContext(
			String keyFilePath,
			String keyStorePassword,
			List<String> trustedCertPaths,
			String protocol) {
		try {
			SSLContext sslContext;
			if (Security.getProvider("BCJSSE") == null) {
				sslContext = SSLContext.getInstance(protocol);
			} else {
				sslContext = SSLContext.getInstance(protocol, "BCJSSE");
			}
			
			// set up Keystore (class to save certificates and private key) and import the client certificate
			KeyStore keyStore; 								// the pkcs12 format stores the private key and the certificate together
			if (Security.getProvider("BC") == null) {
				keyStore = KeyStore.getInstance("PKCS12");
			} else {
				keyStore  = KeyStore.getInstance("PKCS12", "BC");	
			}
			
			File keyFile = new File(keyFilePath);
			FileInputStream keyInput = new FileInputStream(keyFile);
			keyStore.load(keyInput, keyStorePassword.toCharArray());
			keyInput.close();
			
			// set up key manager factory to use our key store
			KeyManagerFactory kmf;
			if (Security.getProvider("BCJSSE") == null) {
				kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			} else {
				kmf = KeyManagerFactory.getInstance("PKIX", "BCJSSE");
			}
			kmf.init(keyStore, keyStorePassword.toCharArray());

			// if any connection should be trusted
			if (trustedCertPaths == null) {
				TrustManager[] trustAllCerts = { new TrustManagerTrustAny() };
				sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());

			} else {

				// add trusted Certificates (only needed if client authentication is used)
				TrustManagerTLS trustManager = new TrustManagerTLS();
				for (String certPath : trustedCertPaths) {
					trustManager.addCertificate(certPath, null);
				}

				// initialize the SSLContext to work with our key managers.
				TrustManager[] trustManagers = {trustManager};
				sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());
			}
			
			return sslContext;

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
	 * @param ownCaCert 		the ca cert of the keystore certificate (ca of the own certificate)
	 * @param trustedCerts	 	list of trusted certificates, can be null to trust all
	 * @param protocol 			the used tls protocol
	 * @return	 				the SSLContext that is used to create the SSL socket
	 */
	public static SSLContext createSSLContext(
			String keyStorePassword,
			PrivateKey privateKey,
			Certificate ownCert,
			Certificate ownCaCert,
			List<Certificate> trustedCerts,
			String protocol) {
		try {
			SSLContext sslContext;
			if (Security.getProvider("BCJSSE") == null) {
				sslContext = SSLContext.getInstance(protocol);
			} else {
				sslContext = SSLContext.getInstance(protocol, "BCJSSE");
			}


			// set up Keystore (class to save certificates and private key) and import the client certificate
			KeyStore keyStore; 								// the pkcs12 format stores the private key and the certificate together
			if (Security.getProvider("BC") == null) {
				keyStore = KeyStore.getInstance("PKCS12");
			} else {
				keyStore  = KeyStore.getInstance("PKCS12", "BC");	
			}
			keyStore.load(null); 

			// add the certificate and entries
			Certificate[] chain = new Certificate[] { ownCert, ownCert };
//			Certificate[] chain = new Certificate[] { ownCert };  				// does not work        
			keyStore.setKeyEntry("own-private-key", privateKey, keyStorePassword.toCharArray(), chain);	// store the private key


			// set up key manager factory to use our key store
			KeyManagerFactory kmf;
			if (Security.getProvider("BCJSSE") == null) {
				kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			} else {
				kmf = KeyManagerFactory.getInstance("PKIX", "BCJSSE");
			}
			kmf.init(keyStore, keyStorePassword.toCharArray());
			
			
			
			

			// if any connection should be trusted
			if (trustedCerts == null) {
				TrustManager[] trustAllCerts = { new TrustManagerTrustAny() };
				sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());

			} else {

				// add trusted Certificates (only needed if client authentication is used)
				TrustManagerTLS trustManager = new TrustManagerTLS();
				for (Certificate cert : trustedCerts) {
					trustManager.addCertificate(cert, null);
				}

				// initialize the SSLContext to work with our key managers.
				TrustManager[] trustManagers = {trustManager};
				sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());
			}
			
			return sslContext;

		} catch (Exception e) {
			logger.error("error creating ssl context: ", e);
			return null;
		}
	}


}
