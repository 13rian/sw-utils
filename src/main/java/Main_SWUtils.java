import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.w3c.dom.Element;

import com.google.gson.JsonElement;
import com.mongodb.ServerAddress;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.crypto.CryptoUtils;
import ch.wenkst.sw_utils.dates.DateHandler;
import ch.wenkst.sw_utils.db.DBHandler;
import ch.wenkst.sw_utils.db.EntityBase;
import ch.wenkst.sw_utils.event.EventBoard;
import ch.wenkst.sw_utils.event.managers.AsyncEventManager;
import ch.wenkst.sw_utils.files.FileHandler;
import ch.wenkst.sw_utils.files.JsonDoc;
import ch.wenkst.sw_utils.files.XMLDoc;
import ch.wenkst.sw_utils.future.TimeoutFuture;
import ch.wenkst.sw_utils.http.builder.HttpRequestBuilder;
import ch.wenkst.sw_utils.http.builder.HttpResponseBuilder;
import ch.wenkst.sw_utils.http.parser.HttpRequestParser;
import ch.wenkst.sw_utils.http.parser.HttpResponseParser;
import ch.wenkst.sw_utils.math.MathOperations;
import ch.wenkst.sw_utils.miscellaneous.NaturaSortComparator;
import ch.wenkst.sw_utils.scheduler.Scheduler;
import ch.wenkst.sw_utils.tests.db.Car;
import ch.wenkst.sw_utils.tests.events.Event;
import ch.wenkst.sw_utils.tests.events.Listener;
import ch.wenkst.sw_utils.tests.scheduler.PrintTask;

public class Main_SWUtils {
//	// define a class initializer that is executed before any other properties and classes are loaded (since it is the 
//	// initializer for the main program). It sets the needed system property for the logger
//	// no need to set the JVM arguments anymore in the Debug Configurations
	static {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
	}
	
	
	final static Logger logger = LogManager.getLogger(Main_SWUtils.class);    // initialize the logger

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		//		// test logger		
		//		int i = 0;
		//		while(i<100000) {
		//			logger.error("error" + i);
		//			i++;
		//		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// make sure to use the right file in lib/security (as described in the folder file_for_ecyption) 		   	   //
		// Java 9: Security.setProperty("crypto.policy", "unlimited"); for the same effect 						   	   //
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 											AES crypto							   			   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
//		System.out.println("\n AES CRYPTO");
//		
//		// Note: The JCE unlimited strength file needs to be installed for the crypto to work
//		CryptoUtils.registerBC(); 		// register the bouncy castle provider
//		
//		String message = "This is the message to encrypt";
//		logger.info("message to encrypt: "  + message);
//		int keyLength = 256;     								// 128,192,256 bits are allowed
//				
//		// generate the secret key
//		SecretKey secretKey = CryptoUtils.generateKey(keyLength);		
//		
//		// encrypt the message
//		String encryptedMsg = CryptoUtils.encrypt(message, secretKey);
//		logger.info("encrypted String: " + encryptedMsg);		
//		
//		// decrypt the cipher text
//		String decryptedMsg = CryptoUtils.decrypt(encryptedMsg, secretKey);
//		logger.info("decrypted cipher text: " + decryptedMsg);
//		
//		
//		// test only encrypting byte arrays
//		String msg2 = "message2";
//		logger.info("message2 to encrypt: " + msg2);
//		
//		// encrypt
//		byte[] encryptedBytes = CryptoUtils.encrypt(msg2.getBytes(StandardCharsets.UTF_8), secretKey);
//		
//		// decrypt
//		byte[] decryptedBytes = CryptoUtils.decrypt(encryptedBytes, secretKey);
//		logger.info("message decrypted: " + new String(decryptedBytes, StandardCharsets.UTF_8));
//		
//		// unregister the bouncy castle provider
//		CryptoUtils.unregisterBC(); 		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 											CMS crypto							   			   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
//		System.out.println("\n CMS CRYPTO");
//		
//		// Note: The JCE unlimited strength file needs to be installed for the crypto to work
//		CryptoUtils.registerBC(); 		// register the bouncy castle provider
//			
//		
//		// define the certificate directories
//		String sep = File.separator;
//		String receiverEncDir = System.getProperty("user.dir") + sep + "cmsCerts" + sep + "encryption" + sep + "receiver" + sep;
//		String senderEncDir = System.getProperty("user.dir") + sep + "cmsCerts" + sep + "encryption" + sep + "sender" + sep;
//		String senderSigDir = System.getProperty("user.dir") + sep + "cmsCerts" + sep + "signature" + sep + "sender" + sep;
//		
//		// load the certificates/keys for encryption
//		X509Certificate receiverEncCert = CryptoUtils.loadCertificate(receiverEncDir + "certificate.pem");  	// receiver certificate
//		X509Certificate senderEncCert = CryptoUtils.loadCertificate(senderEncDir + "certificate.pem"); 		// sender certificate
//		PrivateKey senderEncKey = CryptoUtils.loadPrivateKey(senderEncDir + "key.p12", "celsi-pw");   		// sender private key
//		
//		// load the key for the signature
//		PrivateKey senderSigKey = CryptoUtils.loadPrivateKey(senderSigDir + "key.p12", "celsi-pw");   		// sender private key
//		
//		// load the certificate for the verification
//		X509Certificate senderSigCert = CryptoUtils.loadCertificate(senderSigDir + "certificate.pem"); 		// sender certificate
//		
//
//		// load the certificates/keys for decryption
//		PrivateKey receiverEncKey = CryptoUtils.loadPrivateKey(receiverEncDir + "key.p12", "celsi-pw");   		// sender private key
//	
//		// print out the signature algorithm
//		logger.info("signature algorithm: " + senderEncCert.getSigAlgName());
//		
//		
//		
//		
//		// 									start the encryption 												 //
//		///////////////////////////////////////////////////////////////////////////////////////////////////////////
//		String message = "Hello";
//		
//		// sign the original message
//		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
//		byte[] signature = CryptoUtils.sign(senderSigKey, messageBytes);
//				
//		String sigBase64 = Conversion.byteArrayToBase64(signature);
//		logger.info("signature of the message: " + Conversion.byteArrayToBase64(signature));
//		logger.info("signature base64 length: " + sigBase64.length() + " , byte length: " + signature.length);
//		
//		
//		// encrypt: message:signature (this is not standard but the message needs to be transmitted somehow)
//		String dataToEncryptStr = Conversion.byteArrayToBase64(messageBytes) + ":" + sigBase64;
//		byte[] dataToEncrypt = dataToEncryptStr.getBytes(StandardCharsets.UTF_8);
//		byte[] encryptedBytes = CryptoUtils.createEnvelopeData(receiverEncCert, senderEncCert, senderEncKey, dataToEncrypt);
//		logger.info(Conversion.byteArrayToBase64(encryptedBytes));
//		
//		
//		
//		
//		// 									start the decryption 												 //
//		///////////////////////////////////////////////////////////////////////////////////////////////////////////
//		// decrypt
//		byte[] decryptedBytes = CryptoUtils.decrypt(receiverEncCert, receiverEncKey, encryptedBytes);
//		
//		// extract the signature and the message
//		String decryptedStr = new String(decryptedBytes, StandardCharsets.UTF_8);
//		String[] parts = decryptedStr.split(":");
//		byte[] mes = Conversion.base64StrToByteArray(parts[0]);
//		byte[] sig = Conversion.base64StrToByteArray(parts[1]);
//		
//		String decrypted = new String(mes,  StandardCharsets.UTF_8);
//		logger.info("decrypted: " + decrypted);
//		
//		// verify
//		boolean isValidSig = CryptoUtils.verifySig(senderSigCert, mes, sig);
//		logger.info("is sig valid: " + isValidSig);
//		
//				
//				
//
//		// unregister the bouncy castle provider
//		CryptoUtils.unregisterBC(); 
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 										Password hash							   			   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n PASSWORD HASH");
		
		// hash the password
		String password =  "securePW";
		String pwHash = "";
		try {
			pwHash = CryptoUtils.hashPassword(password);
		} catch (Exception e) {
			logger.error("failed to hash the password: ", e);
		}
		logger.info("password: " + password + ", hash: " + pwHash);
		
		
		// verify the password
		boolean passwordValid = CryptoUtils.validatePassword(password, pwHash);
		logger.info("is the password hash valid: " + passwordValid);
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 										test Security Utils									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n SECURITY UTILS"); 
		
		// certificate handling method
		String certDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "certs";
		String keyPath = FileHandler.findFileByPattern(certDir, "TLS", "pem");
		String certPath = FileHandler.findFileByPattern(certDir, "TLS", "cer");
		
		// load the key in der (tested, it is working)
		String keyDER = CryptoUtils.loadDERPrivateKey(keyPath);
		System.out.println(keyDER);
		
		// load the cert in der format (tested it is working)
		String certDER = CryptoUtils.loadDERCertificate(certPath);
		System.out.println(certDER);
		
		
		
		// print all registered providers
		logger.info(CryptoUtils.getRegisteredProviders());
		
		// print the default providers
		logger.info(CryptoUtils.getDefaultProviders());
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 										test natural sort									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n NATURAL SORT TEST"); 
		
		// define the list to sort
		List<String> testWords = Arrays.asList("Room3", "room3", "Room10", "room10", "room11", "Room11", "room4.8", "Room15.7", "doom8", "doom11", "doom1", "roomNumber", "roomnumber");
		
		// ignore the capital letters
		Collections.sort(testWords, new NaturaSortComparator(true));
		logger.info("capital letters ignored");
		logger.info(String.join(", ", testWords)); 
		
		// do not ignore capital letters
		Collections.sort(testWords, new NaturaSortComparator(false));
		logger.info("capital letters not ignored");
		logger.info(String.join(", ", testWords)); 
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 									test date handler										   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n DATE HANDLER TEST");
		
		// print date of today
		Calendar now = Calendar.getInstance();
		//now.setTime(new Date());
		logger.info("Today date: " + DateHandler.dateToStr(now, "dd.MM.yyyy"));
		
		// get a date from a String
		Calendar exampleDate = DateHandler.strToDate("17.3.1951", "dd.MM.yyyy");
		logger.info("example date: " + exampleDate.get(Calendar.YEAR) + "_" + (exampleDate.get(Calendar.MONTH)+1) + "_" + exampleDate.get(Calendar.DAY_OF_MONTH));
		
		// get the Easter Sunday
		Calendar easterSunday = DateHandler.easterDate(2017);
		logger.info("Easter Sunday in 2017: " + DateHandler.dateToStr(easterSunday, "dd.MM.yyyy"));
		
		
		// check if two dates represent the same day
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(new Date());
		tomorrow.add(Calendar.DATE, 1);
		logger.info("are today and tomorrow the same dates: " + DateHandler.areDatesEqual(now, tomorrow));
		
		Calendar laterToday = Calendar.getInstance();
		laterToday.setTime(new Date());
		laterToday.add(Calendar.SECOND, 3);
		logger.info("are now and 3 seconds later the same dates: " + DateHandler.areDatesEqual(now, laterToday));
		
		
		// check if the timestamp is from today or not
		long nowTimestamp = System.currentTimeMillis();
		logger.info("is timestamp of now from today: " + DateHandler.isTimestampToday(nowTimestamp));
		
		long yesterdayTimestamp = nowTimestamp - 24*60*60*1000;
		logger.info("is timestamp of yesterday from today: " + DateHandler.isTimestampToday(yesterdayTimestamp));
		
		
		// test the holiday Utility for ch, print out all holidays in one year
		Calendar date = DateHandler.strToDate("01.01.2017", "dd.MM.yyyy");
		logger.info("ch holidays");
		for (int i=0; i<365; i++) {
			if (DateHandler.isHoliday("ch", date)) {
				logger.info("holiday: " + DateHandler.dateToStr(date, "dd.MM.yyyy"));
			}
			
			// increment the date by one day
			date.add(Calendar.DATE, 1);
		}
		
		// test the holiday Utility for de, print out all holidays in one year
		date = DateHandler.strToDate("01.01.2017", "dd.MM.yyyy");
		logger.info("de holidays");
		for (int i=0; i<365; i++) {
			if (DateHandler.isHoliday("de", date)) {
				logger.info("holiday: " + DateHandler.dateToStr(date, "dd.MM.yyyy"));
			}
			
			// increment the date by one day
			date.add(Calendar.DATE, 1);
		}
		
		
		// test to parse a date with different patterns
		SimpleDateFormat[] knownPatterns = {
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"), 
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
		};
		
		// set the time zone of the utc pattern
		knownPatterns[0].setTimeZone(TimeZone.getTimeZone("GMT"));
		
		String date1 = "2018-01-18T15:45:00Z"; 		// the Z stands for the UTC time, therefore the time zone needs to be set
		String date2 = "2018-01-18T15:45:00";
		long timestamp1 = DateHandler.parseDate(date1, knownPatterns);
		long timestamp2 = DateHandler.parseDate(date2, knownPatterns);
		logger.info("timestamp1: " + timestamp1);
		logger.info("timestamp2: " + timestamp2);
		
		

		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 									test the xml read										   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n XML READ TEST");

		XMLDoc xmlDoc = new XMLDoc();
		xmlDoc.openXMLFromFile("resource/xmlReadTest.xml");
		Element rootElement = xmlDoc.getRootElement();   // get the root element
		Element employee = xmlDoc.getChildElement(rootElement, 2);
		Element firstNameEl = xmlDoc.getChildElementByName(employee, "Firstname", 0);
		logger.info(xmlDoc.getValueFromElement(firstNameEl));


		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 									test the xml write										   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n XML WRITE TEST");

		// create a new document and drop the old one
		xmlDoc.createNewDocument();

		// create the root element
		Element xmlRoot = xmlDoc.createRootElement("XMLTest", "http://www.celsi.ch");

		// create players
		Element players = xmlDoc.createElement("Players");        // create a player element
		xmlDoc.appendChildToParent(xmlRoot, players);         // append it to the root element

		// create player without attribute
		Element player1 = xmlDoc.createElement("Player");				        // create a player element
		xmlDoc.appendChildToParent(players, player1);                    	 	// append it to the root element

		Element name = xmlDoc.createStrElement("name", "Paul Morphy"); 		// create text element
		Element age = xmlDoc.createStrElement("age", "27"); 		  		// create text element

		// append the text elements
		xmlDoc.appendChildToParent(player1, name);
		xmlDoc.appendChildToParent(player1, age);


		// create player with attribute
		Element player2 = xmlDoc.createElement("Player");            
		xmlDoc.addAttribute(player2, "id", "5"); 								// add an attribute
		xmlDoc.appendChildToParent(players, player2);                    		// append it to the root element

		Element name2 = xmlDoc.createStrElement("name", "Bobby Fischer"); 	// create text element
		Element age2 = xmlDoc.createStrElement("age", "31"); 		    	// create text element

		// append the text elements
		xmlDoc.appendChildToParent(player2, name2);
		xmlDoc.appendChildToParent(player2, age2);

		// write to file
		int indentNumber = 4; 	// specify the indent number
		xmlDoc.writeToFile("resource/xmlWriteTest.xml", indentNumber);

		// write to a String
		String xmlString = xmlDoc.writeToString(4);
		logger.info(xmlString);
		
		
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											json read 													//
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// get the file name of the json file to parse
		String jsonFilePath = Utils.getWorkDir() + File.separator + "resource" + File.separator + "jsonRead.json";

		// parse the json file
		JsonDoc jsonDoc = new JsonDoc();
		jsonDoc.openJsonFromFile(jsonFilePath);
		JsonElement rootEl = jsonDoc.getRootElement();
		
		// read out the primitive properties
		logger.info("PRIMITIVE PROPS: ");
		
		String strProp = jsonDoc.readString(rootEl, "strProp", "");
		int intProp = jsonDoc.readInt(rootEl, "intProp", 0);
		long longProp = jsonDoc.readLong(rootEl, "longProp", 0L);
		double doubleProp = jsonDoc.readDouble(rootEl, "floatProp", 0.0);
		boolean booleanProp = jsonDoc.readBoolean(rootEl, "booleanProp", false);
		
		logger.info("string prop: " + strProp);
		logger.info("int prop: " + intProp);
		logger.info("long prop: " + longProp);
		logger.info("double prop: " + doubleProp);
		logger.info("boolean prop: " + booleanProp);
		
			
		
		// read out the arrays
		System.out.println("\n");
		logger.info("ARRAY PROPS: ");
		
		String[] strArr = jsonDoc.readStringArray(rootEl, "strArrProp");
		int[] intArr = jsonDoc.readIntArray(rootEl, "intArrProp");
		long[] longArr = jsonDoc.readLongArray(rootEl, "longArrProp");
		double[] doubleArr = jsonDoc.readDoubleArray(rootEl, "floatArrProp");
		boolean[] booleanArr = jsonDoc.readBooleanArray(rootEl, "booleanArrProp");
		
				
		logger.info("strArr: " + Arrays.toString(strArr)); 
		logger.info("intArr: " + Arrays.toString(intArr));
		logger.info("longArr: " + Arrays.toString(longArr));
		logger.info("doubleArr: " + Arrays.toString(doubleArr));
		logger.info("booleanArr: " + Arrays.toString(booleanArr));
		

			
		
		// read out the object
		System.out.println("\n");
		logger.info("OBJECT PROP: ");
		
		JsonElement childElement = jsonDoc.getChildElementByName(rootEl, "objProp");
		String nameStr = jsonDoc.readString(childElement, "name", "");
		double weight = jsonDoc.readDouble(childElement, "weight", 0.0);
		
		logger.info("name: " + nameStr);
		logger.info("weight: " + weight);
		
		
		
		// read out the object
		System.out.println("\n");
		logger.info("OBJECT ARRAY PROP: ");
		
		JsonElement[] childElements = jsonDoc.getChildElementsByName(rootEl, "objArrProp");
		nameStr = jsonDoc.readString(childElements[2], "name", "");
		weight = jsonDoc.readDouble(childElements[2], "weight", 0.0);
		
		logger.info("name of 3. element: " + nameStr);
		logger.info("weight of the 3. element: " + weight);

	
		
		
		// convert an object to a json string
		HashMap<String, Object> hm = new HashMap<>();
		hm.put("name", "Brian");
		hm.put("weight", 65.3);
		hm.put("blacklisted", false);
		
		HashMap<String, Object> address = new HashMap<>();
		address.put("city", "LA");
		address.put("street", "Spooner street");
		address.put("zip", 56998);
		hm.put("address", address);
				
		String jsonString = JsonDoc.objToJsonStr(hm, true);
		logger.info("json of obj: ");
		System.out.println(jsonString);
		
		
		// write the object to a json file
		JsonDoc.objToJsonFile("test.json", hm, true);
		
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 											json write 													//
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		// write a new json document
		jsonDoc = new JsonDoc();
		jsonDoc.createNewDocument();
		rootEl = jsonDoc.getRootElement();
		
		
		// add some simple properties
		jsonDoc.addString(rootEl, "strProp", "strVal");
		jsonDoc.addNumber(rootEl, "intProp", 50);
		jsonDoc.addNumber(rootEl, "floatProp", 44.36);
		jsonDoc.addBoolean(rootEl, "booleanProp", false);
		
		
		// add some array properties
		ArrayList<Object> strList = new ArrayList<>();
		strList.add("val1");
		strList.add("val2");
		strList.add("val3");
		jsonDoc.addArray(rootEl, "strArr", strList);
		
		
		ArrayList<Object> mixedList = new ArrayList<>();
		mixedList.add("val1");
		mixedList.add(53.69);
		
		JsonElement element = jsonDoc.createElement();
		jsonDoc.addString(element, "strProp", "string val");
		jsonDoc.addNumber(element, "intProp", 56.367);
		mixedList.add(element);
		jsonDoc.addArray(rootEl, "mixedArr", mixedList);
		
		
		// add an object property
		JsonElement objEl = jsonDoc.addElement(rootEl, "objProp");
		jsonDoc.addString(objEl, "name", "Brian");
		jsonDoc.addNumber(objEl, "weight", 68.6);
		
		// add a test element and remove it again
		jsonDoc.addElement(rootEl, "testProp");
		jsonDoc.removeElementByName(rootEl, "testProp");
		
		String jsonWriteFile = Utils.getWorkDir() + File.separator + "resource" + File.separator + "jsonWrite.json";
		logger.info("json test file written successfully: " + jsonDoc.writeToFile(jsonWriteFile, true));
	
		

		
		
		///////////////////////////////////////////////////////////////////////////////////////////////
		// 									test MathOperations 									 //
		///////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n MATH TEST");
		double numberD = 6.78d;
		double fPartD = MathOperations.getDecimalPart(numberD);
		logger.info("decimal part of double " + numberD + ": " + fPartD);

		float numberF = 3.085f;
		float fPartF = MathOperations.getDecimalPart(numberF);
		logger.info("decimal part of float " + numberF + ": " + fPartF);


		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										test conversions									 //
		///////////////////////////////////////////////////////////////////////////////////////////////
//		System.out.println("\n CONVERSION TEST");
//
//		// base64 String
//		String base64Str = "aGVsbG8=";
//		String base64Dec = Conversion.base64StrToStr(base64Str);
//		logger.info("base64 decoded: " + base64Dec);
//		logger.info("base64 encoded: " + Conversion.strToBase64Str(base64Dec));

//		// hex String
//		String hexStr = "68656C6C6F";   // hello
//		String hexDec = Conversion.hexStrToStr(hexStr);
//		logger.info("hex decoded: " + hexDec);
//		logger.info("hex encoded: " + Conversion.strToHexStr(hexDec));
		
//		String hexString = Conversion.byteArrayToHexStr(Conversion.hexStrToByteArray(hexStr));
//		String b64String = Conversion.byteArrayToBase64(Conversion.base64StrToByteArray(base64Str));

//		String hexStrNum = "B1B2";
//		logger.info(Conversion.hexStrToInt(hexStrNum));
//
//		logger.info(Conversion.intToHexStr(41378));
//		logger.info(Conversion.longToHexStr(45490));
			
		
//
//		// concatenate 2 arrays
//		int[] arr1 = {1,2,3};
//		int[] arr2 = {4,5,6};
//		int[] arr3 = {7,8};
//		int[] intConcat = Conversion.concatArrays(arr1, arr2, arr3);
//		logger.info("Concateneated int array: " + Arrays.toString(intConcat));
//
//		String[] arr4 = {"This", "is", "a"};
//		String[] arr5 = {"concatenated", "string"};
//		String[] stringConcat = Conversion.concatArrays(arr4, arr5);
//		logger.info("Concateneated string array: " + Arrays.toString(stringConcat)); 
//		
//		// string handling
//		logger.info("expanded String FF: " + Conversion.padLeft("FF", '0', 4));
//		logger.info("reversed string Hello: " + Conversion.strReverse("Hello"));


		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										FileHandler									 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n FILE HANDLER TEST");

		// read the last few lines of a file
		String filePath = Utils.getWorkDir() + File.separator + "resource" + File.separator + "xmlWriteTest.xml";
		// path, number of lines, nCharacters to read in each iteration (should be at least as big as the largest expected line)
		ArrayList<String> lines = FileHandler.readLastLines(filePath, 5);
		logger.info("extracted lines: \n" + lines); 
		

		// get the content of a file as String
		String filePath2 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile.txt";
		logger.info("read from file: \n" + FileHandler.readStrFromFile(filePath2));

		// write a String to a file
		String filePath3 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile" + File.separator + "newFile.txt";
		logger.info("new file written?: " + FileHandler.writeStrToFile(filePath3, "test content written by Java8"));

		// copy a file
		String fileToCopy = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile.txt";
		String dest = Utils.getWorkDir() + File.separator + "resource" + File.separator + "copyDir" + File.separator + "testFile.txt";
		logger.info("file copied?: " + FileHandler.copyFile(fileToCopy, dest, true));

		// move a file
		// create a test file to rename and to move
		String fileToCopy2 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFile.txt";
		String dest2 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileRename.txt";
		logger.info("created testFile2.txt to remove?: " + FileHandler.moveFile(fileToCopy2, dest2, true));
		
		// rename the file
		String fileToRename = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileRename.txt";
		String renameFile = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileMove.txt";
		logger.info("file renamed?: " + FileHandler.moveFile(fileToRename, renameFile, true));
		
		// copy the file again in order to use it the next time
		FileHandler.copyFile(renameFile, fileToCopy, true);
		
		// move the file
		String fileToMove = Utils.getWorkDir() + File.separator + "resource" + File.separator + "testFileMove.txt";
		String moveDest = Utils.getWorkDir() + File.separator + "resource" + File.separator + "moveDir" + File.separator + "testFileMove.txt";
		logger.info("file moved?: " + FileHandler.moveFile(fileToMove, moveDest, true));
		
		// delete a file that does not exist
		logger.info("file deleted?: " + FileHandler.deleteFile("testi"));
		
		// delete a file that does exist
		logger.info("file deleted?: " + FileHandler.deleteFile(dest));
		logger.info("file deleted?: " + FileHandler.deleteFile(moveDest));
		
		
		// recursively copy a directory
		String dirToCopy = Utils.getWorkDir() + File.separator + "resource" + File.separator + "dirToCopy";
		String copiedDir = Utils.getWorkDir() + File.separator + "resource" + File.separator + "copiedDir";
		logger.info("directory recursively copied: " + FileHandler.copyDir(dirToCopy, copiedDir, FileHandler.MERGE_NO_REPLACE));
		
		
		// recursively delete a directory
		logger.info("directory recursively deleted: " + FileHandler.deleteDir(copiedDir));
		
		

		// get all files from a directory
		String dir = Utils.getWorkDir() + File.separator + "resource" + File.separator;
		logger.info("Number of files in " + dir + ": " + FileHandler.getFilesFromDir(dir).length);
		
		// read the last line of a file
		String filePath4 = Utils.getWorkDir() + File.separator + "resource" + File.separator + "xmlReadTest.xml";
		logger.info("first line of the file: " + FileHandler.readFirstLine(filePath4));
		
		// read the nth line number of a file
		logger.info("23th line of the file: " + FileHandler.readNthLine(filePath4, 23));


		
		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										Utils								 		 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n UTILS");
		logger.info("current working dir: " + Utils.getWorkDir());
		
		try {
			int[] arr = {1,2};
			int number = arr[3]; 			// this will throw the exception as the index is out of bounds
			System.out.println(number);

		} catch (Exception e) {
			String stackTrace = Utils.exceptionToString(e);
			logger.info("exception: \n" + stackTrace);
		}
		
		
		// test the allOf future method
		CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			System.out.println("future1 finished");
			return "result_1";
		});
		
		
		CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
			}
			System.out.println("future2 finished");
			return "result_2";
		});

		CompletableFuture[] futures = {future1, future2};

		logger.debug("start allOf");
		CompletableFuture<List<Object>> futureList = Utils.allOfCombletableFuture(futures);
		logger.debug("end allOf");
		
		try {
			List<Object> resultList = futureList.get(2000, TimeUnit.MILLISECONDS);
			logger.info("result1: " + resultList.get(0) + ", result2: " + resultList.get(1));
			
		} catch (Exception e) {
			logger.error("exception waiting for the combined future: ", e);
		}
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 							test the http builder and parser								   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n HTTP TEST");
		
		// test the http request builder
		logger.info("test the http request builder");
		HttpRequestBuilder reqBuilder = new HttpRequestBuilder();
		
		reqBuilder.preparePost("https://i-api.eon.de/gwa/ClsManagementAdapter_In/v1/")
		.setHeaderProperty("Content-Type", "text/plain")
		.setHeaderProperty("Authorization", "Some secret code")
		.setBody("This is the html request body\n with no deep meaning");
		
		logger.info("http request: \n" + reqBuilder.toString());
		
		
		
		// test the http response builder
		logger.info("test the http response builder");
		HttpResponseBuilder respBuilder = new HttpResponseBuilder();

		respBuilder.status(200)
		.setHeaderProperty("Content-Type", "text/plain")
		.setHeaderProperty("Authorization", "Some secret code")
		.setBody("Some http response body \n with no deep meaning");

		logger.info("http response: \n" + respBuilder.toString());
		
		
		
		// test the http request parser
		logger.info("test the http request parser");
		HttpRequestParser reqParser = new HttpRequestParser();
		
		// feed chunk 1
		String requestChunk =  
				"POST /test HTTP/1.1\n" +
				"Authorization: Bearer 2dd4f332cad2705bb89a209e407bc636\n" +
				"Host: localhost:8001\n";
		
		
		reqParser.addData(requestChunk.getBytes(StandardCharsets.US_ASCII));
		logger.info("successfully parsed: " + reqParser.isComplete());
		
		// feed chunk 2
		requestChunk = 
				"User-Agent: AHC/1.0\n" +
				"Connection: keep-alive\n" +
				"Accept: */*\n" +
				"Content-Type: text/xml\n" +
				"Content-Length: 17\n" +
				"\n" +
				"aergf.qa";
		reqParser.addData(requestChunk.getBytes(StandardCharsets.US_ASCII));
		logger.info("successfully parsed: " + reqParser.isComplete());
		
		// feed chunk 3
		requestChunk = "e4t.435zg";
		reqParser.addData(requestChunk.getBytes(StandardCharsets.US_ASCII));
		logger.info("successfully parsed: " + reqParser.isComplete());
		
		
		logger.info("http-method: " + reqParser.getHttpMethod());
		logger.info("status-txt: " + reqParser.getRequestURI());
		logger.info("body: " + reqParser.getBodyStr());
		
		
		
		// test the http response parser
		logger.info("test the http response parser");
		logger.info("normal http response");
		HttpResponseParser respParser = new HttpResponseParser();
		String crlf = "\r\n";
		
		// test a normal http response
		String httpResponse = 
				"HTTP/1.1 200 OK\n" +
				"Access-Control-Allow-Origin: *\n" +
				"Access-Control-Allow-Headers: origin, content-type, accept, authorization\n" +
				"Access-Control-Allow-Credentials: true\n" +
				"Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD\n" +
				"Content-Type: text/plain\n" +
				"Content-Length: 37\n" +
				"\n" +
				"{\"params\":\"d81\",\"command\":\"testpost\"}";

		respParser.addData(httpResponse.getBytes(StandardCharsets.US_ASCII));

		logger.info("status: " + respParser.getStatus());
		logger.info("status-txt: " + respParser.getStatusTxt());
		logger.info("body: " + respParser.getBodyStr());

		
		// test a http response without a content length (the server closes the connection to indicate that the response is complete)
		logger.info("http without a content length");
		String httpResponse2 = 
				"HTTP/1.1 200 OK\n" +
				"Access-Control-Allow-Origin: *\n" +
				"Access-Control-Allow-Headers: origin, content-type, accept, authorization\n" +
				"Access-Control-Allow-Credentials: true\n" +
				"Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD\n" +
				"Content-Type: text/plain\n" +
				"\n" +
				"{\"params\":\"d81\",\"command\":\"testpost\"}";

		respParser.clearAfterFullMessage();
		respParser.addData(httpResponse2.getBytes(StandardCharsets.US_ASCII));
		respParser.fullMessageReceived();

		logger.info("status: " + respParser.getStatus());
		logger.info("status-txt: " + respParser.getStatusTxt());
		logger.info("body: " + respParser.getBodyStr());
		
		
		// test a chunked http response
		logger.info("chunked http response");
		String httpResponse3 = 
				"HTTP/1.1 200 OK" + crlf +
				"Access-Control-Allow-Origin: *" + crlf +
				"Access-Control-Allow-Headers: origin, content-type, accept, authorization" + crlf +
				"Access-Control-Allow-Credentials: true" + crlf +
				"Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD" + crlf +
				"Transfer-Encoding: chunked" + crlf +
				"Content-Type: text/plain" + crlf +
				crlf +
				"3" + crlf +
				"lol" + crlf +
				"3" + crlf + 
				"man" + crlf + 
				"0"
				+ crlf;

		respParser.clearAfterFullMessage();
		respParser.addData(httpResponse3.getBytes(StandardCharsets.US_ASCII));
		

		logger.info("status: " + respParser.getStatus());
		logger.info("status-txt: " + respParser.getStatusTxt());
		logger.info("body: " + respParser.getBodyStr());
		
		
	
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// 						test scheduler and timeoutFuture									   //
		/////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("\n SCHEDULER TEST");
		
		// timeout future using the inbuilt timeout
		TimeoutFuture<Boolean> future = new TimeoutFuture<>(500);
		logger.info("start to wait for futur timeout");
		Boolean result = future.get();
		logger.info("future timeout reached, result: " + result);
		
		
		// start the thread pool
		int nThreads = 10;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setCorePoolSize(nThreads); 
		
		// create the scheduler with a poll interval of 100ms
		Scheduler scheduler = new Scheduler(100, executor);
		scheduler.start();
		
		// create two tasks and schedule them into the future
		PrintTask printTask1 = new PrintTask(System.currentTimeMillis() + 2000, "PrintTask_1");
		PrintTask printTask2 = new PrintTask(System.currentTimeMillis() + 1000, "PrintTask_2");
		
		logger.info("schedule tasks");
		scheduler.addToTasks(printTask1);
		scheduler.addToTasks(printTask2);
		
		
		// test the timeout future
		System.out.println("\n TIMEOUT_FUTURE TEST");
		
		// create a completable future that will be completed with a timeout
		TimeoutFuture<Integer> timeoutFuture1 = new TimeoutFuture<Integer>(2000);
		TimeoutFuture<Integer> timeoutFuture2 = new TimeoutFuture<Integer>(1000);
		
		// complete future1, can be done by another thread
		Utils.sleep(500);
		timeoutFuture1.complete(5);
		
		try {
			// get value of future1 (already completed before)
			Integer result1 = (Integer)timeoutFuture1.get();
			logger.info("future1 result: " + result1);
			
			
			// get value of future2 (blocks until timeout reached)
			Integer result2 = timeoutFuture2.get();
			if (result2 == null) {
				logger.info("future2 timed out");
			} else {
				logger.info("reult2: " + result);
			}
			
		} catch (Exception e) {
			logger.error("error calling future.get():", e);
		} 
		
		
		// stop the scheduler
		scheduler.stopScheduler();
		
		// shutdown the executor
		logger.info("shutdown executor");
		executor.shutdown();
		

		
		
		

		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										MongoDB										 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////	
		System.out.println("\n MONGO DB TEST");
		
		// note: to make the query most effective always query the one that restricts the results the most first

		// define the parameters to connect to the mongoDB, the name of the collection is indicated as annotation in the entity Car
		String mongoHost = "192.168.1.183";
		int mongoPort = 27017;
		String dbName = "test_db";

		// test the connection
		ArrayList<ServerAddress> hosts = new ArrayList<>();
		hosts.add(new ServerAddress(mongoHost, mongoPort));
		// List<ServerAddress> hosts = Arrays.asList(new ServerAddress(mongoHost, mongoPort));
		boolean isReachable = DBHandler.testMongoDBConnection(hosts, 5000);
		logger.info("connectin to mongoDB? " + isReachable);

		if (isReachable) {

			// connect to the database
			DBHandler.connectToDB(mongoHost, mongoPort);

			// delete all collections in the database (use only here for test cases!!!)
			DBHandler.dropDatabase(dbName);

			// create objects to save in the database
			Car ford = new Car("Ford", 1600);
			Car ford2 = new Car("Ford", 2000);
			Car audi = new Car("Audi", 2500);
			Car vw = new Car("VW", 2500);
			Car mercedes = new Car("Mercedes", 1800);

			// save objects o the database
			DBHandler.saveToDB(ford, dbName);
			DBHandler.saveToDB(ford2, dbName);
			DBHandler.saveToDB(audi, dbName);
			DBHandler.saveToDB(vw, dbName);
			DBHandler.saveToDB(mercedes, dbName);

			// retrieve entities from the database (single constraint)
			Query<?> weightQuery = DBHandler.createFilterQuery(Car.class, "weight <=", 1900, null, dbName);
			List<?> lightCars = DBHandler.getListFromQuery(weightQuery);
			for (Object item: lightCars) {
				Car car = (Car)item;
				logger.info("low weight: " + car.getName());
			}

			// retrieve entities from the database (multiple constraints)
			String[] constraints = {"weight <=", "name ="};
			Object[] constraintVals = {1900, "Ford"};
			Query<?> weightNameQuery = DBHandler.createAndQuery(Car.class, constraints, constraintVals, null, dbName);
			List<?> lightFordCars = DBHandler.getListFromQuery(weightNameQuery);
			for (Object item: lightFordCars) {
				Car car = (Car)item;
				logger.info("light Ford car: " + car.getName());
			}

			// update value
			Query<?> updateValueQuery = DBHandler.createFilterQuery(Car.class, "weight =", 2500, null, dbName);
			Query<EntityBase> updateValueQueryBE = (Query<EntityBase>)updateValueQuery; 						// query needs to be casted
			DBHandler.updateEntityInDB("weight", 3000, dbName, updateValueQueryBE);


			// update values
			Query<?> updateValuesQuery = DBHandler.createFilterQuery(Car.class, "weight =", 3000, null, dbName);
			Query<EntityBase> updateValuesQueryBE = (Query<EntityBase>)updateValuesQuery; 						// query needs to be casted
			String[] fields = {"name", "weight"};
			Object[] values = {"Tesla", 2900};
			DBHandler.updateEntityInDB(fields, values, dbName, updateValuesQueryBE);


			// delete objects from the database
			Query<?> deleteEntityQuery = DBHandler.createFilterQuery(Car.class, "name =", "Tesla", null, dbName);
			Query<EntityBase> deleteEntityQueryBE = (Query<EntityBase>)deleteEntityQuery; 										// query needs to be casted
			DBHandler.deleteFromDB(dbName, deleteEntityQueryBE);

			
			// delete all collections in the database (use only here for test cases!!!)
			DBHandler.dropDatabase(dbName);
			

			// disconnect from the database
			DBHandler.disconnectFromDB();
		} else {
			System.out.println();
		}
		
		

		///////////////////////////////////////////////////////////////////////////////////////////////
		// 										EventFramework								 		 //
		///////////////////////////////////////////////////////////////////////////////////////////////	
		System.out.println("\n EVENT FRAMEWORK TEST");

		
		// asynchronous events (without the event board) //
		///////////////////////////////////////////////////
		
		// create the thread pool needed to send the asynchronous events
		logger.info("start the thread pool");
		int corePooSize = 10;
		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	    threadPool.setCorePoolSize(corePooSize); 
		
		// create two EventManager to send two events
		AsyncEventManager asyncEventManager1 = new AsyncEventManager("new Email", threadPool);
		AsyncEventManager asyncEventManager2 = new AsyncEventManager("new SMSss", threadPool);
		
		// create the listeners and register it for the 2 events defined in the event board
		Listener listener1 = new Listener("listener1", 2000);
		Listener listener2 = new Listener("listener2", 3000);
		asyncEventManager1.register(listener1);
		asyncEventManager1.register(listener2);
		asyncEventManager2.register(listener1);
		asyncEventManager2.register(listener2);

		// fire the 4 events
		System.out.println("async without event board");
		// two different events will be processed asynchronously
		asyncEventManager1.fire(new Event("mail", "mail 1 received"));   
		asyncEventManager2.fire(new Event("sms", "sms 1 received")); 
		Utils.sleep(20);
		
		// fire the same events again before the handleEvent method has finished, this will buffer the events
		// until the first ones are finished
		asyncEventManager1.fire(new Event("mail", "mail 2 received"));
		asyncEventManager2.fire(new Event("sms", "sms 2 received"));
		
		// wait
		Utils.sleep(8000);
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// synchronous events with event board //
		/////////////////////////////////////////
		
		EventBoard eventBoard = new EventBoard();	
		
		// create the listeners and register them
		listener1 = new Listener("listener1", 500);
		listener2 = new Listener("listener2", 300);
		eventBoard.registerListerner(listener1, "new Email");
		eventBoard.registerListerner(listener1, "new SMSss");
		eventBoard.registerListerner(listener2, "new Email");
		eventBoard.registerListerner(listener2, "new SMSss");
		
		// fire 4 events
		System.out.println("sync with event board");
		eventBoard.fireEvent("new Email", new Event("mail", "mail 1 received"));
		eventBoard.fireEvent("new Email", new Event("mail", "mail 2 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 1 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 2 received"));
		
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// same events synchronous, different events asynchronous with event board //
		/////////////////////////////////////////////////////////////////////////////
		
		eventBoard = new EventBoard(threadPool, false);	
		
		// create the listeners and register them
		listener1 = new Listener("listener1", 2000);
		listener2 = new Listener("listener2", 3000);
		eventBoard.registerListerner(listener1, "new Email");
		eventBoard.registerListerner(listener1, "new SMSss");
		eventBoard.registerListerner(listener2, "new Email");
		eventBoard.registerListerner(listener2, "new SMSss");
		
		// fire 4 events
		System.out.println("same events sync, different events async with event board");
		eventBoard.fireEvent("new Email", new Event("mail", "mail 1 received"));
		eventBoard.fireEvent("new Email", new Event("mail", "mail 2 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 1 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 2 received"));
		
		// wait
		Utils.sleep(8000);
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		// asynchronous events with event board //
		//////////////////////////////////////////
		
		eventBoard = new EventBoard(threadPool, true);	
		
		// create the listeners and register them
		listener1 = new Listener("listener1", 2000);
		listener2 = new Listener("listener2", 3000);
		eventBoard.registerListerner(listener1, "new Email");
		eventBoard.registerListerner(listener1, "new SMSss");
		eventBoard.registerListerner(listener2, "new Email");
		eventBoard.registerListerner(listener2, "new SMSss");
		
		// fire 4 events
		System.out.println("async with event board");
		eventBoard.fireEvent("new Email", new Event("mail", "mail 1 received"));
		eventBoard.fireEvent("new Email", new Event("mail", "mail 2 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 1 received"));
		eventBoard.fireEvent("new SMSss", new Event("sms", "sms 2 received"));

		
		// wait and shutdown the thread-pool
		Utils.sleep(3500);
		threadPool.shutdown();

		
		logger.info("end of main test routine reached");
	}
	
	
	


}
