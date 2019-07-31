package ch.wenkst.sw_utils.miscellaneous;

public class DataWrapper {
	private Object data;
	
	/**
	 * simple wrapper class around an object. the idea is to wrap any object in
	 * a class to get some advantages of classes
	 * @param data	the object that is held by the Data Wrapper
	 */
	public DataWrapper(Object data) {
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
