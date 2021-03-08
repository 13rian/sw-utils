package ch.wenkst.sw_utils.crypto.tls;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.crypto.SecurityConstants;
import ch.wenkst.sw_utils.crypto.SecurityUtils;

public class TrustManagerTLS implements X509TrustManager {
	private static final Logger logger = LoggerFactory.getLogger(TrustManagerTLS.class);
	
	private KeyStore trustStore; 
	private X509TrustManager trustManager;

	
	/**
	 * trust store for the ssl context
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 */
	public TrustManagerTLS() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {		
		trustStore = KeyStore.getInstance(SecurityConstants.JKS);
		trustStore.load(null);
	}
	
	
	/** 
	 * adds a certificate to the TrustManager
	 * @param cert: 	the certificate to add
	 * @param alias 	the alias under which the certificate is saved in the trust store, can be null
	 * @throws KeyStoreException 
	 */
	public void addCertificate(Certificate cert, String alias) throws KeyStoreException {
		if (alias == null) {
			alias = UUID.randomUUID().toString();
		}
		trustStore.setCertificateEntry(alias, cert);
	}
	
	
	/**
	 * initializes the trust manager with the trust store that should already contain certificate entries
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws KeyStoreException
	 */
	public void initTrustManager() throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = setupTrustManagerFactory();
		trustManager = x509TrustManager(trustManagerFactory);
	}
	
	
	private TrustManagerFactory setupTrustManagerFactory() throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException {
		TrustManagerFactory trustManagerFactory;
		if (SecurityUtils.bcjsseProviderRegistered()) {
			trustManagerFactory = TrustManagerFactory.getInstance(SecurityConstants.PKIX, SecurityConstants.BCJSSE);
		} else {
			trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		}
		trustManagerFactory.init(trustStore);
		return trustManagerFactory;
	}
	
	
	private X509TrustManager x509TrustManager(TrustManagerFactory trustManagerFactory) {
		TrustManager tms[] = trustManagerFactory.getTrustManagers();
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				return (X509TrustManager) tms[i];
			}
		}
		logger.error("x509 trust manager could not be created, no instance was found in the trust manager factory");
		return null;
	}
	
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (trustManager == null) {
			throw new CertificateException("No X509TrustManager in TrustManagerFactory");
		}
		
		trustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (trustStore == null) {
			throw new CertificateException("No X509TrustManager in TrustManagerFactory");
		}

		trustManager.checkServerTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if (trustStore != null) {
			return trustManager.getAcceptedIssuers();
		
		} else {
			return new X509Certificate[0];
		}
	}
}
