package ch.wenkst.sw_utils.file;

import java.io.File;
import java.io.FileReader;
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
		// create a new json parser
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
	 * @return 				true if the files was successfully written, false if an error occurred
	 */
	public static boolean objToJsonFile(String filePath, Object obj, boolean prettyPrint) {
		try {
			String jsonSring = objToJsonStr(obj, prettyPrint);
			if (jsonSring == null) {
				return false;
			}
			
			// write the string to a json file
			Path path = Paths.get(filePath);
			Files.write(path, jsonSring.getBytes(StandardCharsets.UTF_8));
			return true;

		} catch (Exception e) {
			logger.error("failed to write the object to a json file with path " + filePath, e);
			return false;
		}
	}
	
	
	/**
	 * converts a java object to a json string
	 * @param obj 			the object that is converted to a json string
	 * @param prettyPrint 	true if the json should be well formatted
	 * @return 				the json string that represents the passed object or null if an error occurred
	 */
	public static String objToJsonStr(Object obj, boolean prettyPrint) {
		Gson gson = null;
		try {
			if (prettyPrint) {
				gson = new GsonBuilder().setPrettyPrinting().create();
			} else {
				gson = new Gson();
			}
			return gson.toJson(obj);
			
		} catch (Exception e) {
			logger.error("failed to convert the passed object to a json string: ", e);
			return null;
		}
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
	 * @return 				true if the files was successfully written, false if an error occurred
	 */
	public boolean writeToFile(String filePath, boolean prettyPrint) {
		return objToJsonFile(filePath, rootEl, prettyPrint);
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
	 * @return 			the child element or null if an error occurred
	 */
	public JsonElement addElement(JsonElement parent, String tag) {
		try {
			JsonElement child = new JsonObject(); 				// create a new json object
			parent.getAsJsonObject().add(tag, child); 			// add the element to the passed parent
			return child;
		
		} catch (Exception e) {
			logger.error("failed to add a new element with tag name " + tag, e);
			return null;
		}
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
		try {
			parent.getAsJsonObject().remove(tag);
		
		} catch (Exception e) {
			logger.error("failed to remove the element with tag name " + tag, e);
		}
	}
	
	
	/**
	 * adds a string to the passed json element with the passed tag name
	 * @param parent 	json element to which the string is added
	 * @param tag 		the tag name of the property that is added
	 * @param str 		the string value of the element
	 */
	public void addString(JsonElement parent, String tag, String str) {
		try {
			parent.getAsJsonObject().addProperty(tag, str);
		} catch (Exception e) {
			logger.error("failed to add the string to the parent with tag name: " + tag, e);
		}
	}
	
	
	/**
	 * adds a number to the passed json element with the passed tag name
	 * @param parent 	json element to which the number is added
	 * @param tag 		the tag name of the property that is added
	 * @param number 	the number value of the element
	 */
	public void addNumber(JsonElement parent, String tag, Number number) {
		try {
			parent.getAsJsonObject().addProperty(tag, number);
		} catch (Exception e) {
			logger.error("failed to add the number to the parent with tag name: " + tag, e);
		}
	}
	
	
	/**
	 * adds a boolean to the passed json element with the passed tag name
	 * @param parent 	json element to which the boolean is added
	 * @param tag 		the tag name of the property that is added
	 * @param bool 		the boolean value of the element
	 */
	public void addBoolean(JsonElement parent, String tag, Boolean bool) {
		try {
			parent.getAsJsonObject().addProperty(tag, bool);
		} catch (Exception e) {
			logger.error("failed to add the boolean to the parent with tag name: " + tag, e);
		}
	}
	
	
	/**
	 * adds an array to the passed json element with the passed tag name
	 * @param parent 		the json element to which the array is added
	 * @param tag 			tag name of the property that is added
	 * @param valuesArr 	the array value of the element
	 */
	public void addArray(JsonElement parent, String tag, List<Object> valuesArr) {
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
	 * @return	 			true if successfully read
	 */
	public boolean openJsonFromFile(String fileName) {
		try {
			FileReader reader = new FileReader(new File(fileName)); 	// open the file
			rootEl = parser.parse(reader); 								// get the json root element
			
			reader.close();
			return true;

		} catch (Exception e) {
			logger.error("Exception reading json file: " + e);
			return false;
		}		
	}

	
	/**
	 * reads an json document from the passed xml string, the encoding is set to utf-8
	 * @param jsonString 	JSON-String
	 * @return	 			true if successfully read
	 */
	public boolean openJsonFromString(String jsonString) {
		try {
			rootEl = parser.parse(jsonString); 					// get the json root element
			return true;

		} catch (Exception e) {
			logger.error("Exception reading json string: " + e);
			return false;
		}		
	}


	/**
	 * reads an json document from the passed byte array
	 * @param jsonBytes 	the byte array containing the json data (utf-8)
	 * @return	 			true if successfully read
	 */
	public boolean openJsonFromByteArray(byte[] jsonBytes) {
		try {
			String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
			rootEl = parser.parse(jsonString); 									// get the json root element
			return true;

		} catch (Exception e) {
			logger.error("Exception reading json byte array: " + e);
			return false;
		}	
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
		try {
			JsonObject parentObj = parent.getAsJsonObject();
			return parentObj.get(tag);
			
		} catch (Exception e) {
			logger.error("failed to read the child element of the tag " + tag, e);
			return null;
		}
	}
	
	
	
	/**
	 * returns all the child element with the passed tag name or null if the tag was not found
	 * @param parent 		parent element
	 * @param tag	 		tag name of the child element that is returned
	 * @return 				json element
	 */
	public JsonElement[] getChildElementsByName(JsonElement parent, String tag) {
		try {
			JsonObject parentObj = parent.getAsJsonObject();
			JsonArray jsonArr = parentObj.get(tag).getAsJsonArray();
			
			JsonElement[] jsonElArr = new JsonElement[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				jsonElArr[i] = jsonArr.get(i);
			}
			
			return jsonElArr;	
			
		} catch (Exception e) {
			logger.error("failed to read the child elements of the tag " + tag, e);
			return null;
		}
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
	 * @return 				the string array or an empty array if an error occurred
	 */
	public String[] readStringArray(JsonElement element, String tag) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();
			
			String[] strArr = new String[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				strArr[i] = jsonArr.get(i).getAsString();
			}
			
			return strArr;

		} catch (Exception e) {
			logger.error("failed to read the string array form tag " + tag);
			return new String[0];
		}
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
	 * @return 				the integer array or an empty array if an error occurred
	 */
	public int[] readIntArray(JsonElement element, String tag) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();
			
			int[] intArr = new int[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				intArr[i] = jsonArr.get(i).getAsInt();
			}
			
			return intArr;

		} catch (Exception e) {
			logger.error("failed to read the int array form tag " + tag);
			return new int[0];
		}
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
	 * @return 				the long array or an empty array if an error occurred
	 */
	public long[] readLongArray(JsonElement element, String tag) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();
			
			long[] longArr = new long[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				longArr[i] = jsonArr.get(i).getAsLong();
			}
			
			return longArr;

		} catch (Exception e) {
			logger.error("failed to read the long array form tag " + tag);
			return new long[0];
		}
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
	 * @return 				the long array or an empty array if an error occurred
	 */
	public double[] readDoubleArray(JsonElement element, String tag) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();
			
			double[] doubleArr = new double[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				doubleArr[i] = jsonArr.get(i).getAsDouble();
			}
			
			return doubleArr;

		} catch (Exception e) {
			logger.error("failed to read the double array form tag " + tag);
			return new double[0];
		}
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
	 * @return 				the long array or an empty array if an error occurred
	 */
	public boolean[] readBooleanArray(JsonElement element, String tag) {
		try {
			JsonObject jsonObj = element.getAsJsonObject();
			JsonArray jsonArr = jsonObj.get(tag).getAsJsonArray();
			
			boolean[] booleanArr = new boolean[jsonArr.size()];
			for (int i=0; i<jsonArr.size(); i++) {
				booleanArr[i] = jsonArr.get(i).getAsBoolean();
			}
			
			return booleanArr;

		} catch (Exception e) {
			logger.error("failed to read the boolean array form tag " + tag);
			return new boolean[0];
		}
	}
}
