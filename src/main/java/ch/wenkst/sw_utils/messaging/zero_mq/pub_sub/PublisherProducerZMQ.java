package ch.wenkst.sw_utils.messaging.zero_mq.pub_sub;

import org.zeromq.ZMQ;

import ch.wenkst.sw_utils.messaging.zero_mq.ClientZMQ;

public class PublisherProducerZMQ extends ClientZMQ {
	
	/**
	 * publisher that send messages with a certain key
	 * @param host 			the host of the broker to which the publishers connect
	 * @param port 			the port of the broker to which the publishers connect
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public PublisherProducerZMQ(String host, int port, String protocol) {
		super(host, port, protocol);
	}
	
	
	/**
	 * opens the connection for the communication
	 */
	public void connect() {
		super.connect(ZMQ.PUB);
	}
	
	
	/**
	 * publishes a message with a key, the string will be utf-8 encoded
	 * @param message 	the message to publish
	 * @param key 		the key of the message to which subscribers can subscribe
	 */
	public void sendMessage(String message, String key) {
		super.sendMessage(message, key);
	}
	
	
	/**
	 * publishes a message with a key
	 * @param message 	the message to publish
	 * @param key 		the key of the message to which subscribers can subscribe
	 */
	public synchronized void sendMessage(byte[] message, String key) {
		super.sendMessage(message, key);
	}
	
	
	/**
	 * closes all connections
	 */
	@Override
	public void disconnect() {
		super.disconnect();
	}

}
