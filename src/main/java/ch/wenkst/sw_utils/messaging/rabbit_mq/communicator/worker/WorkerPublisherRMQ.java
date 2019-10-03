package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;

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
	}
	
	
	/**
	 * declares a new queue
	 */
	public void declareQueue() {
		declareQueue(durable, exclusive, autoDelete);
	}
	
	
	/**
	 * declares a new queue 
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param exclusive		true if we are declaring an exclusive queue (restricted to this connection)
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 */
	public void declareQueue(boolean durable, boolean exclusive, boolean autoDelete) {
		this.durable = durable;
		this.exclusive = exclusive;
		this.autoDelete = autoDelete;
		
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
	 * @param message 			the message to send
	 */
	public void publishMessage(MessageRMQ message) {
		try {
			// name of the exchange is empty (first param), which denotes the default or nameless exchange: 
			// messages are routed to the queue with the name specified by routingKey (here queueName), if it exists.
			channel.basicPublish("", queueName, message.getProperties(), message.getBody());

		} catch (Exception e) {
			logger.error("error sending message to queue " + queueName + ": ", e);
		}
	}
}
