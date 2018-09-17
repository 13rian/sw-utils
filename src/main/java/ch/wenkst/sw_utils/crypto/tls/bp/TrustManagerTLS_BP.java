package ch.wenkst.sw_utils.crypto.tls.bp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trust store that handles elliptic curve certificates (brainpool included)
 */
public class TrustManagerTLS_BP implements X509TrustManager {
	private static final Logger logger = LoggerFactory.getLogger(TrustManagerTLS_BP.class);
	
	private KeyStore trustStore; 
	private X509TrustManager trustManager;
	private TrustManagerFactory trustManagerFactory;
	private CertificateFactory certFactory;

	public TrustManagerTLS_BP() {
		super();
		
		try {
			// truststore
			trustStore = KeyStore.getInstance("JKS");
			trustStore.load(null); 						// create empty truststore
			
			// CertificateFactory
			certFactory = CertificateFactory.getInstance("X.509", "BC");

		} catch (Exception e) {
			logger.error("unable to create TrustStore: ", e);
		}
	}
	
	
	/** 
	 * adds a certificate to the TrustManager
	 * @param certpath 		path to the cert-file (.crt)
	 */
	public void addCertificate(String certpath) {
		try {
			// load Certificate
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certpath));
			Certificate cert = certFactory.generateCertificate(bis);
			trustStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
			
			// set up trust manager factory to use our trust store
			trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
			trustManagerFactory.init(trustStore);

			// acquire X509 trust manager from factory
			TrustManager tms[] = trustManagerFactory.getTrustManagers();
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					trustManager = (X509TrustManager)tms[i];
					break;
				}
			}

		} catch (Exception e) {
			logger.error("unable to add certificate from " + certpath, e);
		} 
	}
	
	
	/** 
	 * adds a certificate to the TrustManager
	 * @param cert: 	the certificate to add
	 */
	public void addCertificate(Certificate cert) {
		try {
			// add the certificate to the truststore
			trustStore.setCertificateEntry(UUID.randomUUID().toString(), cert);
			
			// set up trust manager factory to use our trust store
			trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
			trustManagerFactory.init(trustStore);

			// acquire X509 trust manager from factory
			TrustManager tms[] = trustManagerFactory.getTrustManagers();
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					trustManager = (X509TrustManager)tms[i];
					break;
				}
			}

		} catch (Exception e) {
			logger.error("unable to add certificate: ", e);
		} 
	}

	
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (trustManager == null) {
			throw new CertificateException("No X509TrustManager in TrustManagerFactory");
		}
		
		// do default verification
		trustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (trustStore == null) {
			throw new CertificateException("No X509TrustManager in TrustManagerFactory");
		}

		// do default verification
		trustManager.checkServerTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		if (trustStore != null) {
			X509Certificate[] issuers = trustManager.getAcceptedIssuers();
			return issuers;
		
		} else {
			return new X509Certificate[0];
		}
	}

}
