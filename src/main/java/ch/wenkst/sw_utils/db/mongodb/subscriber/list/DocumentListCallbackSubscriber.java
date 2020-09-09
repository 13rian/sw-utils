package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

public class DocumentListCallbackSubscriber extends DocumentListSubscriber {
	private DocumentListCallback callback;
	
	public DocumentListCallbackSubscriber(DocumentListCallback callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {				
		callback.onResult(result, error);
	}
}
