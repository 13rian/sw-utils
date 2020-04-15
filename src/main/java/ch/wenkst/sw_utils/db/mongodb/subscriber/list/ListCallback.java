package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

import java.util.List;

import ch.wenkst.sw_utils.db.mongodb.base.BaseEntity;

@FunctionalInterface
public interface ListCallback {
	/**
	 * Called when the operation completes.
	 * @param dbResults 	the result of the operation, is null if an error occurred
	 * @param error     	the error of the operation or null if the operation completed successfully
	 */
	public void onResult(List<? extends BaseEntity> dbResults, Exception error);
}
