import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
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
		String cryptoUtilsDir = System.getProperty("user.dir") + sep + "resource" + sep + "cryptoUtils" + sep;
		String certDir = cryptoUtilsDir + "certs" + sep + "server" + sep;
		
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
		
//		try {
//			KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
//			keyFactory.
//			
//		} catch (Exception e) {
//			logger.error("exception: ", e);
//		}
		
		
		// load the b64 encoded der ke
		String b64Key1 = derFromKeyFile(pemKeyPath, FORMAT_PEM);
		String b64Key1_5 = loadPEM(pemKeyPath);
		String b64Key2 = derFromKeyFile(derKeyPath, FORMAT_DER);
		
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(b64Key1_5));
			KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
			PrivateKey pk = kf.generatePrivate(keySpec);
			System.out.println("parsed");

		} catch (Exception e) {
			logger.error("exception: ", e);
		}

		
		
		
		System.out.println("end");

	}
	
	
	private static String loadPEM(String path) {
		try {
			InputStream inputStream = new FileInputStream(path);
			String pem = new String(inputStream.readAllBytes(), StandardCharsets.ISO_8859_1);
			Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
			String encoded = parse.matcher(pem).replaceFirst("$1");
			inputStream.close();
			// return Base64.getMimeDecoder().decode(encoded);
			return encoded;

		} catch (Exception e) {
			logger.error("error parsing the pem-file " + path, e);
			return null;
		}
	}
	
	
	
	
	/**
	 * loads a certificate from a pem or a der file
	 * @param path 	the path to the certificate file
	 * @return 	the certificate
	 */
	public static X509Certificate certFromFile(String path) {
		try {
			File keyFile = new File(path);
			FileInputStream keyInput = new FileInputStream(keyFile);

			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			return (X509Certificate) cf.generateCertificate(keyInput);

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
			logger.error("error reading the certificate: ", e);
			return null;
		}
	}
	
	
	/**
	 * loads the der bytes from the passed certificate file
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
			String b64Cert = derCertFromPem(path);
			return b64Cert;
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}
	}
	
	
	/**
	 * load the certificate as b64 encoded der from the passed pem cert file path
	 * @param path 			path of the cert file in pem format from which the certificate is read
	 * @return 				certificate as b64 encoded der
	 */
	private static String derCertFromPem(String path) {
		try {
			InputStream inputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			StringBuilder strBuilder = new StringBuilder();


			String line = "";
			boolean certBegan = false;
			while (line != null) {
				line = bufferedReader.readLine(); 		// read the next line

				// check for the first line
				if (line.startsWith("-----BEGIN ") && line.endsWith(" CERTIFICATE-----")) {
					certBegan = true;
					continue;
				} 

				// check for the last line
				if (line.startsWith("-----END ") && line.endsWith(" CERTIFICATE-----")) {
					break;
				}

				// append the line to the cert
				if (certBegan) {
					strBuilder.append(line.trim());
				}
			}

			// close the resources
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			String b64Cert =  strBuilder.toString();
			return b64Cert;

		} catch (Exception e) {
			logger.error("error reading the certificate from " + path, e);
			return null;
		}
	}
	
	
	
	
	
	
	
	
	
	/////////////// key
	/**
	 * loads the der bytes from the passed key file
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
			String b64Key = derKeyFromPem(path);
			return b64Key;
			
		} else {
			logger.error("passed file format " + format + " is not implemented");
			return null;
		}
	}
	
	
	/**
	 * load the key as b64 encoded der from the passed pem key file path
	 * @param path 			path of the cert file in pem format from which the certificate is read
	 * @return 				certificate as b64 encoded der
	 */
	private static String derKeyFromPem(String path) {
		try {
			InputStream inputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			StringBuilder strBuilder = new StringBuilder();


			String line = "";
			boolean certBegan = false;
			while (line != null) {
				line = bufferedReader.readLine(); 		// read the next line

				// check for the first line
				if (line.startsWith("-----BEGIN ") && line.endsWith(" PRIVATE KEY-----")) {
					certBegan = true;
					continue;
				} 

				// check for the last line
				if (line.startsWith("-----END ") && line.endsWith(" PRIVATE KEY-----")) {
					break;
				}

				// append the line to the cert
				if (certBegan) {
					strBuilder.append(line.trim());
				}
			}

			// close the resources
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			String b64Cert =  strBuilder.toString();
			return b64Cert;

		} catch (Exception e) {
			logger.error("error reading the private key from " + path, e);
			return null;
		}
	}
	
}
