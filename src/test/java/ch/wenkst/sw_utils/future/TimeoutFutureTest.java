package ch.wenkst.sw_utils.future;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.Utils;

public class TimeoutFutureTest {
	private static ThreadPoolExecutor executor = null;

	
	@BeforeAll
	public static void initializeExternalResources() {
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(2);
	}



	/**
	 * let the timeout future run into a timeout
	 */
	@Test()
	@DisplayName("future that times out")
	public void timedOutFutureTest() {
		TimeoutFuture<Boolean> future = new TimeoutFuture<>(500);
		
		// wait for the future to time out
		Assertions.assertDoesNotThrow(() -> {
			Boolean result = future.get();
			Assertions.assertEquals(null, result, "result of future that timed out");
		});
	}
	
	
	
	/**
	 * complete a timeout future before the timeout is reached
	 */
	@Test()
	@DisplayName("result of timeout future")
	public void futureResultTest() {
		TimeoutFuture<Boolean> future = new TimeoutFuture<>(1000);
		
		// asynchronously complete the future after some timeout
		executor.execute(() -> {
			Utils.sleep(200);
			future.complete(true);
		});
		
		// wait for the result of the future
		Assertions.assertDoesNotThrow(() -> {
			Boolean result = future.get();
			Assertions.assertEquals(true, result, "result of future");
		});
	}

	
	@AfterAll
	public static void tearDownExternalResources() {
		executor.shutdown();
	}

}
