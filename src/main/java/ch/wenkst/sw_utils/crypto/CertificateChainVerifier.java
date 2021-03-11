package ch.wenkst.sw_utils.crypto;

import java.security.cert.X509Certificate;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateChainVerifier {
	private static final Logger logger = LoggerFactory.getLogger(CertificateChainVerifier.class);
	
	
	private X509Certificate[] chain;
	private X509Certificate[] trustedCerts;
	
	
	/**
	 * verifies the passed certificate chain with the trusted certificates
	 * @param chain				certificate chain to verify
	 * @param trustedCerts		all trusted certificates
	 */
	public CertificateChainVerifier(X509Certificate[] chain, X509Certificate[] trustedCerts) {
		this.chain = chain;
		this.trustedCerts = trustedCerts;
	}
	
	
	/**
	 * verifies the server certificates against the certificates in the truststore, the signature is tested and
	 * it is checked if one of the certificates are outdated. Note it is better to use the method checkServerTrusted
	 * @return 				true if the certificates from the server are valid, false if they are invalid
	 */
	public boolean verify() {
		for (X509Certificate cert : chain) {
			if (isCertOutdated(cert)) {
				logger.error("server-certificate is outdated");
				return false;
			}
			
			boolean isCertTrusted = isCertifcateTrustedByAny(cert, trustedCerts);
			if (!isCertTrusted) {
				return false;
			}
		}

		return true;
	}
	
	
	private boolean isCertOutdated(X509Certificate cert) {
		return Instant.now().toEpochMilli() > cert.getNotAfter().getTime();
	}
	
	
	private boolean isCertifcateTrustedByAny(X509Certificate certToVerify, X509Certificate[] trustedCerts) {
		for (X509Certificate trustedCert : trustedCerts) {
			if (isCertOutdated(trustedCert)) {
				logger.error("certificate in trust store is outdated");
				return false;
			}
			
			boolean isCertTrusted = isCertifcateTrusted(certToVerify, trustedCert);
			if (isCertTrusted) {
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean isCertifcateTrusted(X509Certificate certToVerify, X509Certificate trustedCert) {
		try {
			certToVerify.verify(trustedCert.getPublicKey());
			return true; 

		} catch (Exception e) {
			return false;
		} 
	}
}
