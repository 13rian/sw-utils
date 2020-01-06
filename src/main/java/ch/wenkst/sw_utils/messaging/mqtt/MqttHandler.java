package ch.wenkst.sw_utils.messaging.mqtt;

import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttHandler {
	private static final Logger logger = LoggerFactory.getLogger(MqttHandler.class);
	
	protected String brokerUrl; 				// the url to the mqtt broker
	protected MqttConnectOptions options; 		// mqtt connection configuration
	
	// note: only one client could be used but is seems that it is not possible to publish a message in a listener with the same client
	protected MqttClient publisher; 			// mqtt client to publish messages
	protected MqttClient subscriber; 			// mqtt client to subscribe to messages
	
	
	/**
	 * handles the connection to the mqtt broker
	 */
	public MqttHandler() {
		
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
	
		
		// create the connect options
		options = new MqttConnectOptions();
		
		// set the ssl server socket factory for tls encryption
		if (sslContext != null) {
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			options.setSocketFactory(sslSocketFactory);
		}
		
		options.setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(60);
		options.setKeepAliveInterval(60);
		options.setHttpsHostnameVerificationEnabled(false); 		// disable the host name verification
		
		// set the username and the password
		if (username != null && password != null) {
			options.setUserName(username);
			options.setPassword(password.toCharArray());
		}
	}
	
	
	
	/**
	 * returns true if the broker is reachble and false if not
	 * @return
	 */
	public boolean isReachable() {
		try {
			MqttClient testPublisher = new MqttClient(brokerUrl,  UUID.randomUUID().toString(), null);
			testPublisher.connect(options);
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
		publisher = new MqttClient(brokerUrl, clientId + "_sub", null);
		subscriber = new MqttClient(brokerUrl, clientId + "_pub", null);
		publisher.connect(options);
		subscriber.connect(options);
	}
		
	
	/**
	 * adds one subscription to the mqtt client
	 * @param topic 			the topic to listen to
	 * @param messageListener
	 * @throws MqttException
	 */
	public void addSubscription(String topic, IMqttMessageListener messageListener) throws MqttException {
		subscriber.subscribe(topic, messageListener);
	}
	
	
	/**
	 * unsubscribes the mqtt client form the passed topic
	 * @param topic 			the topic
	 * @throws MqttException 
	 */
	public void removeSubscription(String topic) throws MqttException {
		subscriber.unsubscribe(topic);
	}


	public String getBrokerUrl() {
		return brokerUrl;
	}

	public MqttConnectOptions getOptions() {
		return options;
	}

	public MqttClient getPublisher() {
		return publisher;
	}

	public MqttClient getSubscriber() {
		return subscriber;
	}
}
