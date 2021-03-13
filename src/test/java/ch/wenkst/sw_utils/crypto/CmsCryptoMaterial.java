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

import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyParsingException;
import ch.wenkst.sw_utils.file.FileUtils;

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
	
	
	public void loadCryptoMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, KeyParsingException {
		loadReceiverEncryptionMaterial();
		loadSenderEncryptionMaterial();
		loadSenderSignatureMaterial();
	}
	
	
	private String cmsCertDir() {
		String sep = File.separator;
		String cryptoUtilsDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep;
		return cryptoUtilsDir + "cmsCerts" + sep;
	}
	
	
	private void loadReceiverEncryptionMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, KeyParsingException {
		String receiverEncDir = cmsCertDir + "encryption" + File.separator + "receiver";
		String certFile = FileUtils.findFileByPattern(receiverEncDir, "", "cer");
		String keyFile = FileUtils.findFileByPattern(receiverEncDir, "", "pem");
		receiverEncKey = SecurityUtils.keyFromFile(keyFile);
		receiverEncCert = (X509Certificate) SecurityUtils.certFromFile(certFile);
	}
	
	
	private void loadSenderEncryptionMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, KeyParsingException {
		String senderEncDir = cmsCertDir + "encryption" + File.separator + "sender";
		String certFile = FileUtils.findFileByPattern(senderEncDir, "", "cer");
		String keyFile = FileUtils.findFileByPattern(senderEncDir, "", "pem");
		senderEncKey = SecurityUtils.keyFromFile(keyFile);
		senderEncCert = (X509Certificate) SecurityUtils.certFromFile(certFile); 
	}
	
	
	private void loadSenderSignatureMaterial() throws UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, KeyParsingException {
		String senderSigDir = cmsCertDir + "signature" + File.separator + "sender";
		String certFile = FileUtils.findFileByPattern(senderSigDir, "", "cer");
		String keyFile = FileUtils.findFileByPattern(senderSigDir, "", "pem");
		senderSigKey = SecurityUtils.keyFromFile(keyFile);	
		senderSigCert = (X509Certificate) SecurityUtils.certFromFile(certFile); 
	}
}
