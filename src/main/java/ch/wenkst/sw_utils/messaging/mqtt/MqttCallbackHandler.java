package ch.wenkst.sw_utils.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;

public class MqttCallbackHandler implements MqttCallback {
	private static final Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	private MqttHandler mqttHandler;
	
	
	public MqttCallbackHandler(MqttHandler mqttHandler) {
		this.mqttHandler = mqttHandler;
	}

	
	@Override
	public void connectionLost(Throwable cause) {
		new Thread(() -> {
			logger.error("connection to the mqtt server was lost, try to reconnect: ", cause);
			
			while (!mqttHandler.isReachable()) {
				logger.debug("mqtt server is not reachable, try to reconnect in 10 seconds");
				Utils.sleep(10000);
			}
			
			try {
				logger.info("mqtt server is reachable, reconnect the client");
				mqttHandler.reconnect();
				
			} catch (Exception e) {
				logger.error("failed to reconnect the mqtt client: ", e);
			}
		}).run();
	}
	

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		
	}
}
