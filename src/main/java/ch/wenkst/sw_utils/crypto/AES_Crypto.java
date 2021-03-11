package ch.wenkst.sw_utils.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.wenkst.sw_utils.conversion.Conversion;

public class AES_Crypto {
	private SecretKey secretKey;
	
	
	/**
	 * generates a key used for the AES encryption
	 * @param keyLength 	number of bits for the key: 128, 192 or 256 bits
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void useNewKey(int keyLength) throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyGenerator keyGen = KeyGenerator.getInstance(SecurityConstants.AES, SecurityConstants.BC);
		keyGen.init(keyLength, new SecureRandom()); 							
		secretKey = keyGen.generateKey();
	}
	
	
	/**
	 * use the passed key for encryption and decryption
	 * @param secretKey
	 */
	public void useSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}


	/**
	 * encrypts the passed message with the passed key using AES
	 * @param clearText 	the message to encrypt
	 * @return 			the encrypted string as base64 string
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public String encrypt(String clearText) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] clearTextBytes = clearText.getBytes(StandardCharsets.UTF_8);
		byte[] cipherTextBytes = encrypt(clearTextBytes);
		return Conversion.byteArrayToBase64(cipherTextBytes);   
	}


	/**
	 * encrypts the passed message
	 * @param clearText 	the message bytes to encrypt
	 * @return 				the encrypted bytes
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public byte[] encrypt(byte[] clearText) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), SecurityConstants.AES);
		Cipher cipher = Cipher.getInstance(SecurityConstants.AES, SecurityConstants.BC);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);	    
		return cipher.doFinal(clearText);   
	}
	
	


	/**
	 * decrypts the passed message with the passed key using AES
	 * @param cipherText 	the encrypted message (base64 string)
	 * @return 				the decrypted message as base64 string
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public String decrypt(String cipherText) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		byte[] cipherTextBytes = Conversion.base64StrToByteArray(cipherText);
		byte[] clearTextBytes = decrypt(cipherTextBytes);
		return new String(clearTextBytes, StandardCharsets.UTF_8);   
	}



	/**
	 * decrypts the passed message with the passed key using AES
	 * @param cipherText 	the encrypted message
	 * @return 				the decrypted message
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public byte[] decrypt(byte[] cipherText) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), SecurityConstants.AES);
		Cipher cipher = Cipher.getInstance(SecurityConstants.AES, SecurityConstants.BC);
		cipher.init(Cipher.DECRYPT_MODE, keySpec);	    
		return cipher.doFinal(cipherText);
	}
}
