package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

import java.util.ArrayList;
import java.util.List;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import ch.wenkst.sw_utils.db.mongodb.base.BaseEntity;

public abstract class ListSubscriber implements Subscriber<BaseEntity> {	
	protected final List<BaseEntity> result;		// result list of the db
	protected Exception error = null; 				// the first error that occurred
	protected volatile boolean completed; 			// true if the stream is completed, false otherwise

	/**
	 * base subscriber for a reactive stream mongodb operation
	 */
	public ListSubscriber() {
		result = new ArrayList<BaseEntity>();
	}

	@Override
	public void onSubscribe(final Subscription subscription) {
		subscription.request(Integer.MAX_VALUE);
	}

	@Override
	public void onNext(BaseEntity t) {
		result.add(t);
	}

	@Override
	public void onError(final Throwable t) {
		if (error == null) {
			error = new Exception(t);
		}
		onComplete();
	}	

	public List<? extends BaseEntity> getResult() {
		return result;
	}

	public Throwable getError() {
		return error;
	}

	public boolean isCompleted() {
		return completed;
	}
}
