package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

public class PojoListCallbackSubscriber extends PojoListSubscriber {
	private PojoListCallback callback;
	
	public PojoListCallbackSubscriber(PojoListCallback callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {				
		callback.onResult(result, error);
	}
}
