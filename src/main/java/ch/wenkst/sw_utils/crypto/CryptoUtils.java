package ch.wenkst.sw_utils.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERTaggedObject;
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
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.file.FileUtils;

public class CryptoUtils {
	private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

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
	 * loads the certificate in hex encoded der format from the passed certificate file path directory, the purpose specifies which certificate
	 * should be loaded
	 * @param certPath 		path of the cert file in cer format from which the certificate is read
	 * @return 				hex encoded der
	 */
	public static String loadDERCertificate(String certPath) {
		String result = "";

		// extract the certificate
		try {
			byte[] certBytes = FileUtils.readByteArrFromFile(certPath);
			result = Conversion.byteArrayToHexStr(certBytes);

		} catch (Exception e) {
			logger.error("error loading the certificate from " + certPath, e);
		}

		return result;
	}


	/**
	 * loads a security certificate from the byte array in the DER format
	 * @param certBytes 	byte array containing the certificate information	
	 * @return 				the Java object that contains the security certificate
	 */
	public static Certificate loadCertFromDER(byte[] certBytes) {
		try {
			// certificate factory
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
			ByteArrayInputStream is = new ByteArrayInputStream(certBytes); 
			Certificate cert = certFactory.generateCertificate(is);

			return cert;

		} catch (Exception e) {
			logger.error("error reading the certificate: ", e);
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
	
	
	/**
	 * loads the certificate from the passed file path
	 * @param path	 path to the certificate
	 * @return		 the certificate
	 */
	public static X509Certificate loadCertificate(String path) {				
		try {
			File keyFile = new File(path);
			FileInputStream keyInput = new FileInputStream(keyFile);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(keyInput);

		} catch (Exception e) {
			logger.error("error loading the certificate: ", e);
			return null;
		}
	}



	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											methods to handle keys 									 		 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * load the private key as hex encoded der from the passed keyFile path
	 * @param keyPath 		path of the key file in pem format from which the key is read
	 * @return 				hex encoded der
	 */
	public static String loadDERPrivateKey(String keyPath) {
		String result = "";

		try {
			InputStream inputStream = new FileInputStream(keyPath);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			StringBuilder strBuilder = new StringBuilder();


			String line = "";
			while (line != null) {
				line = bufferedReader.readLine(); 		// read the next line

				// check for the first line
				if (line.startsWith("-----BEGIN ") && line.endsWith(" PRIVATE KEY-----")) {
					continue;
				} 

				// check for the last line
				if (line.startsWith("-----END ") && line.endsWith(" PRIVATE KEY-----")) {
					break;
				}

				// append the line to the key
				strBuilder.append(line.trim());
			}

			// close the resources
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			byte[] keyBytes = Conversion.base64StrToByteArray(strBuilder.toString());

			// convert the der key to hex
			result = Conversion.byteArrayToHexStr(keyBytes);

		} catch (Exception e) {
			logger.error("error reading the key from ", e);
		}


		return result;
	}


	/**
	 * loads the private key from the byte array in DER format (PKCS8 encoded key)
	 * @param pkcs8key 		the byte array containing the key information
	 * @return 				the private key
	 */
	public static PrivateKey loadPrivateKeyFromDER(byte[] pkcs8key) {

		try {
			ASN1InputStream asninput = new ASN1InputStream(pkcs8key);
			ASN1Primitive p = null;
			String strPrivKey = null;
			String curveName = ""; 		// to extract the name of the curve

			while ((p = asninput.readObject()) != null) {
				// read out the private key
				ASN1Sequence asn1 = ASN1Sequence.getInstance(p);
				ASN1OctetString octstr = ASN1OctetString.getInstance(asn1.getObjectAt(1)); 	// private key is the second object
				strPrivKey = Conversion.byteArrayToHexStr(octstr.getOctets());				// as a control if correct key read
				//				System.out.println("privateKey: " + strPrivKey);				


				// read out the curve name of the private key
				DefaultAlgorithmNameFinder nameFinder = new DefaultAlgorithmNameFinder();
				ASN1Encodable asn1Enc = asn1.getObjectAt(2); 					// object identifier (oid) is the third object
				DERTaggedObject derObj = (DERTaggedObject)asn1Enc;
				ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)derObj.getObject();
				curveName = nameFinder.getAlgorithmName(oid);				
			}

			// close the input stream
			asninput.close();

			// create the curve parameters from the curve name
			ECNamedCurveParameterSpec curveParams = ECNamedCurveTable.getParameterSpec(curveName);
			BigInteger bigIntKey = new BigInteger(strPrivKey, 16); 			// key as big integer
			ECPrivateKeySpec priKeySpec = new ECPrivateKeySpec(bigIntKey, curveParams);

			// create the key from the private key specs
			KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
			PrivateKey privateKey = factory.generatePrivate(priKeySpec);

			return privateKey;

		} catch (Exception e) {
			logger.error("failed to read in key: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads the private key from the keyfile
	 * @param path	 	path to the key, only .p12
	 * @param password  export password of the private key
	 * @return	 		the private key
	 */
	public static PrivateKey loadPrivateKey(String path, String password) {		
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			File keyFile = new File(path);

			FileInputStream fis = new FileInputStream(keyFile);
			ks.load(fis, password.toCharArray());

			Enumeration<String> aliases = ks.aliases();
			String alias = aliases.nextElement();
			return (PrivateKey) ks.getKey(alias, password.toCharArray());

		} catch (Exception e) {
			logger.error("error loading private key: ", e);
			return null;
		}
	}
	



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
