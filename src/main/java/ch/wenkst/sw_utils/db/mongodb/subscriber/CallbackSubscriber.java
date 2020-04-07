package ch.wenkst.sw_utils.db.mongodb.subscriber;

public class CallbackSubscriber<T> extends BaseSubscriber<T> {
	private IResultCallback<T> callback;
	
	public CallbackSubscriber(IResultCallback<T> callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {
		callback.onResult(result, error);
	}
}
