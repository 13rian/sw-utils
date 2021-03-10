package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.messaging.rabbit_mq.ConnectionConfigRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.PublisherBase;

public class WorkerPublisherRMQ extends PublisherBase {
	private static final Logger logger = LoggerFactory.getLogger(WorkerPublisherRMQ.class);
	

	/**
	 * publishes a message that is only received by one of the registered workerConsumers and acknowledged
	 * even if a worker dies processing a message the message will not get acknowledged and is resent again
	 * @param queueName 			name of the worker queue
	 */
	public WorkerPublisherRMQ(String queueName) {
		super("");
		this.queueName = queueName;
	}


	@Override
	public void setup(ConnectionConfigRMQ connectionConfig) {
		connect(connectionConfig);
		declareQueue();		
	}
	

	@Override
	public void publishMessage(MessageRMQ message) {
		try {
			publishToChannel(message, queueName);

		} catch (Exception e) {
			logger.error("error sending message to queue " + exchangeName + ": ", e);
		}
	}
}
