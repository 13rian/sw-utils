package ch.wenkst.sw_utils.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttCallbackHandler implements MqttCallback {
	private static final Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	public MqttCallbackHandler() {

	}

	@Override
	public void connectionLost(Throwable cause) {
		logger.error("mqtt connection lost callback called: ", cause);
	}
	

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		
	}
}
