package ch.wenkst.sw_utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.AfterAll;

class Lifecycle {
	 
	@BeforeAll
	static void initializeExternalResources() {
		System.out.println("Initializing external resources...");
	}
 
	@BeforeEach
	void initializeMockObjects() {
		System.out.println("Initializing mock objects...");
	}
 
	@Test
	void someTest() {
		System.out.println("Running some test...");
		assertTrue(true);
	}
 
	@Test
	void otherTest() {
		assumeTrue(true);
 
		System.out.println("Running another test...");
		assertNotEquals(1, 42, "Why would these be the same?");
	}
 
	@Test
	@Disabled
	void disabledTest() {
		System.exit(1);
	}
 
	@AfterEach
	void tearDown() {
		System.out.println("Tearing down...");
	}
 
	@AfterAll
	static void freeExternalResources() {
		System.out.println("Freeing external resources...");
	}
 
}
