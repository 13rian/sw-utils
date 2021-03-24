package ch.wenkst.sw_utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UtilsTest {
	private static ThreadPoolExecutor executor = null;
	
	@BeforeAll
	public static void initializeExternalResources() {
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(2);
	}
	
	
	@Test()
	public void waitForTwoFutures() {
		CompletableFuture<String> future1 = getTestFuture("result1", 600);
		CompletableFuture<String> future2 = getTestFuture("result2", 400);
		CompletableFuture<List<Object>> combinedFuture = Utils.allOfCombletableFuture(future1, future2);
				
		// wait for both futures to complete
		Assertions.assertDoesNotThrow(() -> {
			List<Object> resultList = null;
			resultList = combinedFuture.get(2000, TimeUnit.MILLISECONDS);
			
			Assertions.assertEquals("result1", resultList.get(0), "result of future 1");
			Assertions.assertEquals("result2", resultList.get(1), "result of future 2");
		});
	}
	
	
	private CompletableFuture<String> getTestFuture(String result, int processTime) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(processTime);
			} catch (InterruptedException e) {
			}
			return result;
		}, executor);
	}
	
	
	@AfterAll
	public static void tearDownExternalResources() {
		executor.shutdown();
	}
}
