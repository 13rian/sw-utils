package ch.wenkst.sw_utils.communication.udp;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import ch.wenkst.sw_utils.communication.udp.client.UdpClient;

public class UdpTestClient extends UdpClient {
	private CompletableFuture<String> messageFuture = new CompletableFuture<>();
	
	
	public void sendTestMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8); 
		sendMessage(messageBytes);
	}
	

	@Override
	protected void processMessage(byte[] message) {
		String textMessage = new String(message, StandardCharsets.UTF_8);
		messageFuture.complete(textMessage);
	}
	

	@Override
	public void startWork() {

	}

	@Override
	public void terminateWork() {
			
	}


	public CompletableFuture<String> getMessageFuture() {
		return messageFuture;
	}
}
