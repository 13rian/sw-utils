package ch.wenkst.sw_utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.crypto.CryptoUtils.FileFormat;
import ch.wenkst.sw_utils.crypto.CryptoUtils.KeyFormat;
import ch.wenkst.sw_utils.crypto.CryptoUtils.KeyType;
import ch.wenkst.sw_utils.file.FileUtils;

public class MainCryptoTest {
	public static void main(String[] args) throws Exception {	
		// define the directory where the private keys are saved
		String keyDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "keys" + File.separator;
		
		
		// register the bouncy castle provider
		CryptoUtils.registerBC();
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 										CERTS 													 //
		///////////////////////////////////////////////////////////////////////////////////////////////////
		String sep = File.separator;
		String certDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep + "certs" + sep;
		
		// cert
		String pemCertPath = certDir + "server.cert.pem";
		String derCertPath = certDir + "server.cert.cer";
		
		// load the object from the file
		X509Certificate cert1 = CryptoUtils.certFromFile(pemCertPath);
		X509Certificate cert2 = CryptoUtils.certFromFile(derCertPath);
		
		// load the b64 encoded der certificate
		byte[] cert1Bytes = CryptoUtils.derFromCertFile(pemCertPath, FileFormat.PEM);
		byte[] cert2Bytes = CryptoUtils.derFromCertFile(derCertPath, FileFormat.DER);
		
		X509Certificate cert11 = CryptoUtils.certFromDer(cert1Bytes);
		X509Certificate cert22 = CryptoUtils.certFromDer(cert2Bytes);
		
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 										KEYS 													 //
		///////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// 										EC 														  //
		////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// 			pem				//
		//////////////////////////////
		// legacy sec1
		String filePath = keyDir + "eckey_sec1.pem";
		PrivateKey pk = CryptoUtils.keyFromFile(filePath, KeyType.EC, FileFormat.PEM, KeyFormat.SEC1);
		System.out.println(pk.getAlgorithm());
		
		
		// pkcs8
		filePath = keyDir + "eckey_pkcs8.pem";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.EC, FileFormat.PEM, KeyFormat.PKCS8);
	    System.out.println (pk.getAlgorithm());
		
		
		// 			der				//
		//////////////////////////////
		// legacy sec1
		filePath = keyDir + "eckey_sec1.der";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.EC, FileFormat.DER, KeyFormat.SEC1);
	    System.out.println (pk.getAlgorithm());
	    
	    
		// pkcs8
		filePath = keyDir + "eckey_pkcs8.der";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.EC, FileFormat.DER, KeyFormat.PKCS8);
	    System.out.println (pk.getAlgorithm());
	    

	    
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// 										RSA 														  //
		////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// 			pem				//
		//////////////////////////////
	    // legacy pkcs1
		filePath = keyDir + "rsakey_pkcs1.pem";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.RSA, FileFormat.PEM, KeyFormat.PKCS1);
	    System.out.println (pk.getAlgorithm());
	    
	    
	    // pkcs8
		filePath = keyDir + "rsakey_pkcs8.pem";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.RSA, FileFormat.PEM, KeyFormat.PKCS8);
	    System.out.println (pk.getAlgorithm());
	    
	    
		// 			der				//
		//////////////////////////////
	    // legacy pkcs1
		filePath = keyDir + "rsakey_pkcs1.der";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.RSA, FileFormat.DER, KeyFormat.PKCS1);
	    System.out.println (pk.getAlgorithm());
	    
	    
	    // pkcs8
		filePath = keyDir + "rsakey_pkcs8.der";
		pk = CryptoUtils.keyFromFile(filePath, KeyType.RSA, FileFormat.DER, KeyFormat.PKCS8);
	    System.out.println (pk.getAlgorithm());
	    
	    
   
//	    // RSA
//	    // pkcs1 pem
//	    filePath = Utils.getWorkDir() + File.separator + "key" + File.separator + "rsakey_pkcs1.pem";
//	    BufferedReader br = new BufferedReader(new FileReader(filePath));
//	    PEMParser pp = new PEMParser(br);
//	    PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
//	    KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
//	    pp.close();
//	    System.out.println(kp.getPrivate().getAlgorithm());
//	    
//	    
//	    
//	    // RSA
//	    // pkcs1 der
//	    filePath = Utils.getWorkDir() + File.separator + "key" + File.separator + "rsakey_pkcs1.der";
//	    b64Key = CryptoUtils.derFromKeyFile(filePath, CryptoUtils.FILE_FORMAT_DER);
//	    keyBytes = Conversion.base64StrToByteArray(b64Key);
//	    
//	    
//	    ASN1Sequence aseq = ASN1Sequence.getInstance(keyBytes);
//	    RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(aseq);
//	   	    
//	    algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption);
//	    server_pkcs8 = new PrivateKeyInfo(algId, rsaPrivateKey).getEncoded();
//	    fact = KeyFactory.getInstance ("RSA","BC");
//	    pkey = fact.generatePrivate (new PKCS8EncodedKeySpec(server_pkcs8));
//	    // for test only:
//	    System.out.println (pkey.getClass().getName() + " " + pkey.getAlgorithm());
//	    
//	    
//	    // pkcs8 pem
//	    
//	    // like in crypto utils but with instance RSA instead of EC
//	    filePath = Utils.getWorkDir() + File.separator + "key" + File.separator + "rsakey_pkcs8.pem";
//	    b64Key = CryptoUtils.derFromKeyFile(filePath, CryptoUtils.FILE_FORMAT_PEM);
//	    keyBytes = Conversion.base64StrToByteArray(b64Key);
//	    
//	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
//	    KeyFactory keyFactory;
//		if (Security.getProvider("BC") == null) {
//			keyFactory = KeyFactory.getInstance("RSA");
//		} else {
//			keyFactory = KeyFactory.getInstance("RSA", "BC");
//		}
//		
//		pk = keyFactory.generatePrivate(keySpec);	
//		System.out.println(kp.getPrivate().getAlgorithm());
//	    
//
//	    // pkcs8 der
//	    
//	    // like in crypto utils but with instance RSA instead of EC
//	    filePath = Utils.getWorkDir() + File.separator + "key" + File.separator + "rsakey_pkcs8.der";
//	    b64Key = CryptoUtils.derFromKeyFile(filePath, CryptoUtils.FILE_FORMAT_DER);
//	    keyBytes = Conversion.base64StrToByteArray(b64Key);
//	    
//	    keySpec = new PKCS8EncodedKeySpec(keyBytes);
//		if (Security.getProvider("BC") == null) {
//			keyFactory = KeyFactory.getInstance("RSA");
//		} else {
//			keyFactory = KeyFactory.getInstance("RSA", "BC");
//		}
//		
//		pk = keyFactory.generatePrivate(keySpec);	
//		System.out.println(kp.getPrivate().getAlgorithm());

	    
	}
}
