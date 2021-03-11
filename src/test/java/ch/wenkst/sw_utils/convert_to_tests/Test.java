package ch.wenkst.sw_utils.convert_to_tests;
import ch.wenkst.sw_utils.logging.Log;

public class Test {
	public Log log = Log.getLogger(Test.class);
	
	
	public void print() {
		log.info("this is a print message");
	}
}
