package ch.wenkst.sw_utils.crypto;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;

public class PasswordHashUtils {
	private static final Logger logger = LoggerFactory.getLogger(PasswordHashUtils.class);
	
	
	private PasswordHashUtils() {
		
	}
	
	/** 
	 * generates a password hash using the PBKDF2WithHmacSHA1 algorithm.
	 * @param password 		the string password to hash
	 * @return 				hash:salt:iterationCount, the hash and the salt is in base64 or null if an error occured
	 */
	public static String hashPassword(String password) {
		try {
			int iterations = 1000; 					// the number how often the password is hashed
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
		
		} catch (Exception e) {
			logger.error("error hashing the password");
			return null;
		}
	}


	/** 
	 * checks if the passed password has the same hash than the passed one 
	 * @param password 			the password that is validated
	 * @param pwHash 			hash:salt:iterationCount, the hash and the salt is in base64
	 * @return 					true, if the passed password matches the passed hash
	 */
	public static boolean validatePassword(String password, String pwHash) {
		try {
			// get the specifications of the hash
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

		} catch (Exception e) {
			logger.error("error validating the hash of the password: " + e.getMessage(), e);
			return false;
		}
	}
}
