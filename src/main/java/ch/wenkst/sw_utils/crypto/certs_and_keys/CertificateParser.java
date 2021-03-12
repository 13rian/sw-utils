package ch.wenkst.sw_utils.crypto.certs_and_keys;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.crypto.SecurityUtils;
import ch.wenkst.sw_utils.file.FileUtils;

public class CertificateParser {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	
	private PemParser pemParser = new PemParser();
	
	
	/**
	 * loads a list of certificates form pem or der files. if a certificate could not be loaded
	 * it will not be added to the list
	 * @param paths 	list of absolute file paths to the certificates
	 * @return 			list of certificates
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 * @throws FileNotFoundException 
	 */
	public List<Certificate> certsFromFiles(List<String> paths) throws FileNotFoundException, CertificateException, NoSuchProviderException {
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
	public Certificate certFromFile(String path) throws FileNotFoundException, CertificateException, NoSuchProviderException {
		File certFile = new File(path);
		FileInputStream certInput = new FileInputStream(certFile);
		CertificateFactory certFactory = SecurityUtils.certificateFactoryInstance();
		return certFactory.generateCertificate(certInput);
	}
	
	
	/**
	 * loads a security certificate from the byte array in the DER format
	 * @param certBytes 	byte array containing the certificate information	
	 * @return 				the Java object that contains the security certificate
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 */
	public Certificate certFromDer(byte[] certBytes) throws CertificateException, NoSuchProviderException {
		CertificateFactory certFactory = SecurityUtils.certificateFactoryInstance();
		ByteArrayInputStream is = new ByteArrayInputStream(certBytes); 
		return certFactory.generateCertificate(is);
	}
	
	
	/**
	 * loads a byte array containing the binary der data representing the cert of the passed cert file
	 * @param path 		path to the certificate file
	 * @param format 	format of the file
	 * @return 			certificate der byte array
	 * @throws IOException 
	 */
	public byte[] derFromCertFile(String path, FileFormat fileFormat) throws IOException {		
		if (fileFormat.equals(FileFormat.DER)) {
			return FileUtils.readByteArrFromFile(path);
			
		} else if (fileFormat.equals(FileFormat.PEM)) {
			return pemParser.derFromPem(path);
			
		} else {
			logger.error("passed file format " + fileFormat + " is not implemented");
			return null;
		}
	}
}
