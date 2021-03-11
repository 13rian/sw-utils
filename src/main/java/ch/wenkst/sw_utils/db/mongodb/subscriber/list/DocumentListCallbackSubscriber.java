package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

import org.bson.Document;

public class DocumentListCallbackSubscriber extends ListSubscriber<Document> {
	private DocumentListResultCallback callback;
	
	public DocumentListCallbackSubscriber(DocumentListResultCallback callback) {
		this.callback = callback;
	}
			

	@Override
	public void onComplete() {				
		callback.onResult(result, error);
	}
}
