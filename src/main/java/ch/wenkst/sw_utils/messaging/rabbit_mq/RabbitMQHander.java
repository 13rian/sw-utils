package ch.wenkst.sw_utils.messaging.rabbit_mq;

import java.util.ArrayList;

import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.IMessageReceiver;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.broadcaster.BroadcastPublisherRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing.RoutingConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.routing.RoutingPublisherRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker.WorkerConsumerRMQ;
import ch.wenkst.sw_utils.messaging.rabbit_mq.communicator.worker.WorkerPublisherRMQ;

public class RabbitMQHander {
		
	private String host = ""; 				// rabbitMQ server host
	private String username = ""; 			// user name of the rabbitMQ server account
	private String password = ""; 			// password of the rabbitmMQ server account
	private String p12FilePath = null; 		// p12 file path for the key store
	private String p12Password = ""; 		// password for the p12-file
	private String caCertPath = null; 		// ca-cert file path for the trust store 
	private boolean isTLS = false;

	
	/**
	 * constructor used for non-encrypted connection to the message server rabbitMQ
	 * @param host	 		host of the rabbitMQ server
	 * @param username		the user name of the rabbitMQ server account
	 * @param password 		the password of the rabbitMQ server account
	 */
	public RabbitMQHander(String host, String username, String password) {
		this.host = host;
		this.username = username;
		this.password = password;
		
		// no tls
		isTLS = false;
	}
	
	
	/**
	 * constructor used for tls-encrypted connection to the message server rabbitMQ
	 * @param host	 		host of the rabbitMQ server
	 * @param username		the user name of the rabbitMQ server account
	 * @param password 		the password of the rabbitMQ server account
	 * @param p12FilePath   p12 file path for the key store
	 * @param caCertPath 	ca-cert file path for the trust store
	 */
	public RabbitMQHander(String host, String username, String password, String p12FilePath, String p12Password, String caCertPath) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.p12FilePath = p12FilePath;
		this.p12Password = p12Password;
		this.caCertPath = caCertPath;
		
		// use tls
		isTLS = true;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									factory methods to create new rabbitMQ-communicators 							  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		return new WorkerPublisherRMQ(this, queueName);
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
		return new WorkerConsumerRMQ(this, queueName, messageReceiver);
	}
	
	
	/**
	 * returns a new instance of a broadcast publisher. It sends messages on the declared exchange. More than
	 * one broadcast publisher can send messages on the same exchange. Temporary queues are used, which means
	 * that the queue gets a random name and will be removed after the client disconnected.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @return
	 */
	public BroadcastPublisherRMQ getBroadcastPublisher(String exchangeName) {
		return new BroadcastPublisherRMQ(this, exchangeName);
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
		return new BroadcastConsumerRMQ(this, exchangeName, messageReceiver);
	}
	
	
	/**
	 * returns a new instance of a routing publisher. It sends messages on the declared exchange for a certain routing key.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @return
	 */
	public RoutingPublisherRMQ getRoutingPublisher(String exchangeName) {
		return new RoutingPublisherRMQ(this, exchangeName);
	}
	
	
	/**
	 * returns a new instance of a routing consumer. It receives messages for all registered routing keys.
	 * @param exchangeName 		name of the exchange to publish message on
	 * @param routingKeys 		list of routing keys, only messages with routing keys in this list are received
	 * @param messageReceiver	method that defines what happens with the received message
	 * @return
	 */
	public RoutingConsumerRMQ getRoutingConsumer(String exchangeName, ArrayList<String> routingKeys, IMessageReceiver messageReceiver) {
		return new RoutingConsumerRMQ(this, exchangeName, routingKeys, messageReceiver);
	}
	


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getP12FilePath() {
		return p12FilePath;
	}


	public void setP12FilePath(String p12FilePath) {
		this.p12FilePath = p12FilePath;
	}


	public String getCaCertPath() {
		return caCertPath;
	}


	public void setCaCertPath(String caCertPath) {
		this.caCertPath = caCertPath;
	}


	public boolean isTLS() {
		return isTLS;
	}


	public void setTLS(boolean isTLS) {
		this.isTLS = isTLS;
	}


	public String getP12Password() {
		return p12Password;
	}


}
