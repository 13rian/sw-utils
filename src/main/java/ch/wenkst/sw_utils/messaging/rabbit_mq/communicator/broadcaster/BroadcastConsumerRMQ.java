package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster;

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

public class BroadcastConsumerRMQ extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(BroadcastConsumerRMQ.class);   

	// name of the exchange, any queue can be bound to this exchange and receive messages, this way messages are
	// not just sent to one named queue
	private String exchangeName = "";
	private String queueName = "";		// the name of the temporary queue (exists until the client disconnects)

	// holds the user defined method to handle a received message
	private IMessageReceiver messageReceiver = null; 		


	/**
	 * consumes all messages that were broadcasted on the defined exchange
	 * @param mqHandler 			handler that manages the interaction with rabbitMQ
	 * @param exchangeName 			name of the exchange on which all messages are received, regardless of the publisher
	 * @param messageReceiver   	holds the user defined method to handle a received message
	 */
	public BroadcastConsumerRMQ(RabbitMQHander mqHandler, String exchangeName, IMessageReceiver messageReceiver) {
		super(mqHandler);
		this.exchangeName = exchangeName;
		this.messageReceiver = messageReceiver;
	}


	/**
	 * declares a new queue 
	 */
	public void declareExchange() {
		declareExchange(durable, autoDelete);
	}
	
	
	/**
	 * declares a new queue
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 */
	public void declareExchange(boolean durable, boolean autoDelete) {
		this.durable = durable;
		this.autoDelete = autoDelete;
		
		try {
			// declare the new exchange, fanout means it is sent to all queues that are bound to this exchange
			channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, durable, autoDelete, null); 
			
			// create a temporary queue that is removed after the client disconnects
			String queueName = channel.queueDeclare().getQueue();
			
			// bind the queue to the defined exchange, other queues can be bound to this exchange as well
			// any consumer can listen to this exchange
			channel.queueBind(queueName, exchangeName, "");
			
			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}



	/**
	 * registers a worker, which means that only one worker that is not busy gets the message, if all are busy
	 * the messages are queued
	 */
	public void registerConsumer() {
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

}


