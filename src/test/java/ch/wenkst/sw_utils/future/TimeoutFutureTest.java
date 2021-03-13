package ch.wenkst.sw_utils.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.Utils;

public class TimeoutFutureTest extends BaseTest {
	private static ThreadPoolExecutor executor = null;

	
	@BeforeAll
	public static void initializeExternalResources() {
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(2);
	}


	@Test()
	public void timeoutOfTimeoutFuture() throws InterruptedException, ExecutionException {
		TimeoutFuture<Boolean> future = new TimeoutFuture<>(100);
		Boolean result = future.get();
		Assertions.assertEquals(null, result);
	}
	
	
	@Test()
	public void resultFromTimeoutFuture() throws InterruptedException, ExecutionException {
		TimeoutFuture<Boolean> future = new TimeoutFuture<>(1000);
		completeFutureAsync(future);
		Boolean result = future.get();
		Assertions.assertEquals(true, result);
	}
	
	
	private void completeFutureAsync(TimeoutFuture<Boolean> future) {
		executor.execute(() -> {
			Utils.sleep(100);
			future.complete(true);
		});
	}

	
	@AfterAll
	public static void tearDownExternalResources() {
		executor.shutdown();
	}
}
