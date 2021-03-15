package ch.wenkst.sw_utils.event;

public class ProcessedEvent implements Comparable<ProcessedEvent> {
	public ProcessedEvent(long timestamp, String param) {
		this.timestamp = timestamp;
		this.param = param;
	}
	
	public long timestamp;
	public String param;
	
	@Override
	public int compareTo(ProcessedEvent o) {
		return param.compareTo(o.param);
	}
}