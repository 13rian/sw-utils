package ch.wenkst.sw_utils.http.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.http.HttpConstants;
import ch.wenkst.sw_utils.http.HttpStatus;

public class HttpResponseBuilder extends HttpBuilder {
	private static final Logger logger = LoggerFactory.getLogger(HttpResponseBuilder.class);


	/**
	 * holds methods to build a http response
	 */
	public HttpResponseBuilder() {
		super();
	}


	/**
	 * sets the status of the response
	 * @param status 	http status response
	 * @return 			this object
	 */
	public HttpResponseBuilder status(int status) {
		String statusTxt = HttpStatus.intToHttpStatus(status);
		if (statusTxt == null) {
			logger.error("http-status " + status + " is not supported");
		
		} else {
			firstLine = HttpConstants.PROTOCOL + " " + status + " " + statusTxt;
		}
		
		return this;		
	}
}
