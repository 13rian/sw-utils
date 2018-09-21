package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import java.util.ArrayList;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ch.wenkst.sw_utils.crypto.tls.ec.SSLContextGenerator;
import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;

public class CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQHander.class);

	protected RabbitMQHander mqHandler = null; 	// handler that manages the interaction with rabbitMQ

	private Connection connection = null; 	// holds the connection to the rabbitMQ server
	protected Channel channel = null; 			// channel of the rabbitMQ server on which new queues can be declared


	/**
	 * base to create publishers and consumers to interact with the rabbitMQ server
	 * in order to be parallel, each consumer and publsiher needs its own channel
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

		if (mqHandler.isTLS()) {
			// setup the ssl context
			// define the keys and certs that are used for the tls connection
			ArrayList<String> trustedCerts = new ArrayList<>();
			trustedCerts.add(mqHandler.getCaCertPath());
			String tlsProtocol = "TLSv1.2";

			// create the ssl context
			SSLContext sslContext = SSLContextGenerator.createSSLContext(mqHandler.getP12FilePath(), mqHandler.getP12Password(), trustedCerts, tlsProtocol);

			// configure the tls encrypted connection
			factory = new ConnectionFactory();
			factory.setHost(mqHandler.getHost());
			factory.setUsername(mqHandler.getUsername());
			factory.setPassword(mqHandler.getPassword());
			factory.setPort(5671); 					// port for the tls connection
			factory.useSslProtocol(sslContext); 	// set the ssl context
		
		} else {
			// configure the non encrypted connection
			factory = new ConnectionFactory();
			factory.setHost(mqHandler.getHost());
			factory.setUsername(mqHandler.getUsername());
			factory.setPassword(mqHandler.getPassword());
		}

		
		// open a new connection and create a new channel
		try {	
			// open the connection
			connection = factory.newConnection();

			// create the channel
			channel = connection.createChannel();     

			logger.info("successfully established a " + ((mqHandler.isTLS()) ? "tls" : "non") + "-connection to rabbitMQ");
			return true;
			
		} catch (Exception e) {
			logger.info("error establishing a " + ((mqHandler.isTLS()) ? "tls" : "non") + "-encrypted connection to rabbitMQ: ", e);
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
