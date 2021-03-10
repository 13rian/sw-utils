package ch.wenkst.sw_utils.messaging.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttHandler {
	private static final Logger logger = LoggerFactory.getLogger(MqttHandler.class);
	
	protected String brokerUrl;
	protected String clientId;
	protected MqttConnectOptions connectOptions;
	private Map<String, IMqttMessageListener> subsciptions;
	
	// note: only one client could be used but is seems that it is not possible to publish a message in a listener with the same client
	private MqttClient publisher;
	private MqttClient subscriber;
	
	
	/**
	 * handles the connection to the mqtt broker
	 */
	public MqttHandler() {
		subsciptions = new HashMap<>();
	}
	
	
	/**
	 * initializes the mqtt broker
	 * @param brokerUrl 		the url to the broker
	 * @param sslContext 		the ssl context, can be null if no encryption is used
	 * @param username			the username, can be null if no credentials are used
	 * @param password			the password, can be null if no credentials are used
	 */
	public void init(String brokerUrl, SSLContext sslContext, String username, String password) {
		this.brokerUrl = brokerUrl;
		connectOptions = createConnectOptions(sslContext, username, password);
	}
	
	protected MqttConnectOptions createConnectOptions(SSLContext sslContext, String username, String password) {
		MqttConnectionConfig mqttConnectionConfig = new MqttConnectionConfig();
		return mqttConnectionConfig.createConnectOptions(sslContext, username, password);
	}
	
	
	/**
	 * returns true if the broker is reachable and false if not
	 * @return
	 */
	public boolean isReachable() {
		try {
			MqttClient testPublisher = new MqttClient(brokerUrl,  UUID.randomUUID().toString(), null);
			testPublisher.connect(connectOptions);
			testPublisher.disconnect();
			testPublisher.close();
			logger.debug("mqtt broker is reachable, broker url: " + brokerUrl);
			return true;

		} catch (Exception e) {
			logger.error("mqtt broker cannot be reached under the url: " + brokerUrl, e);
			return false;
		}
	}
	
	
	/**
	 * creates a new mqtt client
	 * @param clientId 		unique id of the client
	 * @return 				mqtt publisher
	 * @throws MqttException 
	 * @throws MqttSecurityException 
	 */
	public void setupClient(String clientId) throws MqttSecurityException, MqttException {
		this.clientId = clientId;
		logger.debug("open a connection to the mqtt broker with the clientId: " + clientId);
		setupClient();
	}
	
	
	/**
	 * creates a new mqtt client
	 * @return 				mqtt publisher
	 * @throws MqttException 
	 * @throws MqttSecurityException 
	 */
	public synchronized void setupClient() throws MqttSecurityException, MqttException {	
		publisher = new MqttClient(brokerUrl, clientId + "_sub", null);
		publisher.connect(connectOptions);
		publisher.setCallback(new MqttCallbackHandler());
		
		subscriber = new MqttClient(brokerUrl, clientId + "_pub", null);
		subscriber.connect(connectOptions);
	}
	
	
	/**
	 * publishes a message over the mqtt broker
	 * @param topic 		the topic
	 * @param message		the message bytes
	 * @param qos			the quality of service
	 * @param retained		true if the message should be retained, i. e. send to clients that have just connected
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 */
	public synchronized void publish(String topic, byte[] message, int qos, boolean retained) throws MqttPersistenceException, MqttException {
		logger.debug("published message: " + new String(message) + " on topic: " + topic);
		publisher.publish(topic, message, qos, retained);
	}
	
	
	/**
	 * closes all connections to the mqtt broker
	 */
	public synchronized void tearDownClient() {
		disconnectMqttClient(publisher);
		disconnectMqttClient(subscriber);
	}
	
	
	private void disconnectMqttClient(MqttClient mqttClient) {
		try {
			if (mqttClient.isConnected()) {
				mqttClient.disconnect();
			}
			mqttClient.close();
			
		} catch (Exception e) {
			logger.error("error closing mqtt client connection: ", e);
		}
	}
		
	
	/**
	 * adds one subscription to the mqtt client
	 * @param topic 			the topic to listen to
	 * @param messageListener
	 * @throws MqttException
	 */
	public synchronized void addSubscription(String topic, IMqttMessageListener messageListener) throws MqttException {
		subsciptions.put(topic, messageListener);
		subscriber.subscribe(topic, messageListener);
	}
	
	
	/**
	 * unsubscribes the mqtt client form the passed topic
	 * @param topic 			the topic
	 * @throws MqttException 
	 */
	public synchronized void removeSubscription(String topic) throws MqttException {
		subsciptions.remove(topic);
		subscriber.unsubscribe(topic);
	}


	public String getBrokerUrl() {
		return brokerUrl;
	}

	public MqttConnectOptions getOptions() {
		return connectOptions;
	}
}
