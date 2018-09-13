package ch.wenkst.sw_utils.file;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

public class XmlDocTest {
	private static XmlDoc xmlReadDoc = null;	// holds the xml file to read
	private static String xmlWriteStr; 			// holds the xml string to produce 	
	
	
	/**
	 * loads the resources that are needed for the test
	 */
	@BeforeAll
	public static void initializeExternalResources() {
		// load the xml file to read from the resources
		String xmlReadFile = System.getProperty("user.dir") + File.separator +
				"resource" + File.separator + 
				"xml" + File.separator +
				"xmlRead.xml";
		
		xmlReadDoc = new XmlDoc();
		xmlReadDoc.openXMLFromFile(xmlReadFile);
		
		// define the correct xml to write
		String xmlWriteFile = System.getProperty("user.dir") + File.separator +
				"resource" + File.separator + 
				"xml" + File.separator +
				"xmlWrite.xml";
		
		xmlWriteStr = FileHandler.readStrFromFile(xmlWriteFile);
	}
	
	
	/**
	 * parse xml
	 */
	@Test
	@DisplayName("xml read")
	public void xmlReadTest() {
		// get the root element
		Element rootElement = xmlReadDoc.getRootElement();   
		
		// read out a string
		Element employee = xmlReadDoc.getChildElement(rootElement, 0);
		Assertions.assertEquals("Freddy", xmlReadDoc.readString(employee, "Name", null), "read a string element");
		Assertions.assertEquals(null, xmlReadDoc.readString(employee, "InvalidTag", null), "read a non-existing string element");
		
		// read out an integer
		employee = xmlReadDoc.getChildElement(rootElement, 1);
		Assertions.assertEquals(22, (int) xmlReadDoc.readInteger(employee, "Age", 0), "read an integer element");
		Assertions.assertEquals(0, (int) xmlReadDoc.readInteger(employee, "InvalidTag", 0), "read an non-existing integer element");
		
		// read out a long
		employee = xmlReadDoc.getChildElement(rootElement, 2);
		Assertions.assertEquals(42000l, (long) xmlReadDoc.readLong(employee, "Salary", 0l), "read a long element");
		Assertions.assertEquals(0l, (long) xmlReadDoc.readLong(employee, "InvalidTag", 0l), "read an non-existing long element");
		
		// read out a double
		employee = xmlReadDoc.getChildElement(rootElement, 3);
		Assertions.assertEquals(56.7d, (double) xmlReadDoc.readDouble(employee, "Weight", 0d), "read an double element");
		Assertions.assertEquals(0d, (double) xmlReadDoc.readDouble(employee, "InvalidTag", 0d), "read an non-existing double element");
		
		// read an attribute
		Assertions.assertEquals("4", xmlReadDoc.readAttribute(employee, "ID", "invalid"), "read existing attribute");
		Assertions.assertEquals("invalid", xmlReadDoc.readAttribute(employee, "date", "invalid"), "read non-existing attribute");
	}
	
	
	
	/**
	 * write xml
	 */
	@Test
	@DisplayName("xml write")
	public void xmlWriteTest() {
		// create a new document
		XmlDoc xmlWriteDoc = new XmlDoc();
		xmlWriteDoc.createNewDocument();

		// create the root element
		Element xmlRoot = xmlWriteDoc.createRootElement("XMLTest", "http://www.sw-utils.ch");

		// create players
		Element players = xmlWriteDoc.createElement("Players");        // create a player element
		xmlWriteDoc.appendChildToParent(xmlRoot, players);         // append it to the root element

		// create player without attribute
		Element player1 = xmlWriteDoc.createElement("Player");				        // create a player element
		xmlWriteDoc.appendChildToParent(players, player1);                    	 	// append it to the root element

		Element name = xmlWriteDoc.createStrElement("name", "Paul Morphy"); 		// create text element
		Element age = xmlWriteDoc.createStrElement("age", "27"); 		  		// create text element

		// append the text elements
		xmlWriteDoc.appendChildToParent(player1, name);
		xmlWriteDoc.appendChildToParent(player1, age);


		// create player with attribute
		Element player2 = xmlWriteDoc.createElement("Player");            
		xmlWriteDoc.appendChildToParent(players, player2);                    		// append it to the root element

		Element name2 = xmlWriteDoc.createStrElement("name", "Bobby Fischer"); 	// create text element
		Element age2 = xmlWriteDoc.createStrElement("age", "31"); 		    	// create text element

		// append the text elements
		xmlWriteDoc.appendChildToParent(player2, name2);
		xmlWriteDoc.appendChildToParent(player2, age2);
		
		
		// add some attributes
		xmlWriteDoc.addAttribute(player1, "ID", "1");
		xmlWriteDoc.addAttribute(player2, "ID", "2");


		// write to a String and check if it matches the expectation
		String xmlString = xmlWriteDoc.writeToString(0);
		Assertions.assertEquals(xmlWriteStr, xmlString, "write xml string");
	}
}
