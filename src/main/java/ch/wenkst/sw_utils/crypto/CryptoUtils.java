package ch.wenkst.sw_utils.crypto;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;

public class CryptoUtils {
	private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
	
	
	private CryptoUtils() {
		
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
			return Conversion.byteArrayToBase64(encMessage);

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
			return cipher.doFinal(message);

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
			return new String(decMessage, StandardCharsets.UTF_8);

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
			return cipher.doFinal(encMessage);

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
			return signature.sign(); 												// create the signature

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
			return signature.verify(signatureBytes); 								// verify the signature

		} catch (Exception e) {
			logger.error("error during verify: ", e);
			return false;
		}
	}
}
