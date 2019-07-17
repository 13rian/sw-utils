package ch.wenkst.sw_utils.http.builder;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestBuilder extends HttpBuilder {
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestBuilder.class);
	
	
	/**
	 * holds methods to build a http request
	 */
	public HttpRequestBuilder() {
		super();
	}


	/**
	 * initializes a get request to the passed url
	 * @param url 		the url to which the http request should be made
	 * @return 			this object
	 */
	public HttpRequestBuilder prepareGet(String url) {
		return prepareRequest(url, "GET");
	}
	
	/**
	 * initializes a post request to the passed url
	 * @param url 	the url to which the http request should be made
	 * @return 		this object
	 */
	public HttpRequestBuilder preparePost(String url) {
		return prepareRequest(url, "POST");
	}
	
	/**
	 * initializes a put request to the passed url
	 * @param url 		the url to which the http request should be made
	 * @return 			this object
	 */
	public HttpRequestBuilder preparePut(String url) {
		return prepareRequest(url, "PUT");
	}
	
	/**
	 * initializes a delete request to the passed url
	 * @param url 	the url to which the http request should be made
	 * @return 		this object
	 */
	public HttpRequestBuilder prepareDelete(String url) {
		return prepareRequest(url, "DELETE");
	}
	
	
	/**
	 * initializes a http request
	 * @param url 		the url to which the http request should be made	
	 * @param method 	the http method
	 * @return 			this object
	 */
	private HttpRequestBuilder prepareRequest(String url, String method) {
		try {
			// define the first line of the http request
			URL hostURL = new URL(url);
			String urlPath = hostURL.getPath();
			firstLine = method + " " + urlPath + " HTTP/1.1";
			
			// extract and set the host
			String host = hostURL.getHost();
			int port = hostURL.getPort();

			// set the host property
			String hostProperty = "";
			if (port > -1) {
				hostProperty = host + ":" + port;
			} else {
				hostProperty = host;
			}

			headerProperties.put("host", hostProperty);

		} catch (Exception e) {
			logger.error("error extracting the host of the passed url: " + url, e);
		}

		return this;
	}
}






