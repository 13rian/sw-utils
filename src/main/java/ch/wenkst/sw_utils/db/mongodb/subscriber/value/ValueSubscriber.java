package ch.wenkst.sw_utils.db.mongodb.subscriber.value;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class ValueSubscriber<T> implements Subscriber<T> {	
	protected T result;						// result of the db operation
	protected Exception error = null; 		// the first error that occurred
	
	public ValueSubscriber() {
		result = null;
	}

	@Override
	public void onSubscribe(final Subscription subscription) {
		subscription.request(Integer.MAX_VALUE);
	}

	@Override
	public void onNext(final T t) {
		result = t;
	}

	@Override
	public void onError(final Throwable t) {
		if (error == null) {
			error = new Exception(t);
		}
		onComplete();
	}
}
