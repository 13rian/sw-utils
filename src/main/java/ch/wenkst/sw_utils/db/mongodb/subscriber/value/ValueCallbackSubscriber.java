package ch.wenkst.sw_utils.db.mongodb.subscriber.value;

public class ValueCallbackSubscriber<T> extends ValueSubscriber<T> {
	private ValueCallback<T> callback;
	
	public ValueCallbackSubscriber(ValueCallback<T> callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {
		callback.onResult(result, error);
	}
}
