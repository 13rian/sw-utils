package ch.wenkst.sw_utils.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttCallbackHandler implements MqttCallback {
	private static final Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	private MqttClient mqttClient;
	
	
	public MqttCallbackHandler(MqttClient mqttClient) {
		this.mqttClient = mqttClient;
	}

	
	@Override
	public void connectionLost(Throwable cause) {
		logger.error("connection to the mqtt server was lost, try to reconnect");
		
		try {
			mqttClient.reconnect();
		} catch (Exception e) {
			logger.error("failed to reconnect the mqtt client: ", e);
		}
	}
	

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		
	}
}
