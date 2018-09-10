package ch.wenkst.sw_utils.miscellaneous;

public class StatusResult {
	protected boolean success = true; 		// true if the result is successful, false otherwise 
	protected Object result = null; 		// the result object
	protected String errorMsg = ""; 		// the error message if it is an unsuccessful result
	
	
	/**
	 * constructor for an empty status result object
	 */
	public StatusResult() {
		
	}
	
	
	/**
	 * constructor for a status result object that has status success and contains the passed result object
	 * @param result 	result object
	 */
	public StatusResult(Object result) {
		this.result = result;
	}
	
	
	/**
	 * constructor for a status result object that has status error and contains the passed error message
	 * @param errorMsg
	 */
	public StatusResult(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}
