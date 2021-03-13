package ch.wenkst.sw_utils.communication.tls;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.CryptoProvider;
import ch.wenkst.sw_utils.crypto.SecurityConstants;
import ch.wenkst.sw_utils.crypto.SecurityUtils;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyParsingException;
import ch.wenkst.sw_utils.crypto.tls.SSLContextGenerator;
import ch.wenkst.sw_utils.file.FileUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TlsTest extends BaseTest {
	private String certDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "tls_comm" + File.separator;
	private String caCertDir = certDir + "ca";
	private String clientCertDir = certDir + "client";
	private String serverCertDir = certDir + "server";
	
	private String tlsProtocol = SecurityConstants.TLS_1_3;
	private String testHost = "127.0.0.1";
	private int testPort = 7779;
	private TlsTestServer tlsServer;
	private TlsTestClient tlsClient;
	
	
	@BeforeAll
	public void createServerAndClient() throws CertificateException, NoSuchProviderException, KeyParsingException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
		CryptoProvider.registerBCJSSE();
		setupServer();
		setupClient();
	}
	
	
	private void setupClient() throws UnrecoverableKeyException, KeyManagementException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, KeyParsingException, IOException {
		tlsClient = new TlsTestClient();
		SSLContext sslContext = clientSslContext();
		tlsClient.init(testHost, testPort, sslContext, "tls-test-client");
		tlsClient.connect(1000);
		tlsClient.startClient();
	}
	
	
	private void setupServer() throws CertificateException, NoSuchProviderException, KeyParsingException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
		tlsServer = new TlsTestServer();
		SSLContext sslContext = serverSslContext();
		tlsServer.init(testPort, sslContext, true, "tls-test-server");
		tlsServer.start();
	}
	
	
	private SSLContext serverSslContext() throws KeyParsingException, CertificateException, NoSuchProviderException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
		String keyPath = FileUtils.findFileByPattern(serverCertDir, "", "pem");
		String certPath = FileUtils.findFileByPattern(serverCertDir, "", "cer");
		String caCertPath = FileUtils.findFileByPattern(caCertDir, "", "cer");
		
		PrivateKey privateKey = SecurityUtils.keyFromFile(keyPath);
		Certificate cert = SecurityUtils.certFromFile(certPath);
		Certificate caCert = SecurityUtils.certFromFile(caCertPath);
		List<Certificate> trustedCerts = Arrays.asList(new Certificate[] {caCert});
		
		return SSLContextGenerator.createSSLContext("test-pw", privateKey, cert, caCert, trustedCerts, tlsProtocol);
	}
	
	
	private SSLContext clientSslContext() throws KeyParsingException, CertificateException, NoSuchProviderException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
		String keyPath = FileUtils.findFileByPattern(clientCertDir, "", "pem");
		String certPath = FileUtils.findFileByPattern(clientCertDir, "", "cer");
		String caCertPath = FileUtils.findFileByPattern(caCertDir, "", "cer");
		
		PrivateKey privateKey = SecurityUtils.keyFromFile(keyPath);
		Certificate cert = SecurityUtils.certFromFile(certPath);
		Certificate caCert = SecurityUtils.certFromFile(caCertPath);
		List<Certificate> trustedCerts = Arrays.asList(new Certificate[] {caCert});
		
		return SSLContextGenerator.createSSLContext("test-pw", privateKey, cert, caCert, trustedCerts, tlsProtocol);
	}	
	
	
	@Test
	public void clientMessageToServer() throws InterruptedException, ExecutionException, TimeoutException {
		String clientMessage = "Hello Server!";
		tlsClient.sendTestMessage(clientMessage);
		TlsTestSession session = tlsServer.getTestSession();
		String receivedMessage = session.getMessageFuture().get(1, TimeUnit.SECONDS);
		Assertions.assertEquals(clientMessage, receivedMessage);
	}
	
	
	@Test
	public void serverMessageToClient() throws InterruptedException, ExecutionException, TimeoutException {
		String serverMessage = "Hello Client!";
		TlsTestSession session = tlsServer.getTestSession();
		session.sendTestMessage(serverMessage);
		String receivedMessage = tlsClient.getMessageFuture().get(1, TimeUnit.SECONDS);
		Assertions.assertEquals(serverMessage, receivedMessage);
	}
	

	
	@AfterAll
	public void tearDownClientAndServer() throws InterruptedException {
		tlsClient.disconnect();
		tlsClient.stopClient();
		tlsServer.stopServer();
		
		tlsServer.getTestSession().join();
		tlsClient.join();
		tlsServer.join();
		
		CryptoProvider.unregisterBCJSSE();
	}
}
