package ch.wenkst.sw_utils.http.parser;

public class HttpResponseParser extends HttpParser {
	// status information of the response
	private int status = -1; 				// status of the http message
	private String statusTxt = null; 		// the reason phrase of the status e.g. OK for status 200


	/**
	 * holds methods to parse a http response message
	 */
	public HttpResponseParser() {
		super();
	}



	/**
	 * extracts the status code and phrase from the status line
	 */
	private void parseStatusLine() {
		String[] parts = firstLine.split(" ");
		status = Integer.parseInt(parts[1]);
		statusTxt = parts[2];		
	}
	
	
	/**
	 * returns true if no error status was received (all statuses with 200)
	 * @return 		true if the http status is a 200 status, false otherwise
	 */
	public boolean isPositiveStatus() {
		return (int) Math.floor(getStatus()/100) == 2;
	}



	public int getStatus() {
		if (statusTxt == null) {
			parseStatusLine();
		}
		
		return status;
	}

	public String getStatusTxt() {
		if (statusTxt == null) {
			parseStatusLine();
		}
		
		return statusTxt;
	}
}
