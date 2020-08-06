package ch.wenkst.sw_utils.callback;

@FunctionalInterface
public interface OperationCallback {
	/**
	 * callback for an operation that finished
	 */
	public void onFinish();
}
