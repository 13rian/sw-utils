package ch.wenkst.sw_utils.http;

public class HttpTestResponses {
	public static String nonChunkedResponse() {
		return new StringBuilder()
				.append("HTTP/1.1 404 Not Found").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Origin: *").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Headers: origin, content-type, accept, authorization").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Credentials: true").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD").append(HttpConstants.CRLF)
				.append("Content-Type: text/plain").append(HttpConstants.CRLF)
				.append("Content-Length: 9").append(HttpConstants.CRLF)
				.append(HttpConstants.CRLF)
				.append("Some Body")
				.toString();
	}
	
	
	public static String nonChunkedNoLengthRequest() {
		return new StringBuilder()
				.append("HTTP/1.1 404 Not Found").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Origin: *").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Headers: origin, content-type, accept, authorization").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Credentials: true").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD").append(HttpConstants.CRLF)
				.append("Content-Type: text/plain").append(HttpConstants.CRLF)
				.append(HttpConstants.CRLF)
				.append("Some Body")
				.toString();
	}
	
	
	public static String chunkedResponse() {		
		return new StringBuilder()
				.append("HTTP/1.1 200 OK").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Origin: *").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Headers: origin, content-type, accept, authorization").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Credentials: true").append(HttpConstants.CRLF)
				.append("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD").append(HttpConstants.CRLF)
				.append("Transfer-Encoding: chunked").append(HttpConstants.CRLF)
				.append("Content-Type: text/plain").append(HttpConstants.CRLF)
				.append(HttpConstants.CRLF)
				.append(chunkedResponseBody())
				.append(HttpConstants.CRLF)
				.toString();
	}
	
	
	private static String chunkedResponseBody() {
		return new StringBuilder()
				.append("c").append(HttpConstants.CRLF)
				.append("interesting ").append(HttpConstants.CRLF)
				.append("4").append(HttpConstants.CRLF) 
				.append("text").append(HttpConstants.CRLF) 
				.append("0")
				.append(HttpConstants.CRLF)
				.toString();
	}
}
