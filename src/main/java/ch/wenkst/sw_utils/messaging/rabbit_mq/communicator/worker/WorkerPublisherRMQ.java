package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;

public class WorkerPublisherRMQ extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(WorkerPublisherRMQ.class);
	
	
	private String queueName = "";  		// name of the queue on which messages are published
	

	/**
	 * publishes a message that is only received by one of the registered workerConsumers and acknowledged
	 * even if a worker dies processing a message the message will not get acknowledged and is resent again
	 * @param mqHandler 	handler that manages the interaction with rabbitMQ
	 * @param queueName 	name of the queue on which messages are published
	 */
	public WorkerPublisherRMQ(RabbitMQHander mqHandler, String queueName) {
		super(mqHandler);
		
		this.queueName = queueName;
		
		// declare a new queue
		declareQueue();		
	}
	
	
	/**
	 * declares a new queue 
	 */
	private void declareQueue() {
		boolean durable = false; 		// if true the queue survives a server restart 
		boolean exclusive = false; 		// if true this queue is restricted to this connection
		boolean autoDelete = true; 		// if true the queue is deleted if no longer used

		try {
			// declare the new queue
			channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
			logger.info("successfully declared a new queue " + queueName);

		} catch (Exception e) {
			logger.error("error declaring the new queue " + queueName + ": ", e);
		}
	}
	
	
	
	/**
	 * sends a message to the passed routing key, the routing key is the name of the queue a receiver needs to listen
	 * @param messageBytes 			message bytes to send
	 * @param headerProperties 		the message header properties
	 */
	public void publishMessage(byte[] messageBytes, Map<String, Object> headerProperties) {
		try {
			// name of the exchange is empty (first param), which denotes the default or nameless exchange: 
			// messages are routed to the queue with the name specified by routingKey (here queueName), if it exists.
			AMQP.BasicProperties msgProperties = MessageProperties.TEXT_PLAIN;
			msgProperties = msgProperties.builder().headers(headerProperties).build();
			channel.basicPublish("", queueName, msgProperties, messageBytes);

		} catch (Exception e) {
			logger.error("error sending message to queue " + queueName + ": ", e);
		}
	}
	
	
	/**
	 * sends a message to the passed routing key, the routing key is the name of the queue a receiver needs to listen
	 * @param messageBytes 			message bytes to send
	 */
	public void publishMessage(byte[] messageBytes) {
		try {
			// name of the exchange is empty (first param), which denotes the default or nameless exchange: 
			// messages are routed to the queue with the name specified by routingKey (here queueName), if it exists.
			channel.basicPublish("", queueName, null, messageBytes);

		} catch (Exception e) {
			logger.error("error sending message to queue " + queueName + ": ", e);
		}
	}
	
	
	
	
	
	
	
	
	
	
}
