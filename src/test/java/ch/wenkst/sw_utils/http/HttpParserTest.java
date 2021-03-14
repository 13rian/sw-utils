package ch.wenkst.sw_utils.http;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.http.parser.HttpRequestParser;
import ch.wenkst.sw_utils.http.parser.HttpResponseParser;

public class HttpParserTest extends BaseTest {
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// 									http request 									   //
	/////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void parseNonChunkedRequestAllAtOnce() {
		HttpRequestParser reqParser = ifNonChunkedRequestParsedAllAtOnce();
		thenNonChunkeRequestCorrectlyParsed(reqParser);
	}
	
	
	private HttpRequestParser ifNonChunkedRequestParsedAllAtOnce() {
		HttpRequestParser reqParser = new HttpRequestParser();
		String httpRequest = HttpTestRequests.nonChunkedRequest();
		reqParser.addData(httpRequest.getBytes(StandardCharsets.UTF_8));
		return reqParser;
	}
	
	
	private void thenNonChunkeRequestCorrectlyParsed(HttpRequestParser reqParser) {
		Assertions.assertTrue(reqParser.isComplete());
		Assertions.assertEquals(reqParser.getHttpMethod(), "POST");
		Assertions.assertEquals(reqParser.getRequestURI(), "/test");
		Assertions.assertEquals(reqParser.getBodyStr(), "Some Body");
	}
	
	
	@Test
	public void parseNonChunkedRequestBytePacketWise() {
		HttpRequestParser reqParser = ifNonChunkedRequestParsedBytePacketWise();
		thenNonChunkeRequestCorrectlyParsed(reqParser);
	}
	
	
	public HttpRequestParser ifNonChunkedRequestParsedBytePacketWise() {
		HttpRequestParser reqParser = new HttpRequestParser();
		String httpRequest = HttpTestRequests.nonChunkedRequest();

		byte[] chunk1 = httpRequest.substring(0, 98).getBytes(StandardCharsets.UTF_8);
		reqParser.addData(chunk1);
		Assertions.assertFalse(reqParser.isComplete());
		
		byte[] chunk2 = httpRequest.substring(98, 150).getBytes(StandardCharsets.UTF_8);
		reqParser.addData(chunk2);
		Assertions.assertFalse(reqParser.isComplete());
		
		byte[] chunk3 = httpRequest.substring(150).getBytes(StandardCharsets.UTF_8);
		reqParser.addData(chunk3);
		Assertions.assertTrue(reqParser.isComplete());
		return reqParser;
	}
	

	@Test
	public void httpRequestHeader() {
		HttpRequestParser reqParser = new HttpRequestParser();
		String httpRequest = HttpTestRequests.nonChunkedRequest();
		reqParser.addData(httpRequest.getBytes(StandardCharsets.UTF_8));
		
		Assertions.assertEquals(reqParser.getHeaderField("User-Agent"), "AHC/1.0");
		Assertions.assertEquals(reqParser.getHeaderField("Connection"), "keep-alive");
		Assertions.assertNull(reqParser.getHeaderField("fake-field"));
	}


	@Test
	public void parseChunkedHttpReequest() {
		HttpRequestParser reqParser = new HttpRequestParser();
		String chunkedHttpRequest = HttpTestRequests.chunkedRequest();
		reqParser.addData(chunkedHttpRequest.getBytes(StandardCharsets.UTF_8));
		
		Assertions.assertTrue(reqParser.isComplete());
		Assertions.assertEquals(reqParser.getHttpMethod(), "PUT");
		Assertions.assertEquals(reqParser.getRequestURI(), "/test");
		Assertions.assertEquals(reqParser.getBodyStr(), "interesting text");
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// 									http response 									   //
	/////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void parseNonChunkedResponseAllAtOnce() {
		
	}
	

	@Test
	public void httpResponse() {
		HttpResponseParser respParser = ifNonChunkedResponseParsedAllAtOnce();
		thenNonChunkedResponseCorrectlyParsed(respParser);
	}
	
	
	private HttpResponseParser ifNonChunkedResponseParsedAllAtOnce() {
		HttpResponseParser respParser = new HttpResponseParser();
		String httpResponse = HttpTestResponses.nonChunkedResponse();	
		respParser.addData(httpResponse.getBytes(StandardCharsets.UTF_8));
		return respParser;
	}
	
	
	private void thenNonChunkedResponseCorrectlyParsed(HttpResponseParser respParser) {
		Assertions.assertTrue(respParser.isComplete());
		Assertions.assertEquals(respParser.getStatus(), 404);
		Assertions.assertEquals(respParser.getStatusTxt(), "Not Found");
		Assertions.assertEquals(respParser.getBodyStr(), "Some Body");
	}
	
	
	@Test
	public void httpResponseChunked() {
		HttpResponseParser respParser = ifNonChunkedResponseParsedBytePacketWise();
		thenNonChunkedResponseCorrectlyParsed(respParser);
	}
	
	
	private HttpResponseParser ifNonChunkedResponseParsedBytePacketWise() {
		HttpResponseParser respParser = new HttpResponseParser();
		String httpResponse = HttpTestResponses.nonChunkedResponse();

		byte[] chunk1 = httpResponse.substring(0, 81).getBytes(StandardCharsets.UTF_8);
		respParser.addData(chunk1);
		Assertions.assertFalse(respParser.isComplete());
		
		byte[] chunk2 = httpResponse.substring(81, 120).getBytes(StandardCharsets.UTF_8);
		respParser.addData(chunk2);
		Assertions.assertFalse(respParser.isComplete());
		
		byte[] chunk3 = httpResponse.substring(120).getBytes(StandardCharsets.UTF_8);
		respParser.addData(chunk3);
		Assertions.assertTrue(respParser.isComplete());
		return respParser;
	}
	
	
	@Test
	public void httpResponseNoLength() {
		HttpResponseParser respParser = new HttpResponseParser();
		String responseWithNoLength = HttpTestResponses.nonChunkedNoLengthRequest();
		respParser.addData(responseWithNoLength.getBytes(StandardCharsets.UTF_8));
		
		respParser.fullMessageReceived();		// this method needs to be called after the server closed the connection
		
		Assertions.assertTrue(respParser.isComplete());
		Assertions.assertEquals(respParser.getStatus(), 404);
		Assertions.assertEquals(respParser.getStatusTxt(), "Not Found");
		Assertions.assertEquals(respParser.getBodyStr(), "Some Body");
	}

	
	@Test
	public void parseChunkedResponse() {
		HttpResponseParser respParser = new HttpResponseParser();
		String httpRequest = HttpTestResponses.chunkedResponse();	
		respParser.addData(httpRequest.getBytes(StandardCharsets.UTF_8));
		
		Assertions.assertTrue(respParser.isComplete());
		Assertions.assertEquals(respParser.getStatus(), 200);
		Assertions.assertEquals(respParser.getStatusTxt(), "OK");
		Assertions.assertEquals(respParser.getBodyStr(), "interesting text");
	}
}
