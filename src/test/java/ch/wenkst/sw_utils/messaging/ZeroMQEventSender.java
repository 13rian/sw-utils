package ch.wenkst.sw_utils.messaging;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.messaging.zero_mq.server_one_client.ClientProducerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.server_one_client.ServerConsumerZMQ;

public class ZeroMQEventSender {
	private static final Logger logger = LoggerFactory.getLogger(ZeroMQEventSender.class);
	
	
	protected Executor executor;
	private int threadCount = 10;
	
	
	public ZeroMQEventSender() {
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		((ThreadPoolExecutor) executor).setCorePoolSize(threadCount); 
	}
	
	
	public ClientProducerZMQ setupClientConsumer(int port) {
		ClientProducerZMQ client = new ClientProducerZMQ("localhost", port, "tcp") {
			@Override
			protected void onReceivedMessage(byte[] msgBytes) {
				logger.info("client producer received message: " + new String(msgBytes, StandardCharsets.UTF_8));				
			}
		};
		client.connect();
		
		return client;
	}
	
	
	public ServerConsumerZMQ setupServerConsumer(int port) {
		ServerConsumerZMQ server = new ServerConsumerZMQ("localhost", port, "tcp") {
			@Override
			protected void onReceivedMessage(byte[] msgBytes) {
				logger.info("server producer received message: " + new String(msgBytes, StandardCharsets.UTF_8));
			}
		};
		server.connect();
		return server;
	}
}
