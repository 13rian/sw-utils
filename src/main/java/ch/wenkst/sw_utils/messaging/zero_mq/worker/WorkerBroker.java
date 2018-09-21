package ch.wenkst.sw_utils.messaging.zero_mq.worker;

import org.zeromq.ZMQ;

import ch.wenkst.sw_utils.messaging.zero_mq.BrokerZMQ;

public class WorkerBroker extends BrokerZMQ {

	
	/**
	 * acts as a broker between clients that send requests to workers. Only one worker will receive the message
	 * that is sent by the client.
	 * @param producerHost 			host for the server that faces the worker producers
	 * @param producerPort 			port for the server that faces the worker producers
	 * @param producerProtocol 		protocol for the server that faces the worker producers
	 * @param workerHost 			hosts for the server that faces the worker consumers
	 * @param workerPort 			port for the server that faces the worker consumers
	 * @param workerProtocol 		protocol for the server that faces the worker consumers
	 */
	public WorkerBroker(String producerHost, int producerPort, String producerProtocol, String workerHost, int workerPort, String workerProtocol) {
		super(producerHost, producerPort, producerProtocol, workerHost, workerPort, workerProtocol);
		setName("worker-broker");
	}
	
	
	
	/**
	 * creates the two server sockets for the worker broker and starts the broker
	 */
	public void connect() {
		super.openSockets(ZMQ.ROUTER, ZMQ.DEALER);
		this.start();
	}
	
}
