package ch.wenkst.sw_utils.http;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.http.parser.HttpRequestParser;
import ch.wenkst.sw_utils.http.parser.HttpResponseParser;

public class HttpParserTest {
	private static String crlf = "\r\n"; 			// carriage return line feed to indicate new lines
	
	// http request
	private static String httpRequest;  			// non-chunked http request
	private static String reqBody; 					// body of the non-chunked http-request
	private static String httpRequestChunked; 		// chunked http request
	private static String reqBodyChunked; 			// body of the chunked http request
	
	// http response
	private static String httpResponse;  			// non-chunked http response
	private static String respBody; 				// body of the non-chunked http-response
	private static String httpResponseNoLength; 	// http response with no length in the header
	private static String httpResponseChunked; 		// chunked http response
	private static String respBodyChunked; 			// body of the chunked http response
	
	
	
	/**
	 * defines the http requests and responses for the test
	 */
	@BeforeAll
	public static void prepareHttpRequests() {
		// non-chunked http request
		reqBody = "{\"value\": 765}";
		
		httpRequest = new StringBuilder()
				.append("POST /test HTTP/1.1").append(crlf)
				.append("Host: localhost:8001").append(crlf)
				.append("User-Agent: AHC/1.0").append(crlf)
				.append("Connection: keep-alive").append(crlf)
				.append("Accept: */*").append(crlf)
				.append("Content-Type: application/json").append(crlf)
				.append("Content-Length: 14").append(crlf)
				.append(crlf)
				.append(reqBody)
				.toString();
		
		// chunked http request
		reqBodyChunked = new StringBuilder()
				.append("c").append(crlf)
				.append("interesting ").append(crlf)
				.append("4").append(crlf) 
				.append("text").append(crlf) 
				.append("0")
				.append(crlf)
				.toString();
			
		httpRequestChunked = new StringBuilder()
				.append("PUT /test HTTP/1.1").append(crlf)
				.append("Host: localhost:8001").append(crlf)
				.append("User-Agent: AHC/1.0").append(crlf)
				.append("Connection: keep-alive").append(crlf)
				.append("Accept: */*").append(crlf)
				.append("Transfer-Encoding: chunked").append(crlf)
				.append("Content-Type: text/plain").append(crlf)
				.append(crlf)
				.append(reqBodyChunked)
				.toString();
		
		
		// not chunked http response
		respBody = "{\"params\":\"d81\",\"command\":\"testpost\"}";
	
		httpResponse = new StringBuilder()
				.append("HTTP/1.1 404 Not Found").append(crlf)
				.append("Access-Control-Allow-Origin: *").append(crlf)
				.append("Access-Control-Allow-Headers: origin, content-type, accept, authorization").append(crlf)
				.append("Access-Control-Allow-Credentials: true").append(crlf)
				.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD").append(crlf)
				.append("Content-Type: text/plain").append(crlf)
				.append("Content-Length: 37").append(crlf)
				.append(crlf)
				.append(respBody)
				.toString();
		
		
		// not chunked http response with no length
		httpResponseNoLength = new StringBuilder()
				.append("HTTP/1.1 404 Not Found").append(crlf)
				.append("Access-Control-Allow-Origin: *").append(crlf)
				.append("Access-Control-Allow-Headers: origin, content-type, accept, authorization").append(crlf)
				.append("Access-Control-Allow-Credentials: true").append(crlf)
				.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD").append(crlf)
				.append("Content-Type: text/plain").append(crlf)
				.append(crlf)
				.append(respBody)
				.toString();
		
		
		// chunked http response 
		respBodyChunked = new StringBuilder()
				.append("1f").append(crlf)
				.append("absolutely super stunning body ").append(crlf)
				.append("20").append(crlf) 
				.append("that is sent in the chunk format").append(crlf) 
				.append("0")
				.append(crlf)
				.toString();
		
		httpResponseChunked = new StringBuilder()
				.append("HTTP/1.1 200 OK").append(crlf)
				.append("Access-Control-Allow-Origin: *").append(crlf)
				.append("Access-Control-Allow-Headers: origin, content-type, accept, authorization").append(crlf)
				.append("Access-Control-Allow-Credentials: true").append(crlf)
				.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD").append(crlf)
				.append("Transfer-Encoding: chunked").append(crlf)
				.append("Content-Type: text/plain").append(crlf)
				.append(crlf)
				.append(respBodyChunked)
				.append(crlf)
				.toString();
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// 									http request 									   //
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * parse a full http request with a body that is not chunked
	 */
	@Test
	@DisplayName("http request")
	public void httpRequest() {
		HttpRequestParser reqParser = new HttpRequestParser();
				
		// add the data to the parser
		reqParser.addData(httpRequest.getBytes(StandardCharsets.UTF_8));
		
		// test the parsed values
		Assertions.assertTrue(reqParser.isComplete(), "http request is complete");
		Assertions.assertEquals(reqParser.getHttpMethod(), "POST", "read the http method");
		Assertions.assertEquals(reqParser.getRequestURI(), "/test", "read the request uri");
		Assertions.assertEquals(reqParser.getBodyStr(), reqBody, "read the body");
	}
	
	
	/**
	 * parse a http request packet by packet with a body that is not chunked
	 */
	@Test
	@DisplayName("packet http request")
	public void httpRequestChunked() {
		HttpRequestParser reqParser = new HttpRequestParser();
		
		// define the 3 chunks that are fed into the parser
		byte[] chunk1 = httpRequest.substring(0, 98).getBytes(StandardCharsets.UTF_8);
		byte[] chunk2 = httpRequest.substring(98, 150).getBytes(StandardCharsets.UTF_8);
		byte[] chunk3 = httpRequest.substring(150).getBytes(StandardCharsets.UTF_8);
				
		// feed the 3 chunks into the parser
		reqParser.addData(chunk1);
		Assertions.assertFalse(reqParser.isComplete(), "is complete after first chunk");
		
		reqParser.addData(chunk2);
		Assertions.assertFalse(reqParser.isComplete(), "is complete after second chunk");
		
		reqParser.addData(chunk3);
		Assertions.assertTrue(reqParser.isComplete(), "is complete after third chunk");
		
		
		// test the parsed values
		Assertions.assertTrue(reqParser.isComplete(), "http request is complete");
		Assertions.assertEquals(reqParser.getHttpMethod(), "POST", "read the http method");
		Assertions.assertEquals(reqParser.getRequestURI(), "/test", "read the request uri");
		Assertions.assertEquals(reqParser.getBodyStr(), reqBody, "read the body");
	}
	
	
	/**
	 * parse a http request with a body that is chunked
	 */
	@Test
	@DisplayName("http chunked request")
	public void httpPacketRequest() {
		HttpRequestParser reqParser = new HttpRequestParser();
				
		// add the data to the parser
		reqParser.addData(httpRequestChunked.getBytes(StandardCharsets.UTF_8));
		
		// test the parsed values
		Assertions.assertTrue(reqParser.isComplete(), "http request is complete");
		Assertions.assertEquals(reqParser.getHttpMethod(), "PUT", "read the http method");
		Assertions.assertEquals(reqParser.getRequestURI(), "/test", "read the request uri");
		Assertions.assertEquals(reqParser.getBodyStr(), "interesting text", "read the body");
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// 									http response 									   //
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * parse a full http response with a body that is not chunked
	 */
	@Test
	@DisplayName("http response")
	public void httpResponse() {
		HttpResponseParser respParser = new HttpResponseParser();
				
		// add the data to the parser
		respParser.addData(httpResponse.getBytes(StandardCharsets.UTF_8));
		
		// test the parsed values
		Assertions.assertTrue(respParser.isComplete(), "http request is complete");
		Assertions.assertEquals(respParser.getStatus(), 404, "read the http status");
		Assertions.assertEquals(respParser.getStatusTxt(), "Not Found", "read the status text");
		Assertions.assertEquals(respParser.getBodyStr(), respBody, "read the body");
	}
	
	
	/**
	 * parse a full http response with a body that is not chunked. the content length header property can be
	 * omitted if the server just closes the connection after the request
	 */
	@Test
	@DisplayName("http response no length")
	public void httpResponseNoLength() {
		HttpResponseParser respParser = new HttpResponseParser();
				
		// add the data to the parser
		respParser.addData(httpResponseNoLength.getBytes(StandardCharsets.UTF_8));
		
		// this method needs to be called after the server closed the connection
		respParser.fullMessageReceived();
		
		// test the parsed values
		Assertions.assertTrue(respParser.isComplete(), "http request is complete");
		Assertions.assertEquals(respParser.getStatus(), 404, "read the http status");
		Assertions.assertEquals(respParser.getStatusTxt(), "Not Found", "read the status text");
		Assertions.assertEquals(respParser.getBodyStr(), respBody, "read the body");
	}
	
	
	/**
	 * parse a http response packet by packet with a body that is not chunked
	 */
	@Test
	@DisplayName("packet http response")
	public void httpResponseChunked() {
		HttpResponseParser respParser = new HttpResponseParser();
		
		// define the 3 chunks that are fed into the parser
		byte[] chunk1 = httpResponse.substring(0, 161).getBytes(StandardCharsets.UTF_8);
		byte[] chunk2 = httpResponse.substring(161, 296).getBytes(StandardCharsets.UTF_8);
		byte[] chunk3 = httpResponse.substring(296).getBytes(StandardCharsets.UTF_8);
				
		// feed the 3 chunks into the parser
		respParser.addData(chunk1);
		Assertions.assertFalse(respParser.isComplete(), "is complete after first chunk");
		
		respParser.addData(chunk2);
		Assertions.assertFalse(respParser.isComplete(), "is complete after second chunk");
		
		respParser.addData(chunk3);
		Assertions.assertTrue(respParser.isComplete(), "is complete after third chunk");
		
		
		// test the parsed values
		Assertions.assertTrue(respParser.isComplete(), "http request is complete");
		Assertions.assertEquals(respParser.getStatus(), 404, "read the http status");
		Assertions.assertEquals(respParser.getStatusTxt(), "Not Found", "read the status text");
		Assertions.assertEquals(respParser.getBodyStr(), respBody, "read the body");
	}
	
	
	/**
	 * parse a http response with a body that is chunked
	 */
	@Test
	@DisplayName("http chunked response")
	public void httpPacketResponse() {
		HttpResponseParser respParser = new HttpResponseParser();
				
		// add the data to the parser
		respParser.addData(httpResponseChunked.getBytes(StandardCharsets.UTF_8));
		
		// test the parsed values
		Assertions.assertTrue(respParser.isComplete(), "http request is complete");
		Assertions.assertEquals(respParser.getStatus(), 200, "read the http status");
		Assertions.assertEquals(respParser.getStatusTxt(), "OK", "read the status text");
		
		String body = "absolutely super stunning body that is sent in the chunk format";
		Assertions.assertEquals(respParser.getBodyStr(), body, "read the body");
	}
}
