package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster;

import com.rabbitmq.client.BuiltinExchangeType;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.ConsumerBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageReceiver;

public class BroadcastConsumerRMQ extends ConsumerBase {

	/**
	 * consumes all messages that were broadcasted on the defined exchange
	 * @param exchangeName 			name of the exchange, multiple queues can bind to a named exchange
	 * @param messageReceiver   	holds the user defined method to handle a received message
	 */
	public BroadcastConsumerRMQ(String exchangeName, MessageReceiver messageReceiver) {
		super(messageReceiver, exchangeName);
	}
	
	
	@Override
	public void setup(ConnectionConfigRMQ connectionConfig) {
		connect(connectionConfig);
		declareExchange(BuiltinExchangeType.FANOUT, null);
		registerConsumer();
	}


	@Override
	protected boolean isAutoAcknowledgeMessage() {
		return true;
	}
}
