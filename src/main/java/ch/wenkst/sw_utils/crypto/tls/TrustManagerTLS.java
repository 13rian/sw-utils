package ch.wenkst.sw_utils.crypto.tls;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.crypto.SecurityUtils;


/**
 * Trust store that handles elliptic curve certificates (brainpool included)
 */
public class TrustManagerTLS implements X509TrustManager {
	private static final Logger logger = LoggerFactory.getLogger(TrustManagerTLS.class);
	
	private KeyStore trustStore; 
	private X509TrustManager trustManager;
	private TrustManagerFactory trustManagerFactory;

	
	/**
	 * trust store for the ssl context, if brainpool curves should be included
	 * the BC and the BCJSSE crypto providers need to be registered
	 */
	public TrustManagerTLS() {
		super();
		
		try {
			// create the trust store
			trustStore = KeyStore.getInstance("JKS");
			trustStore.load(null); 						// create empty truststore

		} catch (Exception e) {
			logger.error("unable to create TrustStore: ", e);
		}
	}
	
	
	/** 
	 * adds a certificate to the TrustManager
	 * @param certpath 		path to the cert-file
	 * @param alias 		the alias under which the certificate is saved in the trust store, can be null
	 */
	public void addCertificate(String certpath, String alias) {
		try {
			// load certificate
			Certificate cert = SecurityUtils.certFromFile(certpath);
			addCertificate(cert, alias);

		} catch (Exception e) {
			logger.error("unable to add certificate from " + certpath, e);
		} 
	}
	
	
	/** 
	 * adds a certificate to the TrustManager
	 * @param cert: 	the certificate to add
	 * @param alias 	the alias under which the certificate is saved in the trust store, can be null
	 */
	public void addCertificate(Certificate cert, String alias) {
		try {
			// add the certificate to the truststore
			if (alias == null) {
				alias = UUID.randomUUID().toString();
			}
			trustStore.setCertificateEntry(alias, cert);
			
			// set up trust manager factory to use our trust store
			if (Security.getProvider("BCJSSE") == null) {
				trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			} else {
				trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "BCJSSE");
			}
			trustManagerFactory.init(trustStore);

			// acquire X509 trust manager from factory
			TrustManager tms[] = trustManagerFactory.getTrustManagers();
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					trustManager = (X509TrustManager) tms[i];
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
			return trustManager.getAcceptedIssuers();
		
		} else {
			return new X509Certificate[0];
		}
	}
}
