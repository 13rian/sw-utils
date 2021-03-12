package ch.wenkst.sw_utils.crypto.certs_and_keys;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.crypto.SecurityUtils;

public class PemParser {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	
	public byte[] derFromPem(String path) throws IOException {
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
	public List<String> loadPem(String path) throws IOException {
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
	
	
	public List<String> readPemFromBufferedReader(BufferedReader bufferedReader) throws IOException {
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
}
