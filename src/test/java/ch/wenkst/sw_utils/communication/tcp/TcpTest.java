package ch.wenkst.sw_utils.communication.tcp;

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
public class TcpTest extends BaseTest {
	private String testHost = "127.0.0.1";
	private int testPort = 7778;
	private TcpTestClient tcpClient;
	private TcpTestServer tcpServer;
	
	
	@BeforeAll
	public void createServerAndClient() {
		setupServer();
		setupClient();
	}
	
	
	private void setupServer() {
		tcpServer = new TcpTestServer();
		tcpServer.init(testPort, "tcp-test-server");
		tcpServer.start();
	}
	
	
	private void setupClient() {
		tcpClient = new TcpTestClient();
		tcpClient.init(testHost, testPort, "tcp-test-client");
		tcpClient.connect();
		tcpClient.startClient();
	}
	
	
	@Test
	public void clientMessageToServer() throws InterruptedException, ExecutionException, TimeoutException {
		String clientMessage = "Hello Server!";
		tcpClient.sendTestMessage(clientMessage);
		TcpTestSession session = tcpServer.getTestSession();
		String receivedMessage = session.getMessageFuture().get(1, TimeUnit.SECONDS);
		Assertions.assertEquals(clientMessage, receivedMessage);
	}
	
	
	@Test
	public void serverMessageToClient() throws InterruptedException, ExecutionException, TimeoutException {
		String serverMessage = "Hello Client!";
		TcpTestSession session = tcpServer.getTestSession();
		session.sendTestMessage(serverMessage);
		String receivedMessage = tcpClient.getMessageFuture().get(1, TimeUnit.SECONDS);
		Assertions.assertEquals(serverMessage, receivedMessage);
	}
	

	
	@AfterAll
	public void tearDownClientAndServer() throws InterruptedException {
		tcpClient.disconnect();
		tcpClient.stopClient();
		tcpServer.stopServer();
		
		tcpServer.getTestSession().join();
		tcpClient.join();
		tcpServer.join();
	}
}
