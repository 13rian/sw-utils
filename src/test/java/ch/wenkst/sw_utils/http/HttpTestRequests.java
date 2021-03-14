package ch.wenkst.sw_utils.http;

public class HttpTestRequests {
	public static String nonChunkedRequest() {
		return new StringBuilder()
				.append("POST /test HTTP/1.1").append(HttpConstants.CRLF)
				.append("Host: localhost:8001").append(HttpConstants.CRLF)
				.append("User-Agent: AHC/1.0").append(HttpConstants.CRLF)
				.append("Connection: keep-alive").append(HttpConstants.CRLF)
				.append("Accept: */*").append(HttpConstants.CRLF)
				.append("Content-Type: application/json").append(HttpConstants.CRLF)
				.append("Content-Length: 9").append(HttpConstants.CRLF)
				.append(HttpConstants.CRLF)
				.append("Some Body")
				.toString();
	}
	
	
	public static String chunkedRequest() {
		return new StringBuilder()
				.append("PUT /test HTTP/1.1").append(HttpConstants.CRLF)
				.append("Host: localhost:8001").append(HttpConstants.CRLF)
				.append("User-Agent: AHC/1.0").append(HttpConstants.CRLF)
				.append("Connection: keep-alive").append(HttpConstants.CRLF)
				.append("Accept: */*").append(HttpConstants.CRLF)
				.append("Transfer-Encoding: chunked").append(HttpConstants.CRLF)
				.append("Content-Type: text/plain").append(HttpConstants.CRLF)
				.append(HttpConstants.CRLF)
				.append(chunkedRequestBody())
				.toString();
	}
	
	
	private static String chunkedRequestBody() {
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
