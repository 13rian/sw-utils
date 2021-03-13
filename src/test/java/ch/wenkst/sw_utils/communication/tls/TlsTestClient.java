package ch.wenkst.sw_utils.communication.tls;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import ch.wenkst.sw_utils.communication.tls.client.TlsClient;

public class TlsTestClient extends TlsClient {
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
		System.out.println("start work was called");
	}

	@Override
	public void terminateWork() {
			
	}


	public CompletableFuture<String> getMessageFuture() {
		return messageFuture;
	}
}
