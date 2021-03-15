package ch.wenkst.sw_utils.file.xml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ch.wenkst.sw_utils.file.XmlDoc;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class XmlDocTest {
	private XmlTestFiles testFiles;
		
	
	@BeforeAll
	public void setupTestFiles() throws SAXException, IOException, ParserConfigurationException {
		testFiles = new XmlTestFiles();
		testFiles.setup();
	}
	
	
	@Test
	public void readString() {
		Element employee = testFiles.employeeElement();
		Assertions.assertEquals("Freddy", testFiles.xmlReadDoc.readString(employee, "Name", "wrongName"));
		Assertions.assertEquals("defaultString", testFiles.xmlReadDoc.readString(employee, "InvalidTag", "defaultString"));
	}
	
	
	@Test
	public void readInteger() {
		Element employee = testFiles.employeeElement();
		Assertions.assertEquals(30, (int) testFiles.xmlReadDoc.readInteger(employee, "Age", 15));
		Assertions.assertEquals(0, (int) testFiles.xmlReadDoc.readInteger(employee, "InvalidTag", 0));
	}
	
	
	@Test
	public void readLong() {
		Element employee = testFiles.employeeElement();
		Assertions.assertEquals(45000l, (long) testFiles.xmlReadDoc.readLong(employee, "Salary", 0l));
		Assertions.assertEquals(0l, (long) testFiles.xmlReadDoc.readLong(employee, "InvalidTag", 0l));
	}
	
	
	@Test
	public void readDouble() {
		Element employee = testFiles.employeeElement();
		Assertions.assertEquals(65.4d, (double) testFiles.xmlReadDoc.readDouble(employee, "Weight", 0d));
		Assertions.assertEquals(0d, (double) testFiles.xmlReadDoc.readDouble(employee, "InvalidTag", 0d));
	}
	
	
	@Test
	public void readAttribute() {
		Element employee = testFiles.employeeElement();
		Assertions.assertEquals("1", testFiles.xmlReadDoc.readAttribute(employee, "ID", "invalid"));
		Assertions.assertEquals("invalid", testFiles.xmlReadDoc.readAttribute(employee, "date", "invalid"));
	}
	
	
	@Test
	public void readNestedValuesFromRoot() {
		Assertions.assertEquals("CryptoLeaks", testFiles.xmlReadDoc.readAnyValue("Name"));
		Assertions.assertEquals("Switzerland", testFiles.xmlReadDoc.readAnyValue("Location.Country"));
		Assertions.assertEquals("Freddy", testFiles.xmlReadDoc.readAnyValue("Employees.Employee.Name"));
	}
	
	
	@Test
	public void readAnyElementsFromRoot() {
		List<Element> buildings = testFiles.xmlReadDoc.getAnyElements("Location.Buildings");
		Assertions.assertEquals("A5", testFiles.xmlReadDoc.stringFromElement(buildings.get(1)));
	}
	
	
	@Test
	public void readNestedValueFromAnyElement() {
		Element rootElement = testFiles.xmlReadDoc.getRootElement();
		Element locationElement = testFiles.xmlReadDoc.getChildElementByName(rootElement, "Location");
		Assertions.assertEquals("8400", testFiles.xmlReadDoc.readAnyValue(locationElement, "Address.ZIP"));
	}
	
	
	@Test
	public void readAnyElementsFromAnyelement() {
		Element rootElement = testFiles.xmlReadDoc.getRootElement();
		Element locationElement = testFiles.xmlReadDoc.getChildElementByName(rootElement, "Location");
		List<Element> buildings = testFiles.xmlReadDoc.getAnyElements(locationElement, "Buildings");
		Assertions.assertEquals("A5", testFiles.xmlReadDoc.stringFromElement(buildings.get(1)));
	}
	

	
	@Test
	public void xmlWriteTest() throws UnsupportedEncodingException, TransformerException, ParserConfigurationException {
		// create a new document
		XmlDoc xmlWriteDoc = new XmlDoc();
		xmlWriteDoc.createNewDocument();

		// create the root element
		Element xmlRoot = xmlWriteDoc.createRootElement("XMLTest", "http://www.sw-utils.ch");

		// create players
		Element players = xmlWriteDoc.createElement("Players");
		xmlWriteDoc.appendChildToParent(xmlRoot, players);

		// create player without attribute
		Element player1 = xmlWriteDoc.createElement("Player");
		xmlWriteDoc.appendChildToParent(players, player1);
		
		Element name = xmlWriteDoc.createStrElement("name", "Paul Morphy");
		Element age = xmlWriteDoc.createStrElement("age", "27");

		// append the text elements
		xmlWriteDoc.appendChildToParent(player1, name);
		xmlWriteDoc.appendChildToParent(player1, age);


		// create player with attribute
		Element player2 = xmlWriteDoc.createElement("Player");            
		xmlWriteDoc.appendChildToParent(players, player2);

		Element name2 = xmlWriteDoc.createStrElement("name", "Bobby Fischer");
		Element age2 = xmlWriteDoc.createStrElement("age", "31");

		// append the text elements
		xmlWriteDoc.appendChildToParent(player2, name2);
		xmlWriteDoc.appendChildToParent(player2, age2);
		
		
		// add some attributes
		xmlWriteDoc.addAttribute(player1, "ID", "1");
		xmlWriteDoc.addAttribute(player2, "ID", "2");


		// write to a String and check if it matches the expectation
		String xmlString = xmlWriteDoc.writeToString(0);
		Assertions.assertEquals(testFiles.xmlWriteStr, xmlString);
	}
}
