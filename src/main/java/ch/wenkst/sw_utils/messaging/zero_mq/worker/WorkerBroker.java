package ch.wenkst.sw_utils.messaging.zero_mq.worker;

import org.zeromq.SocketType;

import ch.wenkst.sw_utils.messaging.zero_mq.BrokerConfigZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.BrokerZMQ;

public class WorkerBroker extends BrokerZMQ {

	/**
	 * acts as a broker between clients that send requests to workers. Only one worker will receive the message
	 * that is sent by the client.
	 * @param brokerConfig 		broker configuration
	 */
	public WorkerBroker(BrokerConfigZMQ brokerConfig) {
		super(brokerConfig);
		setName("worker-broker");
	}
	
	
	@Override
	public void connect() {
		super.openSockets(SocketType.ROUTER, SocketType.DEALER);
		this.start();
	}
}
