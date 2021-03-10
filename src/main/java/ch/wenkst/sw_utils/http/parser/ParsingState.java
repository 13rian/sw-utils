package ch.wenkst.sw_utils.http.parser;

public enum ParsingState {
	NONE,
	FIRST_LINE_RECEIVED,
	HEADER_RECEIVED,
	CHUNK_LENGTH_RECEIVED,
	BODY_RECEIVED
}
