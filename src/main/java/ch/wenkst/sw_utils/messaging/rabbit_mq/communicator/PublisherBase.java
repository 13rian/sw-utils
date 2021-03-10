package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;

public abstract class PublisherBase extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(PublisherBase.class);
	
	protected String exchangeName;
	protected String queueName;

	public PublisherBase(String exchangeName) {
		this.exchangeName = exchangeName;
	}
	
	
	public abstract void setup(ConnectionConfigRMQ connectionConfig);
	
	
	/**
	 * declares a new exchange
	 * @param type		type of the exchange
	 */
	protected void declareExchange(BuiltinExchangeType type) {		
		try {
			channel.exchangeDeclare(exchangeName, type, durable, autoDelete, null);
			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}
	
	
	protected void declareQueue() {		
		try {
			channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
			logger.info("successfully declared a new queue " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new queue " + exchangeName + ": ", e);
		}
	}
	
	
	/**
	 * broadcasts a message
	 * @param message 			the message to send
	 */
	public void publishMessage(MessageRMQ message) {
		publishMessage(message, "");
	}
	
	
	/**
	 * broadcasts a message
	 * @param message 			the message to send
	 * @param routingKey 		the routing key of the message
	 */
	public void publishMessage(MessageRMQ message, String routingKey) {
		try {
			publishToChannel(message, routingKey);
			
		} catch (Exception e) {
			logger.error("error broadcasting message on exchangeName " + exchangeName + ": ", e);
		}
	}
	
	
	public void publishToChannel(MessageRMQ message, String routingKey) throws IOException {
		channel.basicPublish(exchangeName, routingKey, message.getProperties(), message.getBody());
	}
}
