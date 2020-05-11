package ch.wenkst.sw_utils.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.file.FileUtils;

public class SecurityUtils {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	public static final String BC = "BC";
	public static final String BCJSSE = "BCJSSE";
	public static final String PKCS12 = "PKCS12";
	public static final String X509 = "X.509";
	public static final String RSA = "RSA";
	public static final String EC = "EC";
	
	
	public enum KeyType {
		RSA, 		// rsa keys
		EC 			// elliptic keys
	}
	
	/**
	 * file formats of the private key
	 */
	public enum FileFormat {
		PEM,
		DER
	}
	
	/**
	 * cryptographic standards how the key is stored in the file
	 */
	public enum KeyFormat {
		PKCS1, 		// legacy format from openssl for rsa private keys
		PKCS8, 		// new standard that should be used whenever possible, only standard supported by java
		SEC1, 		// legacy format from openssl for ec private keys
	}
	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 								methods to register bouncy castle providers 								 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * registers the bouncy castle provider as security provider, if it was not registered before
	 */
	public static void registerBC() {	
		// Register the bouncy castle security provider, if not registered yet
		if (Security.getProvider(BC) == null) {
			Security.addProvider(new BouncyCastleProvider());
			setSourceOfRandom();
			logger.info("Successfully registered Bouncy Castle as crypto provider.");
		}
	}


	/**
	 * unregisters the bouncy castle provider as security provider
	 */
	public static void unregisterBC() {
		// unregister the bouncy castle provider if registered before
		if (Security.getProvider(BC) != null) {
			Security.removeProvider(BC);
			logger.info("Successfully unregistered Bouncy Castle as crypto provider.");
		}
	}


	/**
	 * registers the bouncy castle provider for tls handshakes (BCJSSE) at position 2 and the bouncy castle provider for
	 * api crypto (BC) at position 3, if they were not registered before.
	 * The Sun security provider needs to be in front of the BCJSSE provider since it is internally used by BCJSSE.
	 * Note: The BCJSSE provider needs the JCE unlimited strength file installed
	 */
	public static void registerBCJSSE() {
		// insert the bouncy castle JSSE provider at position 2 if not already registered
		if (Security.getProvider(BCJSSE) == null) {
			try {
				// with this approach it is not necessary to have the bcjsse dependency in the class path
				Object bc = Class.forName("org.bouncycastle.jsse.provider.BouncyCastleJsseProvider").newInstance();
				Security.insertProviderAt((Provider) bc, 2);
				logger.info("successfully registered bouncy castle bcjsse as security provider at position 2.");
				
			} catch (Exception e) {
				logger.error("failed to register bouncy castle bcjsse as security provider: ", e);
			}
		}

		// insert the bouncy castle provider at position 3 if not already registered
		if (Security.getProvider(BC) == null) {
			Security.insertProviderAt(new BouncyCastleProvider(), 3);
			logger.info("Successfully registered Bouncy Castle BC as security provider at position 3.");
		}
		
		setSourceOfRandom();
	}


	/**
	 * unregisters both bouncy castle providers BCJSSE and BC
	 */
	public static void unregisterBCJSSE() {
		// unregister the bouncy castle BCJSSE provider if registered before
		if (Security.getProvider(BCJSSE) != null) {
			Security.removeProvider(BCJSSE);
			logger.info("Successfully unregistered Bouncy Castle BCJSSE as crypto provider.");
		}

		// unregister the bouncy castle BC provider if registered before
		if(Security.getProvider(BC) != null) {
			Security.removeProvider(BC);
			logger.info("Successfully unregistered Bouncy Castle BC as crypto provider.");
		}
	}








	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									methods to handle certificates		 									 //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * loads the base64 encoded key or certificate form the passed pem-file
	 * @param path 	path to the pem-file
	 * @return 		base64 encoded der content of the pem file
	 */
	private static List<String> loadPem(String path) {
		ArrayList<String> result = new ArrayList<>();
		try {
			InputStream inputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
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
					result.add(pemObj);
					strBuilder.setLength(0);
					readContent = false;
					continue;
				}

				// append the line to the cert
				if (readContent) {
					strBuilder.append(line.trim());
				}
			}

			// close the resources
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			return result;

		} catch (Exception e) {
			logger.error("error reading the private key from " + path, e);
			return null;
		}
	}	
	
	
	/**
	 * loads a list of certificates form pem or der files. if a certificate could not be loaded
	 * it will not be added to the list
	 * @param paths 	list of absolute file paths to the certificates
	 * @return 			list of certificates
	 */
	public static List<Certificate> certsFromFiles(List<String> paths) {
		ArrayList<Certificate> result = new ArrayList<>();
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
	 */
	public static Certificate certFromFile(String path) {
		try {
			File certFile = new File(path);
			FileInputStream certInput = new FileInputStream(certFile);

			CertificateFactory certFactory;
			if (Security.getProvider(BC) == null) {
				certFactory = CertificateFactory.getInstance(X509, new BouncyCastleProvider());
			} else {
				certFactory = CertificateFactory.getInstance(X509, BC);
			}
			
			return certFactory.generateCertificate(certInput);

		} catch (Exception e) {
			logger.error("error loading the certificate: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads a security certificate from the byte array in the DER format
	 * @param certBytes 	byte array containing the certificate information	
	 * @return 				the Java object that contains the security certificate
	 */
	public static Certificate certFromDer(byte[] certBytes) {
		try {
			CertificateFactory certFactory;
			if (Security.getProvider(BC) == null) {
				certFactory = CertificateFactory.getInstance(X509, new BouncyCastleProvider());
			} else {
				certFactory = CertificateFactory.getInstance(X509, BC);
			}
			ByteArrayInputStream is = new ByteArrayInputStream(certBytes); 
			return certFactory.generateCertificate(is);

		} catch (Exception e) {
			logger.error("error creating a certificate from the passed bytes: ", e);
			return null;
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
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && !pemObjs.isEmpty()) {
				String b64CertStr =  pemObjs.get(0);
				return Conversion.base64StrToByteArray(b64CertStr);

			} else {
				logger.error("no pem objects found in the passed pem file");
				return null;
			}
			
		} else {
			logger.error("passed file format " + fileFormat + " is not implemented");
			return null;
		}
	}
	


	/**
	 * verifies the server certificates against the certificates in the truststore, the signature is tested and
	 * it is checked if one of the certificates are outdated. Note it is better to use the method checkServerTrusted
	 * from the X509TrustManager 
	 * @param chain			certificate chain from the server
	 * @param trustedCerts 	the trusted certificates in the truststore
	 * @return 				true if the certificates from the server are valid, false if they are invalid
	 */
	public static boolean verifyChain(X509Certificate[] chain, X509Certificate[] trustedCerts) {

		// flag to set for each certificate in the chain if it was verified from at least one certificate in the chain
		boolean isCertTrusted = false; 		

		for (X509Certificate cert : chain) {
			// check if it is outdated
			if (System.currentTimeMillis() > cert.getNotAfter().getTime()) {
				logger.error("server-certificate is outdated");
				return false;
			};

			for (X509Certificate trustedCert : trustedCerts) {
				// check if the certificate in the trust store is ourdated
				if (System.currentTimeMillis() > cert.getNotAfter().getTime()) {
					logger.error("certificate in trust store is outdated");
					return false;
				};

				// Verifying by public key
				try {
					cert.verify(trustedCert.getPublicKey());

					// if no error is thrown the certificate is valid
					isCertTrusted = true; 
					break;

				} catch (Exception e) {

				} 
			}

			// check if the cert is trusted
			if (!isCertTrusted) {
				return false;
			}
		}


		// all certificates are valid
		return true;
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
	 */
	public static PrivateKey keyFromFile(String path, KeyType keyType, FileFormat fileFormat, KeyFormat keyFormat) {
		try {
			// load the pkcs8 encoded key bytes
			byte[] pkcs8KeyBytes = derFromKeyFile(path, keyType, fileFormat, keyFormat);	
		
			// create the private key from the der bytes
			return keyFromDer(pkcs8KeyBytes, keyType);
			
		} catch (Exception e) {
			logger.error("failed to parse the key file form path " + path, e);
			return null;
		}
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
			if (Security.getProvider(BC) == null) {
				keyFactory = KeyFactory.getInstance(RSA, new BouncyCastleProvider());
			} else {
				keyFactory = KeyFactory.getInstance(RSA, BC);
			}
		}

		// key factory for ec curves
		else if (keyType.equals(KeyType.EC)) {
			if (Security.getProvider(BC) == null) {
				keyFactory = KeyFactory.getInstance(EC, new BouncyCastleProvider());
			} else {
				keyFactory = KeyFactory.getInstance(EC, BC);
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
		if (Security.getProvider(BC) == null) {
			keyFactory = KeyFactory.getInstance(RSA, new BouncyCastleProvider());
		} else {
			keyFactory = KeyFactory.getInstance(RSA, BC);
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
		if (Security.getProvider(BC) == null) {
			keyFactory = KeyFactory.getInstance(EC, new BouncyCastleProvider());
		} else {
			keyFactory = KeyFactory.getInstance(EC, BC);
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
		if (Security.getProvider(BC) == null) {
			keyStore = KeyStore.getInstance(PKCS12, new BouncyCastleProvider());
		} else {
			keyStore = KeyStore.getInstance(PKCS12, BC);
		}

		File keyFile = new File(path);
		FileInputStream fis = new FileInputStream(keyFile);
		keyStore.load(fis, password.toCharArray());
		
		return keyStore;
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 								methods to print security specific informations 							  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns a string with the default providers that handle the security
	 * @return 		a string containing all the default providers
	 */
	public static String getDefaultProviders() {
		StringBuilder builder = new StringBuilder("\n");

		try {
			// provider of the SSLContext
			String name = SSLContext.getInstance("TLSv1.2").getProvider().getName();
			builder.append("TLSv1.2 SSLContext Provider: ").append(name).append("\n");

			// provider of the CerificateFactory
			name = CertificateFactory.getInstance("X.509").getProvider().getName();
			builder.append("X.509 CertificateFactory Provider: ").append(name).append("\n");

			// provider of the default KeyStore
			name = KeyStore.getInstance(KeyStore.getDefaultType()).getProvider().getName();
			builder.append("Default KeyStore Provider: ").append(name).append("\n");

			// provider of the PKCS12 KeyStore
			name = KeyStore.getInstance("PKCS12").getProvider().getName();
			builder.append("PKCS12 KeyStore Provider: ").append(name).append("\n");

			// provider of the KeyManagerFactory
			name = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).getProvider().getName();
			builder.append("Default KeyManagerFactory Provider: ").append(name).append("\n");

			// provider of the TrustManagerFactory
			name = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).getProvider().getName();
			builder.append("Default TrustManagerFactory Provider: ").append(name).append("\n");


			// default keyStore type
			name = KeyStore.getDefaultType();
			builder.append("KeyStore.getDefaultType(): ").append(name).append("\n");


			// default KeyManagerFactory algorithm
			name = KeyManagerFactory.getDefaultAlgorithm();
			builder.append("KeyStore.getDefaultType(): ").append(name).append("\n");


			// default TrustManagerFactory provider
			name = TrustManagerFactory.getDefaultAlgorithm();
			builder.append("KeyStore.getDefaultType(): ").append(name).append("\n");

			return builder.toString();


		} catch (Exception e) {
			logger.error("error getting the default security types: ", e);
			return "";
		}
	}


	/**
	 * returns a String with the registered providers in the correct order they are registered
	 * @return 		string with the registered security providers 
	 */
	public static String getRegisteredProviders() {
		StringBuilder builder = new StringBuilder("[");

		for (Provider provider : Security.getProviders()) {
			builder.append(provider.getName()).append(", ");
		}

		builder.deleteCharAt(builder.length()-1);
		builder.deleteCharAt(builder.length()-1);
		builder.append("]");

		return builder.toString();
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											methods to handle passwords 							  		  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//										utility methods 												     //
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * changes the source of random to /dev/urandom if the operating system is linux
	 * this source has a lower chance of blocking
	 */
	public static void setSourceOfRandom() {
		if (Utils.isOSLinux()) {
			logger.info("operating system is linux, change the source of random to /dev/urandom");
			System.setProperty("java.security.egd", "file:/dev/./urandom");
		}
	}
	
	
	public static class KeyInfo {
		private byte[] pkcs8KeyBytes;
		private FileFormat fileFormat;
		private KeyType keyType;
		private KeyFormat keyFormat;
		
		public void addKeyInfo(FileFormat fileFormat, KeyType keyType, KeyFormat keyFormat) {
			this.fileFormat = fileFormat;
			this.keyType = keyType;
			this.keyFormat = keyFormat;
		}
	}
}
