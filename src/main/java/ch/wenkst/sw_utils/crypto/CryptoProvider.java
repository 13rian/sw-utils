package ch.wenkst.sw_utils.crypto;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;

public class CryptoProvider {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	
	private CryptoProvider() {
		
	}
	
	
	/**
	 * registers the bouncy castle provider as security provider, if it was not registered before
	 */
	public static void registerBC() {	
		if (!bcProviderRegistered()) {
			Security.addProvider(new BouncyCastleProvider());
			setSourceOfRandom();
			logger.info("successfully registered Bouncy Castle as crypto provider.");
		}
	}
	
	
	/**
	 * changes the source of random to /dev/urandom if the operating system is linux
	 * this source has a lower chance of blocking
	 */
	public static void setSourceOfRandom() {
		if (Utils.isOSLinux()) {
			logger.info("operating system is linux, change the source of random to /dev/urandom");
			System.setProperty("java.security.egd", "file:/dev/./urandom");
		}
	}


	/**
	 * unregisters the bouncy castle provider as security provider
	 */
	public static void unregisterBC() {
		if (bcProviderRegistered()) {
			Security.removeProvider(SecurityConstants.BC);
			logger.info("successfully unregistered Bouncy Castle as crypto provider.");
		}
	}


	/**
	 * registers the bouncy castle provider for tls handshakes (BCJSSE) at position 2 and the bouncy castle provider for
	 * api crypto (BC) at position 3, if they were not registered before.
	 * The Sun security provider needs to be in front of the BCJSSE provider since it is internally used by BCJSSE.
	 * Note: The BCJSSE provider needs the JCE unlimited strength file installed
	 */
	public static void registerBCJSSE() {
		if (!bcjsseProviderRegistered()) {
			try {
				// with this approach it is not necessary to have the bcjsse dependency in the class path
				Object bc = Class.forName("org.bouncycastle.jsse.provider.BouncyCastleJsseProvider").getDeclaredConstructor().newInstance();
				Security.insertProviderAt((Provider) bc, 1);
				logger.info("successfully registered bouncy castle bcjsse as security provider at position 1.");
				
			} catch (Exception e) {
				logger.error("failed to register bouncy castle bcjsse as security provider: ", e);
			}
		}

		if (!bcProviderRegistered()) {
			Security.insertProviderAt(new BouncyCastleProvider(), 2);
			logger.info("Successfully registered Bouncy Castle BC as security provider at position 2.");
		}
		
		setSourceOfRandom();
	}


	/**
	 * unregisters both bouncy castle providers BCJSSE and BC
	 */
	public static void unregisterBCJSSE() {
		if (bcjsseProviderRegistered()) {
			Security.removeProvider(SecurityConstants.BCJSSE);
			logger.info("Successfully unregistered Bouncy Castle BCJSSE as crypto provider.");
		}

		if (bcProviderRegistered()) {
			Security.removeProvider(SecurityConstants.BC);
			logger.info("Successfully unregistered Bouncy Castle BC as crypto provider.");
		}
	}


	public static boolean bcProviderRegistered() {
		return Security.getProvider(SecurityConstants.BC) != null;
	}
	
	
	public static boolean bcjsseProviderRegistered() {
		return Security.getProvider(SecurityConstants.BCJSSE) != null;
	}
	
	
	
	/**
	 * returns a String with the registered providers in the correct order they are registered
	 * @return 		string with the registered security providers 
	 */
	public static String getRegisteredProviders() {
		StringBuilder builder = new StringBuilder("[");

		for (Provider provider : Security.getProviders()) {
			builder.append(provider.getName()).append(", ");
		}

		builder.deleteCharAt(builder.length()-1);
		builder.deleteCharAt(builder.length()-1);
		builder.append("]");

		return builder.toString();
	}
	
	
	
	/**
	 * returns a string with the default providers that handle the security
	 * @return 		a string containing all the default providers
	 */
	public static String getDefaultProviders() {
		StringBuilder builder = new StringBuilder("\n");

		try {
			// provider of the SSLContext
			String name = SSLContext.getInstance(SecurityConstants.TLS_1_2).getProvider().getName();
			builder.append("TLSv1.2 SSLContext Provider: ").append(name).append("\n");

			// provider of the CerificateFactory
			name = CertificateFactory.getInstance(SecurityConstants.X509).getProvider().getName();
			builder.append("X.509 CertificateFactory Provider: ").append(name).append("\n");

			// provider of the default KeyStore
			name = KeyStore.getInstance(KeyStore.getDefaultType()).getProvider().getName();
			builder.append("Default KeyStore Provider: ").append(name).append("\n");

			// provider of the PKCS12 KeyStore
			name = KeyStore.getInstance(SecurityConstants.PKCS12).getProvider().getName();
			builder.append("PKCS12 KeyStore Provider: ").append(name).append("\n");

			// provider of the KeyManagerFactory
			name = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).getProvider().getName();
			builder.append("Default KeyManagerFactory Provider: ").append(name).append("\n");

			// provider of the TrustManagerFactory
			name = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).getProvider().getName();
			builder.append("Default TrustManagerFactory Provider: ").append(name).append("\n");


			// default keyStore type
			name = KeyStore.getDefaultType();
			builder.append("KeyStore.getDefaultType(): ").append(name).append("\n");


			// default KeyManagerFactory algorithm
			name = KeyManagerFactory.getDefaultAlgorithm();
			builder.append("KeyStore.getDefaultType(): ").append(name).append("\n");


			// default TrustManagerFactory provider
			name = TrustManagerFactory.getDefaultAlgorithm();
			builder.append("KeyStore.getDefaultType(): ").append(name).append("\n");

			return builder.toString();


		} catch (Exception e) {
			logger.error("error getting the default security types: ", e);
			return "";
		}
	}
}
