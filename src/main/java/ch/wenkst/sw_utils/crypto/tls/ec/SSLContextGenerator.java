package ch.wenkst.sw_utils.crypto.tls.ec;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.crypto.tls.TrustManagerTrustAny;


/**
 * generates the SSL context for elliptic curves (not brainpool curves)
 */
public class SSLContextGenerator {
	private static final Logger logger = LoggerFactory.getLogger(SSLContextGenerator.class);


	
	/**
	 * sets up the sslScontext for a secure connection, can be used for the server and the client
	 * @param p12FilePath 		the path to the p12-file containing the private key and the certificate
	 * @param keyStorePassword 	keyStore password (must be the same as chosen to create the certificate)
	 * @param trustedCerts  	a list of paths to crt-files of the trusted certificates (are added to the trustStore)
	 * @param protocol	 		specifies the used tls protocol, e.g. "TLSv1.2"
	 * @return 					ssl context
	 */
	public static SSLContext createSSLContext(String p12FilePath, String keyStorePassword, List<String> trustedCerts, String protocol) {
		try {
			SSLContext sslContext = SSLContext.getInstance(protocol);
			
			// Set up Keystore (class to save certificates and private key) and import the client certificate
			KeyStore keyStore = KeyStore.getInstance("PKCS12");		// the pkcs12 format stores the private key and the certificate together
			File keyFile = new File(p12FilePath);
			FileInputStream keyInput = new FileInputStream(keyFile);
			keyStore.load(keyInput, keyStorePassword.toCharArray());
			keyInput.close();
			
			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, keyStorePassword.toCharArray());
			
			// if any connection should be trusted
			if (trustedCerts == null) {
				TrustManager[] trustAllCerts = { new TrustManagerTrustAny() };
				sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
				return sslContext;

			} else {
				// add trusted Certificates (only needed if authentication is used from the other connection partner)
				TrustManagerTLS trustManager = new TrustManagerTLS();
				for (String certPath : trustedCerts) {
					trustManager.addCertificate(certPath);
				}

				// Initialize the SSLContext to work with our key managers.
				TrustManager[] trustManagers = {trustManager};
				sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());

				return sslContext;
			}

		} catch (Exception e) {
			logger.error("error creating ssl context: ", e);
		}

		return null;
	}
	
	
	
	
	/**
	 * sets up the sslScontext for a secure connection, can be used for the server and the client
	 * @param keyStorePassword	keyStore password (must be the same as chosen to create the certificate)
	 * @param privateKey 		the own private key
	 * @param ownCert	 		the certificate for the keystore (own certificate)
	 * @param caCert	 		certificate of the certificate authority
	 * @param protocol 			the used tls protocol, e.g. "TLSv1.2"
	 * @return	 				the SSLContext that is used to create the SSL socket
	 */
	public static SSLContext createSSLContext(String keyStorePassword, PrivateKey privateKey, Certificate ownCert, Certificate caCert, String protocol) {
		try {
			SSLContext sslContext = SSLContext.getInstance(protocol);


			// Set up Keystore with the key manager factory
			KeyStore keyStore = KeyStore.getInstance("PKCS12");			// create the keystore that save the key and certificates, KeyStore.getInstance("BKS", "BC");
			keyStore.load(null); 

			// add the certificate and entries
			Certificate[] chain = new Certificate[] { ownCert, caCert };
			keyStore.setKeyEntry("importkey", privateKey, keyStorePassword.toCharArray(), chain);	// store the private key


			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, keyStorePassword.toCharArray());
			
			
			
			// if any connection should be trusted
			if (caCert == null) {
				TrustManager[] trustAllCerts = { new TrustManagerTrustAny() };
				sslContext.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
				return sslContext;

			} else {
				// add trusted Certificates (only needed if authentication is used from the other connection partner)
				TrustManagerTLS trustManager = new TrustManagerTLS();
				trustManager.addCertificate(caCert);
				

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


}
