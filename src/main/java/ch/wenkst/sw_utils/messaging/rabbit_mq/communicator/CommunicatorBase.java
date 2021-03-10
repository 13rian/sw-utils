package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;

public class CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(CommunicatorBase.class);

	private Connection connection = null;
	protected Channel channel = null;
	
	
	// define the properties for the channels
	protected boolean durable = false; 			// if true the queue survives a server restart 
	protected boolean exclusive = false; 		// if true this queue is restricted to this connection
	protected boolean autoDelete = true; 		// if true the queue is deleted if no longer used
	

	/**
	 * base to create publishers and consumers to interact with the rabbitMQ server
	 * in order to be parallel, each consumer and publisher needs its own channel
	 */
	public CommunicatorBase() {
		
	}


	/**
	 * connects to the rabbitMQ server and creates a new channel
	 * @param connectionConfig	connection configuration
	 * @return 					true if successfully connected, false if a connection error occurred
	 */
	protected boolean connect(ConnectionConfigRMQ connectionConfig) {
		ConnectionFactory factory = createConnectionFactory(connectionConfig);
		return openConnectionAndChannel(factory);
	}
	
	
	private ConnectionFactory createConnectionFactory(ConnectionConfigRMQ connectionConfig) {
		ConnectionFactory factory = new ConnectionFactory();
		factory = new ConnectionFactory();
		factory.setHost(connectionConfig.getHost());
		factory.setUsername(connectionConfig.getUsername());
		factory.setPassword(connectionConfig.getPassword());
		factory.setPort(connectionConfig.getPort());
		
		SSLContext sslContext = connectionConfig.getSslContext();
		if (sslContext != null) {
			factory.useSslProtocol(sslContext); 
		}
		
		return factory;
	}
	
	
	private boolean openConnectionAndChannel(ConnectionFactory factory) {
		try {	
			connection = factory.newConnection();
			channel = connection.createChannel();     

			logger.info("successfully established a connection to rabbitMQ");
			return true;
			
		} catch (Exception e) {
			logger.info("error establishing a connection to rabbitMQ: ", e);
			return false;
		}
	}
	
	
	
	/**
	 * disconnects from the rabbitMQ server
	 */
	public void disconnect() {
		try {
			closeChannel();
			closeconnection();
			logger.info("successfully disconnected from the rabbitMQ server");

		} catch (Exception e) {
			logger.error("error disconnecting from the rabbitMQ server: ", e);
		}
	}
	
	
	private void closeChannel() throws IOException, TimeoutException {
		if (channel != null && channel.isOpen()) {
			channel.close();
			channel = null;
		}
	}
	
	
	private void closeconnection() throws IOException {
		if (connection != null && connection.isOpen()) {
			connection.close();
			connection = null;
		}
	}


	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}
}
