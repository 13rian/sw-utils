package ch.wenkst.sw_utils.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyAgreeEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientId;
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.file.FileUtils;

public class CryptoUtils {
	private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
	
	// define two constants for the certificate and key format
	public static final int FORMAT_DER = 1;
	public static final int FORMAT_PEM = 2;
	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 								methods to register bouncy castle providers 								 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * registers the bouncy castle provider as security provider, if it was not registered before
	 */
	public static void registerBC() {
		// Register the bouncy castle security provider, if not registered yet
		if(Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
			setSourceOfRandom();
			logger.info("Successfully registered Bouncy Castle as crypto provider.");
		}
	}


	/**
	 * unregisters the bouncy castle provider as security provider
	 */
	public static void unregisterBC() {
		// unregister the bouncy castle provider if registered before
		if(Security.getProvider("BC") != null) {
			Security.removeProvider("BC");
			logger.info("Successfully unregistered Bouncy Castle as crypto provider.");
		}
	}


	/**
	 * registers the bouncy castle provider for tls handshakes (BCJSSE) at position 2 and the bouncy castle provider for
	 * api crypto (BC) at position 3, if they were not registered before.
	 * The Sun security provider needs to be in front of the BCJSSE provider since it is internally used by BCJSSE.
	 * Note: The BCJSSE provider needs the JCE unlimited strength file installed
	 */
	public static void registerBCJSSE() {
		// insert the bouncy castle JSSE provider at position 2 if not already registered
		if (Security.getProvider("BCJSSE") == null) {
			Security.insertProviderAt(new BouncyCastleJsseProvider(), 2);
			logger.info("Successfully registered Bouncy Castle BCJSSE as security provider at position 2.");
		}

		// insert the bouncy castle provider at position 3 if not already registered
		if (Security.getProvider("BC") == null) {
			Security.insertProviderAt(new BouncyCastleProvider(), 3);
			logger.info("Successfully registered Bouncy Castle BC as security provider at position 3.");
		}
		
		setSourceOfRandom();
	}


	/**
	 * unregisters both bouncy castle providers BCJSSE and BC
	 */
	public static void unregisterBCJSSE() {
		// unregister the bouncy castle BCJSSE provider if registered before
		if(Security.getProvider("BCJSSE") != null) {
			Security.removeProvider("BCJSSE");
			logger.info("Successfully unregistered Bouncy Castle BCJSSE as crypto provider.");
		}

		// unregister the bouncy castle BC provider if registered before
		if(Security.getProvider("BC") != null) {
			Security.removeProvider("BC");
			logger.info("Successfully unregistered Bouncy Castle BC as crypto provider.");
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												AES cryptography 													  //	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * generates a key used for the AES encryption
	 * @param keyLength 	number of bits for the key: 128, 192 or 256 bits
	 * @return 				the secret key
	 */
	public static SecretKey generateKey(int keyLength) {
		SecretKey secretKey = null;

		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
			keyGen.init(keyLength, new SecureRandom()); 							
			secretKey = keyGen.generateKey();

		} catch (Exception e) {
			logger.error("error generating the secret key", e);
		}

		return secretKey;
	}


	/**
	 * encrypts the passed message with the passed key using AES
	 * @param message 	the message to encrypt
	 * @param key 		the secret key to encrypt
	 * @return 			the encrypted string
	 */
	public static String encrypt(String message, SecretKey key) {

		try {
			// convert the message to a byte array with UTF-8 encoding
			byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

			// generate the keySpec and encrypt
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES", "BC");                        // get an instance of the cipher
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);	    
			byte[] encMessage = cipher.doFinal(messageBytes);

			// convert the encrypted bytes to a base64 string
			String b64EncMessage = Conversion.byteArrayToBase64(encMessage);

			return b64EncMessage;

		} catch (Exception e) {
			logger.error("error during the aes encryption: ", e);
			return null;
		}	   
	}


	/**
	 * encrypts the passed message with the passed key using AES
	 * @param message 	the message bytes to encrypt
	 * @param key 		the secret key to encrypt
	 * @return 			the encrypted bytes
	 */
	public static byte[] encrypt(byte[] message, SecretKey key) {

		try {
			// generate the keySpec and encrypt
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES", "BC");                        // get an instance of the cipher
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);	    
			byte[] encMessage = cipher.doFinal(message);

			return encMessage;

		} catch (Exception e) {
			logger.error("error during the aes encryption: ", e);
			return null;
		}	   
	}


	/**
	 * decrypts the passed message with the passed key using AES
	 * @param encMessage 	the encrypted message (base64 string)
	 * @param key	 		the secret key to encrypt
	 * @return 				the decrypted message as string
	 */
	public static String decrypt(String encMessage, SecretKey key) {

		try {
			// convert the encrypted message to a byte array
			byte[] encByteArr = Conversion.base64StrToByteArray(encMessage);

			// generate the keySpec and decrypt
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES", "BC");                        // get an instance of the cipher
			cipher.init(Cipher.DECRYPT_MODE, keySpec);	    
			byte[] decMessage = cipher.doFinal(encByteArr);

			// convert the decrypted bytes back to the message
			String message = new String(decMessage, StandardCharsets.UTF_8);

			return message;

		} catch (Exception e) {
			logger.error("error during the aes decryption: ", e);
			return null;
		}	   
	}



	/**
	 * decrypts the passed message with the passed key using AES
	 * @param encMessage 	the encrypted message (as byte array)
	 * @param key	 		the secret key to encrypt
	 * @return 				the decrypted message as a byte array
	 */
	public static byte[] decrypt(byte[] encMessage, SecretKey key) {

		try {
			// generate the keySpec and decrypt
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES", "BC");                        // get an instance of the cipher
			cipher.init(Cipher.DECRYPT_MODE, keySpec);	    
			byte[] decMessage = cipher.doFinal(encMessage);


			return decMessage;

		} catch (Exception e) {
			logger.error("error during the aes decryption: ", e);
			return null;
		}	   
	}


	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												CMS cryptography 													  //	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * CMS encrypt using the diffie-hellman key exchange protocol
	 * @param receiverCert	 	certificate of the receiver
	 * @param senderCert		certificate of the sender
	 * @param senderKey 		private key of the sender
	 * @param bytes 			bytes to encrypt
	 * @return 					the envelope data as byte array
	 */
	public static byte[] createEnvelopeData(X509Certificate receiverCert, X509Certificate senderCert, PrivateKey senderKey, byte[] bytes) {

		try {
			CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
			JceKeyAgreeRecipientInfoGenerator infoGen= new JceKeyAgreeRecipientInfoGenerator(
					CMSAlgorithm.ECDH_SHA1KDF,
					senderKey,
					senderCert.getPublicKey(),
					CMSAlgorithm.AES128_WRAP);

			// add the recipient
			infoGen.addRecipient(receiverCert).setProvider("BC");
			edGen.addRecipientInfoGenerator(infoGen);

			// create the enveloped data
			CMSProcessableByteArray cmsProcData = new CMSProcessableByteArray(bytes);
			OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider("BC").build();
			CMSEnvelopedData ed = edGen.generate(cmsProcData, encryptor);

			return ed.getEncoded();

		} catch (Exception e) {
			logger.error("error during cms encryption: ", e);
			return null;
		}
	}


	/**
	 * decryptes the enveloped cms data
	 * @param receiverCert	 	certificate of the receiver
	 * @param receiverKey 		private key of the receiver
	 * @param encryptedBytes	the cms Data
	 * @return	 				the decrypted byte array
	 */
	public static byte[] decrypt(X509Certificate receiverCert, PrivateKey receiverKey, byte[] encryptedBytes) {
		try {
			CMSEnvelopedData ed = new CMSEnvelopedData(encryptedBytes);
			RecipientInformationStore recipients = ed.getRecipientInfos();

			RecipientId rid = new JceKeyAgreeRecipientId(receiverCert);

			RecipientInformation recipient = recipients.get(rid);
			byte[] content = recipient.getContent(new JceKeyAgreeEnvelopedRecipient(receiverKey).setProvider("BC"));

			return content;


		} catch (Exception e) {
			logger.error("error during CMS decryption:", e);
			return null;
		}
	}


	/**
	 * calculates the signature (ecdsa) of the passed byte array
	 * @param senderKey 	the private key of the sender that is used for the signature
	 * @param bytes 		the bytes to sign
	 * @return	 			the signature
	 */
	public static byte[] sign(PrivateKey senderKey, byte[] bytes) {
		try {
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");   // get the signature instance
			signature.initSign(senderKey); 											// define the private key to sign the data
			signature.update(bytes); 												// set the bytes to sign
			byte[] signatureBytes = signature.sign(); 								// create the signature

			return signatureBytes;

		} catch (Exception e) {
			logger.error("error during the signature: ", e);
			return null;
		}
	}


	/**
	 * verifies the signature
	 * @param senderSigCert 	the certificate of the sender that was used for the signature
	 * @param dataBytes 		the bytes that were signed
	 * @param signatureBytes 	the signature
	 * @return	 				true if the signature is correct
	 */
	public static boolean verifySig(X509Certificate senderSigCert, byte[] dataBytes, byte[] signatureBytes) {
		PublicKey publicKey = senderSigCert.getPublicKey();		// get the public key from the certificate

		try {
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");   // get the signature instance
			signature.initVerify (publicKey); 										// define the public key that is used to verify the signature
			signature.update (dataBytes); 											// set the data bytes that were signed
			boolean isSigValid = signature.verify(signatureBytes); 					// verify the signature

			return isSigValid;

		} catch (Exception e) {
			logger.error("error during verify: ", e);
			return false;
		}
	}




	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									methods to handle certificates		 									 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * loads the base64 encoded key or certificate form the passed pem-file
	 * @param path 	path to the pem-file
	 * @return 		base64 encoded der content of the pem file
	 */
	private static List<String> loadPem(String path) {
		ArrayList<String> result = new ArrayList<>();
		try {
			InputStream inputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			StringBuilder strBuilder = new StringBuilder();

			boolean readContent = false;
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				// check for the first line
				if (line.contains("BEGIN")) {
					readContent = true;
					continue;
				} 

				// check for the last line
				if (line.contains("END")) {
					String pemObj = strBuilder.toString();
					result.add(pemObj);
					strBuilder.setLength(0);
					readContent = false;
					continue;
				}

				// append the line to the cert
				if (readContent) {
					strBuilder.append(line.trim());
				}
			}

			// close the resources
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			return result;

		} catch (Exception e) {
			logger.error("error reading the private key from " + path, e);
			return null;
		}
	}	
	
	
	/**
	 * loads a certificate from a pem or a der file
	 * @param path 	the path to the certificate file
	 * @return 		the certificate
	 */
	public static X509Certificate certFromFile(String path) {
		try {
			File certFile = new File(path);
			FileInputStream certInput = new FileInputStream(certFile);

			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			return (X509Certificate) cf.generateCertificate(certInput);

		} catch (Exception e) {
			logger.error("error loading the certificate: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads a security certificate from the byte array in the DER format
	 * @param certBytes 	byte array containing the certificate information	
	 * @return 				the Java object that contains the security certificate
	 */
	public static X509Certificate certFromDer(byte[] certBytes) {
		try {
			// certificate factory
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
			ByteArrayInputStream is = new ByteArrayInputStream(certBytes); 
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);

			return cert;

		} catch (Exception e) {
			logger.error("error creating a certificate from the passed bytes: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads the base64 encoded certificate form the passed certificate file
	 * @param path 		path to the certificate file
	 * @param format 	der or pem use CryptoUtils.FORMAT_DER or CryptoUtils.FORMAT_PEM
	 * @return 			certificate as b64 encoded der
	 */
	public static String derFromCertFile(String path, int format) {
		if (format == FORMAT_DER) {
			byte[] b64CertBytes = FileUtils.readByteArrFromFile(path);
			String b64Cert = Conversion.byteArrayToBase64(b64CertBytes);
			return b64Cert;
			
		} else if (format == FORMAT_PEM) {
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && pemObjs.size() > 0) {
				return pemObjs.get(0);
			} else {
				logger.error("no pem objects found in the passed pem file");
				return null;
			}
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}
	}
	


	/**
	 * verifies the server certificates against the certificates in the truststore, the signature is tested and
	 * it is checked if one of the certificates are outdated. Note it is better to use the method checkServerTrusted
	 * from the X509TrustManager 
	 * @param chain			certificate chain from the server
	 * @param trustedCerts 	the trusted certificates in the truststore
	 * @return 				true if the certificates from the server are valid, false if they are invalid
	 */
	public static boolean verifyChain(X509Certificate[] chain, X509Certificate[] trustedCerts) {

		// flag to set for each certificate in the chain if it was verified from at least one certificate in the chain
		boolean isCertTrusted = false; 		

		for (X509Certificate cert : chain) {
			// check if it is outdated
			if (System.currentTimeMillis() > cert.getNotAfter().getTime()) {
				logger.error("server-certificate is outdated");
				return false;
			};

			for (X509Certificate trustedCert : trustedCerts) {
				// check if the certificate in the trust store is ourdated
				if (System.currentTimeMillis() > cert.getNotAfter().getTime()) {
					logger.error("certificate in trust store is outdated");
					return false;
				};

				// Verifying by public key
				try {
					cert.verify(trustedCert.getPublicKey());

					// if no error is thrown the certificate is valid
					isCertTrusted = true; 
					break;

				} catch (Exception e) {

				} 
			}

			// check if the cert is trusted
			if (!isCertTrusted) {
				return false;
			}
		}


		// all certificates are valid
		return true;
	}



	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											methods to handle keys 									 		 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * loads a key from a pem or a der file
	 * @param path 		the path to the key file
	 * @param format 	der or pem use CryptoUtils.FORMAT_DER or CryptoUtils.FORMAT_PEM
	 * @return 			the private key
	 */
	public static PrivateKey keyFromFile(String path, int format) {
		// load the b64 encoded der key
		String b64Key;
		if (format == FORMAT_DER) {
			b64Key = derFromKeyFile(path, FORMAT_DER);
						
		} else if (format == FORMAT_PEM) {
			b64Key = derFromKeyFile(path, FORMAT_PEM);
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}		
		
		// create the private key from the der bytes
		PrivateKey pk = keyFromDer(Conversion.base64StrToByteArray(b64Key));
		return pk;
	}
	
	
	/**
	 * loads a private key from the byte array in the DER format
	 * @param keyBytes 		byte array containing the key information	
	 * @return 				the Java object that contains the private key
	 */
	public static PrivateKey keyFromDer(byte[] keyBytes) {
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("EC", "BC");
			PrivateKey pk = kf.generatePrivate(keySpec);			
			return pk;

		} catch (Exception e) {
			logger.error("error creating a private key from the passed bytes: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads the base64 encoded key form the passed key file
	 * @param path 		path to the key file
	 * @param format 	der or pem use CryptoUtils.FORMAT_DER or CryptoUtils.FORMAT_PEM
	 * @return 			key as b64 encoded der
	 */
	public static String derFromKeyFile(String path, int format) {
		if (format == FORMAT_DER) {
			byte[] b64KeyBytes = FileUtils.readByteArrFromFile(path);
			String b64Key = Conversion.byteArrayToBase64(b64KeyBytes);
			return b64Key;
			
		} else if (format == FORMAT_PEM) {
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && pemObjs.size() > 0) {
				return pemObjs.get(0);
			} else {
				logger.error("no pem objects found in the passed pem file");
				return null;
			}
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}
	}
	
	
//	/**
//	 * loads the private key from a p12-file
//	 * @param path	 	path to the key, only .p12
//	 * @param password  export password of the private key
//	 * @return	 		the private key
//	 */
//	public static PrivateKey keyFromP12(String path, String password) {		
//		try {
//			KeyStore ks = KeyStore.getInstance("PKCS12");
//			File keyFile = new File(path);
//
//			FileInputStream fis = new FileInputStream(keyFile);
//			ks.load(fis, password.toCharArray());
//
//			Enumeration<String> aliases = ks.aliases();
//			String alias = aliases.nextElement();
//			return (PrivateKey) ks.getKey(alias, password.toCharArray());
//
//		} catch (Exception e) {
//			logger.error("error loading private key: ", e);
//			return null;
//		}
//	}
	



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 								methods to print security specific informations 							  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns a string with the default providers that handle the security
	 * @return 		a string containing all the default providers
	 */
	public static String getDefaultProviders() {
		StringBuilder builder = new StringBuilder("\n");

		try {
			// provider of the SSLContext
			String name = SSLContext.getInstance("TLSv1.2").getProvider().getName();
			builder.append("TLSv1.2 SSLContext Provider: ").append(name).append("\n");

			// provider of the CerificateFactory
			name = CertificateFactory.getInstance("X.509").getProvider().getName();
			builder.append("X.509 CertificateFactory Provider: ").append(name).append("\n");

			// provider of the default KeyStore
			name = KeyStore.getInstance(KeyStore.getDefaultType()).getProvider().getName();
			builder.append("Default KeyStore Provider: ").append(name).append("\n");

			// provider of the PKCS12 KeyStore
			name = KeyStore.getInstance("PKCS12").getProvider().getName();
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
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											methods to handle passwords 							  		  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * generates a password hash using the PBKDF2WithHmacSHA1 algorithm.
	 * @param password 		the string password to hash
	 * @return 				hash:salt:iterationCount, the hash and the salt is in base64 or null if an error occured
	 */
	public static String hashPassword(String password) {
		try {
			int iterations = 1000; 					// the number how often the password is hashed
			char[] chars = password.toCharArray();

			// create the salt
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[16];
			sr.nextBytes(salt);

			// generate the hash
			PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();

			// concatenate the hash the salt and the number of iterations
			return Conversion.byteArrayToBase64(hash) + ":" + Conversion.byteArrayToBase64(salt) + ":" + iterations;
		
		} catch (Exception e) {
			logger.error("error hashing the password");
			return null;
		}
	}


	/** 
	 * checks if the passed password has the same hash than the passed one 
	 * @param password 			the password that is validated
	 * @param pwHash 			hash:salt:iterationCount, the hash and the salt is in base64
	 * @return 					true, if the passed password matches the passed hash
	 */
	public static boolean validatePassword(String password, String pwHash) {
		try {
			// get the specifications of the hash
			String[] parts = pwHash.split(":");
			byte[] hash = Conversion.base64StrToByteArray(parts[0]);
			byte[] salt = Conversion.base64StrToByteArray(parts[1]);
			int iterations = Integer.parseInt(parts[2]);
			
			
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] testHash = skf.generateSecret(spec).getEncoded();

			int diff = hash.length ^ testHash.length;
			for (int i = 0; i < hash.length && i < testHash.length; i++) {
				diff |= hash[i] ^ testHash[i];
			}
			return diff == 0;

		} catch (Exception e) {
			logger.error("error validating the hash of the password: " + e.getMessage(), e);
			return false;
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//										utility methods 												     //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
	
	

}
