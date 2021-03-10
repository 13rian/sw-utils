package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ConsumerCallback extends DefaultConsumer {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerCallback.class);
	
	private MessageReceiver messageReceiver; 
	private boolean sendAcknowledge;

	
	/**
	 * 
	 * @param channel
	 * @param messageReceiver
	 * @param sendAcknowledge		true if an acknowledge should be sent after the messages was received
	 */
	public ConsumerCallback(Channel channel, MessageReceiver messageReceiver, boolean sendAcknowledge) {
		super(channel);
		this.messageReceiver = messageReceiver;
		this.sendAcknowledge = sendAcknowledge;
	}
	

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
		processMessage(envelope, properties, body);
		if (sendAcknowledge) {
			acknowledgeMessage(envelope);
		}
	}
	
	
	protected void processMessage(Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
		try {
			MessageRMQ message = new MessageRMQ(body, envelope, properties);
			messageReceiver.handleMessage(message);

		} catch (Exception e) {
			logger.error("error handling worker message: ", e);
		}
	}
	
	
	protected void acknowledgeMessage(Envelope envelope) {
		try {	
			getChannel().basicAck(envelope.getDeliveryTag(), false);
		} catch (Exception e) {
			logger.error("error during message acknowledge: ", e);
		}
	}
}
