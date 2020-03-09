package ch.wenkst.sw_utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XmlDoc {
	private static final Logger logger = LoggerFactory.getLogger(XmlDoc.class);

	private Document document = null;   	// hold the xml document


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												write methods 												//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * handles an xml document
	 */
	public XmlDoc() {
		 
	}


	/**
	 * creates a new xml document and deletes the one that might already be present
	 */
	public void createNewDocument() {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder icBuilder;  		// api to get DOM document instance from xml

		try {
			icBuilder = icFactory.newDocumentBuilder();
			document = icBuilder.newDocument();          // create a new document
			// document.setXmlStandalone(true); 			// get rid of the standalone attribute in the declaration of the document			 		

		} catch (Exception e) {
			logger.error("failed creating root element: " + e.getMessage());
		}  
	}


	/**
	 * creates the root element of the xml document with a namespace and appends it to the document
	 * @param rootElementName 	name of the root element of the xml document
	 * @param namespace 		name of the namespace
	 * @return	 				the xml rootElement
	 */
	public Element createRootElement(String rootElementName, String namespace) {
		// create the element that defines the namespace to avoid element name conflicts between multiple elements
		Element rootElement = document.createElementNS(namespace, rootElementName);					
		document.appendChild(rootElement);

		return rootElement;
	}
	
	

	/**
	 * creates an element that can be appended to another element or other element can be appended to it 
	 * @param elementName		the name of the element to create
	 * @return	 				the newly created element
	 */
	public Element createElement(String elementName) {
		Element element = document.createElement(elementName);     // create a new element

		return element;
	}
	
	
	/**
	 * creates an element that can be appended to another element or other element can be appended to it 
	 * @param namespace 		the namespace of the element
	 * @param elementName		the name of the element to create
	 * @return	 				the newly created element
	 */
	public Element createElementNS(String namespace, String elementName) {
		Element element = document.createElementNS(namespace, elementName);     // create a new element

		return element;
	}
	
	
	
	/**
	 * adds an attribute to the passed element
	 * @param element 			element to which the attribute is added
	 * @param attributeName 	the name of the attribute
	 * @param attributeValue 	the value of the attribute
	 */
	public void addAttribute(Element element, String attributeName, String attributeValue) {
		// set the attribute if not empty
		if (attributeName != null && attributeValue != null) {
			element.setAttribute(attributeName, attributeValue);
		}
	}
	
	
	/**
	 * adds attributes to the passed element
	 * @param element 			element to which the attribute is added
	 * @param attributes 		map containing all the attributes to set
	 */
	public void addAttribute(Element element, Map<String, String> attributes) {
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    addAttribute(element, key, value);
		}
	}
	
	
	/**
	 * removes the attributes of element with the passed keys, if the attribute is not found nothing happens
	 * @param element 			element to which the attribute is added
	 * @param keys			 	the name of the attributes to remove
	 */
	public void removeAttribute(Element element, String... keys) {
		for (String key : keys) {
			element.removeAttribute(key);
		}
	}
	
	
	/**
	 * adds the text value to the passed element
	 * @param element 	element to which the text value is added
	 * @param value 	the value to add to the element
	 */
	public void addValue(Element element, String value) {
		Text text = document.createTextNode(value); 	// create the text node 
		element.appendChild(text);						// append the text node to the element
	}
	
	
	/**
	 * removes the child with the passed tag name
	 * @param element 	element form which 
	 * @param tagName 	the tag name of the child element that should be removed
	 */
	public void removeChildByTag(Element element, String tagName) {
		Element childElement = getChildElementByName(element, tagName);
		element.removeChild(childElement);
	}
	
	
	/**
	 * creates a new element that contains a text value
	 * @param elementName 		the name of the element to create
	 * @param textContent 		the content of the new text element
	 * @return 					xml element
	 */
	public Element createStrElement(String elementName, String textContent) {
		// create the new text element
		Element textElement = document.createElement(elementName);

		// add the text to the element
		Text text = document.createTextNode(textContent); 	// create the text node 
		textElement.appendChild(text);						// append the text node to the element

		return textElement;
	}


	/**
	 * writes the document to a file
	 * @param fileName 		the path of the file, in which the xml text is written
	 * @param indentAmount 	number of spaces for the indent
	 * @throws IOException 
	 * @throws TransformerException 
	 */
	public void writeToFile(String fileName, int indentAmount) throws IOException, TransformerException {			
		FileOutputStream fos = new FileOutputStream(fileName);
		writeToStream(fos, indentAmount);
		fos.close();
		logger.debug("xml file successfully written at: " + fileName);
	}


	/**
	 * writes the document to a byte array
	 * @param indentAmount 	 	number of spaces for the indent	
	 * @return 					the byte array representing the document
	 * @throws TransformerException 
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] writeToByteArray(int indentAmount) throws UnsupportedEncodingException, TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeToStream(baos, indentAmount);
		byte[] result = baos.toByteArray();

		return result;
	}


	/**
	 * writes the document to a String
	 * @param indentAmount	the number of spaces for the indent of the xml document
	 * @return	 			the xml-string
	 * @throws UnsupportedEncodingException 
	 * @throws TransformerException 
	 */
	public String writeToString(int indentAmount) throws UnsupportedEncodingException, TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeToStream(baos, indentAmount);
		String result = baos.toString("UTF8");

		return result;
	}


	/**
	 * writes the document to the passed output stream, care if a document was read in with an indent
	 * this method will not change it, use the removeIndent instead to get rid of it
	 * @param os 	 			the output stream to which the document is written		
	 * @param indentAmount 		number of spaces for the indent 
	 * @throws TransformerException 
	 * @throws UnsupportedEncodingException 
	 */
	public void writeToStream(OutputStream os, int indentAmount) throws TransformerException, UnsupportedEncodingException {
		DOMSource source = new DOMSource(document);                  	// create a new input source
		OutputStreamWriter fw = new OutputStreamWriter(os, "UTF8");
		StreamResult result = new StreamResult(fw);        				// holder to save the output

		// create the transformer
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		if (indentAmount < 1) {
			transformer.setOutputProperty(OutputKeys.INDENT, "no");

		} else {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");        // write line breaks in between elements (default indent is 0)
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");

			// set the indent amount
			String indent = String.valueOf(indentAmount);     				// get the indent amount as String
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
		}

		// transform the document to the xml-text that is written to the file
		transformer.transform(source, result);
	}


	/**
	 * removes the indent of an xml file that was read in
	 * @throws XPathExpressionException 
	 */
	public void removeIndent() throws XPathExpressionException {
		XPathFactory xfact = XPathFactory.newInstance();
		XPath xpath = xfact.newXPath();
		NodeList emptyNodes = (NodeList) xpath.evaluate("//text()[normalize-space(.) = '']", document, XPathConstants.NODESET);

		for (int i = 0; i < emptyNodes.getLength(); i++) {
			Node node = emptyNodes.item(i);
			node.getParentNode().removeChild(node);
		}
	}


	/**
	 * appends a child element to a parent element
	 * @param parent 	parent element
	 * @param child 	child element
	 */
	public void appendChildToParent(Element parent, Element child) {
		parent.appendChild(child);
	}	

	
	public Document getDoc() {
		return document;
	}  





	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 												read methods 												//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * creates an xml document from the passed file name
	 * @param fileName	 	path to the xml-file
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void openXmlFromFile(String fileName) throws SAXException, IOException, ParserConfigurationException {
		openXmlFromFile(fileName, false);
	}


	/**
	 * creates an xml document from the passed file name
	 * @param fileName	 		path to the xml-file
	 * @param isNamespaceAware	true if the parser is aware of the xml namespace
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void openXmlFromFile(String fileName, boolean isNamespaceAware) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(isNamespaceAware);			
		DocumentBuilder builder = factory.newDocumentBuilder();

		// load the input xml document from the passed file
		document = builder.parse(new File(fileName));	
	}



	/**
	 * creates an xml document from the passed xml string, the encoding is set to utf-8
	 * @param xmlString 	the string with the xml data
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void openXmlFromString(String xmlString) throws SAXException, IOException, ParserConfigurationException {
		byte[] xmlBytes = xmlString.getBytes(StandardCharsets.UTF_8);

		openXmlFromByteArray(xmlBytes, false);
	}


	/**
	 * creates an xml document from the passed xml string, the encoding is set to utf-8
	 * @param xmlString 		the string with the xml data
	 * @param isNamespaceAware	true if the parser is aware of the xml namespace
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void openXmlFromString(String xmlString, boolean isNamespaceAware) throws SAXException, IOException, ParserConfigurationException {
		byte[] xmlBytes = xmlString.getBytes(StandardCharsets.UTF_8);

		openXmlFromByteArray(xmlBytes, isNamespaceAware);
	}


	/**
	 * creates an xml document from the passed byte array
	 * @param xmlBytes 		the byte array containing the xml data (utf-8)
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void openXmlFromByteArray(byte[] xmlBytes) throws SAXException, IOException, ParserConfigurationException {
		openXmlFromByteArray(xmlBytes, false);
	}


	/**
	 * creates an xml document from the passed byte array
	 * @param xmlBytes 			the bytes representing the xml document
	 * @param isNamespaceAware	true if the parser is aware of the xml namespace
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void openXmlFromByteArray(byte[] xmlBytes, boolean isNamespaceAware) throws SAXException, IOException, ParserConfigurationException {
		InputStream is = new ByteArrayInputStream(xmlBytes);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(isNamespaceAware);
		DocumentBuilder builder = factory.newDocumentBuilder();

		// load the input xml document from the input stream
		document = builder.parse(is);
	}



	/**
	 * returns the root element of the xml document
	 * @return 		xml root element
	 */
	public Element getRootElement() {
		return document.getDocumentElement();
	}



	/**
	 * returns the nth child element of the passed parent element
	 * @param parent	 	the parent element
	 * @param nElement	 	the nth element
	 * @return 				the nth child element or null if there are not enough child elements
	 */
	public Element getChildElement(Element parent, int nElement) {
		// a node does not have to be an element, only return the elements
		NodeList children = parent.getChildNodes();
		int elementCount = 0; 								// the number of child elements found in the parent
		for (int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// check if the nth element was found
				if (elementCount == nElement) {
					return (Element) node;
				}

				// increase the counter
				elementCount++;
			}
		} 

		// the nth element was not found, return null
		logger.error("not enough child elements found");
		return null;		
	}


	/**
	 * returns the nth child element of the passed parent element
	 * @param parent	 	the parent element
	 * @return 				an array list of child elements,  or an empty array if the parent has no children
	 */
	public ArrayList<Element> getChildElements(Element parent) {
		// a node does not have to be an element, only return the elements
		// save all elements in a list
		NodeList children = parent.getChildNodes();
		ArrayList<Element> elements = new ArrayList<>();   // to save all elements
		for (int i=0; i<children.getLength(); i++) {
			Node node = children.item(i);
			// only add the elements to the array list
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elements.add((Element) node);
			}
		} 

		return elements; 		
	}


	/**
	 * returns the next sibling of the passed element or null if it is the last sibling
	 * @param sibling 	element of which the next sibling should be found
	 * @return 			xml element
	 */
	public Element getNextSibling(Element sibling) {
		Node node = sibling.getNextSibling();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return (Element)node;
			}
			node = node.getNextSibling();
		}
		return null;
	}

	/**
	 * returns the nth child element of with the passed tag name
	 * @param parent 		parent element
	 * @param tagName	 	tag name of the child element that is returned
	 * @return  			xml element
	 */
	public Element getChildElementByName(Element parent, String tagName) {
		return getChildElementByName(parent, tagName, 0);
	}


	/**
	 * returns the nth child element of with the passed tag name
	 * @param parent 		parent element
	 * @param tagName	 	tag name of the child element that is returned
	 * @param nElement 		nth element with tag name that is returned
	 * @return 				xml element
	 */
	public Element getChildElementByName(Element parent, String tagName, int nElement) {
		NodeList elements = parent.getElementsByTagName(tagName);   // get all elements with tag name

		if (elements.getLength() < nElement) {
			logger.error(nElement + ". element not found");
			return null;
		} else {
			return (Element)(elements.item(nElement));
		}   
	}


	/**
	 * returns the nth child element of with the passed tag name that is in the passed name space
	 * @param parent 		parent element
	 * @param namespace 	the name space of the tag, only works if constructor with isNamespaceAware true was used, if * name space is ignored 
	 * @param tagName	 	tag name of the child element that is returned
	 * @param nElement 		nth element with tag name that is returned
	 * @return 				xml element
	 */
	public Element getChildElementByNameNS(Element parent, String namespace, String tagName, int nElement) {
		// get all elements with tag name and a name space, this only works if the
		// DocumentBuilderFactory.setNamespaceAware(true) was used to build the document and the 
		// xml document has the name spaces that are used defined
		NodeList elements = parent.getElementsByTagNameNS(namespace, tagName);   


		if (elements.getLength() < nElement) {
			logger.error(nElement + ". element not found");
			return null;
		} else {
			return (Element) (elements.item(nElement));
		}   
	}


	/**
	 * returns the parent node of the passed child element
	 * @param child 	the child xml element
	 * @return 			the parent element
	 */
	public Element getParent(Element child) {
		Node parent = child.getParentNode();
		return (Element) parent;
	}



	/**
	 * returns the value of the passed element as a String
	 * @param element		the element of which the content is returned
	 * @return  			the string value of the passed xml element
	 */
	public String stringFromElement(Element element) {
		return element.getChildNodes().item(0).getNodeValue();
	}
	
	
	/**
	 * reads any value from an xml file, nested properties are separated by a .
	 * example: tag1.tag2.tag3, the root element is not part of the property name
	 * if there arrays are involved always the first element is used
	 * @param property 	the property to read out
	 * @return 			the value specified by the passed property string
	 */
	public String readAnyValue(String property) {
		Element element = readAnyElement(property);
		String value = stringFromElement(element);
		return value;
	}
	
	
	/**
	 * reads any element from an xml file, nested properties are separated by a .
	 * example: tag1.tag2.tag3, the root element is not part of the property name
	 * if there arrays are involved always the first element is used
	 * @param property 	the property to read out
	 * @return 			the element specified by the passed property string
	 */
	public Element readAnyElement(String property) {
		String[] tags = property.split("\\.");
		
		Element parentElement = getRootElement();
		Element childElement = null;
		for (String tag : tags) {
			// read the element by ignoring the name space
			childElement = getChildElementByNameNS(parentElement, "*", tag, 0);
			
			// try to read the element without ignoring the namespace
			if (childElement == null) {
				childElement = getChildElementByName(parentElement, tag, 0);
			}
			
			if (childElement == null) {
				logger.error("no element found for property " + property);
				return null;
			}
			
			parentElement = childElement;
		}
		return childElement;
	}


	/**
	 * reads the integer form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the integer form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the integer value of the passed xml element
	 */
	public Integer readInteger(Element element, String tag, Integer defaultVal) {
		try {
			Element childEl = getChildElementByName(element, tag);
			String value = stringFromElement(childEl);
			return Integer.parseInt(value);

		} catch (Exception e) {
			logger.error("failed to read the int form tag " + tag, e);
			return defaultVal;
		}
	}


	/**
	 * reads the long form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the integer form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the long value of the passed xml element
	 */
	public Long readLong(Element element, String tag, Long defaultVal) {
		try {
			Element childEl = getChildElementByName(element, tag);
			String value = stringFromElement(childEl);
			return Long.parseLong(value);

		} catch (Exception e) {
			logger.error("failed to read the long form tag " + tag, e);
			return defaultVal;
		}
	}


	/**
	 * reads the double form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the integer form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the float value of the passed xml element
	 */
	public Double readDouble(Element element, String tag, Double defaultVal) {
		try {
			Element childEl = getChildElementByName(element, tag);
			String value = stringFromElement(childEl);
			return Double.parseDouble(value);

		} catch (Exception e) {
			logger.error("failed to read the string form tag " + tag, e);
			return defaultVal;
		}
	}


	/**
	 * reads the string form an element
	 * @param element 		the parent element
	 * @param tag 			the name of the element to read the integer form
	 * @param defaultVal 	the default value if the value cannot be read
	 * @return 				the string value of the passed xml element
	 */
	public String readString(Element element, String tag, String defaultVal) {
		try {
			Element childEl = getChildElementByName(element, tag);
			String value = stringFromElement(childEl);
			return value;

		} catch (Exception e) {
			logger.error("failed to read the string form tag " + tag, e);
			return defaultVal;
		}
	}
	
	/**
	 * reads the attribute form the passed element
	 * @param element 		the xml element
	 * @param name 			the name of the attribute to read
	 * @param defaultVal 	the default value if the attribute is not set
	 * @return 				the value of the attribute
	 */
	public String readAttribute(Element element, String name, String defaultVal) {
		String value = element.getAttribute(name);
		if (value.isEmpty()) {
			return defaultVal;
		} else {
			return value;
		}
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 											utility methods 												   //
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * removes the garbage at the end of the xml string
	 * @param xmlString: 	the xml string from which the garbage should be removed
	 * @return: 			xml string without garbage
	 */
	public static String removeTrailingGarbage(String xmlString) {
		// find the position of the first tag
		int index = xmlString.indexOf("?>") + 1;
		while (xmlString.charAt(index) != '<') {
			index++;
		}
		index++; 	// add one to the index for the start tag

		// setup the first tag name
		String firstTagName = "";
		char xmlChar = xmlString.charAt(index);
		while (xmlChar != ' ' && xmlChar != '>') {
			firstTagName = firstTagName + xmlString.charAt(index);
			index++;
			xmlChar = xmlString.charAt(index);
		}

		// find the index of the last tag
		int xmlEndIndex = xmlString.lastIndexOf(firstTagName) + firstTagName.length() + 1;

		// remove the garbage at the end
		String cleanedXml = xmlString.substring(0, xmlEndIndex);
		return cleanedXml;
	}
}

