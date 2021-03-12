package ch.wenkst.sw_utils.crypto.certs_and_keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.crypto.SecurityUtils;
import ch.wenkst.sw_utils.file.FileUtils;

public class KeyParser {
	private PemParser pemParser = new PemParser();
	
	
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
	public PrivateKey keyFromFile(String path) throws KeyParsingException {
		List<KeyInfo> keyInfoList = allKeyInfoCombinations();
		
		for (KeyInfo keyInfo : keyInfoList) {
			try {
				derFromKeyFile(path, keyInfo);
				return keyFromDer(keyInfo.pkcs8KeyBytes, keyInfo.keyType);
				
			} catch (Exception e) { }
		}
		
		throw new KeyParsingException("key file from the path " + path + " could not be parsed, no supported file format");
	}
	
	
	private List<KeyInfo> allKeyInfoCombinations() {
		List<KeyInfo> keyInfoList = new ArrayList<>();
		for (FileFormat fileFormat : FileFormat.values()) { 
			for (KeyType keyType : KeyType.values()) {
				for (KeyFormat keyFormat : KeyFormat.values()) {
					KeyInfo keyInfo = new KeyInfo();
					keyInfo.addKeyInfo(fileFormat, keyType, keyFormat);
					keyInfoList.add(keyInfo);
				}
			}
		}
		
		return keyInfoList;
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
	public PrivateKey keyFromFile(String path, KeyType keyType, FileFormat fileFormat, KeyFormat keyFormat) throws KeyParsingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
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
	public byte[] derFromKeyFile(String path, KeyInfo keyInfo) throws KeyParsingException {
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
	public byte[] derFromKeyFile(String path, KeyType keyType, FileFormat fileFormat, KeyFormat keyFormat) throws KeyParsingException {
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
	private byte[] derFromRsaKeyFile(String path, FileFormat fileFormat, KeyFormat keyFormat) throws IOException, KeyParsingException {
		if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.PKCS8)) {
			return pkcs8KeyFromPkcs8PemFile(path);
			
		} else if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.PKCS1)) {
			return pkcs8KeyFromPkcs1PemFile(path);
			
		} else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.PKCS8)) {
			return FileUtils.readByteArrFromFile(path);
		
		} else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.PKCS1)) {
			return ecPkcs8KeyFromPkcs1Der(path);
		
		} else {
			throw new KeyParsingException("failed to read der ec key, unsupported combination of file format: " + fileFormat + " and keyFormat " + keyFormat);
		}
	}
	
	
	private byte[] pkcs8KeyFromPkcs8PemFile(String pemKeyFile) throws KeyParsingException, IOException {
		List<String> pemObjs = pemParser.loadPem(pemKeyFile);
		if (pemObjs != null && !pemObjs.isEmpty()) {
			String b64Key = pemObjs.get(0);
			return Conversion.base64StrToByteArray(b64Key);
		
		} else {
			throw new KeyParsingException("no pem objects found in the passed pem file");
		}
	}
	
	
	private byte[] pkcs8KeyFromPkcs1PemFile(String pemKeyFile) throws IOException {
		String b64Key = FileUtils.readStrFromFile(pemKeyFile);
		Reader reader = new StringReader(b64Key);
		PEMParser pemParser = new PEMParser(reader);
	    Object keyPair = pemParser.readObject();
	    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) keyPair);
	    byte[] pkcs8KeyBytes = pair.getPrivate().getEncoded();
	    pemParser.close();
	    reader.close();
	    return pkcs8KeyBytes;
	}
	
	
	private byte[] ecPkcs8KeyFromPkcs1Der(String derKeyFile) throws IOException {
		byte[] pkcs1KeyBytes = FileUtils.readByteArrFromFile(derKeyFile);    
	    ASN1Sequence aseq = ASN1Sequence.getInstance(pkcs1KeyBytes);
	    org.bouncycastle.asn1.pkcs.RSAPrivateKey rsaPrivateKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(aseq);
	    AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption);
	    return new PrivateKeyInfo(algId, rsaPrivateKey).getEncoded();
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
	private byte[] derFromEcKeyFile(String path, FileFormat fileFormat, KeyFormat keyFormat) throws IOException, KeyParsingException {
		if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.PKCS8)) {
			return pkcs8KeyFromPkcs8PemFile(path);
			
		} else if (fileFormat.equals(FileFormat.PEM) && keyFormat.equals(KeyFormat.SEC1)) {
			return pkcs8KeyFromPkcs1PemFile(path);
			
		} else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.PKCS8)) {
			return FileUtils.readByteArrFromFile(path);
			
		} else if (fileFormat.equals(FileFormat.DER) && keyFormat.equals(KeyFormat.SEC1)) {
		    return rsaPkcs8KeyFromPkcs1Der(path);
		    
		} else {
			throw new KeyParsingException("failed to read der ec key, unsupported combination of file format: " + fileFormat + " and keyFormat " + keyFormat);
		}
	}
	
	
	private byte[] rsaPkcs8KeyFromPkcs1Der(String derKeyFile) throws IOException {
	    byte[] sec1KeyBytes = FileUtils.readByteArrFromFile(derKeyFile);
	    ASN1Sequence seq = ASN1Sequence.getInstance(sec1KeyBytes);
	    org.bouncycastle.asn1.sec.ECPrivateKey pKey = org.bouncycastle.asn1.sec.ECPrivateKey.getInstance(seq);
	    AlgorithmIdentifier algId = new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, pKey.getParameters());
	    return new PrivateKeyInfo(algId, pKey).getEncoded();
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
	public PrivateKey keyFromDer(byte[] keyBytes, KeyType keyType) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, KeyParsingException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		if (keyType.equals(KeyType.RSA)) {
			return SecurityUtils.rsaKeyFactoryInstance().generatePrivate(keySpec);
		
		} else if (keyType.equals(KeyType.EC)) {
			return SecurityUtils.ecKeyFactoryInstance().generatePrivate(keySpec);
		
		} else {
			throw new KeyParsingException("unsupported key type " + keyType);
		}		
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
	public PrivateKey keyFromP12(String path, String password, String alias) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, IOException {		
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
	public PrivateKey keyFromP12(String path, String password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {		
		KeyStore keyStore = SecurityUtils.keyStoreFromP12(path, password);
		Enumeration<String> aliases = keyStore.aliases();
		String alias = aliases.nextElement();
		return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
	}
	
	
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
	public KeyStore keyStoreFromP12(String path, String password) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore keyStore = SecurityUtils.keyStoreInstance();

		File keyFile = new File(path);
		FileInputStream fis = new FileInputStream(keyFile);
		keyStore.load(fis, password.toCharArray());
		
		return keyStore;
	}
}
