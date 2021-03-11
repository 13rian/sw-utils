package ch.wenkst.sw_utils.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.file.FileUtils;

public class SecurityUtils {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	public enum KeyType {
		RSA,
		EC
	}
	

	public enum FileFormat {
		PEM,
		DER
	}
	

	public enum KeyFormat {
		PKCS1, 		// legacy format from openssl for rsa private keys
		PKCS8, 		// new standard that should be used whenever possible, only standard supported by java
		SEC1, 		// legacy format from openssl for ec private keys
	}
	


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
		List<Certificate> result = new ArrayList<>();
		for (String path: paths) {
			Certificate cert = certFromFile(path);
			if (cert != null) {
				result.add(cert);
			}
		}
		
		return result;
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
		File certFile = new File(path);
		FileInputStream certInput = new FileInputStream(certFile);
		CertificateFactory certFactory = certificateFactoryInstance();
		return certFactory.generateCertificate(certInput);
	}
	
	
	/**
	 * loads a security certificate from the byte array in the DER format
	 * @param certBytes 	byte array containing the certificate information	
	 * @return 				the Java object that contains the security certificate
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 */
	public static Certificate certFromDer(byte[] certBytes) throws CertificateException, NoSuchProviderException {
		CertificateFactory certFactory = certificateFactoryInstance();
		ByteArrayInputStream is = new ByteArrayInputStream(certBytes); 
		return certFactory.generateCertificate(is);
	}
	
	
	private static CertificateFactory certificateFactoryInstance() throws CertificateException, NoSuchProviderException {
		if (CryptoProvider.bcProviderRegistered()) {
			return CertificateFactory.getInstance(SecurityConstants.X509, SecurityConstants.BC);
		} else {
			return CertificateFactory.getInstance(SecurityConstants.X509, new BouncyCastleProvider());
		}
	}
	
	
	/**
	 * loads a byte array containing the binary der data representing the cert of the passed cert file
	 * @param path 		path to the certificate file
	 * @param format 	format of the file
	 * @return 			certificate der byte array
	 * @throws IOException 
	 */
	public static byte[] derFromCertFile(String path, FileFormat fileFormat) throws IOException {		
		if (fileFormat.equals(FileFormat.DER)) {
			return FileUtils.readByteArrFromFile(path);
			
		} else if (fileFormat.equals(FileFormat.PEM)) {
			return derFromPem(path);
			
		} else {
			logger.error("passed file format " + fileFormat + " is not implemented");
			return null;
		}
	}
	
	
	private static byte[] derFromPem(String path) throws IOException {
		List<String> pemObjs = loadPem(path);
		if (pemObjs != null && !pemObjs.isEmpty()) {
			String b64CertStr = pemObjs.get(0);
			return Conversion.base64StrToByteArray(b64CertStr);

		} else {
			logger.error("no pem objects found in the passed pem file");
			return null;
		}
	}
	
	
	/**
	 * loads the base64 encoded key or certificate form the passed pem-file
	 * @param path 	path to the pem-file
	 * @return 		base64 encoded der content of the pem file
	 * @throws IOException 
	 */
	private static List<String> loadPem(String path) throws IOException {
		InputStream inputStream = new FileInputStream(path);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		List<String> pemList = readPemFromBufferedReader(bufferedReader);

		// close the resources
		bufferedReader.close();
		inputStreamReader.close();
		inputStream.close();
		return pemList;
	}
	
	
	private static List<String> readPemFromBufferedReader(BufferedReader bufferedReader) throws IOException {
		List<String> pemList = new ArrayList<>();
		StringBuilder strBuilder = new StringBuilder();
		boolean readContent = false;
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			// check for the first line
			if (line.contains("BEGIN")) {
				readContent = true;
				continue;
			} 

			// check for the last line
			if (line.contains("END")) {
				String pemObj = strBuilder.toString();
				pemList.add(pemObj);
				strBuilder.setLength(0);
				readContent = false;
				continue;
			}

			// append the line
			if (readContent) {
				strBuilder.append(line.trim());
			}
		}
		
		return pemList;
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
	 */
	public static PrivateKey keyFromFile(String path) {
		try {
			// try to load pkcs8 encoded ec keys
			KeyInfo keyInfo = new KeyInfo();
			try {
				keyInfo.addKeyInfo(FileFormat.PEM, KeyType.EC, KeyFormat.SEC1);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			try {
				keyInfo.addKeyInfo(FileFormat.PEM, KeyType.EC, KeyFormat.PKCS8);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			try {
				keyInfo.addKeyInfo(FileFormat.DER, KeyType.EC, KeyFormat.SEC1);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			try {
				keyInfo.addKeyInfo(FileFormat.DER, KeyType.EC, KeyFormat.PKCS8);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			
			// try to load pkcs8 encoded rsa keys
			try {
				keyInfo.addKeyInfo(FileFormat.PEM, KeyType.RSA, KeyFormat.PKCS1);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			try {
				keyInfo.addKeyInfo(FileFormat.PEM, KeyType.RSA, KeyFormat.PKCS8);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			try {
				keyInfo.addKeyInfo(FileFormat.DER, KeyType.RSA, KeyFormat.PKCS1);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			try {
				keyInfo.addKeyInfo(FileFormat.DER, KeyType.RSA, KeyFormat.PKCS8);
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
			} catch (Exception e) { }
			
			throw new KeyParsingException("key file from the path " + path + " could not be parsed, no supported file format");

		} catch (Exception e) {
			logger.error("failed to parse the key file form path " + path, e);
			return null;
		}
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
		byte[] pkcs8KeyBytes = derFromKeyFile(path, keyType, fileFormat, keyFormat);	
		return keyFromDer(pkcs8KeyBytes, keyType);
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
		byte[] pkcs8KeyBytes = derFromKeyFile(path, keyInfo.keyType, keyInfo.fileFormat, keyInfo.keyFormat);
		keyInfo.pkcs8KeyBytes = pkcs8KeyBytes;
		return pkcs8KeyBytes;
	}
	
	
	/**
	 * loads a byte array containing the binary der data representing the key of the passed key file
	 * @param path 			the path to the key file
	 * @param keyType 		type of the key
	 * @param fileFormat 	format of the file
	 * @param keyFormat 	format of the key
	 * @return 				byte array containing the der key in pkcs8 format
	 * @throws KeyParsingException 
	 */
	public static byte[] derFromKeyFile(String path, KeyType keyType, FileFormat fileFormat, KeyFormat keyFormat) throws KeyParsingException {
		try {
			if (keyType.equals(KeyType.RSA)) {
				return derFromRsaKeyFile(path, fileFormat, keyFormat);
			
			} else if (keyType.equals(KeyType.EC)) {
				return derFromEcKeyFile(path, fileFormat, keyFormat);
			
			} else {
				throw new KeyParsingException("unsupported key type: " + keyType);
			}

		} catch (Exception e) {
			throw new KeyParsingException("failed to read the key-file", e.getCause());
		}
	}
	
	
	/**
	 * reads the pkcs8 encoded key bytes from the passed rsa key file
	 * @param path 			path of the key file
	 * @param fileFormat 	format of the file
	 * @param keyFormat 	format of the key
	 * @return 				pkcs8 encoded key bytes of the passed key file
	 * @throws IOException
	 * @throws KeyParsingException 
	 */
	private static byte[] derFromRsaKeyFile(String path, FileFormat fileFormat, KeyFormat keyFormat) throws IOException, KeyParsingException {
		byte[] pkcs8KeyBytes = null;
		
		// pem and pkcs8
		if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.PKCS8)) {
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && !pemObjs.isEmpty()) {
				String b64Key = pemObjs.get(0);
				pkcs8KeyBytes = Conversion.base64StrToByteArray(b64Key);
			
			} else {
				throw new KeyParsingException("no pem objects found in the passed pem file");
			}
		} 
		
		// pem and sec1
		else if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.PKCS1)) {
			String b64Key = FileUtils.readStrFromFile(path);
			Reader reader = new StringReader(b64Key);
			PEMParser pemParser = new PEMParser(reader);
		    Object keyPair = pemParser.readObject();
		    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) keyPair);
		    pkcs8KeyBytes = pair.getPrivate().getEncoded();
		    pemParser.close();
		    reader.close();
		} 
		
		// der and pkcs8 
		else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.PKCS8)) {
			pkcs8KeyBytes = FileUtils.readByteArrFromFile(path);
		} 
		
		// der and sec1
		else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.PKCS1)) {
			byte[] pkcs1KeyBytes = FileUtils.readByteArrFromFile(path);    
		    ASN1Sequence aseq = ASN1Sequence.getInstance(pkcs1KeyBytes);
		    org.bouncycastle.asn1.pkcs.RSAPrivateKey rsaPrivateKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(aseq);
		    AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption);
		    pkcs8KeyBytes = new PrivateKeyInfo(algId, rsaPrivateKey).getEncoded();
		} 
		
		// combination cannot occur
		else {
			throw new KeyParsingException("failed to read der ec key, unsupported combination of file format: " + fileFormat + " and keyFormat " + keyFormat);
		}
		
		return pkcs8KeyBytes;
	}
	
	
	/**
	 * reads the pkcs8 encoded key bytes from the passed ec key file
	 * @param path 			path of the key file
	 * @param fileFormat 	format of the file
	 * @param keyFormat 	format of the key
	 * @return 				pkcs8 encoded key bytes of the passed key file
	 * @throws IOException
	 * @throws KeyParsingException 
	 */
	private static byte[] derFromEcKeyFile(String path, FileFormat fileFormat, KeyFormat keyFormat) throws IOException, KeyParsingException {
		byte[] pkcs8KeyBytes = null;
		
		// pem and pkcs8
		if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.PKCS8)) {
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && !pemObjs.isEmpty()) {
				String b64Key = pemObjs.get(0);
				pkcs8KeyBytes = Conversion.base64StrToByteArray(b64Key);
			
			} else {
				throw new KeyParsingException("no pem objects found in the passed pem file");
			}
		} 
		
		// pem and sec1
		else if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.SEC1)) {
			String b64Key = FileUtils.readStrFromFile(path);
			Reader reader = new StringReader(b64Key);
			PEMParser pemParser = new PEMParser(reader);
		    Object keyPair = pemParser.readObject();
		    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) keyPair);
		    pkcs8KeyBytes = pair.getPrivate().getEncoded();
		    pemParser.close();
		    reader.close();
		} 
		
		// der and pkcs8 
		else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.PKCS8)) {
			pkcs8KeyBytes = FileUtils.readByteArrFromFile(path);
		} 
		
		// der and sec1
		else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.SEC1)) {
		    byte[] sec1KeyBytes = FileUtils.readByteArrFromFile(path);
		    ASN1Sequence seq = ASN1Sequence.getInstance(sec1KeyBytes);
		    org.bouncycastle.asn1.sec.ECPrivateKey pKey = org.bouncycastle.asn1.sec.ECPrivateKey.getInstance(seq);
		    AlgorithmIdentifier algId = new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, pKey.getParameters());
		    pkcs8KeyBytes = new PrivateKeyInfo(algId, pKey).getEncoded();
		} 
		
		// combination cannot occur
		else {
			throw new KeyParsingException("failed to read der ec key, unsupported combination of file format: " + fileFormat + " and keyFormat " + keyFormat);
		}
		
		return pkcs8KeyBytes;
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
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

		// key factory for rsa curves
		KeyFactory keyFactory = null;
		if (keyType.equals(KeyType.RSA)) {
			if (Security.getProvider(SecurityConstants.BC) == null) {
				keyFactory = KeyFactory.getInstance(SecurityConstants.RSA, new BouncyCastleProvider());
			} else {
				keyFactory = KeyFactory.getInstance(SecurityConstants.RSA, SecurityConstants.BC);
			}
		}

		// key factory for ec curves
		else if (keyType.equals(KeyType.EC)) {
			if (CryptoProvider.bcProviderRegistered()) {
				keyFactory = KeyFactory.getInstance(SecurityConstants.EC, SecurityConstants.BC);
			} else {
				keyFactory = KeyFactory.getInstance(SecurityConstants.EC, new BouncyCastleProvider());
			}
		}

		else {
			throw new KeyParsingException("unsupported key type " + keyType);
		}

		return keyFactory.generatePrivate(keySpec);			
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
		KeyStore keyStore = keyStoreFromP12(path, password);
		return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
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
		KeyStore keyStore = keyStoreFromP12(path, password);

		Enumeration<String> aliases = keyStore.aliases();
		String alias = aliases.nextElement();
		return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
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
	 */
	public static PublicKey pubKeyFromPrivKey(PrivateKey privateKey, KeyType keyType) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		// rsa keys
		if (keyType.equals(KeyType.RSA)) {
			return pubRsaKeyFromPrivKey(privateKey);
		} 
		
		// ec keys
		else if (keyType.equals(KeyType.EC)) {
			return pubEcKeyFromPrivKey(privateKey);
		}
		
		else {
			logger.error("unsupported key type " + keyType);
			return null;
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
		// get the key factory
		KeyFactory keyFactory = null;
		if (CryptoProvider.bcProviderRegistered()) {
			keyFactory = KeyFactory.getInstance(SecurityConstants.RSA, SecurityConstants.BC);
		} else {
			keyFactory = KeyFactory.getInstance(SecurityConstants.RSA, new BouncyCastleProvider());
		}

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
		// get the key factory
		KeyFactory keyFactory = null;
		if (CryptoProvider.bcProviderRegistered()) {
			keyFactory = KeyFactory.getInstance(SecurityConstants.EC, SecurityConstants.BC);
		} else {
			keyFactory = KeyFactory.getInstance(SecurityConstants.EC, new BouncyCastleProvider());
		}	

		ECPrivateKey pkEcKey = (ECPrivateKey) ecPrivateKey;
		BigInteger d = pkEcKey.getD();
		org.bouncycastle.jce.spec.ECParameterSpec ecSpec = pkEcKey.getParameters();
		org.bouncycastle.math.ec.ECPoint Q = pkEcKey.getParameters().getG().multiply(d);
		org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(Q, ecSpec);
		return keyFactory.generatePublic(pubSpec);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									methods to handle key/trust stores 							  			  //
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
		KeyStore keyStore;
		if (CryptoProvider.bcProviderRegistered()) {
			keyStore = KeyStore.getInstance(SecurityConstants.PKCS12, SecurityConstants.BC);
		} else {
			keyStore = KeyStore.getInstance(SecurityConstants.PKCS12, new BouncyCastleProvider());
		}

		File keyFile = new File(path);
		FileInputStream fis = new FileInputStream(keyFile);
		keyStore.load(fis, password.toCharArray());
		
		return keyStore;
	}
}
