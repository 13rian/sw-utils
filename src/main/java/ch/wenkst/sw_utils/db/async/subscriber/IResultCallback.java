package ch.wenkst.sw_utils.db.async.subscriber;

import java.util.List;

@FunctionalInterface
public interface IResultCallback<T> {
	/**
	 * Called when the operation completes.
	 * @param <T>
	 * @param result 	the result of the operation, is null if an error occurred
	 * @param error     the error of the operation or null if the operation completed successfully
	 */
	public void onResult(List<T> result, Exception error);
}
