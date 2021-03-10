package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

public class MessageRMQ {	
	protected byte[] body;
	protected Map<String, Object> headerProperties;
	protected Envelope envelope;
	protected AMQP.BasicProperties properties = MessageProperties.TEXT_PLAIN;; 			

	
	/**
	 * holds a message to send over the rabbitMQ server
	 */
	public MessageRMQ() {
		
	}
	
	
	/**
	 * holds a message to send over the rabbitMQ server
	 * @param body 			the message body
	 * @param envelope 		group of parameters used for AMQP's Basic methods
	 * @param properties 	ampq message properties
	 */
	public MessageRMQ(byte[] body, Envelope envelope, BasicProperties properties) {
		this.body = body;
		this.envelope = envelope;
		this.properties = properties;
	}
	
	
	/**
	 * holds a message to send over the rabbitMQ server
	 * @param body 					the message body
	 */
	public MessageRMQ(byte[] body) {
		this.body = body;
		headerProperties = new HashMap<>();
	}
	
	
	/**
	 * holds a message to send over the rabbitMQ server
	 * @param message 				the string message to send, will be sent utf-8 encoded
	 */
	public MessageRMQ(String message) {
		body = message.getBytes(StandardCharsets.UTF_8);
		headerProperties = new HashMap<>();
	}
	
	
	/**
	 * adds a header property to the message
	 * @param key 		the key of the property
	 * @param value 	the value of the property
	 */
	public void addHeaderProperty(String key, Object value) {
		if (headerProperties == null) {
			headerProperties = new HashMap<String, Object>();
		}
		headerProperties.put(key, value);
		properties = properties.builder().headers(headerProperties).build();
	}
	

	/**
	 * returns the header property with the passed key
	 * @param key 	key of the header property
	 * @return 		header property or null if the property does not exist
	 */
	public Object getHeaderProperty(String key) {
		if (properties.getHeaders() != null) {
			return properties.getHeaders().get(key);
		} else {
			return null;
		}
	}
	
	public Map<String, Object> getHeaderProperties() {
		return headerProperties;
	}

	public void setHeaderProperties(Map<String, Object> headerProperties) {
		this.headerProperties = headerProperties;
	}
	
	public byte[] getBody() {
		return body;
	}
	
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public String getBodyStr() {
		return new String(body, StandardCharsets.UTF_8);
	}
	
	public AMQP.BasicProperties getProperties() {
		return properties;
	}

	public void setProperties(AMQP.BasicProperties properties) {
		this.properties = properties;
	}
}
