package ch.wenkst.sw_utils.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;

public class PasswordHashUtilsTest extends BaseTest {
	
	@Test
	public void correctPasswordHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String password =  "securePW";
		String pwHash = PasswordHashUtils.hashPassword(password);
		Assertions.assertTrue(PasswordHashUtils.validatePassword(password, pwHash));
	}
	
	
	@Test
	public void wrongPasswordHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String password =  "securePW";
		String wrongHash = PasswordHashUtils.hashPassword("wrongPassword");
		boolean passwordValid = PasswordHashUtils.validatePassword(password, wrongHash);
		Assertions.assertFalse(passwordValid);
	}
}
