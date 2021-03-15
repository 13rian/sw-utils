package ch.wenkst.sw_utils.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;

public class AES_CryptoTest extends BaseTest {
	private AES_Crypto aesCrypto;
	private String clearText = "Secret message to encrypt";
	
	
	@BeforeAll
	public static void registerBcPRovider() {
		CryptoProvider.registerBC();
	}
	
	
	@BeforeEach
	public void setup() throws NoSuchAlgorithmException, NoSuchProviderException {
		aesCrypto = new AES_Crypto();
		aesCrypto.useNewKey(256);
	}
	
	
	@Test
	public void correctKeyEncryptionDecryption() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String cipherText = aesCrypto.encrypt(clearText);
		String decryptedText = aesCrypto.decrypt(cipherText);
		Assertions.assertEquals(clearText, decryptedText);
	}
	
	
	@Test
	public void wrongKeyEncryptionDecryption() {
	    Assertions.assertThrows(GeneralSecurityException.class, () -> {
	    	String cipherText = aesCrypto.encrypt(clearText);
	    	aesCrypto.useNewKey(256);
			String decryptedText = aesCrypto.decrypt(cipherText);
			Assertions.assertEquals(clearText, decryptedText);
	    });
	}
	
	
	@AfterAll
	public static void unregisterBcProvider() {
		CryptoProvider.unregisterBC();
	}
}
