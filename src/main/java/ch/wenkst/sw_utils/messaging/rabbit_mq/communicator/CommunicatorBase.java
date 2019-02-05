package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;

public class CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(CommunicatorBase.class);

	protected RabbitMQHander mqHandler = null; 	// handler that manages the interaction with rabbitMQ

	private Connection connection = null; 		// holds the connection to the rabbitMQ server
	protected Channel channel = null; 			// channel of the rabbitMQ server on which new queues can be declared
	
	
	// define the properties for the channels
	protected boolean durable = false; 			// if true the queue survives a server restart 
	protected boolean exclusive = false; 		// if true this queue is restricted to this connection
	protected boolean autoDelete = true; 		// if true the queue is deleted if no longer used
	

	/**
	 * base to create publishers and consumers to interact with the rabbitMQ server
	 * in order to be parallel, each consumer and publisher needs its own channel
	 * @param mqHandler 	handler that manages the interaction with rabbitMQ 
	 */
	public CommunicatorBase(RabbitMQHander mqHandler) {
		this.mqHandler = mqHandler;
		
		// connect to the server
		connect();
	}


	/**
	 * connects to the rabbitMQ server and creates a new channel. The TLS connection is setup if the
	 * isTLS flag of the mqHandler is set to true
	 * @return 		true if successfully connected, false if a connection error occurred
	 */
	private boolean connect() {
		ConnectionFactory factory = null;

		if (mqHandler.getSslContext() != null) {
			// configure the tls encrypted connection
			factory = new ConnectionFactory();
			factory.setHost(mqHandler.getHost());
			factory.setUsername(mqHandler.getUsername());
			factory.setPassword(mqHandler.getPassword());
			factory.setPort(mqHandler.getPort()); 	 																	
			factory.useSslProtocol(mqHandler.getSslContext()); 
		
		} else {
			// configure the non encrypted connection
			factory = new ConnectionFactory();
			factory.setHost(mqHandler.getHost());
			factory.setUsername(mqHandler.getUsername());
			factory.setPassword(mqHandler.getPassword());
			factory.setPort(mqHandler.getPort()); 
		}

		
		// open a new connection and create a new channel
		try {	
			// open the connection
			connection = factory.newConnection();

			// create the channel
			channel = connection.createChannel();     

			logger.info("successfully established a " + ((mqHandler.isTls()) ? "tls" : "non") + "-connection to rabbitMQ");
			return true;
			
		} catch (Exception e) {
			logger.info("error establishing a " + ((mqHandler.isTls()) ? "tls" : "non") + "-encrypted connection to rabbitMQ: ", e);
			return false;
		}
	}
	
	
	
	/**
	 * disconnects from the rabbitMQ server
	 */
	public void disconnect() {
		try {
			// close the channel
			if (channel != null && channel.isOpen()) {
				channel.close();
				channel = null;
			}

			// close the connection
			if (connection != null && connection.isOpen()) {
				connection.close();
				connection = null;
			}

			logger.info("successfully disconnected from the rabbitMQ server");

		} catch (Exception e) {
			logger.error("error disconnecting from the rabbitMQ server: ", e);
		}
	}

}
