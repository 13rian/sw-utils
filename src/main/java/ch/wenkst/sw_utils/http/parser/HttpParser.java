package ch.wenkst.sw_utils.http.parser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.http.HttpConstants;

public class HttpParser {
	private static final Logger logger = LoggerFactory.getLogger(HttpParser.class);


	private List<Byte> proccessedBytes = new ArrayList<>();
	protected String firstLine = "";
	protected Map<String, String> headerFields = null;
	private int contentLength = -1;
	private List<Byte> chunkBytes = new ArrayList<>();
	private List<Byte> bodyBytes = new ArrayList<>();
	private ParsingState state = ParsingState.NONE;
	

	// indicates if the server usded the chunked encoding (length \r\n chunk length \r\n chunk 0, 0 indicates the end of the received chunks)
	// the length is encoded as hex
	private boolean isChunked = false; 								
	private int chunkLength = -1;



	/**
	 * holds methods to parse a http message
	 */
	public HttpParser() {
		headerFields = new HashMap<>();  					
	}



	/**
	 * adds the passed byte array to the data already present in the parser
	 * @param bytesToAdd 		new bytes that belong to this request
	 */
	public void addData(byte[] bytesToAdd) {
		for (byte b : bytesToAdd) {
			parseByte(b);
		}	
	}


	/**
	 * processes one byte of the http message depending on the state of the parser
	 * @param b 		the byte to process
	 */
	private void parseByte(byte b) {
		switch (state) {

		case NONE : 
			processFirstLineByte(b);
			break;

		case FIRST_LINE_RECEIVED :
			processHeaderByte(b);
			break;

		case HEADER_RECEIVED :
			processBodyByte(b);
			break;

		case CHUNK_LENGTH_RECEIVED :
			processChunkedBodyByte(b);
			break;

		case BODY_RECEIVED : 
			processAfterBodyBytes(b);
			break;
		}
	}
	
	
	/**
	 * processes a byte of the first line in the http request
	 * @param b	byte to process
	 */
	private void processFirstLineByte(byte b) {
		if (b == HttpConstants.LINE_FEED_BYTE) {
			parseFirstLine();
		} else {
			proccessedBytes.add(b);
		}
	}
	
	
	private void parseFirstLine() {
		String line = processedBytesToString();
		extractFirstLineStatus(line);
		proccessedBytes.clear();
	}
	
	
	private String processedBytesToString() {
		byte[] bytesLine = Conversion.arrayListToByteArray(proccessedBytes);
		String line = new String(bytesLine, StandardCharsets.US_ASCII);
		return line.trim();
	}
	
	
	private void extractFirstLineStatus(String line) {
		if (line.contains(HttpConstants.PROTOCOL)) {
			firstLine = line;
			state = ParsingState.FIRST_LINE_RECEIVED;
		}
	}
	
	
	/**
	 * processes a byte in the header part of the http request
	 * @param b		byte to process
	 */
	private void processHeaderByte(byte b) {
		if (b == HttpConstants.LINE_FEED_BYTE) {
			parseHeaderLine();
		} else {
			proccessedBytes.add(b);
		}
	}
	
	
	private void parseHeaderLine() {
		String line = processedBytesToString();
		if (line.isEmpty()) {
			dealWithEmptyHeaderLine();	
		} else {
			extractHeaderField(line);
		}			
		proccessedBytes.clear(); 
	}
	
	
	private void dealWithEmptyHeaderLine() {
		extractContentLength();
		checkForChunkedMessages();
		if (contentLength == 0) {
			state = ParsingState.BODY_RECEIVED;
		} else {
			state = ParsingState.HEADER_RECEIVED;
		} 	
	}
	
	
	private void extractHeaderField(String line) {
		String[] parts = line.split(":");
		headerFields.put(parts[0].trim().toLowerCase(), parts[1].trim());
	}
	
	
	/**
	 * processes a byte in the http body
	 * @param b		the byte to process
	 */
	private void processBodyByte(byte b) {
		if (isChunked) {
			processChunkSizeByte(b);
		} else {
			processRegularBodyByte(b);
		}
	}
	
	
	private void processChunkSizeByte(byte b) {
		if (b == HttpConstants.LINE_FEED_BYTE) {			
			parseChunkSize();
		} else {
			proccessedBytes.add(b);
		}
	}
	
	
	private void parseChunkSize() {
		String line = processedBytesToString();
		if (!line.isEmpty()) {
			chunkLength = Integer.parseInt(line, 16);
			proccessedBytes.clear(); 
			updateStateFromCurrentChunkLength();
		}
	}
	
	
	private void updateStateFromCurrentChunkLength() {
		if (chunkLength == 0) {
			state = ParsingState.BODY_RECEIVED;			// a chunk length of 0 means that the complete body was received
		} else {
			state = ParsingState.CHUNK_LENGTH_RECEIVED;
		}
	}
	
	
	private void processRegularBodyByte(byte b) {
		bodyBytes.add(b);
		if (bodyBytes.size() == contentLength) {												
			state = ParsingState.BODY_RECEIVED;
		}
	}
	
	
	/**
	 * processes a body byte of a http request that uses the chunked encoding
	 * @param b		byte to process
	 */
	private void processChunkedBodyByte(byte b) {
		chunkBytes.add(b);
		if (chunkBytes.size() == chunkLength) {
			bodyBytes.addAll(chunkBytes);
			chunkBytes.clear();
			state = ParsingState.HEADER_RECEIVED;
		}
	}
	
	
	/**
	 * processes bytes that are received after the full body was received, this should not happen if 
	 * the http protocol is correctly followed
	 * @param b		the byte to process
	 */
	private void processAfterBodyBytes(byte b) {
		// in the chunked encoding the last chunk is ended with \r\n, therefore only print the error if there is some other data
		if (noNewLineByte(b)) {
			logger.error("more than the full http-content was received");
		}
	}
	
	
	private boolean noNewLineByte(byte b) {
		return b != HttpConstants.CARRIAGE_RETURN_BYTE && b != HttpConstants.LINE_FEED_BYTE;
	}	
	
	
	private void extractContentLength() {
		String contentLengthStr = headerFields.get("content-length");
		try {
			if (contentLengthStr != null) {
				contentLength = Integer.parseInt(contentLengthStr);
			}
		} catch (Exception e) {
			logger.error("error parsing the content length header field: ", e);
		}
	}
	
	
	private void checkForChunkedMessages() {
		if (contentLength < 0) {
			logger.debug("content length was not found in the header");

			isChunked = isChunked();
			if (!isChunked) {
				logger.debug("http message has no content length and is not chunked");
			}
		}
	}


	/**
	 * reads then transfer encoding from the header fields and returns true if it is set to chunked, if not present false it returned
	 * @return 		true id the transfer encoding is chunked, false otherwise
	 */
	private boolean isChunked() {
		String transferEncoding = headerFields.get("transfer-encoding");
		
		return (transferEncoding != null && transferEncoding.equals("chunked"));
	}


	/**
	 * returns true if the http message is complete and false otherwise
	 * @return 		true if the http message is complete, false otherwise
	 */
	public boolean isComplete() {
		return state == ParsingState.BODY_RECEIVED;
	}



	/**
	 * reinitializes the parser after a full request was received
	 */
	public void clearAfterFullMessage() {
		state = ParsingState.NONE;

		headerFields = new HashMap<>();  				
		contentLength = -1; 				
		bodyBytes = new ArrayList<>(); 
		isChunked = false;
		chunkLength = -1;
	}


	/**
	 * can be called when the content length is not defined and the server closed the connection to indicate that the 
	 * end of the http message was received.
	 * @return 		true if the request could be parsed (i. e. a at least header was received), false if not
	 */
	public boolean fullMessageReceived() {
		if (state == ParsingState.HEADER_RECEIVED) {
			proccessedBytes.clear();
			state = ParsingState.BODY_RECEIVED;
			return true;

		} else {
			logger.error("fullMessageReceived called but the state is not " + ParsingState.BODY_RECEIVED.toString());
			return false;
		}
	}
	
	
	/**
	 * retrieves the value of the passed header field
	 * @param prop 	the name of the header field property
	 * @return 		the value of the header field or null if the field is not present
	 */
	public String getHeaderField(String prop) {
		prop = prop.toLowerCase();
		return headerFields.get(prop);
	}


	public byte[] getBodyBytes() {
		return Conversion.arrayListToByteArray(bodyBytes);
	}

	public String getBodyStr() {
		return new String(getBodyBytes(), StandardCharsets.US_ASCII);
	}
}
