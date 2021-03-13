package ch.wenkst.sw_utils.communication.udp;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import ch.wenkst.sw_utils.communication.udp.server.UdpServer;

public class UdpTestServer extends UdpServer {
	private int remotePort;
	private CompletableFuture<String> messageFuture = new CompletableFuture<>();


	@Override
	protected void processMessage(byte[] message, InetAddress address, int remotePort) {
		this.remotePort = remotePort;
		String textMessage = new String(message, StandardCharsets.UTF_8);
		messageFuture.complete(textMessage);	
	}
	
	
	public void sendTestMessage(String host, String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes, host, remotePort);
	}


	public CompletableFuture<String> getMessageFuture() {
		return messageFuture;
	}
}
