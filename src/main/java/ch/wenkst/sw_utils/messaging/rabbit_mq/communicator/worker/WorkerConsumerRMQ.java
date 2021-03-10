package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.ConsumerBase;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageReceiver;

public class WorkerConsumerRMQ extends ConsumerBase {

	/**
	 * consumes messages from worker publishers and acknowledges the message, only one message at a time is accepted
	 * @param queueName 			the name of the worker queue
	 * @param messageReceiver   	holds the user defined method to handle a received message
	 */
	public WorkerConsumerRMQ(String queueName, MessageReceiver messageReceiver) {
		super(messageReceiver, "");
		this.queueName = queueName;
	}

	
	@Override
	public void setup(ConnectionConfigRMQ connectionConfig) {
		connect(connectionConfig);
		declareQueue();
		registerConsumer();
	}


	@Override
	protected boolean isAutoAcknowledgeMessage() {
		return false;
	}
}


