package ch.wenkst.sw_utils.crypto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.certs_and_keys.FileFormat;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyParsingException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityUtilsTest extends BaseTest {
	private String certDir;
	private String keyDir;
	
	
	@BeforeAll
	public void registerBcProvider() {
		CryptoProvider.registerBC();
		certDir = certDir();
		keyDir = keyDir();
	}
	
	
	private String certDir() {
		return Utils.getWorkDir() + File.separator + "resource" + File.separator + "cryptoUtils" + File.separator + "certs" + File.separator;
	}
	
	
	private String keyDir() {
		return Utils.getWorkDir() + File.separator + "resource" + File.separator + "keys" + File.separator;
	}
	
	
	@Test
	public void parsePemCertirifacteFile() throws FileNotFoundException, CertificateException, NoSuchProviderException {
		String pemCertPath = certDir + "server.cert.pem";
		X509Certificate cert = (X509Certificate) SecurityUtils.certFromFile(pemCertPath);
		Assertions.assertNotNull(cert);
	}
	
	
	@Test
	public void parseDerCertificateFile() throws FileNotFoundException, CertificateException, NoSuchProviderException {
		String derCertPath = certDir + "server.cert.cer";
		X509Certificate cert = (X509Certificate) SecurityUtils.certFromFile(derCertPath);
		Assertions.assertNotNull(cert);
	}
	
	
	@Test
	public void derFromPemCertificateFile() throws IOException {
		String pemCertPath = certDir + "server.cert.pem";
		byte[] certBytes = SecurityUtils.derFromCertFile(pemCertPath, FileFormat.PEM);
		Assertions.assertNotNull(certBytes);
	}
	
	
	@Test
	public void derFromDerCertificateFile() throws IOException {
		String derCertPath = certDir + "server.cert.cer";
		byte[] certBytes = SecurityUtils.derFromCertFile(derCertPath, FileFormat.DER);
		Assertions.assertNotNull(certBytes);
	}
	
	
	@Test
	public void certificateFromDer() throws IOException, CertificateException, NoSuchProviderException {
		String derCertPath = certDir + "server.cert.cer";
		byte[] certBytes = SecurityUtils.derFromCertFile(derCertPath, FileFormat.DER);
		X509Certificate cert = (X509Certificate) SecurityUtils.certFromDer(certBytes);
		Assertions.assertNotNull(cert);
	}
	
	
	@Test
	public void parseSec1EcPemKey() throws KeyParsingException {
		String filePath = keyDir + "eckey_sec1.pem";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@Test
	public void parsePkcs8EcPemKey() throws KeyParsingException {
		String filePath = keyDir + "eckey_pkcs8.pem";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@Test
	public void parseSec1EcDerKey() throws KeyParsingException {
		String filePath = keyDir + "eckey_sec1.der";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@Test
	public void parsePkcs8EcDerKey() throws KeyParsingException {
		String filePath = keyDir + "eckey_pkcs8.der";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}


	@Test
	public void parsePkcs1RsaPemKey() throws KeyParsingException {
		String filePath = keyDir + "rsakey_pkcs1.pem";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@Test
	public void parsePkcs8RsaPemKey() throws KeyParsingException {
		String filePath = keyDir + "rsakey_pkcs8.pem";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@Test
	public void parsePkcs1RsaDerKey() throws KeyParsingException {
		String filePath = keyDir + "rsakey_pkcs1.der";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@Test
	public void parsePkcs8RsaDerKey() throws KeyParsingException {
		String filePath = keyDir + "rsakey_pkcs8.der";
		PrivateKey key = SecurityUtils.keyFromFile(filePath);
		Assertions.assertNotNull(key);
	}
	
	
	@AfterAll
	public void unregisterBcProvider() {
		CryptoProvider.unregisterBC();
	}
}
