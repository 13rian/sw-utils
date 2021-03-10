package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.DefaultConsumer;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;

public abstract class ConsumerBase extends CommunicatorBase {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerBase.class);
	
	
	protected MessageReceiver messageReceiver;
	protected String exchangeName;
	protected String queueName;

	public ConsumerBase(MessageReceiver messageReceiver, String exchangeName) {
		this.messageReceiver = messageReceiver;
		this.exchangeName = exchangeName;
	}
	
	
	public abstract void setup(ConnectionConfigRMQ connectionConfig);
	
	protected abstract boolean isAutoAcknowledgeMessage();
	
	
	/**
	 * creates a new exchange and binds a queue with a random name to it
	 * @param type			type of the exchange
	 * @param routingKeys	the routing keys to bind, can be null for no routing
	 */
	protected void declareExchange(BuiltinExchangeType type, List<String> routingKeys) {
		try {
			channel.exchangeDeclare(exchangeName, type, durable, autoDelete, null); 
			queueName = channel.queueDeclare().getQueue(); 		// should be a random name from the server
			bindQueueToExchange(routingKeys);
			logger.info("successfully declared a new exchange " + exchangeName);

		} catch (Exception e) {
			logger.error("error declaring the new exchange " + exchangeName + ": ", e);
		}
	}
	
	
	private void bindQueueToExchange(List<String> routingKeys) throws IOException {
		if (routingKeys == null) {
			channel.queueBind(queueName, exchangeName, "");
		} else {
			for (String routingKey : routingKeys) {
				channel.queueBind(queueName, exchangeName, routingKey);
			}
		}
	}
	
	
	protected void declareQueue() {
		try {
			int prefetchCount = 1;
			channel.basicQos(prefetchCount);		// ensure that messages are sent to receivers that are not busy (not round robin principle)
			channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
			logger.info("successfully declared a new queue " + queueName);

		} catch (Exception e) {
			logger.error("error declaring the new queue: ", e);
		}
	}
	
	
	protected void registerConsumer() {
		try {
			boolean autoAck = isAutoAcknowledgeMessage();
			DefaultConsumer consumer = new ConsumerCallback(channel, messageReceiver, !autoAck);
			channel.basicConsume(queueName, autoAck, consumer); 		// empty string will generate random name

		} catch (Exception e) {
			logger.error("error registering the default consumer: ", e);
		}
	}
}
