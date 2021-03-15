package ch.wenkst.sw_utils.file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonDoc {
	private static final Logger logger = LoggerFactory.getLogger(JsonDoc.class);

	private JsonParser parser = null;   	// the json parser
	private JsonElement rootEl = null; 		// the root element of the json file


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												write methods 												//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * handles a json document
	 */
	public JsonDoc() {
		parser = new JsonParser();  
	}


	/**
	 * creates a new document and deletes the one that might already be present
	 */
	public void createNewDocument() {
		parser = new JsonParser(); 
		rootEl = new JsonObject();
	}
	
	
	/**
	 * converts a java object to a json string and writes it to the passed file path.
	 * the character encoding will be utf-8
	 * @param filePath 		the path of the file to which the json string is saved
	 * @param obj 			the object that is converted to a json string
	 * @param prettyPrint 	true if the json should be well formatted
	 * @throws IOException 
	 */
	public void objToJsonFile(String filePath, Object obj, boolean prettyPrint) throws IOException {
		String jsonSring = objToJsonStr(obj, prettyPrint);
		Path path = Paths.get(filePath);
		Files.write(path, jsonSring.getBytes(StandardCharsets.UTF_8));
	}
	
	
	/**
	 * converts a java object to a json string
	 * @param obj 			the object that is converted to a json string
	 * @param prettyPrint 	true if the json should be well formatted
	 * @return 				the json string that represents the passed object or null if an error occurred
	 */
	public static String objToJsonStr(Object obj, boolean prettyPrint) {
		Gson gson = null;
		if (prettyPrint) {
			gson = new GsonBuilder().setPrettyPrinting().create();
		} else {
			gson = new Gson();
		}
		return gson.toJson(obj);
	}
	
	
	/**
	 * converts the document to a json string
	 * @param prettyPrint 	true if the json should be well formatted 
	 * @return 				the json string of the document
	 */
	public String writeToString(boolean prettyPrint) {
		return objToJsonStr(rootEl, prettyPrint);
	}
	
	
	/**
	 * writes the json document to a file
	 * @param filePath 		path of the file to which the json document should be written
	 * @param prettyPrint 	true if the json should be well formatted
	 * @throws IOException 
	 */
	public void writeToFile(String filePath, boolean prettyPrint) throws IOException {
		objToJsonFile(filePath, rootEl, prettyPrint);
	}
	
	
	/**
	 * writes this document to a byte array, the character encoding is utf-8
	 * @param prettyPrint 	true if the json should be well formatted
	 * @return 				the byte array of this document or null if an error occurred
	 */
	public byte[] writeToByteArray(boolean prettyPrint) {
		byte[] result = null;
		String jsonStr = objToJsonStr(rootEl, prettyPrint);
		if (jsonStr == null) {
			return result;
		}
		
		result = jsonStr.getBytes(StandardCharsets.UTF_8);
		return result;		
	}
	

	/**
	 * adds a new json element to the passed parent
	 * @param parent 	the parent element to which a new element is added
	 * @param tag 		the tag name of the element that is added
	 */
	public JsonElement addElement(JsonElement parent, String tag) {
		JsonElement child = new JsonObject();
		parent.getAsJsonObject().add(tag, child);
		return child;
	}
	
	
	/**
	 * creates a new json element
	 * @return 		json element
	 */
	public JsonElement createElement() {
		return new JsonObject();
	}
	
	
	/**
	 * removes the element with the passed tag name from the parent
	 * @param parent 	parent element
	 * @param tag 		name of the element that is removed
	 */
	public void removeElementByName(JsonElement parent, String tag) {
		parent.getAsJsonObject().remove(tag);
	}

	
	/**
	 * adds a string to the passed json element with the passed tag name
	 * @param parent 	json element to which the string is added
	 * @param tag 		the tag name of the property that is added
	 * @param str 		the string value of the element
	 */
	public void addString(JsonElement parent, String tag, String str) {
		parent.getAsJsonObject().addProperty(tag, str);
	}
	
	
	/**
	 * adds a number to the passed json element with the passed tag name
	 * @param parent 	json element to which the number is added
	 * @param tag 		the tag name of the property that is added
	 * @param number 	the number value of the element
	 */
	public void addNumber(JsonElement parent, String tag, Number number) {
		parent.getAsJsonObject().addProperty(tag, number);
	}
	
	
	/**
	 * adds a boolean to the passed json element with the passed tag name
	 * @param parent 	json element to which the boolean is added
	 * @param tag 		the tag name of the property that is added
	 * @param bool 		the boolean value of the element
	 */
	public void addBoolean(JsonElement parent, String tag, Boolean bool) {
		parent.getAsJsonObject().addProperty(tag, bool);
	}
	
	
	/**
	 * adds an array to the passed json element with the passed tag name
	 * @param <T>
	 * @param parent 		the json element to which the array is added
	 * @param tag 			tag name of the property that is added
	 * @param valuesArr 	the array value of the element
	 */
	public <T> void addArray(JsonElement parent, String tag, List<T> valuesArr) {
		JsonArray jsonArr = new JsonArray();
		
		for (Object value : valuesArr) {
			if (value instanceof Boolean) {
				jsonArr.add((Boolean) value);
			 
			} else if (value instanceof Character) {
				jsonArr.add((Character) value);
			
			} else if (value instanceof JsonElement) {
				jsonArr.add((JsonElement) value);
			
			} else if (value instanceof Number) {
				jsonArr.add((Number) value);
			
			} else if (value instanceof String) {
				jsonArr.add((String) value);
			
			} else {
				logger.error("failed to add the values array, values can only be of instance Boolean, Character, JsonElement, Number or String");
				return;
			}
		}
		
		parent.getAsJsonObject().add(tag, jsonArr);
	}

	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												read methods 												//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * creates a json document from the passed file name
	 * @param fileName	 	path to the json-file
	 * @throws IOException 
	 */
	public void openJsonFromFile(String fileName) throws IOException {
		FileReader reader = new FileReader(new File(fileName));
		rootEl = parser.parse(reader);
		reader.close();	
	}

	
	/**
	 * reads an json document from the passed xml string, the encoding is set to utf-8
	 * @param jsonString 	JSON-String
	 */
	public void openJsonFromString(String jsonString) {
		rootEl = parser.parse(jsonString);
	}


	/**
	 * reads an json document from the passed byte array
	 * @param jsonBytes 	the byte array containing the json data (utf-8)
	 */
	public void openJsonFromByteArray(byte[] jsonBytes) {
		String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
		rootEl = parser.parse(jsonString);
	}


	/**
	 * returns the root element of the json document
	 * @return 		json element
	 */
	public JsonElement getRootElement() {
		return rootEl;
	}
	
	
	/**
	 * returns the child element with the passed tag name or null if the tag was not found
	 * @param parent 		parent element
	 * @param tag	 		tag name of the child element that is returned
	 * @return 				json element
	 */
	public JsonElement getChildElementByName(JsonElement parent, String tag) {
		JsonObject parentObj = parent.getAsJsonObject();
		return parentObj.get(tag);
	}
	
	
	
	/**
	 * returns all the child element with the passed tag name or null if the tag was not found
	 * @param parent 		parent element
	 * @param tag	 		tag name of the child element that is returned
	 * @return 				json element
	 */
	public JsonElement[] getChildElementsByName(JsonElement parent, String tag) {
		JsonObject parentObj = parent.getAsJsonObject();
		JsonArray jsonArr = parentObj.get(tag).getAsJsonArray();

		JsonElement[] jsonElArr = new JsonElement[jsonArr.size()];
		for (int i=0; i<jsonArr.size(); i++) {
			jsonElArr[i] = jsonArr.get(i);
		}

		return jsonElArr;	
	}	
	
	
	/**
	 * reads the string form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the string form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the string value of the passed json element
	 */
	public String readString(JsonElement element, String tag, String defaultVal) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			return jsonObj.get(tag).getAsString();

		} catch (Exception e) {
			logger.error("failed to read the string form tag " + tag);
			return defaultVal;
		}
	}
	
	
	/**
	 * reads the string array form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the string form
	 * @return 				the string array
	 */
	public String[] readStringArray(JsonElement element, String tag) {
		JsonObject jsonObj = element.getAsJsonObject();
		JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();

		String[] strArr = new String[jsonArr.size()];
		for (int i=0; i<jsonArr.size(); i++) {
			strArr[i] = jsonArr.get(i).getAsString();
		}

		return strArr;
	}
	
	
	/**
	 * reads the integer form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the integer form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the integer value of the passed json element
	 */
	public Integer readInt(JsonElement element, String tag, Integer defaultVal) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			return jsonObj.get(tag).getAsInt();

		} catch (Exception e) {
			logger.error("failed to read the int form tag " + tag);
			return defaultVal;
		}
	}
	
	
	/**
	 * reads the integer array form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the string form
	 * @return 				the integer array
	 */
	public int[] readIntArray(JsonElement element, String tag) {
		JsonObject jsonObj = element.getAsJsonObject();
		JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();

		int[] intArr = new int[jsonArr.size()];
		for (int i=0; i<jsonArr.size(); i++) {
			intArr[i] = jsonArr.get(i).getAsInt();
		}

		return intArr;
	}
	
	
	/**
	 * reads the long form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the long form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the long value of the passed json element
	 */
	public Long readLong(JsonElement element, String tag, Long defaultVal) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			return jsonObj.get(tag).getAsLong();

		} catch (Exception e) {
			logger.error("failed to read the long form tag " + tag);
			return defaultVal;
		}
	}
	
	
	/**
	 * reads the long array form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the string form
	 * @return 				the long array
	 */
	public long[] readLongArray(JsonElement element, String tag) {

			JsonObject jsonObj = element.getAsJsonObject();
			JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();
			
			long[] longArr = new long[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				longArr[i] = jsonArr.get(i).getAsLong();
			}
			
			return longArr;
	}
	
	
	/**
	 * reads the double form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the double form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the double value of the passed json element
	 */
	public Double readDouble(JsonElement element, String tag, Double defaultVal) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			return jsonObj.get(tag).getAsDouble();

		} catch (Exception e) {
			logger.error("failed to read the double form tag " + tag);
			return defaultVal;
		}
	}
	
	
	/**
	 * reads the double array form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the string form
	 * @return 				the long array or an empty array
	 */
	public double[] readDoubleArray(JsonElement element, String tag) {
		JsonObject jsonObj = element.getAsJsonObject();
		JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();

		double[] doubleArr = new double[jsonArr.size()];
		for (int i=0; i<jsonArr.size(); i++) {
			doubleArr[i] = jsonArr.get(i).getAsDouble();
		}

		return doubleArr;
	}

	
	/**
	 * reads the boolean form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the boolean form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the boolean value of the passed json element
	 */
	public Boolean readBoolean(JsonElement element, String tag, Boolean defaultVal) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			return jsonObj.get(tag).getAsBoolean();

		} catch (Exception e) {
			logger.error("failed to read the boolean form tag " + tag);
			return defaultVal;
		}
	}
	
	
	/**
	 * reads the boolean array form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the string form
	 * @return 				the long array
	 */
	public boolean[] readBooleanArray(JsonElement element, String tag) {
		JsonObject jsonObj = element.getAsJsonObject();
		JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();

		boolean[] booleanArr = new boolean[jsonArr.size()];
		for (int i=0; i<jsonArr.size(); i++) {
			booleanArr[i] = jsonArr.get(i).getAsBoolean();
		}

		return booleanArr;
	}
}
