package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

import java.util.List;

import org.bson.Document;

@FunctionalInterface
public interface DocumentListCallback {
	/**
	 * Called when the operation completes.
	 * @param dbResults 	the result of the operation, is null if an error occurred
	 * @param error     	the error of the operation or null if the operation completed successfully
	 */
	public void onResult(List<Document> dbResults, Exception error);
}
