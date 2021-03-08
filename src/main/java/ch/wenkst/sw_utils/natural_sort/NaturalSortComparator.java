package ch.wenkst.sw_utils.natural_sort;

import java.util.Comparator;

public class NaturalSortComparator implements Comparator<String> {
	private boolean isIgnoreCase = false;


	/**
	 * comparator that orders strings like humans would do:
	 * digits before letters and smaller digits before larger digits
	 */
	public NaturalSortComparator() {
		     
	}


	/**
	 * comparator that orders strings like humans would do:
	 * digits before letters and smaller digits before larger digits
	 * @param isIgnoreCase         false if capital letters should be before lower case letters
	 */
	public NaturalSortComparator(boolean isIgnoreCase) {
		super();
		this.isIgnoreCase = isIgnoreCase;
	}


	public int compare(String string1, String string2) {
		if (oneOfThemNull(string1, string2)) {
			return 0;
		}
		
		StringPairComparator spc = new StringPairComparator(string1, string2, isIgnoreCase);
		return spc.compare();
	}
	
	
	private boolean oneOfThemNull(String string1, String string2) {
		return string1 == null || string2 == null;
	}
}
