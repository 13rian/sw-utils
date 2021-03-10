package ch.wenkst.sw_utils.http.parser;

public class HttpRequestParser extends HttpParser {
	private String httpMethod = null;
	private String requestURI = null;


	/**
	 * holds methods to parse a http request message
	 */
	public HttpRequestParser() {
		super();
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
	
	
	private void parseRequestLine() {
		String[] parts = firstLine.split(" ");
		httpMethod = parts[0];
		requestURI = parts[1];
	}
	
	
	@Override
	public void clearAfterFullMessage() {
		super.clearAfterFullMessage();
		httpMethod = null;
		requestURI = null;
	}
}
