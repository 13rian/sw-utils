package ch.wenkst.sw_utils.file.json;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;

import com.google.gson.JsonElement;

import ch.wenkst.sw_utils.file.JsonDoc;

public class JsonTestFiles {
	public JsonDoc jsonReadDoc;
	
	
	public void setup() throws IOException {
		String sep = File.separator;
		String jsonDir = System.getProperty("user.dir") + sep + "resource" + sep + "json" + sep;
		
		String jsonReadFile = jsonDir + "jsonRead.json";
		jsonReadDoc = new JsonDoc();
		jsonReadDoc.openJsonFromFile(jsonReadFile);
	}
	
	
	public JsonElement rootElementForRead() {
		return jsonReadDoc.getRootElement();
	}
	
	
	public JsonDoc newJsonDoc() {
		JsonDoc jsonDoc = new JsonDoc();
		jsonDoc.createNewDocument();
		return jsonDoc;
	}
	
	
	public void documentCorrect(JsonDoc jsonDoc, String expected) {
		boolean prettyPrint = false;
		String jsonString = jsonDoc.writeToString(prettyPrint);
		Assertions.assertEquals(expected, jsonString);
	}
}
