package ch.wenkst.sw_utils.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordHashUtilsTest {
	
	@Test
	public void correctPasswordHash() {
		String password =  "securePW";
		String pwHash = PasswordHashUtils.hashPassword(password);
		Assertions.assertTrue(PasswordHashUtils.validatePassword(password, pwHash));
	}
	
	
	@Test
	public void wrongPasswordHash() {
		String password =  "securePW";
		String wrongHash = PasswordHashUtils.hashPassword("wrongPassword");
		boolean passwordValid = PasswordHashUtils.validatePassword(password, wrongHash);
		Assertions.assertFalse(passwordValid);
	}
}
