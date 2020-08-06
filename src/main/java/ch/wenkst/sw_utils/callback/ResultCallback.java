package ch.wenkst.sw_utils.callback;

@FunctionalInterface
public interface ResultCallback<T> {
	/**
	 * callback to pass a result or an error message
	 * @param result		the result
	 * @param errorMsg		the error message in case of an error
	 */
	public void onResult(T result, String errorMsg);
}
