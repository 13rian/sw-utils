package ch.wenkst.sw_utils.messaging.rabbit_mq;

import javax.net.ssl.SSLContext;

import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastPublisherRMQ;

public class ConnectionConfigRMQ {	
	private String host = "localhost"; 				// rabbitMQ server host
	private int port = 5672; 						// rabbitMQ server port, default for non-ssl is 5672
	private String username = "guest"; 				// user name of the rabbitMQ server account
	private String password = "guest"; 				// password of the rabbitmMQ server account
	private SSLContext sslContext = null; 			// the ssl context to handle the tls connection
	
	
	
	public ConnectionConfigRMQ host(String host) {
		this.host = host;
		return this;
	}
	
	
	public ConnectionConfigRMQ port(int port) {
		this.port = port;
		return this;
	}
	
	
	public ConnectionConfigRMQ username(String username) {
		this.username = username;
		return this;
	}
	
	
	public ConnectionConfigRMQ password(String password) {
		this.password = password;
		return this;
	}
	
	
	public ConnectionConfigRMQ sslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
		return this;
	}
	
	
	/**
	 * tests if the rabbit mq server is reachable
	 * @return 	true if reachable, false if not
	 */
	public boolean isReachable() {	
		BroadcastPublisherRMQ broadcaster = new BroadcastPublisherRMQ("testQueue");
		broadcaster.setup(this);
		
		boolean isReachable = true;
		try {
			broadcaster.publishToChannel(new MessageRMQ("test message"), "");
		} catch (Exception e) {
			isReachable = false;
		}
		
		if (isReachable) {
			broadcaster.disconnect();
		}
		
		return isReachable;
	}	
	

	public boolean isTls() {
		return sslContext != null;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}
}
