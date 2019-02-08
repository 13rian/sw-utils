package ch.wenkst.sw_utils.db.subscriber;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.mongodb.MongoTimeoutException;


public class BlockingSubscriber<T> extends BaseSubscriber<T> {
	private final CountDownLatch latch; 			// count down that is needed for the blocking operation


	/**
	 * subscriber for the reactive stream mongodb operation that has the possibility to
	 * wait for the end of the operation
	 */
	public BlockingSubscriber() {
		super();
		latch = new CountDownLatch(1);
	}

	@Override
	public void onComplete() {
		latch.countDown();
		super.onComplete();
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 							methods to convert it to a blocking operation 							   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * waits until the result is received
	 * @return				list containing the results of the publisher
	 * @throws Exception
	 */
	public List<T> get() throws Exception {
		return await().getResult();
	}
	
	
	/**
	 * 
	 * @param timeout 		maximal time to wait until an exception is thrown
	 * @param unit 			time unit to wait
	 * @return 				list containing the results of the publisher
	 * @throws Exception
	 */
	public List<T> get(final long timeout, final TimeUnit unit) throws Exception {
		return await(timeout, unit).getResult();
	}

	/**
	 * waits until the result is received
	 * @return
	 * @throws Throwable
	 */
	private BlockingSubscriber<T> await() throws Exception {
		return await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	
	/**
	 * waits for the passed amount of time until the result is received
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception
	 */
	private BlockingSubscriber<T> await(final long timeout, final TimeUnit unit) throws Exception {
		if (!latch.await(timeout, unit)) {
			throw new MongoTimeoutException("Publisher onComplete timed out");
		}
		if (error != null) {
			throw error;
		}
		return this;
	}
}
