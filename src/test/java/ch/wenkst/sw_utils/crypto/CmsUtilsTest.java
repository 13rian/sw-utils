package ch.wenkst.sw_utils.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

import org.bouncycastle.cms.CMSException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyParsingException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CmsUtilsTest extends BaseTest {
	private CmsCryptoMaterial cryptoMaterial;
	private CMS_Crypto cmsCrypto;
	private String clearText = "Hello";
	private byte[] clearTextBytes = clearText.getBytes(StandardCharsets.UTF_8);
	
	@BeforeAll
	public void registerBcPRovider() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, KeyParsingException {
		CryptoProvider.registerBC();
		cryptoMaterial = new CmsCryptoMaterial();
		cryptoMaterial.loadCryptoMaterial();
		cmsCrypto = new CMS_Crypto();
	}
	
	
	@Test
	public void correctCmsEncryption() throws InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, CMSException, IOException {
		byte[] cipherText = cmsCrypto.cmsEncrypt(
				clearTextBytes,
				cryptoMaterial.senderSigKey,
				cryptoMaterial.senderEncKey,
				cryptoMaterial.senderEncCert,
				cryptoMaterial.receiverEncCert);
		
		byte[] decrypted = cmsCrypto.cmsDecrypt(
				cipherText,
				cryptoMaterial.receiverEncKey,
				cryptoMaterial.receiverEncCert,
				cryptoMaterial.senderSigCert);
		
		String clearTextDecrypted = new String(decrypted, StandardCharsets.UTF_8);
		Assertions.assertEquals(clearText, clearTextDecrypted);
	}
	
	
	@Test
	public void wrongKeyCmsDecryption() throws InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, CMSException, IOException {
		byte[] cipherText = cmsCrypto.cmsEncrypt(
				clearTextBytes,
				cryptoMaterial.senderSigKey,
				cryptoMaterial.senderEncKey,
				cryptoMaterial.senderEncCert,
				cryptoMaterial.receiverEncCert);
		
		
		Assertions.assertThrows(CMSException.class, () -> {
			cmsCrypto.cmsDecrypt(
					cipherText,
					cryptoMaterial.senderSigKey, 		// this is the wrong decryption key (receiverEncKey)
					cryptoMaterial.receiverEncCert,
					cryptoMaterial.senderSigCert);
	    });
	}
	
	
	@Test
	public void wrongSigCmsDencryption() throws InvalidKeyException, CertificateEncodingException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, CMSException, IOException {
		byte[] cipherText = cmsCrypto.cmsEncrypt(
				clearTextBytes,
				cryptoMaterial.senderSigKey,
				cryptoMaterial.senderEncKey,
				cryptoMaterial.senderEncCert,
				cryptoMaterial.receiverEncCert);
		
		
		Assertions.assertThrows(SignatureException.class, () -> {
			cmsCrypto.cmsDecrypt(
					cipherText,
					cryptoMaterial.receiverEncKey,
					cryptoMaterial.receiverEncCert,
					cryptoMaterial.receiverEncCert); 			// this is the wrong sender sig cert
	    });
	}
	
	
	@AfterAll
	public void unregisterBcProviders() {
		CryptoProvider.unregisterBC();
	}
}
