package ch.wenkst.sw_utils.crypto.tls.for_grizzly;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;

import ch.wenkst.sw_utils.crypto.tls.TrustManagerTrustAny;

public class ContextConfiguratorTLS extends SSLContextConfigurator {
	private static final Logger logger = LogManager.getLogger(ContextConfiguratorTLS.class);    // initialize the logger

	private String keyStorePassword =""; 			// the keyStore password (must be the same that was  used to create the certificate)
	private String keyFilePath = ""; 				// path to the p12-file containing the private key and the certificate
	private ArrayList<String> trustedCerts = null;	// a list of paths to crt-files of the trusted certificates
	private String protocol = ""; 					// the used TLS protocol

	/**
	 * sets up the sslScontext for a secure connection, can be used for the server and the client
	 * @param keyStorePassword 		keyStore password (must be the same as chosen to create the certificate)
	 * @param keyFilePath 			the path to the p12-file containing the private key and the certificate
	 * @param trustedCerts			a list of paths to crt-files of the trusted certificates (are added to the trustStore)
	 * @param protocol	 			specifies the used tls protocol
	 */
	public ContextConfiguratorTLS(String keyStorePassword, String keyFilePath, ArrayList<String> trustedCerts, String protocol) {
		this.keyStorePassword = keyStorePassword;
		this.keyFilePath = keyFilePath;
		this.trustedCerts = trustedCerts;
		this.protocol = protocol;
	}


	/**
	 * constructor to setup an SSL context that contains no certificates and keys,
	 * it is used for clients that accept any connection
	 */
	public ContextConfiguratorTLS() {

	}


	@Override
	public boolean validateConfiguration() {
		return true;
	}


	@Override
	public boolean validateConfiguration(boolean needsKeyStore) {
		return true;
	}


	@Override
	public SSLContext createSSLContext() {
		try {
			SSLContext sslContext = SSLContext.getInstance(protocol);
			
			// Set up Keystore (class to save certificates and private key) and import the client certificate
			KeyStore keyStore = KeyStore.getInstance("PKCS12");		// the pkcs12 format stores the private key and the certificate together
			File keyFile = new File(keyFilePath);
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
				// add trusted Certificates (only needed if client authentication is used)
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


}
