package ch.wenkst.sw_utils.messaging.mqtt;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class MqttConnectionConfig {
	protected MqttConnectOptions connectOptions;
	
	
	public MqttConnectOptions createConnectOptions(SSLContext sslContext, String username, String password) {
		connectOptions = new MqttConnectOptions();
		configureSsl(sslContext);
		configureConnectionParameters();
		configureCredentials(username, password);
		return connectOptions;
	}
	
	
	private void configureSsl(SSLContext sslContext) {
		if (sslContext != null) {
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			connectOptions.setSocketFactory(sslSocketFactory);
		}
	}
	
	
	private void configureConnectionParameters() {
		connectOptions.setAutomaticReconnect(true);
		connectOptions.setCleanSession(false);				// to keep the subscriptions after disconnect
		connectOptions.setConnectionTimeout(30);
		connectOptions.setKeepAliveInterval(60);
		connectOptions.setHttpsHostnameVerificationEnabled(false);
	}
	
	
	private void configureCredentials(String username, String password) {
		if (username != null && password != null) {
			connectOptions.setUserName(username);
			connectOptions.setPassword(password.toCharArray());
		}
	}
}
