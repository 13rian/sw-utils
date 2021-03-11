package ch.wenkst.sw_utils.crypto;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CmsCryptoMaterial {
	private String cmsCertDir;
	
	public PrivateKey receiverEncKey;
	public X509Certificate receiverEncCert;
	
	public PrivateKey senderEncKey;
	public X509Certificate senderEncCert;
	
	public PrivateKey senderSigKey;
	public X509Certificate senderSigCert;
	
	
	public CmsCryptoMaterial() {
		cmsCertDir = cmsCertDir();
	}
	
	
	public void loadCryptoMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		loadReceiverEncryptionMaterial();
		loadSenderEncryptionMaterial();
		loadSenderSignatureMaterial();
	}
	
	
	private String cmsCertDir() {
		String sep = File.separator;
		String cryptoUtilsDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep;
		return cryptoUtilsDir + "cmsCerts" + sep;
	}
	
	
	private void loadReceiverEncryptionMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		String receiverEncDir = cmsCertDir + "encryption" + File.separator + "receiver" + File.separator;
		receiverEncKey = SecurityUtils.keyFromP12(receiverEncDir + "key.p12", "celsi-pw");
		receiverEncCert = (X509Certificate) SecurityUtils.certFromFile(receiverEncDir + "certificate.pem"); 
	}
	
	
	private void loadSenderEncryptionMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		String senderEncDir = cmsCertDir + "encryption" + File.separator + "sender" + File.separator;
		senderEncKey = SecurityUtils.keyFromP12(senderEncDir + "key.p12", "celsi-pw");   		
		senderEncCert = (X509Certificate) SecurityUtils.certFromFile(senderEncDir + "certificate.pem");
	}
	
	
	private void loadSenderSignatureMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		String senderSigDir = cmsCertDir + "signature" + File.separator + "sender" + File.separator;
		senderSigKey = SecurityUtils.keyFromP12(senderSigDir + "key.p12", "celsi-pw");   		
		senderSigCert = (X509Certificate) SecurityUtils.certFromFile(senderSigDir + "certificate.pem"); 
	}
}
