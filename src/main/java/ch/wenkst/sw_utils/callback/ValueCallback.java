package ch.wenkst.sw_utils.callback;

@FunctionalInterface
public interface ValueCallback<T> {
	public void onValue(T value);
}
