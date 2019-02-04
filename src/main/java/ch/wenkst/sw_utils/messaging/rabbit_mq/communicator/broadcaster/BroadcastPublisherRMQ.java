package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;

public class BroadcastPublisherRMQ extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(BroadcastPublisherRMQ.class);  
	
	// name of the exchange, any queue can be bound to this exchange and receive messages, this way messages are
	// not just sent to one named queue
	private String exchangeName = "";  		 
	

	/**
	 * publishes a message that is broadcasted to all consumers that listen for the declared exchange
	 * @param mqHandler 	handler that manages the interaction with rabbitMQ
	 * @param exchangeName 	name of the exchange, any queue can be bound to this exchange and receive messages
	 */
	public BroadcastPublisherRMQ(RabbitMQHander mqHandler, String exchangeName) {
		super(mqHandler);
		
		this.exchangeName = exchangeName;
		
		// declare a new exchange
		declareExchange();		
	}
	
	
	/**
	 * declares a new exchange and bind a temporary queue to it, that is removed after the client disconnected 
	 */
	private void declareExchange() {
		try {
			// declare the new exchange, fanout means it is sent to all queues that are bound to this exchange
			channel.exchangeDeclare(exchangeName, "fanout");   
			
			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}
	
	
	
	/**
	 * broadcasts a message to all listening consumers on a defines exchange
	 * @param messageBytes 			message bytes to send
	 * @param headerProperties 		the message header properties
	 */
	public void publishMessage(byte[] messageBytes, Map<String, Object> headerProperties) {
		try {
			// publish the message on the exchange, the temporary queue is used
			AMQP.BasicProperties msgProperties = MessageProperties.TEXT_PLAIN;
			msgProperties = msgProperties.builder().headers(headerProperties).build();
			channel.basicPublish(exchangeName, "", msgProperties, messageBytes);

		} catch (Exception e) {
			logger.error("error broadcastiing message on exchangeName " + exchangeName + ": ", e);
		}
	}
	
	
	
	/**
	 * broadcasts a message to all listening consumers on a defines exchange
	 * @param messageBytes 		message bytes to send
	 * @return 					true if the message was successfully published, false if an error occurred
	 */
	public boolean publishMessage(byte[] messageBytes) {
		try {
			// publish the message on the exchange, the temporary queue is used
			channel.basicPublish(exchangeName, "", null, messageBytes);
			return true;
			
		} catch (Exception e) {
			logger.error("error broadcasting message on exchangeName " + exchangeName + ": ", e);
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
