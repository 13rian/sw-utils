package ch.wenkst.sw_utils.http.parser;

public class HttpResponseParser extends HttpParser {
	private int status = -1;
	private String statusTxt = null;


	/**
	 * holds methods to parse a http response message
	 */
	public HttpResponseParser() {
		super();
	}
	
	
	/**
	 * returns true if no error status was received (all statuses with 200)
	 * @return 		true if the http status is a 200 status, false otherwise
	 */
	public boolean isSuccessStatus() {
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
	
	
	private void parseStatusLine() {
		String[] parts = firstLine.split(" ");
		status = Integer.parseInt(parts[1]);
		
		statusTxt = "";
		for (int i=2; i<parts.length; i++) {
			statusTxt = statusTxt + parts[i] + " ";
		}
		statusTxt = statusTxt.trim();
	}
	
	
	@Override
	public void clearAfterFullMessage() {
		super.clearAfterFullMessage();
		status = -1;
		statusTxt = null;
	}
}
