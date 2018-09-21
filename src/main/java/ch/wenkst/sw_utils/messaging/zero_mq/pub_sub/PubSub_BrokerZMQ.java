package ch.wenkst.sw_utils.messaging.zero_mq.pub_sub;

import org.zeromq.ZMQ;

import ch.wenkst.sw_utils.messaging.zero_mq.BrokerZMQ;

public class PubSub_BrokerZMQ extends BrokerZMQ {

	
	/**
	 * acts as a broker between subscribers and publishers. the messages are published with certain keys
	 * to which the subscribers can subscribe. The messages are filtered by the publisher, which means that
	 * if a subscriber has not subscribed for a certain key, it won't receive the message.
	 * @param subscriberHost 			hosts for the server that faces the subscribers
	 * @param subscriberPort 			port for the server that faces the subscribers
	 * @param subscriberProtocol 		protocol for the server that faces the subscribers
	 * @param publisherHost 			host for the server that faces the publishers
	 * @param publisherPort 			port for the server that faces the publishers
	 * @param publisherProtocol 		protocol for the server that faces the publishers
	 */
	public PubSub_BrokerZMQ(String subscriberHost, int subscriberPort, String subscriberProtocol, String publisherHost, int publisherPort, String publisherProtocol) {
		super(subscriberHost, subscriberPort, subscriberProtocol, publisherHost, publisherPort, publisherProtocol);
		setName("pubsub-broker");
	}
	
	
	
	/**
	 * creates the two server sockets for the worker broker and starts the broker
	 */
	public void connect() {
		super.openSockets(ZMQ.XPUB, ZMQ.XSUB);
		this.start();
	}
	
}
