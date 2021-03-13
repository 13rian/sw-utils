package ch.wenkst.sw_utils.communication.tls;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import ch.wenkst.sw_utils.communication.tls.server.TlsSession;

public class TlsTestSession extends TlsSession {
	private CompletableFuture<String> messageFuture = new CompletableFuture<>();

	
	@Override
	protected void processMessage(byte[] message) {
		String textMessage = new String(message, StandardCharsets.UTF_8);
		messageFuture.complete(textMessage);
	}
	
	
	public void sendTestMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes);
	}

	public CompletableFuture<String> getMessageFuture() {
		return messageFuture;
	}
}
