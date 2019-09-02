package ch.wenkst.sw_utils.http.builder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpBuilder {
	protected String firstLine = ""; 							    // defines the first line of the http message, either the request or the response line
	protected HashMap<String,String> headerProperties = null; 		// header fields of the request
	protected byte[] bodyBytes = new byte[0]; 						// the body as bytes
	private static final String CRLF = "\r\n";
	


	/**
	 * holds methods to build a http message
	 */
	public HttpBuilder() {
		// define the header properties that are always present
		headerProperties = new HashMap<>();
		headerProperties.put("connection", "keep-alive");
		headerProperties.put("accept", "*/*");
		headerProperties.put("content-type", "text/plain");
	}



	/**
	 * sets a header property of the request
	 * @param key 		name of the header property
	 * @param value		value of the header property	
	 * @return 			this object
	 */
	public HttpBuilder setHeaderProperty(String key, String value) {
		key = key.toLowerCase();
		headerProperties.put(key, value);

		return this;
	}



	/**
	 * sets the body of the request
	 * @param body 		body as String
	 * @return 			this object
	 */
	public HttpBuilder setBody(String body) {
		return setBody(body.getBytes());
	}
	
	
	
	/**
	 * sets the body of the request
	 * @param body 		body as byte array
	 * @return 			this object	
	 */
	public HttpBuilder setBody(byte[] body) {
		this.bodyBytes = body;

		// set the content length property
		String bodyLength = String.valueOf(bodyBytes.length);
		headerProperties.put("content-length", bodyLength);

		return this;
	}
	
	
	/**
	 * converts the http request to a byte array
	 * @return 		byte array of the http request
	 */
	public byte[] toByteArr() {
		// create the string that defines the http message without a body
		StringBuilder stringBuilder = new StringBuilder();
		
		// the request/status line
		stringBuilder.append(firstLine);
		stringBuilder.append(CRLF);
		
		// the header properties
		for (Map.Entry<String, String> entry : headerProperties.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    
		    stringBuilder.append(key);
			stringBuilder.append(": ");
			stringBuilder.append(value);
			stringBuilder.append(CRLF);
		}
		
		// add the empty line before the body
		stringBuilder.append(CRLF);
		
		
		// covert to bytes
		byte[] headerBytes = stringBuilder.toString().getBytes(StandardCharsets.US_ASCII);
		
		
		
		// concatenate the header bytes and the body bytes to get create the full http message
        byte[] httpMessageBytes = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, httpMessageBytes, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, httpMessageBytes, headerBytes.length, bodyBytes.length);
            
        return httpMessageBytes;
	}
	
	
	/**
	 * returns the byte array representing the http request  
	 */
	public String toString() {
		byte[] httpMessageBytes = toByteArr();
		return new String(httpMessageBytes, StandardCharsets.US_ASCII);
	}



}








