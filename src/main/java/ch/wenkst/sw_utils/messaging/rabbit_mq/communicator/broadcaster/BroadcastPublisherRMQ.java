package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster;

import com.rabbitmq.client.BuiltinExchangeType;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.PublisherBase;

public class BroadcastPublisherRMQ extends PublisherBase {

	/**
	 * publishes a message that is broadcasted to all consumers that listen for the declared exchange
	 * @param exchangeName 		name of the exchange, any queue can be bound to this exchange and receive messages
	 */
	public BroadcastPublisherRMQ(String exchangeName) {
		super(exchangeName);
	}


	@Override
	public void setup(ConnectionConfigRMQ connectionConfig) {
		connect(connectionConfig);
		declareExchange(BuiltinExchangeType.FANOUT);
	}
}
