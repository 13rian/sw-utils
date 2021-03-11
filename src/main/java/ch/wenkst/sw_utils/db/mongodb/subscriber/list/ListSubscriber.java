package ch.wenkst.sw_utils.db.mongodb.subscriber.list;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class ListSubscriber<T> implements Subscriber<T> {	
	protected final List<T> result;				// result list of the db
	protected Exception error = null; 			// the first error that occurred

	public ListSubscriber() {
		result = new ArrayList<>();
	}

	@Override
	public void onSubscribe(final Subscription subscription) {
		subscription.request(Integer.MAX_VALUE);
	}

	@Override
	public void onNext(T t) {
		result.add(t);
	}

	@Override
	public void onError(final Throwable t) {
		if (error == null) {
			error = new Exception(t);
		}
		onComplete();
	}
}
