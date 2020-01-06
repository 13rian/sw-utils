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
	 * create a successful status result
	 * @param result 	the result 
	 * @return
	 */
	public static StatusResult fromResult(Object result) {
		StatusResult statusResult = new StatusResult();
		statusResult.setSuccess(true);
		statusResult.setResult(result);
		return statusResult;
	}
	
	
	
	/**
	 * creates a failed status result
	 * @param errorMsg 		the error message
	 * @return
	 */
	public static StatusResult fromError(String errorMsg) {
		StatusResult statusResult = new StatusResult();
		statusResult.setSuccess(false);
		statusResult.setErrorMsg(errorMsg);
		return statusResult;
	}

	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@SuppressWarnings("unchecked")
	public <T> T getResult() {
		return (T) result;
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
