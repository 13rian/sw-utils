package ch.wenkst.sw_utils.callback;

@FunctionalInterface
public interface ResultCallback<T> {
	public void onResult(T result, String errorMsg);
}
