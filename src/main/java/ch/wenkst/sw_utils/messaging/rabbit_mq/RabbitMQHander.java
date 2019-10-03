package ch.wenkst.sw_utils.messaging.rabbit_mq;

import java.util.List;

import javax.net.ssl.SSLContext;

import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.IMessageReceiver;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.MessageRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastPublisherRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing.RoutingConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing.RoutingPublisherRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker.WorkerConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker.WorkerPublisherRMQ;

public class RabbitMQHander {	
	private static RabbitMQHander instance = null; 	// instance for the singleton access
	
	private String host = "localhost"; 				// rabbitMQ server host
	private int port = 5672; 						// rabbitMQ server port, default for non-ssl is 5672
	private String username = "guest"; 				// user name of the rabbitMQ server account
	private String password = "guest"; 				// password of the rabbitmMQ server account
	private SSLContext sslContext = null; 			// the ssl context to handle the tls connection
	
	
	/**
	 * handles the connection to the message server rabbitMQ
	 */
	protected RabbitMQHander() {

	}
	
	
	/**
	 * returns the instance of the rabbitMQHandler
	 * @return
	 */
	public static RabbitMQHander getInstance() {
		if(instance == null) {
			instance = new RabbitMQHander();
		}	      
		return instance;
	}


	/**
	 * initialization for non-encrypted connection to the message server rabbitMQ
	 * @param host	 		host of the rabbitMQ server
	 * @param port 			port of the rabbitMQ server
	 * @param username		the user name of the rabbitMQ server account
	 * @param password 		the password of the rabbitMQ server account
	 */
	public void init(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	
	/**
	 * initialization for tls-encrypted connection to the message server rabbitMQ
	 * @param host	 		host of the rabbitMQ server
	 * @param port 			port of the rabbitMQ server
	 * @param username		the user name of the rabbitMQ server account
	 * @param password 		the password of the rabbitMQ server account
	 * @param sslContext   	ssl context to handle the tls connection
	 */
	public void init(String host, int port, String username, String password, SSLContext sslContext) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.sslContext = sslContext;
	}
	
	
	/**
	 * tests if the rabbit mq server is reachable
	 * @return 	true if reachable, false if not
	 */
	public boolean isReachable() {	
		BroadcastPublisherRMQ broadcaster = getBroadcastPublisher("testQueue");
		boolean isReachable = broadcaster.publishMessage(new MessageRMQ("test message"));
		if (isReachable) {
			broadcaster.disconnect();
		}
		
		return isReachable;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											workers							  					    //
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns a new instance of a worker publisher. A worker publisher sends messages to worker consumers
	 * Each message is only processed by one worker consumer. The worker consumers need to acknowledge each
	 * message after they are finished. If one worker consumer dies during processing , the message is not 
	 * acknowledged and is sent to another worker after some time. If all worker consumers are busy the message
	 * is queued.
	 * @param queueName 	the name of the queue on which messages are published
	 * @return
	 */
	public WorkerPublisherRMQ getWorkerPublisher(String queueName) {
		WorkerPublisherRMQ publisher = new WorkerPublisherRMQ(this, queueName);
		publisher.declareQueue();
		return publisher;
	}
	
	
	/**
	 * returns a new instance of a worker publisher. A worker publisher sends messages to worker consumers
	 * Each message is only processed by one worker consumer. The worker consumers need to acknowledge each
	 * message after they are finished. If one worker consumer dies during processing , the message is not 
	 * acknowledged and is sent to another worker after some time. If all worker consumers are busy the message
	 * is queued.
	 * @param queueName 	the name of the queue on which messages are published
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param exclusive		true if we are declaring an exclusive queue (restricted to this connection)
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 * @return
	 */
	public WorkerPublisherRMQ getWorkerPublisher(String queueName, boolean durable, boolean exclusive, boolean autoDelete) {
		WorkerPublisherRMQ publisher = new WorkerPublisherRMQ(this, queueName);
		publisher.declareQueue(durable, exclusive, autoDelete);
		return publisher;
	}
	
	
	
	/**
	 * returns a new instance of a worker consumer. A worker consumer receives messages from worker publisher.
	 * Each message is only processed by one worker consumer. The worker consumers need to acknowledge each
	 * message after they are finished. If one worker consumer dies during processing , the message is not 
	 * acknowledged and is sent to another worker after some time. If all worker consumers are busy the message
	 * is queued.
	 * @param queueName 		the name of the queue on which messages are published
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public WorkerConsumerRMQ getWorkerConsumer(String queueName, IMessageReceiver messageReceiver) {
		WorkerConsumerRMQ consumer = new WorkerConsumerRMQ(this, queueName, messageReceiver);
		consumer.declareQueue();
		consumer.registerConsumer();
		return consumer;
	}
	
	
	/**
	 * returns a new instance of a worker consumer. A worker consumer receives messages from worker publisher.
	 * Each message is only processed by one worker consumer. The worker consumers need to acknowledge each
	 * message after they are finished. If one worker consumer dies during processing , the message is not 
	 * acknowledged and is sent to another worker after some time. If all worker consumers are busy the message
	 * is queued.
	 * @param queueName 		the name of the queue on which messages are published
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param exclusive		true if we are declaring an exclusive queue (restricted to this connection)
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public WorkerConsumerRMQ getWorkerConsumer(String queueName, boolean durable, boolean exclusive, boolean autoDelete, IMessageReceiver messageReceiver) {
		WorkerConsumerRMQ consumer = new WorkerConsumerRMQ(this, queueName, messageReceiver);
		consumer.declareQueue(durable, exclusive, autoDelete);
		consumer.registerConsumer();
		return consumer;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// 										broadcasters							  					//
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns a new instance of a broadcast publisher. It sends messages on the declared exchange. More than
	 * one broadcast publisher can send messages on the same exchange. Temporary queues are used, which means
	 * that the queue gets a random name and will be removed after the client disconnected.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @return
	 */
	public BroadcastPublisherRMQ getBroadcastPublisher(String exchangeName) {
		BroadcastPublisherRMQ publisher = new BroadcastPublisherRMQ(this, exchangeName);
		publisher.declareExchange();
		return publisher;
	}
	
	
	/**
	 * returns a new instance of a broadcast publisher. It sends messages on the declared exchange. More than
	 * one broadcast publisher can send messages on the same exchange. Temporary queues are used, which means
	 * that the queue gets a random name and will be removed after the client disconnected.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 * @return
	 */
	public BroadcastPublisherRMQ getBroadcastPublisher(String exchangeName, boolean durable, boolean autoDelete) {
		BroadcastPublisherRMQ publisher = new BroadcastPublisherRMQ(this, exchangeName);
		publisher.declareExchange(durable, autoDelete);
		return publisher;
	}
	
	
	/**
	 * returns a new instance of a broadcast consumer. It receives all messages on the declared exchange. All consumers
	 * that listen for a certain exchange will receive all messages. Temporary queues are used, which means that the 
	 * queue gets a random name and will be removed after the client disconnected.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public BroadcastConsumerRMQ getBroadcastConsumer(String exchangeName, boolean durable, boolean autoDelete, IMessageReceiver messageReceiver) {
		BroadcastConsumerRMQ consumer = new BroadcastConsumerRMQ(this, exchangeName, messageReceiver);
		consumer.declareExchange(durable, autoDelete);	
		consumer.registerConsumer();
		return consumer;
	}
	
	
	/**
	 * returns a new instance of a broadcast consumer. It receives all messages on the declared exchange. All consumers
	 * that listen for a certain exchange will receive all messages. Temporary queues are used, which means that the 
	 * queue gets a random name and will be removed after the client disconnected.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public BroadcastConsumerRMQ getBroadcastConsumer(String exchangeName, IMessageReceiver messageReceiver) {
		BroadcastConsumerRMQ consumer = new BroadcastConsumerRMQ(this, exchangeName, messageReceiver);
		consumer.declareExchange();	
		consumer.registerConsumer();
		return consumer;
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											routers							  						//
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns a new instance of a routing publisher. It sends messages on the declared exchange for a certain routing key.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @return
	 */
	public RoutingPublisherRMQ getRoutingPublisher(String exchangeName) {
		RoutingPublisherRMQ publisher = new RoutingPublisherRMQ(this, exchangeName);
		publisher.declareExchange(); 
		return publisher;
	}
	
	
	/**
	 * returns a new instance of a routing publisher. It sends messages on the declared exchange for a certain routing key.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 * @return
	 */
	public RoutingPublisherRMQ getRoutingPublisher(String exchangeName, boolean durable, boolean autoDelete) {
		RoutingPublisherRMQ publisher = new RoutingPublisherRMQ(this, exchangeName);
		publisher.declareExchange(durable, autoDelete); 
		return publisher;
	}
	
	
	/**
	 * returns a new instance of a routing consumer. It receives messages for all registered routing keys.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param routingKeys 		list of routing keys, only messages with routing keys in this list are received
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public RoutingConsumerRMQ getRoutingConsumer(String exchangeName, List<String> routingKeys, IMessageReceiver messageReceiver) {
		RoutingConsumerRMQ consumer = new RoutingConsumerRMQ(this, exchangeName, routingKeys, messageReceiver);
		consumer.declareExchange();	
		consumer.registerConsumer();
		return consumer;
	}
	
	
	/**
	 * returns a new instance of a routing consumer. It receives messages for all registered routing keys.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param routingKeys 		list of routing keys, only messages with routing keys in this list are received
	 * @param durable 		true if we are declaring a durable queue (the queue will survive a server restart) 
	 * @param autoDelete	true if we are declaring an autodelete queue (server will delete it when no longer in use)
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public RoutingConsumerRMQ getRoutingConsumer(String exchangeName, List<String> routingKeys, boolean durable, boolean autoDelete, IMessageReceiver messageReceiver) {
		RoutingConsumerRMQ consumer = new RoutingConsumerRMQ(this, exchangeName, routingKeys, messageReceiver);
		consumer.declareExchange(durable, autoDelete);	
		consumer.registerConsumer();
		return consumer;
	}
	

	public boolean isTls() {
		return sslContext != null;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}
}
