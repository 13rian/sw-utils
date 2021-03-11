package ch.wenkst.sw_utils.messaging.zero_mq.pub_sub;

import org.zeromq.SocketType;

import ch.wenkst.sw_utils.messaging.zero_mq.BrokerConfigZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.BrokerZMQ;

public class PubSub_BrokerZMQ extends BrokerZMQ {

	
	/**
	 * acts as a broker between subscribers and publishers. the messages are published with certain keys
	 * to which the subscribers can subscribe. The messages are filtered by the publisher, which means that
	 * if a subscriber has not subscribed for a certain key, it won't receive the message.
	 * @param brokerConfig 		broker configuration
	 */
	public PubSub_BrokerZMQ(BrokerConfigZMQ brokerConfig) {
		super(brokerConfig);
		setName("pubsub-broker");
	}
	
	
	@Override
	public void connect() {
		super.openSockets(SocketType.XPUB, SocketType.XSUB);
		this.start();
	}
}
