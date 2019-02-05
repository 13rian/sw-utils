package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;
import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;

public class RoutingPublisherRMQ extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(RoutingPublisherRMQ.class);
	
	// name of the exchange, any queue can be bound to this exchange and receive messages, this way messages are
	// not just sent to one named queue
	private String exchangeName = ""; 
	
	
	/**
	 * publishes a message that is broadcasted to all consumers that listen for the declared exchange. Additionally,
	 * a routing key is defined that defines a subset of the messages published
	 * @param mqHandler 	handler that manages the interaction with rabbitMQ
	 * @param exchangeName 	name of the exchange, any queue can be bound to this exchange and receive messages
	 */
	public RoutingPublisherRMQ(RabbitMQHander mqHandler, String exchangeName) {
		super(mqHandler);
		
		this.exchangeName = exchangeName;
		
		// declare a new exchange
		declareExchange();		
	}
	
	
	/**
	 * declares a new exchange
	 */
	private void declareExchange() {
		try {
			// declare the new exchange, direct: a message goes to the queues whose binding key exactly matches
			// the routing key of the message.
			channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, durable, autoDelete, null);
			
			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}	
	
	
	/**
	 * broadcasts a message to all listening consumers on a defines exchange
	 * @param message 			the message to send
	 * @param routingKey 		defines a subset of all messages, consumers can listen to only a
	 * 							subset of the routing keys
	 */
	public void publishMessage(MessageRMQ message, String routingKey) {
		try {
			// publish the message on the exchange, the temporary queue is used
			channel.basicPublish(exchangeName, routingKey, message.getProperties(), message.getBody());

		} catch (Exception e) {
			logger.error("error broadcasting message on exchangeName " + exchangeName + ", routing key " + routingKey + ": ", e);
		}
	}
	
}
