package ch.wenkst.sw_utils.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MathUtilsTest {
	/**
	 * extract the decimal part of a number
	 */
	@Test
	@DisplayName("decimal places")
	public void getDecimalPartTest() {
		// extract the decimal places of a float
		Assertions.assertEquals(78663, MathUtils.getDecimalPlaces(6.78663f), "decimal places of 6.78663f are 78663");
		Assertions.assertEquals(8894, MathUtils.getDecimalPlaces(113.8894f), "decimal places of 113.8894f are 0.8894");
		Assertions.assertNotEquals(6.0, MathUtils.getDecimalPlaces(6.78663f), "decimal places of 6.78663f are not 6");
		Assertions.assertNotEquals(0.889 - MathUtils.getDecimalPlaces(113.8894f), "decimal places of 113.8894f are not 889");
		
		// extract the decimal places of a double
		Assertions.assertEquals(78663, MathUtils.getDecimalPlaces(6.78663d), "decimal places of 6.78663d are 78663");
		Assertions.assertEquals(8894, MathUtils.getDecimalPlaces(113.8894d), "decimal places of 113.8894d are 0.8894");
		Assertions.assertNotEquals(6.0, MathUtils.getDecimalPlaces(6.78663d), "decimal places of 6.78663d are not 6");
		Assertions.assertNotEquals(0.889 - MathUtils.getDecimalPlaces(113.8894d), "decimal places of 113.8894d are not 889");		
	}
}
