

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.messaging.zero_mq.pub_sub.PubSub_BrokerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.pub_sub.PublisherProducerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.pub_sub.SubscriberConsumerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.server_one_client.ClientProducerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.server_one_client.ServerConsumerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.worker.WorkerBroker;
import ch.wenkst.sw_utils.messaging.zero_mq.worker.WorkerConsumerZMQ;
import ch.wenkst.sw_utils.messaging.zero_mq.worker.WorkerProducerZMQ;


/**
 * start on the smgw:
 * 
 * /JRE_SMGW/bin/java -jar smgw_messaging.jar
 * 
 */
public class Main_ZMQ_Messaging {
	static {
		// set the system property for the logger config path
		System.setProperty("logger.config.file.path", "config/loggerConfig.properties");
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Main_ZMQ_Messaging.class);  // initialize the logger

	private ThreadPoolExecutor threadPool = null;  		// thread pool	
	

	public static void main(String[] args) {
		// initialize the logger
		Main_ZMQ_Messaging app = new Main_ZMQ_Messaging();
					
		try {
			app.init();
			app.runMessagingTest();
			logger.info("end of the test routine reached");

		} catch (Exception e) {
			logger.error("error during the main method", e);
		} 
	}
	
	

	/**
	 * initilaizes the application
	 */
	private void init() {
		// create all objects from which only one instance is needed, they are used within each other
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		threadPool.setCorePoolSize(5);			
	}

	
	
	/**
	 * tests the messaging library in the smgw_commons
	 */
	public void runMessagingTest() {
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 									one-server one-client 	 											 //
		// Just one server and one client that communicate with each other with no broker in the middle. This 	 //
		// can only be used for one server and one client 														 // 																								 //
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		logger.info("\n");
		logger.info("---------------------------------------------------------------------------------------------");
		logger.info("ONE SERVER, ONE CLIENT");
		logger.info("---------------------------------------------------------------------------------------------");
		
		// create the client, the message can be sent before the server is started
		ClientProducerZMQ client = new ClientProducerZMQ("localhost", 7000, "tcp") {
			
			@Override
			protected void onReceivedMessage(byte[] msgBytes) {
				logger.info("received message form the server: " + new String(msgBytes, StandardCharsets.UTF_8));				
			}
		};
		client.connect();
		client.sendMessage("Hello Server");
		
		
		// create the server
		ServerConsumerZMQ server = new ServerConsumerZMQ("localhost", 7000, "tcp") {
			
			@Override
			protected void onReceivedMessage(byte[] msgBytes) {
				// received message form the client
				logger.info("received message form the client: " + new String(msgBytes, StandardCharsets.UTF_8));
			}
		};
		server.connect();
		Utils.sleep(1000);

		server.sendMessage("Hello Client");
		
		
		// close all
		Utils.sleep(2000);
		server.disconnect();
		client.disconnect();
		
		
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											worker scenario 											 //
		// a client send a message and each message is only processed by one worker. The worker needs to  		 //
		// acknowledge the message. This way the client knows that the message was processed.  					 //
		// A broker is in the middle. 																			 //
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		logger.info("\n");
		logger.info("---------------------------------------------------------------------------------------------");
		logger.info("WORKER SCENARIO");
		logger.info("---------------------------------------------------------------------------------------------");
		
		// start the broker
		WorkerBroker workerBroker = new WorkerBroker("localhost", 5000, "tcp", "localhost", 5001, "tcp");
		workerBroker.connect();
		
		
		// create the 2 workers
		WorkerConsumerZMQ workerConsumer1 = new WorkerConsumerZMQ("localhost", 5001, "tcp") {
			@Override
			protected byte[] processRequest(byte[] reqBytes) {
				logger.debug("worker1: request received: " + new String(reqBytes, StandardCharsets.UTF_8));
				Utils.sleep(1000);				
				return "worker1 result".getBytes(StandardCharsets.UTF_8);
			}
		};
		workerConsumer1.connect();
		
		WorkerConsumerZMQ workerConsumer2 = new WorkerConsumerZMQ("localhost", 5001, "tcp") {
			@Override
			protected byte[] processRequest(byte[] reqBytes) {
				logger.debug("worker2: request received: " + new String(reqBytes, StandardCharsets.UTF_8));
				Utils.sleep(1000);				
				return "worker2 result".getBytes(StandardCharsets.UTF_8);
			}
		};
		workerConsumer2.connect();
		
		
		// create the 2 worker producers that send requests
		WorkerProducerZMQ workerProducer1 = new WorkerProducerZMQ("localhost", 5000, "tcp");
		WorkerProducerZMQ workerProducer2 = new WorkerProducerZMQ("localhost", 5000, "tcp");
		
		workerProducer1.connect();
		workerProducer2.connect();

		// send out some request
		threadPool.execute(() -> {
			byte[] resp = workerProducer1.sendRequest("request message 1 form producer 1".getBytes(StandardCharsets.UTF_8), 5000);
			logger.debug("response 1: " + new String(resp, StandardCharsets.UTF_8));
		});
		threadPool.execute(() -> {
			byte[] resp = workerProducer1.sendRequest("request message 2 form producer 1".getBytes(StandardCharsets.UTF_8), 5000);
			logger.debug("response 2: " + new String(resp, StandardCharsets.UTF_8));
		});
		threadPool.execute(() -> {
			byte[] resp = workerProducer2.sendRequest("request message 3 form producer 2".getBytes(StandardCharsets.UTF_8), 5000);
			logger.debug("response 3: " + new String(resp, StandardCharsets.UTF_8));
		});
		byte[] resp = workerProducer2.sendRequest("request message 4 form producer 2".getBytes(StandardCharsets.UTF_8), 3000);
		logger.debug("response 4: " + new String(resp, StandardCharsets.UTF_8));
		
		
		// close all
		Utils.sleep(2000);
		workerProducer1.disconnect();
		workerProducer2.disconnect();
		workerConsumer1.disconnect();
		workerConsumer2.disconnect();
		workerBroker.disconnect();
		
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 									subscriber and publisher 											 //
		// a publisher sends out a message with a certain key and the subscriber can subscribe to different 	 //
		// keys. the messages are filtered on the publisher side and not send to the subscriber if not  		 //
		// registered for this key. A broker is in the middle.													 //
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		logger.info("\n");
		logger.info("---------------------------------------------------------------------------------------------");
		logger.info("PUBLISHER AND SUBSCRIBER");
		logger.info("---------------------------------------------------------------------------------------------");
		
		// start the broker
		PubSub_BrokerZMQ pubsubBroker = new PubSub_BrokerZMQ("localhost", 6000, "tcp", "localhost", 6001, "tcp");
		pubsubBroker.connect();
		
		
		// create a subscriber
		SubscriberConsumerZMQ subscriber = new SubscriberConsumerZMQ("localhost", 6000, "tcp") {
			
			@Override
			protected void onReceivedMessage(byte[] msgBytes, String key) {
				logger.debug("subscriber received message: " + new String(msgBytes, StandardCharsets.UTF_8) + ", key: " + key);				
			}
		};
		subscriber.connect();
		subscriber.subscribe("a", "b"); 		// to subscribe to all messages use ""
		

		// create the publisher
		PublisherProducerZMQ publisher = new PublisherProducerZMQ("localhost", 6001, "tcp");
		publisher.connect();
		
		threadPool.execute(() -> {
			Utils.sleep(1000);
			// will receive messages with key a
			logger.debug("publish message with key a");
			publisher.sendMessage("msg_A", "a");
			
			// will receive messages with key b
			logger.debug("publish message with key b");
			publisher.sendMessage("msg_B", "b");			
			
			// will not receive messages with key b
			logger.debug("publish message with key c");
			publisher.sendMessage("msg_C1", "c");
			
			// subscribe the subscriber to c-key
			logger.debug("subscribe so key c");
			subscriber.subscribe("c");
			
			// will receive the message as the subscriber subscribed to key c
			Utils.sleep(100);
			logger.debug("publish message with key c");
			publisher.sendMessage("msg_C2", "c");
			
			// unsubscribe form the key c again
			Utils.sleep(100);
			logger.debug("unsubscribe form key c");
			subscriber.unsubscribe("c");
			
			// will not receive the message as the subscriber unsubscribed to key c
			Utils.sleep(100);
			logger.debug("publish message with key c");
			publisher.sendMessage("msg_C3", "c");
			
			
			// will not receive the message as the subscriber never subscribed to the key d
			logger.debug("publish message with key d");
			publisher.sendMessage("msg_D", "d");
		});
		
		
		
		// close all
		Utils.sleep(3000);
		publisher.disconnect();
		subscriber.disconnect();
		pubsubBroker.disconnect();
		
				
		
		// shutdown the thread pool
		threadPool.shutdown();
		
	}
	
}
