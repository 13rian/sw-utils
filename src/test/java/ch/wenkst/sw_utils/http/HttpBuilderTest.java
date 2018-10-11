package ch.wenkst.sw_utils.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.http.builder.HttpRequestBuilder;
import ch.wenkst.sw_utils.http.builder.HttpResponseBuilder;

public class HttpBuilderTest {
	private static String crlf = "\r\n"; 			// carriage return line feed to indicate new lines
	
	private static String httpRequest;  			// http request that is generated during the tests
	private static String httpResponse;  			// http response that is generated during the tests

	
	/**
	 * prepare the request and response that are created during the tests
	 */
	@BeforeAll
	public static void prepareHttpRequests() {
		// http request
		httpRequest = new StringBuilder()
				.append("POST /v1/ HTTP/1.1").append(crlf)
				.append("Authorization: Some secret code").append(crlf)
				.append("Accept: */*").append(crlf)
				.append("Connection: keep-alive").append(crlf)
				.append("Host: wenkst").append(crlf)
				.append("Content-Length: 50").append(crlf)
				.append("Content-Type: text/plain").append(crlf)
				.append(crlf)
				.append("This is the html request body with no deep meaning")
				.toString();
				
		// http response
		httpResponse = new StringBuilder()
				.append("HTTP/1.1 403 Forbidden").append(crlf)
				.append("Authorization: Some secret code").append(crlf)
				.append("Accept: */*").append(crlf)
				.append("Connection: keep-alive").append(crlf)
				.append("Content-Length: 31").append(crlf)
				.append("Content-Type: text/plain").append(crlf)
				.append(crlf)
				.append("you have no access to this page")
				.toString();
	}
	

	/**
	 * create a http request
	 */
	@Test
	@DisplayName("http request")
	public void httpRequest() {
		HttpRequestBuilder reqBuilder = new HttpRequestBuilder();
		
		reqBuilder.preparePost("https://wenkst/v1/")
		.setHeaderProperty("Content-Type", "text/plain")
		.setHeaderProperty("Authorization", "Some secret code")
		.setBody("This is the html request body with no deep meaning");
		
		Assertions.assertEquals(httpRequest, reqBuilder.toString(), "http request");
	}
	
	
	/**
	 * create a http response
	 */
	@Test
	@DisplayName("http response")
	public void httpResponse() {
		HttpResponseBuilder respBuilder = new HttpResponseBuilder();

		respBuilder.status(403)
		.setHeaderProperty("Content-Type", "text/plain")
		.setHeaderProperty("Authorization", "Some secret code")
		.setBody("you have no access to this page");
		
		Assertions.assertEquals(httpResponse, respBuilder.toString(), "http response");
	}
	
}
