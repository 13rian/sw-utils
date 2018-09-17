package ch.wenkst.sw_utils.http.builder;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseBuilder extends HttpBuilder {
	private static final Logger logger = LoggerFactory.getLogger(HttpResponseBuilder.class);

	private HashMap<Integer, String> httpStatusMap = null;


	/**
	 * holds methods to build a http response
	 */
	public HttpResponseBuilder() {
		super();

		// not complete at all, needs to be completed if desired
		httpStatusMap = new HashMap<>();
		httpStatusMap.put(100, "Continue");
		httpStatusMap.put(101, "Switching Protocols");
		
		httpStatusMap.put(200, "OK");
		httpStatusMap.put(201, "Created");
		httpStatusMap.put(202, "Accepted");
		httpStatusMap.put(203, "Non-Authoritative Information");
		httpStatusMap.put(204, "No Content");
		httpStatusMap.put(205, "Reset Content");
		httpStatusMap.put(206, "Partial Content");
		
		
		httpStatusMap.put(300, "Multiple Choices");
		httpStatusMap.put(301, "Moved Permanently");
		httpStatusMap.put(302, "Found");
		httpStatusMap.put(303, "See Other");
		httpStatusMap.put(304, "Not Modified");
		httpStatusMap.put(307, "Temporary Redirect");
		httpStatusMap.put(308, "Permanent Redirect");
		
		httpStatusMap.put(400, "Bad Request");
		httpStatusMap.put(401, "Unauthorized");
		httpStatusMap.put(403, "Forbidden");
		httpStatusMap.put(404, "Not Found");
		httpStatusMap.put(405, "Method Not Allowed");
		httpStatusMap.put(406, "Not Acceptable");
		httpStatusMap.put(407, "Proxy Authentication Require");
		httpStatusMap.put(408, "Request Timeout");
		httpStatusMap.put(409, "Conflict");
		httpStatusMap.put(410, "Gone");
		httpStatusMap.put(411, "Length Required");
		httpStatusMap.put(412, "Precondition Failed");
		httpStatusMap.put(413, "Payload Too Large");
		httpStatusMap.put(414, "URI Too Long");
		httpStatusMap.put(415, "Unsupported Media Type");
		httpStatusMap.put(416, "Range Not Satisfiable");
		httpStatusMap.put(417, "Expectation Failed");
		httpStatusMap.put(426, "Upgrade Required");
		httpStatusMap.put(428, "Precondition Required");
		httpStatusMap.put(429, "Too Many Requests");
		httpStatusMap.put(431, "Request Header Fields Too Large");
		httpStatusMap.put(451, "Unavailable For Legal Reasons");
		
		httpStatusMap.put(500, "Internal Server Error");
		httpStatusMap.put(501, "Not Implemented");
		httpStatusMap.put(502, "Bad Gateway");
		httpStatusMap.put(503, "Service Unavailable");
		httpStatusMap.put(504, "Gateway Timeout");
		httpStatusMap.put(505, "HTTP Version Not Supported");
		httpStatusMap.put(511, "Network Authentication Required");
	}


	/**
	 * sets the status of the response
	 * @param status 	http status response
	 * @return 			this object
	 */
	public HttpResponseBuilder status(int status) {
		String statusTxt = httpStatusMap.get(status);
		if (statusTxt == null) {
			logger.error("http-status " + status + " is not supported");
		
		} else {
			// define the first line of the response
			firstLine = "HTTP/1.1 " + status + " " + statusTxt;
		}
		
		return this;		
	}







}






