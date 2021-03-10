package ch.wenkst.sw_utils.http;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.http.builder.HttpRequestBuilder;
import ch.wenkst.sw_utils.http.builder.HttpResponseBuilder;

public class HttpBuilderTest {

	/**
	 * create a http request
	 * @throws MalformedURLException 
	 */
	@Test
	@DisplayName("http request")
	public void httpRequest() throws MalformedURLException {
		HttpRequestBuilder reqBuilder = new HttpRequestBuilder();
		
		reqBuilder.preparePost("https://wenkst/v1/")
		.headerProperty("Content-Type", "text/plain")
		.headerProperty("Authorization", "Some secret code")
		.body("This is the html request body with no deep meaning");
		
		String request = reqBuilder.toString();
		Assertions.assertTrue(request.contains("POST /v1/ HTTP/1.1"));
		Assertions.assertTrue(request.contains("content-type"));
		Assertions.assertTrue(request.contains("authorization"));
		Assertions.assertTrue(request.contains("This is the html request body with no deep meaning"));
	}
	
	
	/**
	 * create a http response
	 */
	@Test
	@DisplayName("http response")
	public void httpResponse() {
		HttpResponseBuilder respBuilder = new HttpResponseBuilder();

		respBuilder.status(403)
		.headerProperty("Content-Type", "text/plain")
		.headerProperty("Authorization", "Some secret code")
		.body("you have no access to this page");
		
		String response = respBuilder.toString();
		Assertions.assertTrue(response.contains("HTTP/1.1 403 Forbidden"));
		Assertions.assertTrue(response.contains("content-type"));
		Assertions.assertTrue(response.contains("authorization"));
		Assertions.assertTrue(response.contains("you have no access to this page"));
	}
}
