package ch.wenkst.sw_utils.file.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.file.XmlDoc;

public class XmlTestFiles {
	public XmlDoc xmlReadDoc;
	public String xmlWriteStr;
	
	
	public void setup() throws SAXException, IOException, ParserConfigurationException {
		String sep = File.separator;
		String xmlDir = System.getProperty("user.dir") + sep + "resource" + sep + "xml" + sep;
		
		String xmlReadFile = xmlDir + "xmlRead.xml";
		xmlReadDoc = new XmlDoc();
		xmlReadDoc.openXmlFromFile(xmlReadFile);
		
		String xmlWriteFile = xmlDir + "xmlWrite.xml";
		xmlWriteStr = FileUtils.readStrFromFile(xmlWriteFile);
	}
	
	
	public Element employeeElement() {
		Element rootEl = xmlReadDoc.getRootElement();
		Element employeesElement = xmlReadDoc.getChildElementByName(rootEl, "Employees");
		return xmlReadDoc.getChildElement(employeesElement, 0);
	}
}
