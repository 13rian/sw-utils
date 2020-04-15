package ch.wenkst.sw_utils.db.mongodb.subscriber.value;

@FunctionalInterface
public interface ValueCallback<T> {
	/**
	 * Called when the operation completes.
	 * @param <T>
	 * @param result 	the result of the operation, is null if an error occurred
	 * @param error     the error of the operation or null if the operation completed successfully
	 */
	public void onResult(T result, Exception error);
}
