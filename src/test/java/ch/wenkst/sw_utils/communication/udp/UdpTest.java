package ch.wenkst.sw_utils.communication.udp;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ch.wenkst.sw_utils.BaseTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UdpTest extends BaseTest {
	private String testHost = "127.0.0.1";
	private int testPort = 7779;
	private UdpTestClient udpClient;
	private UdpTestServer udpServer;
	
	
	@BeforeAll
	public void createServerAndClient() {
		setupServer();
		setupClient();
	}
	
	
	private void setupServer() {
		udpServer = new UdpTestServer();
		udpServer.init(testPort, "tcp-test-server");
		udpServer.startServer();
	}
	
	
	private void setupClient() {
		udpClient = new UdpTestClient();
		udpClient.init(testHost, testPort, "udp-test-client");
		udpClient.connect();
		udpClient.startClient();
	}
	
	
	@Test
	public void clientMessageToServer() throws InterruptedException, ExecutionException, TimeoutException {
		String clientMessage = "Hello Server!";
		udpClient.sendTestMessage(clientMessage);
		String receivedMessage = udpServer.getMessageFuture().get(1, TimeUnit.SECONDS);
		Assertions.assertEquals(clientMessage, receivedMessage);
	}
	
	
	@Test
	public void serverMessageToClient() throws InterruptedException, ExecutionException, TimeoutException {
		String serverMessage = "Hello Client!";
		udpServer.sendTestMessage(testHost, serverMessage);
		String receivedMessage = udpClient.getMessageFuture().get(1, TimeUnit.SECONDS);
		Assertions.assertEquals(serverMessage, receivedMessage);
	}
	

	
	@AfterAll
	public void tearDownClientAndServer() throws InterruptedException {
		udpClient.disconnect();
		udpClient.stopClient();
		udpServer.stopServer();
		
		udpClient.join();
		udpServer.join();
	}
}
