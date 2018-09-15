package ch.wenkst.sw_utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UtilsTest {
	private static ThreadPoolExecutor executor = null;
	
	@BeforeAll
	public static void initializeExternalResources() {
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(2);
	}
	
	
	/**
	 * get all the results of a combined future
	 */
	@Test()
	@DisplayName("combined future")
	public void combinedFutureTest() {
		// setup future 1
		CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
			}
			return "result1";
		}, executor);
		
		// setup future 2
		CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
			}
			return "result2";
		}, executor);

		// create a combined future
		CompletableFuture<?>[] futures = {future1, future2};
		CompletableFuture<List<Object>> combinedFuture = Utils.allOfCombletableFuture(futures);
				
		// wait for both futures to complete
		Assertions.assertDoesNotThrow(() -> {
			List<Object> resultList = null;
			resultList = combinedFuture.get(2000, TimeUnit.MILLISECONDS);
			
			Assertions.assertEquals("result1", resultList.get(0), "result of future 1");
			Assertions.assertEquals("result2", resultList.get(1), "result of future 2");
		});
	}
	
	
	@AfterAll
	public static void tearDownExternalResources() {
		executor.shutdown();
	}
}
