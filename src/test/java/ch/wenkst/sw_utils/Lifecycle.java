package ch.wenkst.sw_utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterAll;

/**
 * demonstrates the lifecycle of a unit test
 * in order to execute all the unit tests in the project, rClick on the project, run as, JUnit Test
 */
public class Lifecycle {
	 
	@BeforeAll
	public static void initializeExternalResources() {
		System.out.println("Initializing external resources...");
	}
 
	@BeforeEach
	public void initializeMockObjects() {
		System.out.println("Initializing mock objects...");
	}
 
	@Test
	public void someTest() {
		System.out.println("Running some test...");
		assertTrue(true);
	}
 
	@Test
	public void otherTest() {
		assumeTrue(true);
 
		System.out.println("Running another test...");
		assertNotEquals(1, 42, "Why would these be the same?");
	}
 
	@Test
	@Disabled
	public void disabledTest() {
		System.exit(1);
	}
 
	@AfterEach
	public void tearDown() {
		System.out.println("Tearing down...");
	}
 
	@AfterAll
	public static void freeExternalResources() {
		System.out.println("Freeing external resources...");
	}
 
}
