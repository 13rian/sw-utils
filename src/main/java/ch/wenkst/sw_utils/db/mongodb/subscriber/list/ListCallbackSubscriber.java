package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

public class ListCallbackSubscriber extends ListSubscriber {
	private ListCallback callback;
	
	public ListCallbackSubscriber(ListCallback callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {				
		callback.onResult(result, error);
	}
}
