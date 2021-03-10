package ch.wenkst.sw_utils.http.builder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.http.HttpConstants;

public class HttpBuilder {
	protected String firstLine = "";
	protected HashMap<String, String> headerProperties = null;
	protected byte[] bodyBytes = new byte[0];

	
	public HttpBuilder() {
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
	public HttpBuilder headerProperty(String key, String value) {
		key = key.toLowerCase();
		headerProperties.put(key, value);

		return this;
	}



	/**
	 * sets the body of the request
	 * @param body 		body as String
	 * @return 			this object
	 */
	public HttpBuilder body(String body) {
		return body(body.getBytes());
	}
	
	
	
	/**
	 * sets the body of the request
	 * @param body 		body as byte array
	 * @return 			this object	
	 */
	public HttpBuilder body(byte[] body) {
		this.bodyBytes = body;
		String bodyLength = String.valueOf(bodyBytes.length);
		headerProperties.put("content-length", bodyLength);
		return this;
	}
	
	
	/**
	 * converts the http request to a byte array
	 * @return 		byte array of the http request
	 */
	public byte[] toByteArr() {
		byte[] requestTargetBytes = requestTarget().getBytes(StandardCharsets.US_ASCII);
		byte[] headerBytes = header().getBytes(StandardCharsets.US_ASCII);
		return Conversion.concatArrays(requestTargetBytes, headerBytes, bodyBytes);
	}
	
	
	private String requestTarget() {
		return firstLine + HttpConstants.CRLF;
	}
	
	
	private String header() {
		StringBuilder stringBuilder = new StringBuilder();

		headerProperties.entrySet()
				.stream()
				.forEach((entry) -> {
					stringBuilder.append(entry.getKey());
					stringBuilder.append(": ");
					stringBuilder.append(entry.getValue());
					stringBuilder.append(HttpConstants.CRLF);
				});
		
		stringBuilder.append(HttpConstants.CRLF);
		return stringBuilder.toString();
	}
	
	
	public String toString() {
		byte[] httpMessageBytes = toByteArr();
		return new String(httpMessageBytes, StandardCharsets.US_ASCII);
	}
}
