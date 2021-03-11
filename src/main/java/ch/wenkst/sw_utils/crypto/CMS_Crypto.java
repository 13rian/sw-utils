package ch.wenkst.sw_utils.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyAgreeEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientId;
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;

import ch.wenkst.sw_utils.conversion.Conversion;

public class CMS_Crypto {
	
	/**
	 * encrypts the passed clear text with cms
	 * @param clearText			the message to encryapt
	 * @param senderSigKey		the signature key of the sender
	 * @param senderEncKey		the encryption key of the sender
	 * @param senderEncCert		the encryption certificate of the sender
	 * @param receiverEncCert	the encryption certificate of the sender
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 * @throws CertificateEncodingException
	 * @throws CMSException
	 * @throws IOException
	 */
	public byte[] cmsEncrypt(byte[] clearText, PrivateKey senderSigKey, PrivateKey senderEncKey, X509Certificate senderEncCert, X509Certificate receiverEncCert) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, CertificateEncodingException, CMSException, IOException {
		byte[] signature = sign(senderSigKey, clearText);
		String signedMessage = Conversion.byteArrayToBase64(clearText) + ":" + Conversion.byteArrayToBase64(signature);
		byte[] signedMessageBytes = signedMessage.getBytes(StandardCharsets.UTF_8);
		byte[] cipherText = createEnvelopeData(receiverEncCert, senderEncCert, senderEncKey, signedMessageBytes);
		return cipherText;
	}
	
	
	/**
	 * decrypts the passed cipher text with cms
	 * @param cipherText			the cipher text to decrypt
	 * @param receiverEncKey		the encryption key of the receiver
	 * @param receiverEncCert		the encryption certificate of the receiver
	 * @param senderSigCert			the signature certificate of the sender
	 * @return
	 * @throws CMSException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 */
	public byte[] cmsDecrypt(byte[] cipherText, PrivateKey receiverEncKey, X509Certificate receiverEncCert, X509Certificate senderSigCert) throws CMSException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		byte[] signedMessageBytes = decryptEnvelopeData(receiverEncCert, receiverEncKey, cipherText);
		String signedMessage = new String(signedMessageBytes, StandardCharsets.UTF_8);
		String[] parts = signedMessage.split(":");
		byte[] clearText = Conversion.base64StrToByteArray(parts[0]);
		byte[] signature = Conversion.base64StrToByteArray(parts[1]);
		
		boolean sigValid = verifySig(senderSigCert, clearText, signature);
		if (!sigValid) {
			throw new SignatureException("invalid signature");
		}
		return clearText;
	}


	/**
	 * CMS encrypt using the diffie-hellman key exchange protocol
	 * @param receiverCert	 	certificate of the receiver
	 * @param senderCert		certificate of the sender
	 * @param senderKey 		private key of the sender
	 * @param bytes 			bytes to encrypt
	 * @return 					the envelope data as byte array
	 * @throws CertificateEncodingException 
	 * @throws CMSException 
	 * @throws IOException 
	 */
	private byte[] createEnvelopeData(X509Certificate receiverCert, X509Certificate senderCert, PrivateKey senderKey, byte[] bytes) throws CertificateEncodingException, CMSException, IOException {
		CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
		JceKeyAgreeRecipientInfoGenerator infoGen = new JceKeyAgreeRecipientInfoGenerator(
				CMSAlgorithm.ECDH_SHA1KDF,
				senderKey,
				senderCert.getPublicKey(),
				CMSAlgorithm.AES128_WRAP);

		// add the recipient
		infoGen.addRecipient(receiverCert).setProvider(SecurityConstants.BC);
		edGen.addRecipientInfoGenerator(infoGen);

		// create the enveloped data
		CMSProcessableByteArray cmsProcData = new CMSProcessableByteArray(bytes);
		OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider(SecurityConstants.BC).build();
		CMSEnvelopedData ed = edGen.generate(cmsProcData, encryptor);
		return ed.getEncoded();
	}


	/**
	 * decryptes the enveloped cms data
	 * @param receiverCert	 	certificate of the receiver
	 * @param receiverKey 		private key of the receiver
	 * @param encryptedBytes	the cms Data
	 * @return	 				the decrypted byte array
	 * @throws CMSException 
	 */
	private byte[] decryptEnvelopeData(X509Certificate receiverCert, PrivateKey receiverKey, byte[] encryptedBytes) throws CMSException {
		CMSEnvelopedData ed = new CMSEnvelopedData(encryptedBytes);
		RecipientInformationStore recipients = ed.getRecipientInfos();
		RecipientId rid = new JceKeyAgreeRecipientId(receiverCert);
		RecipientInformation recipient = recipients.get(rid);
		byte[] content = recipient.getContent(new JceKeyAgreeEnvelopedRecipient(receiverKey).setProvider(SecurityConstants.BC));
		return content;
	}


	/**
	 * calculates the signature (ecdsa) of the passed byte array
	 * @param senderKey 	the private key of the sender that is used for the signature
	 * @param bytes 		the bytes to sign
	 * @return	 			the signature
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	private byte[] sign(PrivateKey senderKey, byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance(SecurityConstants.SHA256withECDSA, SecurityConstants.BC);
		signature.initSign(senderKey);
		signature.update(bytes);
		return signature.sign();
	}


	/**
	 * verifies the signature
	 * @param senderSigCert 	the certificate of the sender that was used for the signature
	 * @param dataBytes 		the bytes that were signed
	 * @param signatureBytes 	the signature
	 * @return	 				true if the signature is correct
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	private boolean verifySig(X509Certificate senderSigCert, byte[] dataBytes, byte[] signatureBytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
		PublicKey publicKey = senderSigCert.getPublicKey();
		Signature signature = Signature.getInstance(SecurityConstants.SHA256withECDSA, SecurityConstants.BC);
		signature.initVerify (publicKey);
		signature.update (dataBytes);
		return signature.verify(signatureBytes);
	}
}
