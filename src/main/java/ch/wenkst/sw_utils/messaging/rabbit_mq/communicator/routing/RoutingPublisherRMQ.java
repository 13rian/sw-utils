package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing;

import com.rabbitmq.client.BuiltinExchangeType;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.PublisherBase;

public class RoutingPublisherRMQ extends PublisherBase {
	
	/**
	 * publishes a message that is broadcasted to all consumers that listen for the declared exchange. Additionally,
	 * a routing key is defined that defines a subset of the messages published
	 * @param exchangeName 		name of the exchange, any queue can be bound to this exchange and receive messages
	 */
	public RoutingPublisherRMQ(String exchangeName) {
		super(exchangeName);	
	}


	@Override
	public void setup(ConnectionConfigRMQ connectionConfig) {
		connect(connectionConfig);
		declareExchange(BuiltinExchangeType.DIRECT);
	}
}
