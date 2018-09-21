package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.CommunicatorBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.IMessageReceiver;

public class WorkerConsumerRMQ extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(WorkerConsumerRMQ.class);

	// name of the queue on which messages are received
	private String queueName = "";  		

	// holds the user defined method to handle a received message
	private IMessageReceiver messageReceiver = null; 		


	/**
	 * consumes messages from worker publishers and acknowledges the message, only one message at a time is accepted
	 * @param mqHandler 		handler that manages the interaction with rabbitMQ
	 * @param queueName 		name of the queue on which messages are published
	 * @param messageReceiver   holds the user defined method to handle a received message
	 */
	public WorkerConsumerRMQ(RabbitMQHander mqHandler, String queueName, IMessageReceiver messageReceiver) {
		super(mqHandler);

		this.queueName = queueName;
		this.messageReceiver = messageReceiver;



		// declare a new queue
		declareQueue();	
		registerConsumer();
	}


	/**
	 * declares a new queue 
	 */
	private void declareQueue() {
		boolean durable = false; 		// if true the queue survives a server restart 
		boolean exclusive = false; 		// if true this queue is restricted to this connection
		boolean autoDelete = true; 		// if true the queue is deleted if no longer used

		try {
			// ensure that messages are sent to receivers that are not busy (not round robin principle)
			int prefetchCount = 1;
			channel.basicQos(prefetchCount);

			// declare the new queue
			channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
			logger.info("successfully declared a new queue " + queueName);

		} catch (Exception e) {
			logger.error("error declaring the new queue " + queueName, e);
		}
	}



	/**
	 * registers a worker, which means that only one worker that is not busy gets the message, if all are busy
	 * the messages are queued
	 */
	private void registerConsumer() {
		try {
			// define the consumer
			WorkerReceiver consumer = new WorkerReceiver(channel);

			// turn off the auto-acknowledge, each worker needs to send the acknowledge, this way it is ensured
			// that each message is process by a worker, even if one dies during processing
			boolean autoAck = false;
			channel.basicConsume(queueName, autoAck, consumer);

		} catch (Exception e) {
			logger.error("error registering a worker consumer: ", e);
		}
	}




	/**
	 * defines a listener that listens for incoming messages
	 */
	private class WorkerReceiver extends DefaultConsumer {

		public WorkerReceiver(Channel channel) {
			super(channel);
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
			// call the user defined method to handle the message
			try {			
				messageReceiver.handleMessage(body, properties.getHeaders());

			} catch (Exception e) {
				logger.error("error handling worker message: ", e);
			}

			// acknowledge the message, even if the worker produced an error
			try {				
				channel.basicAck(envelope.getDeliveryTag(), false);
			} catch (Exception e) {
				logger.error("error during worker message ackinowledge: ", e);
			}
		}

	}




}


