package ch.wenkst.sw_utils.crypto.tls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import ch.wenkst.sw_utils.crypto.CryptoProvider;
import ch.wenkst.sw_utils.crypto.SecurityConstants;
import ch.wenkst.sw_utils.crypto.SecurityUtils;

public class SSLContextGenerator {
	
	private SSLContextGenerator() {
		
	}
	

	/**
	 * sets up the ssl context which can be used for the server or the client from the passed p12-file
	 * @param p12File 			the path to the p12-file containing the private key and the certificate
	 * @param keyStorePassword	keyStore password (must be the same as chosen to create the certificate)
	 * @param trustedCerts		a list of trusted certificates, can be null to trust all
	 * @param protocol 			the used tls protocol
	 * @return	 				the SSLContext
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 * @throws KeyManagementException 
	 */
	public static SSLContext createSSLContext(
			String p12File,
			String keyStorePassword,
			List<Certificate> trustedCerts,
			String protocol)
					throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, 
					IOException, UnrecoverableKeyException, KeyManagementException {

		SSLContext sslContext = sslContextInstance(protocol);
		KeyStore keyStore = keyStoreFromP12File(p12File, keyStorePassword);
		KeyManagerFactory kmf = keyManagerFactoryInstance(keyStore, keyStorePassword);
		TrustManager[] trustManagers = createTrustManagers(trustedCerts);
		sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());
		return sslContext;
	}



	/**
	 * sets up the ssl context which can be used for the server or the client from the passed crypto objects
	 * @param keyStorePassword	keyStore password (must be the same as chosen to create the certificate)
	 * @param privateKey 		the own private key
	 * @param ownCert	 		the certificate for the keystore (own certificate)
	 * @param ownCaCert 		the ca cert of the keystore certificate (ca of the own certificate)
	 * @param trustedCerts	 	list of trusted certificates, can be null to trust all
	 * @param protocol 			the used tls protocol
	 * @return	 				the SSLContext
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 * @throws KeyManagementException 
	 */
	public static SSLContext createSSLContext(
			String keyStorePassword,
			PrivateKey privateKey,
			Certificate ownCert,
			Certificate ownCaCert,
			List<Certificate> trustedCerts,
			String protocol)
					throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException,
					IOException, UnrecoverableKeyException, KeyManagementException {

		SSLContext sslContext = sslContextInstance(protocol);
		KeyStore keyStore = keyStoreFromCryptoObjects(privateKey, ownCert, ownCaCert, keyStorePassword);
		KeyManagerFactory kmf = keyManagerFactoryInstance(keyStore, keyStorePassword);
		TrustManager[] trustManagers = createTrustManagers(trustedCerts);
		sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());
		return sslContext;
	}
	
	
	private static SSLContext sslContextInstance(String protocol) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (CryptoProvider.bcjsseProviderRegistered()) {
			return SSLContext.getInstance(protocol, SecurityConstants.BCJSSE);
		} else {
			return SSLContext.getInstance(protocol);
		}
	}
	
	
	private static KeyStore keyStoreFromP12File(String p12File, String keyStorePassword)
			throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = SecurityUtils.keyStoreInstance();
		File keyFile = new File(p12File);
		FileInputStream keyInput = new FileInputStream(keyFile);
		keyStore.load(keyInput, keyStorePassword.toCharArray());
		keyInput.close();
		return keyStore;
	}
	
	
	private static KeyStore keyStoreFromCryptoObjects(PrivateKey privateKey, Certificate cert, Certificate caCert, String keyStorePassword)
			throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = SecurityUtils.keyStoreInstance();
		keyStore.load(null); 

		Certificate[] chain = new Certificate[] { cert, caCert };      
		keyStore.setKeyEntry("own-private-key", privateKey, keyStorePassword.toCharArray(), chain);
		return keyStore;
	}
	
	
	private static KeyManagerFactory keyManagerFactoryInstance(KeyStore keyStore, String keyStorePassword)
			throws NoSuchAlgorithmException, NoSuchProviderException, UnrecoverableKeyException, KeyStoreException {
		KeyManagerFactory kmf;
		if (CryptoProvider.bcjsseProviderRegistered()) {
			kmf = KeyManagerFactory.getInstance(SecurityConstants.PKIX, SecurityConstants.BCJSSE);
		} else {
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		}
		kmf.init(keyStore, keyStorePassword.toCharArray());
		return kmf;
	}
	
	
	private static TrustManager[] createTrustManagers(List<Certificate> trustedCerts)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException {
		if (trustedCerts == null) {
			return trustAllTrustManagers();
		}

		return trustManagerFromCerts(trustedCerts);
	}
	
	
	private static TrustManager[] trustAllTrustManagers() {
		TrustManager[] trustAllCerts = { new TrustManagerTrustAny() };
		return trustAllCerts;
	}
	
	
	private static TrustManager[] trustManagerFromCerts(List<Certificate> trustedCerts)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException {
		TrustManagerTLS trustManager = new TrustManagerTLS();
		for (Certificate cert : trustedCerts) {
			trustManager.addCertificate(cert, null);
		}
		trustManager.initTrustManager();

		TrustManager[] trustManagers = {trustManager};
		return trustManagers;
	}
}
