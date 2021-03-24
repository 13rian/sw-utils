package ch.wenkst.sw_utils.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import ch.wenkst.sw_utils.conversion.Conversion;

public class PasswordHashUtils {
	private PasswordHashUtils() {

	}

	/** 
	 * generates a password hash using the PBKDF2WithHmacSHA1 algorithm.
	 * @param password 		the string password to hash
	 * @return 				hash:salt:iterationCount, the hash and the salt is in base64 or null if an error occured
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	public static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = 1000;
		char[] chars = password.toCharArray();

		// create the salt
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);

		// generate the hash
		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = skf.generateSecret(spec).getEncoded();

		// concatenate the hash the salt and the number of iterations
		return Conversion.byteArrayToBase64(hash) + ":" + Conversion.byteArrayToBase64(salt) + ":" + iterations;
	}


	/** 
	 * checks if the passed password has the same hash than the passed one 
	 * @param password 			the password that is validated
	 * @param pwHash 			hash:salt:iterationCount, the hash and the salt is in base64
	 * @return 					true, if the passed password matches the passed hash
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	public static boolean validatePassword(String password, String pwHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String[] parts = pwHash.split(":");
		byte[] hash = Conversion.base64StrToByteArray(parts[0]);
		byte[] salt = Conversion.base64StrToByteArray(parts[1]);
		int iterations = Integer.parseInt(parts[2]);


		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] testHash = skf.generateSecret(spec).getEncoded();

		int diff = hash.length ^ testHash.length;
		for (int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}
}
