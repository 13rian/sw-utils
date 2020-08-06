package ch.wenkst.sw_utils.callback;

@FunctionalInterface
public interface ValueCallback<T> {
	/**
	 * callback to pass a value
	 * @param value		any value
	 */
	public void onValue(T value);
}
