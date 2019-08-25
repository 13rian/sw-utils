package ch.wenkst.sw_utils.file;

import java.io.Serializable;

public class DataStorage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private int value;

	public DataStorage(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
}