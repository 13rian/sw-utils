package ch.wenkst.sw_utils.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SecurityUtilsTest {
	
	@Test
	public void correctPasswordHash() {
		String password =  "securePW";
		String pwHash = SecurityUtils.hashPassword(password);
		Assertions.assertTrue(SecurityUtils.validatePassword(password, pwHash));
	}
	
	
	@Test
	public void wrongPasswordHash() {
		String password =  "securePW";
		String wrongHash = SecurityUtils.hashPassword("wrongPassword");
		boolean passwordValid = SecurityUtils.validatePassword(password, wrongHash);
		Assertions.assertFalse(passwordValid);
	}
}
