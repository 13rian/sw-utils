package ch.wenkst.sw_utils.miscellaneous;

public class DataWrapper {
	private Object data;
	
	/**
	 * simple wrapper class around an object. the idea is to wrap any object in
	 * a class to get some advantages of classes
	 * @param <T>
	 * @param data	the object that is held by the Data Wrapper
	 */
	public <T> DataWrapper(T data) {
		this.data = data;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T) data;
	}

	public <T> void setData(T data) {
		this.data = data;
	}
}
