package ch.wenkst.sw_utils.http.parser;

public class HttpRequestParser extends HttpParser {
	private String httpMethod = null; 	// the method of the http request
	private String requestURI = null;	// the request uri


	/**
	 * holds methods to parse a http response message
	 */
	public HttpRequestParser() {
		super();
	}

	
	/**
	 * extracts the method and the request uri from the first line of the http-request
	 */
	private void parseRequestLine() {
		String[] parts = firstLine.split(" ");
		
		httpMethod = parts[0];
		requestURI = parts[1];
	}


	public String getHttpMethod() {
		if (requestURI == null) {
			parseRequestLine();
		}
		
		return httpMethod.toUpperCase();
	}


	public String getRequestURI() {
		if (requestURI == null) {
			parseRequestLine();
		}
		
		return requestURI;
	}
	
	
	@Override
	public void clearAfterFullMessage() {
		super.clearAfterFullMessage();
		httpMethod = null;
		requestURI = null;
	}
}
