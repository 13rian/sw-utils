package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;
import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;

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
			channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, durable, autoDelete, null);
			
			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}	
	
	
	/**
	 * broadcasts a message to all listening consumers on a defines exchange
	 * @param message 			the message to send
	 * @return 					true if the message was successfully published, false if an error occurred
	 */
	public boolean publishMessage(MessageRMQ message) {
		try {
			// publish the message on the exchange, the temporary queue is used
			channel.basicPublish(exchangeName, "", message.getProperties(), message.getBody());
			return true;
			
		} catch (Exception e) {
			logger.error("error broadcasting message on exchangeName " + exchangeName + ": ", e);
			return false;
		}
	}
}
