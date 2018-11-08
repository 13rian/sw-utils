import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.file.FileUtils;

public class MainTest {
	private static final Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	public static final int FORMAT_DER = 1;
	public static final int FORMAT_PEM = 2;

	public static void main(String[] args) {
		CryptoUtils.registerBC();
		
		String sep = File.separator;
		String certDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep + "certs" + sep;
		
		// cert
		String pemCertPath = certDir + "server.cert.pem";
		String derCertPath = certDir + "server.cert.cer";
		
		// load the object from the file
		X509Certificate cert1 = certFromFile(pemCertPath);
		X509Certificate cert2 = certFromFile(derCertPath);
		
		// load the b64 encoded der certificate
		String b64Cert1 = derFromCertFile(pemCertPath, FORMAT_PEM);
		String b64Cert2 = derFromCertFile(derCertPath, FORMAT_DER);
		
		X509Certificate cert11 = certFromDer(Conversion.base64StrToByteArray(b64Cert1));
		X509Certificate cert22 = certFromDer(Conversion.base64StrToByteArray(b64Cert2));
		
		
		
		
		
		// key
		String pemKeyPath = certDir + "server.key.pem";
		String derKeyPath = certDir + "server.key.der";
		

		// load the key object from the file
		PrivateKey key1 = keyFromFile(pemKeyPath, FORMAT_PEM);
		PrivateKey key2 = keyFromFile(derKeyPath, FORMAT_DER);
		
		
		
		
		// load the b64 encoded der key
		String b64Key1 = derFromKeyFile(pemKeyPath, FORMAT_PEM);
		String b64Key2 = derFromKeyFile(derKeyPath, FORMAT_DER);
		
		PrivateKey pk11 = keyFromDer(Conversion.base64StrToByteArray(b64Key1));
		PrivateKey pk22 = keyFromDer(Conversion.base64StrToByteArray(b64Key2));
		
	
		
		
		System.out.println("end");

	}
	
	

	
	
	
	
	/**
	 * loads a certificate from a pem or a der file
	 * @param path 	the path to the certificate file
	 * @return 		the certificate
	 */
	public static X509Certificate certFromFile(String path) {
		try {
			File certFile = new File(path);
			FileInputStream certInput = new FileInputStream(certFile);

			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			return (X509Certificate) cf.generateCertificate(certInput);

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
	public static X509Certificate certFromDer(byte[] certBytes) {
		try {
			// certificate factory
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
			ByteArrayInputStream is = new ByteArrayInputStream(certBytes); 
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);

			return cert;

		} catch (Exception e) {
			logger.error("error creating a certificate from the passed bytes: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads the base64 encoded certificate form the passed certificate file
	 * @param path 		path to the certificate file
	 * @param format 	der or pem use CryptoUtils.FORMAT_DER or CryptoUtils.FORMAT_PEM
	 * @return 			certificate as b64 encoded der
	 */
	public static String derFromCertFile(String path, int format) {
		if (format == FORMAT_DER) {
			byte[] b64CertBytes = FileUtils.readByteArrFromFile(path);
			String b64Cert = Conversion.byteArrayToBase64(b64CertBytes);
			return b64Cert;
			
		} else if (format == FORMAT_PEM) {
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && pemObjs.size() > 0) {
				return pemObjs.get(0);
			} else {
				logger.error("no pem objects found in the passed pem file");
				return null;
			}
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}
	}
		
	
	
	
	
	
	/////////////// key
	/**
	 * loads a key from a pem or a der file
	 * @param path 		the path to the key file
	 * @param format 	der or pem use CryptoUtils.FORMAT_DER or CryptoUtils.FORMAT_PEM
	 * @return 			the private key
	 */
	public static PrivateKey keyFromFile(String path, int format) {
		// load the b64 encoded der key
		String b64Key;
		if (format == FORMAT_DER) {
			b64Key = derFromKeyFile(path, FORMAT_DER);
						
		} else if (format == FORMAT_PEM) {
			b64Key = derFromKeyFile(path, FORMAT_PEM);
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}		
		
		// create the private key from the der bytes
		PrivateKey pk = keyFromDer(Conversion.base64StrToByteArray(b64Key));
		return pk;
	}
	
	
	/**
	 * loads a private key from the byte array in the DER format
	 * @param keyBytes 		byte array containing the key information	
	 * @return 				the Java object that contains the private key
	 */
	public static PrivateKey keyFromDer(byte[] keyBytes) {
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("EC", "BC");
			PrivateKey pk = kf.generatePrivate(keySpec);			
			return pk;

		} catch (Exception e) {
			logger.error("error creating a private key from the passed bytes: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads the base64 encoded key form the passed key file
	 * @param path 		path to the key file
	 * @param format 	der or pem use CryptoUtils.FORMAT_DER or CryptoUtils.FORMAT_PEM
	 * @return 			key as b64 encoded der
	 */
	public static String derFromKeyFile(String path, int format) {
		if (format == FORMAT_DER) {
			byte[] b64KeyBytes = FileUtils.readByteArrFromFile(path);
			String b64Key = Conversion.byteArrayToBase64(b64KeyBytes);
			return b64Key;
			
		} else if (format == FORMAT_PEM) {
			List<String> pemObjs = loadPem(path);
			if (pemObjs != null && pemObjs.size() > 0) {
				return pemObjs.get(0);
			} else {
				logger.error("no pem objects found in the passed pem file");
				return null;
			}
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}
	}
	
	
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
}
