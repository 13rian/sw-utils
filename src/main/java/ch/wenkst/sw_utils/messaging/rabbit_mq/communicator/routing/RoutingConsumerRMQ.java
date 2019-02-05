package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.IMessageReceiver;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;

public class RoutingConsumerRMQ extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(RoutingConsumerRMQ.class);

	// name of the exchange, any queue can be bound to this exchange and receive messages, this way messages are
	// not just sent to one named queue
	private String exchangeName = "";
	private String queueName = "";				// the name of the temporary queue (exists until the client disconnects)
	private List<String> routingKeys = null; 	// list of routing keys, only messages with routing keys in this list are received 

	// holds the user defined method to handle a received message
	private IMessageReceiver messageReceiver = null; 		


	/**
	 * consumes all messages that were broadcasted on the defined exchange
	 * @param mqHandler 			handler that manages the interaction with rabbitMQ
	 * @param exchangeName 			name of the exchange on which all messages are received, regardless of the publisher
	 * @param routingKeys 			list of routing keys, only messages with routing keys in this list are received
	 * @param messageReceiver   	holds the user defined method to handle a received message
	 */
	public RoutingConsumerRMQ(RabbitMQHander mqHandler, String exchangeName, List<String> routingKeys, IMessageReceiver messageReceiver) {
		super(mqHandler);

		this.exchangeName = exchangeName;
		this.routingKeys = routingKeys;
		this.messageReceiver = messageReceiver;
		
		if (routingKeys == null) {
			routingKeys = new ArrayList<>();
		}


		// declare a new queue
		declareExchange();	
		registerConsumer();
	}


	/**
	 * declares a new queue 
	 */
	private void declareExchange() {
		try {
			// declare the new exchange, direct: a message goes to the queues whose binding key exactly matches
			// the routing key of the message.
			channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, durable, autoDelete, null);

			// create a temporary queue that is removed after the client disconnects
			String queueName = channel.queueDeclare().getQueue();

			// bind the queue to all the defined routing keys
			for(String routingKey : routingKeys) {
				channel.queueBind(queueName, exchangeName, routingKey);
			}

			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}



	/**
	 * registers a worker, which means that only one worker that is not busy gets the message, if all are busy
	 * the messages are queued
	 */
	private void registerConsumer() {
		try {
			// define the consumer
			BroadcastReceiver consumer = new BroadcastReceiver(channel);

			// turn on the auto-acknowledge, any message is accepted
			boolean autoAck = true;
			channel.basicConsume(queueName, autoAck, consumer);

		} catch (Exception e) {
			logger.error("error registering a worker consumer: ", e);
		}
	}




	/**
	 * defines a listener that listens for incoming messages
	 */
	private class BroadcastReceiver extends DefaultConsumer {

		public BroadcastReceiver(Channel channel) {
			super(channel);
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
			// call the user defined method to handle the message, no acknowledge needed, since many receivers can 
			// listen for the same exchange
			try {	
				MessageRMQ message = new MessageRMQ(body, envelope, properties);
				messageReceiver.handleMessage(message);

			} catch (Exception e) {
				logger.error("error handling worker message: ", e);
			}
		}

	}


	/**
	 * adds the passed routing key to the routing key list. Only messages that are sent for any of the  routing keys 
	 * in the list are received 
	 * @param routingKey 	routing key to add
	 */
	public void addToRoutingKeys(String routingKey) {
		routingKeys.add(routingKey);

		// bind the queue to the newly added routing key
		try {
			channel.queueBind(queueName, exchangeName, routingKey);
		} catch (IOException e) {
			logger.error("error binding the queue to the routing key: " + routingKey + ": ", e);
		}
	}


	/**
	 * removes the passed routing key from the routing key list. Only messages that are sent for any of the  routing keys 
	 * in the list are received 
	 * @param routingKey 	routing key to remove
	 */
	public void removeFromRoutingKeys(String routingKey) {
		String keyToRemove = null;
		for (String key : routingKeys) {
			if (key.equals(routingKey)) {
				keyToRemove = key;
			}
		}

		if (keyToRemove != null) {
			routingKeys.remove(keyToRemove);
			
			// unbind the queue to the newly added routing key
			try {
				channel.queueUnbind(queueName, exchangeName, routingKey);
			} catch (IOException e) {
				logger.error("error unbinding the queue to the routing key: " + routingKey + ": ", e);
			}
		}
	}

}


