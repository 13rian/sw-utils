package ch.wenkst.sw_utils.communication.tcp;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import ch.wenkst.sw_utils.communication.tcp.server.TcpSession;

public class TcpTestSession extends TcpSession {
	private CompletableFuture<String> messageFuture = new CompletableFuture<>();

	
	@Override
	protected void processMessage(byte[] message) {
		String textMessage = new String(message, StandardCharsets.UTF_8);
		messageFuture.complete(textMessage);
	}

	@Override
	public void startWork() {
		
	}
	
	public void sendTestMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes);
	}

	public CompletableFuture<String> getMessageFuture() {
		return messageFuture;
	}
}
