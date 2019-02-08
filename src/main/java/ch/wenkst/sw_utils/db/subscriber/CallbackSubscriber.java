package ch.wenkst.sw_utils.db.subscriber;

public class CallbackSubscriber<T> extends BaseSubscriber<T> {
	private IResultCallback<T> callback;
	
	public CallbackSubscriber(IResultCallback<T> callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {
		super.onComplete();
		callback.onResult(result, error);
	}
}
