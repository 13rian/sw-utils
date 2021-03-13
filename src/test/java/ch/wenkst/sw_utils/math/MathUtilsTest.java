package ch.wenkst.sw_utils.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;

public class MathUtilsTest extends BaseTest {
	
	@Test
	public void decimalPlacesOfFloat() {
		Assertions.assertEquals(78663, MathUtils.getDecimalPlaces(6.78663f));
		Assertions.assertEquals(8894, MathUtils.getDecimalPlaces(113.8894f));
	}
	
	
	@Test
	public void decimalPlacesOfDouble() {
		Assertions.assertEquals(78663, MathUtils.getDecimalPlaces(6.78663d));
		Assertions.assertEquals(8894, MathUtils.getDecimalPlaces(113.8894d));
	}
}
