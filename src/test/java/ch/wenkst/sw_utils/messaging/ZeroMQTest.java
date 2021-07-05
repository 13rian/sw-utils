package ch.wenkst.sw_utils.messaging;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.event.TestEventListener;
import ch.wenkst.sw_utils.event.board.BoardEventSender;
import ch.wenkst.sw_utils.messaging.zero_mq.server_one_client.ClientProducerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.server_one_client.ServerConsumerZMQ;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZeroMQTest extends BaseTest {
	private static final Logger logger = LoggerFactory.getLogger(ZeroMQTest.class);
	
	private ZeroMQEventSender eventSender;
	
	
	@BeforeAll
	public void setupExecutor() {
		eventSender = new ZeroMQEventSender();
	}
	
	
	@Test
	public void oneServerOneClient() {
		MessageListener clientMessageListener = new MessageListener(100);
		MessageListener serverMessageListener = new MessageListener(100);
		
		ClientProducerZMQ client = eventSender.setupClientConsumer(7000);
		client.sendMessage("Hello Server");
		
		
		// create the server
		ServerConsumerZMQ server = eventSender.setupServerConsumer(7000);
		

		server.sendMessage("Hello Client");
		
		Utils.sleep(1000);
	}
}
