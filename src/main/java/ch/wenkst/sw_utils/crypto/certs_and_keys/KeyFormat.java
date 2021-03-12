package ch.wenkst.sw_utils.crypto.certs_and_keys;

public enum KeyFormat {
	PKCS1, 		// legacy format from openssl for rsa private keys
	PKCS8, 		// new standard that should be used whenever possible, only standard supported by java
	SEC1, 		// legacy format from openssl for ec private keys
}
