package ch.wenkst.sw_utils.file.utils;

import java.io.Serializable;
import java.util.UUID;

public class DataStorage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private int value;

	public static DataStorage getRandomStorage() {
		String name = UUID.randomUUID().toString();
		int value = (int) Math.random();
		return new DataStorage(name, value);
	}
	
	public DataStorage(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataStorage) {
			DataStorage dataStorage = (DataStorage) obj;
			return this.getName().equals(dataStorage.getName()) && this.getValue() == dataStorage.getValue();
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
}