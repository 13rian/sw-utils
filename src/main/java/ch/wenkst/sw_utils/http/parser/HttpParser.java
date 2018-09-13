package ch.wenkst.sw_utils.http.parser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.wenkst.sw_utils.conversion.Conversion;

public class HttpParser {
	private static Logger logger = LogManager.getLogger(HttpParser.class);

	// possible states of the http parser 
	public enum State {NONE, FIRST_LINE_RECEIVED, HEADER_RECEIVED, CHUNK_LENGTH_RECEIVED, BODY_RECEIVED};
	private State state = State.NONE;

	private static final byte CARRIAGE_RETURN_BYTE = (byte) '\r'; 	// define the newline byte
	private static final byte LINE_FEED_BYTE = (byte) '\n'; 		// define the newline byte
	private static final String PROTOCOL = "HTTP/1.1"; 				// defines the http protocol

	private ArrayList<Byte> proccessedBytes = new ArrayList<>();	// holds the processed bytes of the http message, it is used as a temporary buffer
	protected String firstLine = ""; 								// the first line of the http message
	protected ArrayList<String> headerLines = null; 				// holds all lines of the header
	private int contentLength = -1; 								// the content length header property
	private ArrayList<Byte> chunkBytes = new ArrayList<>(); 		// holds the bytes of one body chunk
	private ArrayList<Byte> bodyBytes = new ArrayList<>(); 			// holds the bytes of the body

	// indicates if the server usded the chunked encoding (length \r\n chunk length \r\n chunk 0, 0 indicates the end of the received chunks)
	// the length is encoded as hex
	private boolean isChunked = false; 								
	private int chunkLength = -1; 									// length of a body chunk if the transfer encoding  is set to chunked



	/**
	 * holds methods to parse a http message
	 */
	public HttpParser() {
		headerLines = new ArrayList<>();  					
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

		// the first line was not received, wait for the protocol
		case NONE : 
			if (b != LINE_FEED_BYTE) {
				// the byte is not the new line, add it to the readBytes buffer
				proccessedBytes.add(b);

			} else {
				// extract the line
				byte[] bytesLine = Conversion.arrayListToByteArray(proccessedBytes);
				String line = new String(bytesLine, StandardCharsets.US_ASCII);
				line = line.trim();

				// check if the the line contains the protocol
				if (line.contains(PROTOCOL)) {
					firstLine = line;
					state = State.FIRST_LINE_RECEIVED;
				}

				// clear the buffer
				proccessedBytes.clear();
			}

			break;

		case FIRST_LINE_RECEIVED :
			if (b != LINE_FEED_BYTE) {
				// the byte is not the new line, add it to the readBytes buffer
				proccessedBytes.add(b);

			} else {
				// extract the line
				byte[] bytesLine = Conversion.arrayListToByteArray(proccessedBytes);
				String line = new String(bytesLine, StandardCharsets.US_ASCII);
				line = line.trim();

				if (line.isEmpty()) {
					// empty line found, extract the content length
					extractContentLength();

					// change the state of the parser
					if (contentLength == 0) {
						state = State.BODY_RECEIVED;
					} else {
						state = State.HEADER_RECEIVED;
					} 							

				} else {
					// line is not empty, add it to the header lines list
					headerLines.add(line);
				}			

				// clear the buffer
				proccessedBytes.clear(); 	
			}

			break;

		// the header is received, extract the body
		case HEADER_RECEIVED :
			if (isChunked) {
				// handle chunked http messages, read out the length of the first chunk
				if (b != LINE_FEED_BYTE) {
					// the byte is not the new line, add it to the readBytes buffer
					proccessedBytes.add(b);

				} else {
					// extract the line
					byte[] bytesLine = Conversion.arrayListToByteArray(proccessedBytes);
					String line = new String(bytesLine, StandardCharsets.US_ASCII);
					line = line.trim();

					// ignore empty lines that occur after each chunk
					if (!line.isEmpty()) {

						// extract the chunk length, which is hexadecimal
						chunkLength = Integer.parseInt(line, 16);

						// clear the buffer
						proccessedBytes.clear(); 

						// change the state
						if (chunkLength == 0) {
							// a chunk length of 0 means that the complete body was received
							state = State.BODY_RECEIVED;
						} else {
							// not the full body is parsed, continue to read out the bytes
							state = State.CHUNK_LENGTH_RECEIVED;
						}
					}

				}


			} else {
				// handle non-chunked http messages
				bodyBytes.add(b);

				// check if the whole body was receives
				if (bodyBytes.size() == contentLength ) {												
					state = State.BODY_RECEIVED;
				}
			}

			break;

		case CHUNK_LENGTH_RECEIVED :
			chunkBytes.add(b);

			// check if the whole chunk was received
			if (chunkBytes.size() == chunkLength) {
				// adds all chunk bytes to the body bytes
				bodyBytes.addAll(chunkBytes);
				chunkBytes.clear();
				state = State.HEADER_RECEIVED;
			}

			break;


		// the full body was received, log if more is received
		case BODY_RECEIVED : 
			// in the chunked encoding the last chunk is ended with \r\n, therefore only print the error if there is some other data
			if (b != CARRIAGE_RETURN_BYTE && b != LINE_FEED_BYTE) {
				logger.error("more than the full http-content was received");
			}

			break;
		}
	}




	/**
	 * extracts the content length from the header properties
	 */
	private void extractContentLength() {
		for (String line : headerLines) {
			if (line.contains("Content-Length:")) {
				String[] parts = line.split(":");
				String strLength = parts[1].trim();
				contentLength = Integer.parseInt(strLength);
			}
		}

		// log an error if the content length is missing
		if (contentLength < 0) {
			logger.debug("content length was not found in the header");

			// test if the message is chunked
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
		for (String line : headerLines) {
			if (line.contains("Transfer-Encoding: chunked")) {
				return true;
			}
		}

		return false;
	}


	/**
	 * returns true if the http message is complete and false otherwise
	 * @return 		ture if the http message is complete, false otherwise
	 */
	public boolean isComplete() {
		if (state == State.BODY_RECEIVED) {
			return true;
		} else {
			return false;
		}
	}



	/**
	 * reinitializes the parser after a full request was received
	 */
	public void clearAfterFullMessage() {
		state = State.NONE;

		headerLines = new ArrayList<>();  				
		contentLength = -1; 				
		bodyBytes = new ArrayList<>(); 
		isChunked = false;
		chunkLength = -1;
	}


	/**
	 * can be called when the content length is not defined and teh server closed the connection to indicate that the 
	 * end of the http message was received.
	 */
	public void fullMessageReceived() {
		if (state == State.HEADER_RECEIVED) {
			proccessedBytes.clear();

			state = State.BODY_RECEIVED;

		} else {
			logger.error("fullMessageReceived called but the state is not HEADER_RECEIVED");
		}

	}


	public byte[] getBodyBytes() {
		byte[] result = Conversion.arrayListToByteArray(bodyBytes);
		return result;
	}

	public String getBodyStr() {
		return new String(getBodyBytes(), StandardCharsets.US_ASCII);
	}


}
