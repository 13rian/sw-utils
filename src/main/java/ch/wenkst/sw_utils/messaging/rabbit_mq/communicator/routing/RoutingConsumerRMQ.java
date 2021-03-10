package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.ConsumerBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageReceiver;

public class RoutingConsumerRMQ extends ConsumerBase {
	private static final Logger logger = LoggerFactory.getLogger(RoutingConsumerRMQ.class);
	
	private List<String> routingKeys;


	/**
	 * consumes all messages that were broadcasted on the defined exchange
	 * @param exchangeName 			name of the exchange, multiple queues can bind to a named exchange
	 * @param routingKeys 			list of routing keys, only messages with routing keys in this list are received
	 * @param messageReceiver   	holds the user defined method to handle a received message
	 */
	public RoutingConsumerRMQ(String exchangeName, List<String> routingKeys, MessageReceiver messageReceiver) {
		super(messageReceiver, exchangeName);

		this.routingKeys = routingKeys;
		
		if (routingKeys == null) {
			routingKeys = new ArrayList<>();
		}
	}

	
	@Override
	public void setup(ConnectionConfigRMQ connectionConfig) {
		connect(connectionConfig);
		declareExchange(BuiltinExchangeType.DIRECT, routingKeys);
		registerConsumer();
	}


	/**
	 * adds the passed routing key to the routing key list. Only messages that are sent for any of the  routing keys 
	 * in the list are received 
	 * @param routingKey 	routing key to add
	 */
	public synchronized void addToRoutingKeys(String routingKey) {
		routingKeys.add(routingKey);
		bindRoutingKeyToQueue(routingKey);
	}
	
	
	private void bindRoutingKeyToQueue(String routingKey) {
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
	public synchronized void removeFromRoutingKeys(String routingKey) {
		routingKeys.removeIf((key) -> {
			if (key.equals(routingKey)) {
				unbindChannelFromRoutingKey(routingKey);
				return true;
			}
			return false;
		});
	}
	
	
	private void unbindChannelFromRoutingKey(String routingKey) {
		try {
			channel.queueUnbind(queueName, exchangeName, routingKey);
		} catch (IOException e) {
			logger.error("error unbinding the queue to the routing key: " + routingKey + ": ", e);
		}
	}


	@Override
	protected boolean isAutoAcknowledgeMessage() {
		return true;
	}
}
