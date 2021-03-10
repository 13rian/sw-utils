package ch.wenkst.sw_utils.http.builder;

import java.net.MalformedURLException;
import java.net.URL;

import ch.wenkst.sw_utils.http.HttpConstants;

public class HttpRequestBuilder extends HttpBuilder {
	/**
	 * holds methods to build a http request
	 */
	public HttpRequestBuilder() {
		super();
	}


	public HttpRequestBuilder prepareGet(String url) throws MalformedURLException {
		return setFirstLineAndHostProperty(url, HttpConstants.GET);
	}
	
	
	public HttpRequestBuilder preparePost(String url) throws MalformedURLException {
		return setFirstLineAndHostProperty(url, HttpConstants.POST);
	}
	
	
	public HttpRequestBuilder preparePut(String url) throws MalformedURLException {
		return setFirstLineAndHostProperty(url, HttpConstants.PUT);
	}
	

	public HttpRequestBuilder prepareDelete(String url) throws MalformedURLException {
		return setFirstLineAndHostProperty(url, HttpConstants.DELETE);
	}
	
	
	private HttpRequestBuilder setFirstLineAndHostProperty(String url, String method) throws MalformedURLException {
		URL urlObj = new URL(url);
		setFirstLine(urlObj, method);
		setHostHeaderProperty(urlObj);
		return this;
	}
	
	
	private void setFirstLine(URL url, String method) throws MalformedURLException {
		String fileStr = url.getFile();
		firstLine = method + " " + fileStr + " " + HttpConstants.PROTOCOL;
	}
	
	
	private void setHostHeaderProperty(URL url) {
		String host = url.getHost();
		int port = url.getPort();

		String hostProperty = "";
		if (port > -1) {
			hostProperty = host + ":" + port;
		} else {
			hostProperty = host;
		}

		headerProperties.put("host", hostProperty);
	}
}
