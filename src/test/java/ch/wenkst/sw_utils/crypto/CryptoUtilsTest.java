package ch.wenkst.sw_utils.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.conversion.Conversion;

public class CryptoUtilsTest {
	// encryption crypto material
	private static PrivateKey receiverEncKey = null; 			// receiver encryption key
	private static X509Certificate receiverEncCert = null;  	// receiver encryption certificate
	
	private static PrivateKey senderEncKey = null;   			// sender encryption encryption key
	private static X509Certificate senderEncCert = null; 		// sender encryption certificate
	
	
	// signature crypto material
	private static PrivateKey senderSigKey = null;   			// sender signature key
	private static X509Certificate senderSigCert = null; 		// sender signature certificate

	
	
	/**
	 * NOTE: The JCE unlimited strength file needs to be installed for the crypto to work
	 * register the bouncy castle provider for the crypto operations
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 */
	@BeforeAll
	public static void loadCryptoMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		SecurityUtils.registerBC();
		
		// load the keys and certificates
		// define the certificate directories
		String sep = File.separator;
		String cryptoUtilsDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep;
		String receiverEncDir = cryptoUtilsDir + "cmsCerts" + sep + "encryption" + sep + "receiver" + sep;
		String senderEncDir = cryptoUtilsDir + "cmsCerts" + sep + "encryption" + sep + "sender" + sep;
		String senderSigDir = cryptoUtilsDir + "cmsCerts" + sep + "signature" + sep + "sender" + sep;

		// encryption crypto material
		receiverEncKey = SecurityUtils.keyFromP12(receiverEncDir + "key.p12", "celsi-pw");
		receiverEncCert = (X509Certificate) SecurityUtils.certFromFile(receiverEncDir + "certificate.pem");  

		senderEncKey = SecurityUtils.keyFromP12(senderEncDir + "key.p12", "celsi-pw");   		
		senderEncCert = (X509Certificate) SecurityUtils.certFromFile(senderEncDir + "certificate.pem"); 		

		//  signature crypto material
		senderSigKey = SecurityUtils.keyFromP12(senderSigDir + "key.p12", "celsi-pw");   		
		senderSigCert = (X509Certificate) SecurityUtils.certFromFile(senderSigDir + "certificate.pem"); 		
	}


	/**
	 * tests the aes crypto
	 */
	@Test
	@DisplayName("aes crypto")
	public void aesTest() {
		int keyLength = 256;     											// 128,192,256 bits are allowed
		SecretKey secretKey = CryptoUtils.generateKey(keyLength); 			// the secret key to encrypt
		SecretKey wrongSecretKey = CryptoUtils.generateKey(keyLength); 		// a wrong secret key
		
		
		// encrypt a string
		String message = "secret message"; 									// clear text
		String encryptedMsg = CryptoUtils.encrypt(message, secretKey);	 	// encrypt the message
		
		// decrypt the cipher text with the correct key
		Assertions.assertEquals(message, CryptoUtils.decrypt(encryptedMsg, secretKey), "aes decryption");
		
		// decrypt the cipher text with the wrong key
		Assertions.assertNotEquals(message, CryptoUtils.decrypt(encryptedMsg, wrongSecretKey), "wrong key aes decryption");
		
		
		
		// encrypt only byte arrays
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		byte[] encryptedBytes = CryptoUtils.encrypt(messageBytes, secretKey);
		
		// decrypt the bytes with the correct key
		Assertions.assertArrayEquals(messageBytes, CryptoUtils.decrypt(encryptedBytes, secretKey), "aes decryption of byte array");		
		
		// decrypt the bytes with the wrong key
		MatcherAssert.assertThat("wrong key aes decryption of byte array", messageBytes, CoreMatchers.not(CryptoUtils.decrypt(encryptedBytes, wrongSecretKey)));
	}
	
	
	
	/**
	 * tests the cms crypto
	 * the signature and the encrypted message are sent together in a way that the receiver
	 * can distinguish between the two messages, for example:
	 * encrypt(message:signature)
	 */
	@Test
	@DisplayName("cms encryption")
	public void cmsTest() {
		String message = "secret message";
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		
		// 											test the encryption								 	     //
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		byte[] signature = CryptoUtils.sign(senderSigKey, messageBytes); 			// sig with the correct key
		byte[] signatureWrong = CryptoUtils.sign(receiverEncKey, messageBytes); 	// sig with the wrong key
		
		// generate the clear text
		String clearTextSigOk = Conversion.byteArrayToBase64(messageBytes) + ":" + Conversion.byteArrayToBase64(signature);
		String clearTextSigBad = Conversion.byteArrayToBase64(messageBytes) + ":" + Conversion.byteArrayToBase64(signatureWrong);
		byte[] cltSigOkBytes = clearTextSigOk.getBytes(StandardCharsets.UTF_8);
		byte[] cltSigBadBytes = clearTextSigBad.getBytes(StandardCharsets.UTF_8);
		
		// encrypt the clear text
		byte[] encOkSigOk = CryptoUtils.createEnvelopeData(receiverEncCert, senderEncCert, senderEncKey, cltSigOkBytes);
		byte[] encOkSigBad = CryptoUtils.createEnvelopeData(receiverEncCert, senderEncCert, senderEncKey, cltSigBadBytes);
		byte[] encBadSigOk = CryptoUtils.createEnvelopeData(receiverEncCert, senderEncCert, receiverEncKey, cltSigOkBytes);
		byte[] encBadSigBad = CryptoUtils.createEnvelopeData(receiverEncCert, senderEncCert, receiverEncKey, cltSigBadBytes);
		
		
		// decrypt with the correct key and cert
		Assertions.assertArrayEquals(cltSigOkBytes, CryptoUtils.decrypt(receiverEncCert, receiverEncKey, encOkSigOk), "enc ok sig ok decryption");
		Assertions.assertArrayEquals(cltSigBadBytes, CryptoUtils.decrypt(receiverEncCert, receiverEncKey, encOkSigBad), "enc ok sig bad decryption");
		MatcherAssert.assertThat("enc bad sig ok decryption", cltSigOkBytes, CoreMatchers.not(CryptoUtils.decrypt(receiverEncCert, receiverEncKey, encBadSigOk)));
		MatcherAssert.assertThat("enc bad sig bad decryption", cltSigBadBytes, CoreMatchers.not(CryptoUtils.decrypt(receiverEncCert, receiverEncKey, encBadSigBad)));
		
		
		// decrypt with the wrong key
		MatcherAssert.assertThat("wrong key decryption", encOkSigOk, CoreMatchers.not(CryptoUtils.decrypt(receiverEncCert, senderEncKey, encOkSigOk)));
		
		
		
		// 											test the signature								 	     //
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		Assertions.assertTrue(CryptoUtils.verifySig(senderSigCert, messageBytes, signature), "sig ok cert ok");
		Assertions.assertFalse(CryptoUtils.verifySig(receiverEncCert, messageBytes, signature), "sig ok cert bad"); 		
		Assertions.assertFalse(CryptoUtils.verifySig(senderSigCert, messageBytes, signatureWrong), "sig bad cert ok"); 	
		Assertions.assertFalse(CryptoUtils.verifySig(senderEncCert, messageBytes, signatureWrong), "sig bad cert bad"); 
	}
	
	
	/**
	 * tests the hash of the password that is put into the db
	 */
	@Test
	@DisplayName("password")
	public void passwordTest() {
		String password1 =  "securePW";
		String password2 =  "super_securePW";
		
		
		// hash the password
		String pwHash1 = SecurityUtils.hashPassword(password1);
		String pwHash2 = SecurityUtils.hashPassword(password2);
		
		// test the correct hash
		Assertions.assertTrue(SecurityUtils.validatePassword(password1, pwHash1), "correct password hash");
		Assertions.assertTrue(SecurityUtils.validatePassword(password2, pwHash2), "correct password hash");
		
		// test the wrong hash
		Assertions.assertFalse(SecurityUtils.validatePassword(password1, pwHash2), "wrong password hash");
		Assertions.assertFalse(SecurityUtils.validatePassword(password2, pwHash1), "wrong password hash");
	}
	
	
	/**
	 * load the private key from a pem file
	 */
	@Test
	@DisplayName("private key from pem")
	public void loadPemKeyTest() {
		
	}
	
	
	
	
	/**
	 * unregisters the bouncy castle security providers
	 */
	@AfterAll
	public static void unregisterProviders() {
		SecurityUtils.unregisterBC();
	}
}
