package ch.wenkst.sw_utils;


import java.io.File;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.crypto.SecurityUtils;
import ch.wenkst.sw_utils.crypto.tls.SSLContextGenerator;
import ch.wenkst.sw_utils.messaging.rabbit_mq.RabbitMQHander;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastPublisherRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing.RoutingConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing.RoutingPublisherRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker.WorkerConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker.WorkerPublisherRMQ;

public class MainRabbitMQ {
	// set the system property for the logger
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}

	private static final Logger logger = LoggerFactory.getLogger(MainRabbitMQ.class);

	public static void main(String[] args) {

		// non-encrypted connections to the rabbitMQ server
		// RabbitMQHander messageHandler = new RabbitMQHander("23.97.156.162", "efr", "efrserver");

		// tls-encrypted connections to the rabbitMQ server
		String p12FilePath = System.getProperty("user.dir") + File.separator + "rabbit_mq" + File.separator + "client" + File.separator + "client.cert.p12";
		String caCertPath = System.getProperty("user.dir") + File.separator + "rabbit_mq" + File.separator + "server" + File.separator + "server.cert.pem";		
		Certificate caCert = SecurityUtils.certFromFile(caCertPath);
		List<Certificate> trustedCerts = new ArrayList<>();
		trustedCerts.add(caCert);
		SSLContext sslContext = SSLContextGenerator.createSSLContext(p12FilePath, "pwcelsi", trustedCerts, "TLSv1.2");
		RabbitMQHander messageHandler = RabbitMQHander.getInstance();
		messageHandler.init("192.168.1.141", 5671, "efr", "efrserver", sslContext);
		
		
		// test the connection
		boolean reachable = messageHandler.isReachable();
		logger.info("is reachable: " + reachable);
	


		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											worker scenario														  //
		// A worker publisher send messages to worker consumers. Each message is only processed by one worker consumer.   //
		// The worker consumers need to acknowledge each message after they are finished. If one worker consumer dies 	  //
		// during processing , the message is not acknowledged and is sent to another worker after some time. If all 	  //
		// worker consumers are busy the message is queued. 															  //
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("START WORKING SCENARIO");
		

		// define the name of the queue
		String workerQueueName = "worker_queue";

		// define worker 1
		WorkerConsumerRMQ workerConsumer1 = messageHandler.getWorkerConsumer(workerQueueName, (message) -> {
			String messageStr = message.getBodyStr();
			logger.info("worker1 received message " + messageStr);
			Utils.sleep(200);
			logger.info("worker1 finished processing");
		});

		// define worker 2
		WorkerConsumerRMQ workerConsumer2 = messageHandler.getWorkerConsumer(workerQueueName, (message) -> {
			String messageStr = message.getBodyStr();
			logger.info("worker2 received message " + messageStr);
			Utils.sleep(500);
			logger.info("worker2 finished processing");
		});


		// define a publisher and publish 4 messages
		WorkerPublisherRMQ workerPublisher = messageHandler.getWorkerPublisher(workerQueueName);
		workerPublisher.publishMessage(new MessageRMQ("message1"));
		workerPublisher.publishMessage(new MessageRMQ("message2"));
		workerPublisher.publishMessage(new MessageRMQ("message3"));
		workerPublisher.publishMessage(new MessageRMQ("message4"));


		Utils.sleep(1100);

		// disconnect all publishers and workers
		workerPublisher.disconnect();
		workerConsumer1.disconnect();
		workerConsumer2.disconnect();

		logger.info("disconnected all worker consumers and publishers");



		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 										broadcast scenario														  //
		// A broadcaster sends message on a declared exchange. Many broadcasters can send messages on the same exchange.  //
		// All consumers that listen for the exchange receive the message. For both consumers and publishers temporary 	  //
		// queues are used, which means that the queue is removed after the client disconnects. 						  //
		// The handleMessage method is called synchronously 															  //
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("START BROADCAST SCENARIO");

		// define the name of the exchange
		String broadcastExchangeName = "broadcast_exchange";

		// define 2 consumers that listen for all messages for a certain exchange
		// define consumer 1
		BroadcastConsumerRMQ broadcastConsumer1 = messageHandler.getBroadcastConsumer(broadcastExchangeName, (message) -> {
			String messageStr = message.getBodyStr();
			logger.info("broadcastConsumer1 received message " + messageStr);
		});

		// define consumer 2
		BroadcastConsumerRMQ broadcastConsumer2 = messageHandler.getBroadcastConsumer(broadcastExchangeName, (message) -> {
			String messageStr = message.getBodyStr();
			logger.info("broadcastConsumer2 received message " + messageStr);
		});


		// define 2 broadcast publisher and let each send 2 messages
		BroadcastPublisherRMQ broadcastPublisher1 = messageHandler.getBroadcastPublisher(broadcastExchangeName);
		BroadcastPublisherRMQ broadcastPublisher2 = messageHandler.getBroadcastPublisher(broadcastExchangeName);

		broadcastPublisher1.publishMessage(new MessageRMQ("message1 from broadcaster1"));
		broadcastPublisher2.publishMessage(new MessageRMQ("message1 from broadcaster2"));
		broadcastPublisher1.publishMessage(new MessageRMQ("message2 from broadcaster1"));
		broadcastPublisher2.publishMessage(new MessageRMQ("message2 from broadcaster2"));


		Utils.sleep(500);

		// disconnect all publishers and workers
		broadcastPublisher1.disconnect();
		broadcastPublisher2.disconnect();
		broadcastConsumer1.disconnect();
		broadcastConsumer2.disconnect();

		logger.info("disconnected all broadcast consumers and publishers");
		
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 										routing scenario														  //
		// A broadcaster sends message on a declared exchange for a certain routing key. The consumers listen for certain //
		// routing keys. Only messages for routing keys are received that are in the routing key list. 					  //
		// Adding routing keys does not create any new threads 															  //
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("START ROUTING SCENARIO");
		
		// define the name of the exchange
		String routingExchangeName = "routing_exchange";
		
		// define 2 consumers that listen for different routing keys
		// define consumer 1
		ArrayList<String> routingKeys1 = new ArrayList<>();
//		routingKeys1.add("trace");
//		routingKeys1.add("debug");
		routingKeys1.add("#");

		
		RoutingConsumerRMQ routingConsumer1 = messageHandler.getRoutingConsumer(routingExchangeName, routingKeys1 , (message) -> {
			String messageStr = message.getBodyStr();
			logger.info("routingConsumer1 received message " + messageStr);
		});
		
		// define consumer 1
		ArrayList<String> routingKeys2 = new ArrayList<>();
		routingKeys2.add("info");
		routingKeys2.add("error");
		
		RoutingConsumerRMQ routingConsumer2 = messageHandler.getRoutingConsumer(routingExchangeName, routingKeys2 , (message) -> {
			String messageStr = message.getBodyStr();
			logger.info("routingConsumer2 received message " + messageStr);
		});
		
		
		// define a publisher that sends messages with different routing keys
		RoutingPublisherRMQ routingPublisher = messageHandler.getRoutingPublisher(routingExchangeName);
		routingPublisher.publishMessage(new MessageRMQ("log1 trace"), "trace");
		routingPublisher.publishMessage(new MessageRMQ("log1 debug"), "debug");
		routingPublisher.publishMessage(new MessageRMQ("log1 info"), "info");
		routingPublisher.publishMessage(new MessageRMQ("log1 error"), "error");
		
		
		// remove and add routing keys
		Utils.sleep(100);
		System.out.println("changed routing keys");
		routingConsumer1.addToRoutingKeys("info");  		// receive trace, debug, info
		routingConsumer2.removeFromRoutingKeys("info"); 	// receive only error now	  
		
		// publish again
		routingPublisher.publishMessage(new MessageRMQ("log2 trace"), "trace");
		routingPublisher.publishMessage(new MessageRMQ("log2 debug"), "debug");
		routingPublisher.publishMessage(new MessageRMQ("log2 info"), "info");
		routingPublisher.publishMessage(new MessageRMQ("log2 error"), "error");

		
		
		Utils.sleep(500);

		// disconnect all publishers and workers
		routingPublisher.disconnect();
		routingConsumer1.disconnect();
		routingConsumer2.disconnect();

		logger.info("disconnected all routing consumers and publishers");


	}

}
