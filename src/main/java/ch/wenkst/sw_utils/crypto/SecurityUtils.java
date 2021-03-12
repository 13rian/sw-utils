package ch.wenkst.sw_utils.crypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.crypto.certs_and_keys.CertificateParser;
import ch.wenkst.sw_utils.crypto.certs_and_keys.FileFormat;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyFormat;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyInfo;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyParser;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyParsingException;
import ch.wenkst.sw_utils.crypto.certs_and_keys.KeyType;

public class SecurityUtils {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	private static CertificateParser certParser = new CertificateParser();
	private static KeyParser keyParser = new KeyParser();
	


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									methods to handle certificates		 									 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * loads a list of certificates form pem or der files. if a certificate could not be loaded
	 * it will not be added to the list
	 * @param paths 	list of absolute file paths to the certificates
	 * @return 			list of certificates
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 * @throws FileNotFoundException 
	 */
	public static List<Certificate> certsFromFiles(List<String> paths) throws FileNotFoundException, CertificateException, NoSuchProviderException {
		return certParser.certsFromFiles(paths);
	}
	
	
	/**
	 * loads a certificate from a pem or a der file
	 * @param path 	the path to the certificate file
	 * @return 		the certificate
	 * @throws FileNotFoundException 
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 */
	public static Certificate certFromFile(String path) throws FileNotFoundException, CertificateException, NoSuchProviderException {
		return certParser.certFromFile(path);
	}
	
	
	/**
	 * loads a security certificate from the byte array in the DER format
	 * @param certBytes 	byte array containing the certificate information	
	 * @return 				the Java object that contains the security certificate
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 */
	public static Certificate certFromDer(byte[] certBytes) throws CertificateException, NoSuchProviderException {
		return certParser.certFromDer(certBytes);
	}
	
	
	/**
	 * loads a byte array containing the binary der data representing the cert of the passed cert file
	 * @param path 		path to the certificate file
	 * @param format 	format of the file
	 * @return 			certificate der byte array
	 * @throws IOException 
	 */
	public static byte[] derFromCertFile(String path, FileFormat fileFormat) throws IOException {		
		return certParser.derFromCertFile(path, fileFormat);
	}
	



	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											methods to handle keys 									 		 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * loads an unencrypted key from a pem or a der file, all combinations pem/der, sec1/pkcs8, rsa/ec are tried.
	 * this method should be used if the key format is not known or the key file is changed frequently. 
	 * it takes longer than the overloaded method where all key parameters are specified.
	 * note to developer: derFromKeyFile sometimes works with the wrong formats as well, e.g ec, der, pkcs8 works for
	 *  			      ec, der, sec1 as well the generation of the private key therefore needs to be in the catch as well
	 *   				  if all combinations are tried
	 * @param path 			the path to the key file
	 * @return 				the private key
	 * @throws KeyParsingException 
	 */
	public static PrivateKey keyFromFile(String path) throws KeyParsingException {
		return keyParser.keyFromFile(path);
	}
	
	
	/**
	 * loads an unencrypted key from a pem or a der file
	 * @param path 			the path to the key file
	 * @param keyType 		type of the key
	 * @param fileFormat 	format of the file
	 * @param keyFormat 	format of the key
	 * @return 				the private key or null if an error occurred
	 * @throws KeyParsingException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static PrivateKey keyFromFile(String path, KeyType keyType, FileFormat fileFormat, KeyFormat keyFormat) throws KeyParsingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		return keyParser.keyFromFile(path, keyType, fileFormat, keyFormat);
	}
	
	
	
	/**
	 * loads a bytes array containing the binary der data representing the key of the passed key file
	 * the key bytes are written into the keyInfo as well
	 * @param path 		the path of the key file
	 * @param keyInfo 	key information object
	 * @return
	 * @throws KeyParsingException 
	 */
	public static byte[] derFromKeyFile(String path, KeyInfo keyInfo) throws KeyParsingException {
		return keyParser.derFromKeyFile(path, keyInfo);
	}
	
	
	/**
	 * loads a private key from the byte array in the pkcs8 encoded der format
	 * @param keyBytes 		byte array containing the key information	
	 * @param keyType 		the type of the key
	 * @return 				the java object that contains the private key
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeySpecException 
	 * @throws KeyParsingException 
	 */
	public static PrivateKey keyFromDer(byte[] keyBytes, KeyType keyType) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, KeyParsingException {
		return keyParser.keyFromDer(keyBytes, keyType);
	}

	
	/**
	 * loads the private key from a p12-file from the entry with the passed alias
	 * @param path	 	path to the key, only .p12
	 * @param password  export password of the private key
	 * @param alias 	the alias of the key entry
	 * @return	 		the private key
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchProviderException 
	 */
	public static PrivateKey keyFromP12(String path, String password, String alias) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, IOException {		
		return keyParser.keyFromP12(path, password, alias);
	}


	/**
	 * loads the private key from a p12-file from the first entry
	 * @param path	 	path to the key, only .p12
	 * @param password  export password of the private key
	 * @param alias 	the alias of the key entry
	 * @return	 		the private key
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 */
	public static PrivateKey keyFromP12(String path, String password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {		
		return keyParser.keyFromP12(path, password);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 								methods to get a public key from a private key 								 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * generates a corresponding public key from the passed private key
	 * @param privateKey 	private key
	 * @param keyType 		the type of the key
	 * @return 				public key
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyParsingException 
	 */
	public static PublicKey pubKeyFromPrivKey(PrivateKey privateKey, KeyType keyType) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, KeyParsingException {
		if (keyType.equals(KeyType.RSA)) {
			return pubRsaKeyFromPrivKey(privateKey);
		
		} else if (keyType.equals(KeyType.EC)) {
			return pubEcKeyFromPrivKey(privateKey);
		
		} else {
			logger.error("unsupported key type " + keyType);
			throw new KeyParsingException("unsupported key type " + keyType);
		}
	}
	
	
	/**
	 * generates a corresponding rsa public key from the passed rsa private key
	 * @param rsaPrivateKey 	rsa private key
	 * @return 					rsa public key
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeySpecException 
	 */
	public static PublicKey pubRsaKeyFromPrivKey(PrivateKey rsaPrivateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory keyFactory = rsaKeyFactoryInstance();
		RSAPrivateCrtKey privk = (RSAPrivateCrtKey) rsaPrivateKey;
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
		PublicKey rsaPublicKey = keyFactory.generatePublic(publicKeySpec);
		return rsaPublicKey;
	}
	
	
	/**
	 * generates a corresponding ec public key from the passed ec private key
	 * @param ecPrivateKey 		ec private key
	 * @return 					ec public key
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeySpecException 
	 */
	public static PublicKey pubEcKeyFromPrivKey(PrivateKey ecPrivateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory keyFactory = ecKeyFactoryInstance();
		ECPrivateKey pkEcKey = (ECPrivateKey) ecPrivateKey;
		BigInteger d = pkEcKey.getD();
		org.bouncycastle.jce.spec.ECParameterSpec ecSpec = pkEcKey.getParameters();
		org.bouncycastle.math.ec.ECPoint Q = pkEcKey.getParameters().getG().multiply(d);
		org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(Q, ecSpec);
		return keyFactory.generatePublic(pubSpec);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 								helper methods to create crypto instances						  			  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * creates a key store form a p12 file
	 * @param path 			path to the p12-file
	 * @param password 		password for the p12-file
	 * @return 				key store that contains the entries of the p12-file
	 * @throws KeyStoreException 
	 * @throws NoSuchProviderException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static KeyStore keyStoreFromP12(String path, String password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		return keyParser.keyStoreFromP12(path, password);
	}
	
	
	public static CertificateFactory certificateFactoryInstance() throws CertificateException, NoSuchProviderException {
		if (CryptoProvider.bcProviderRegistered()) {
			return CertificateFactory.getInstance(SecurityConstants.X509, SecurityConstants.BC);
		} else {
			return CertificateFactory.getInstance(SecurityConstants.X509, new BouncyCastleProvider());
		}
	}
	
	
	public static KeyStore keyStoreInstance() throws KeyStoreException, NoSuchProviderException {
		if (CryptoProvider.bcProviderRegistered()) {
			return KeyStore.getInstance(SecurityConstants.PKCS12, SecurityConstants.BC);
		} else {
			return KeyStore.getInstance(SecurityConstants.PKCS12);
		}
	}
	
	
	public static KeyFactory ecKeyFactoryInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (Security.getProvider(SecurityConstants.BC) == null) {
			return KeyFactory.getInstance(SecurityConstants.EC, new BouncyCastleProvider());
		} else {
			return KeyFactory.getInstance(SecurityConstants.EC, SecurityConstants.BC);
		}
	}
	
	
	public static KeyFactory rsaKeyFactoryInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (Security.getProvider(SecurityConstants.BC) == null) {
			return KeyFactory.getInstance(SecurityConstants.RSA, new BouncyCastleProvider());
		} else {
			return KeyFactory.getInstance(SecurityConstants.RSA, SecurityConstants.BC);
		}
	}
}
