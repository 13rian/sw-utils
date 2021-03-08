package ch.wenkst.sw_utils.natural_sort;

public class StringPairComparator {
	private String string1;
	private String string2;
	private boolean isIgnoreCase;

	private int str1Length;
	private int str2Length;

	private int cursor1 = 0;
	private int cursor2 = 0;
	char char1;
	char char2;


	protected StringPairComparator(String string1, String string2, boolean isIgnoreCase) {
		this.isIgnoreCase = isIgnoreCase;
		this.string1 = string1;
		this.string2 = string2;
		str1Length = string1.length();
		str2Length = string2.length();
	}


	public int compare() {
		if (isIgnoreCase) {
			string1 = string1.toLowerCase();
			string2 = string2.toLowerCase();
		}

		return naturalCompare();
	}


	private int naturalCompare() {
		int retVal = compareBySameKindSequences();
		
		if (retVal == 0) {
			retVal = str1Length - str2Length;
		}

		return retVal;
	}
	
	
	private int compareBySameKindSequences() {
		int retVal = compareNextSequence();
		while (bothStringsNotAtEnd() && retVal == 0) {
			retVal = compareNextSequence();
		}
		return retVal;
	}
	
	
	private int compareNextSequence() {
		char1 = string1.charAt(cursor1);
		char2 = string2.charAt(cursor2);

		if (bothDigits(char1, char2)) {
			return compareDigitSequence();
		
		} else if (bothLetters(char1, char2)) {
			return compareLetterSequence();
		
		} else {
			return compareDigitAndLetter();
		}
	}


	private boolean bothStringsNotAtEnd() {
		return cursor1 < str1Length && cursor2 < str2Length;
	}

	private boolean bothDigits(char char1, char char2) {
		return Character.isDigit(char1) && Character.isDigit(char2);
	}


	private int compareDigitSequence() {
		StringBuilder sequence1 = digitSequence(char1, string1, str1Length, cursor1);
		StringBuilder sequence2 = digitSequence(char2, string2, str2Length, cursor2);

		int retVal = compareDigitSequence(sequence1, sequence2);
		if (retVal == 0) {
			cursor1 = cursor1 + sequence1.length();
			cursor2 = cursor2 + sequence2.length();
		}

		return retVal;
	}
	
	
	/**
	 * extracts a complete sequence of digits from the passed string till the next letter or the end of the string
	 * @param ch            first digit which is the starting point of the sequence
	 * @param str           the string from which the sequence is extracted
	 * @param strLength     the length of the passed string
	 * @param cursor        index of the first digit position
	 * @return 				sequence of digits
	 */
	private StringBuilder digitSequence(char ch, String str, int strLength, int cursor) {
		StringBuilder result = new StringBuilder();
		result.append(ch);
		cursor = cursor + 1;

		for (int i=cursor; i<strLength; i++) {
			ch = str.charAt(cursor);
			if (Character.isDigit(ch)) {
				result.append(ch);
				cursor = cursor + 1;

			} else {
				break;
			}
		}

		return result;
	}
	
	
	private int compareDigitSequence(StringBuilder sequence1, StringBuilder sequence2) {
		// longer digit sequence is the larger number
		int retVal = sequence1.length() - sequence2.length();
		if (retVal != 0) {
			return retVal;
		}

		// first number that is larger matters
		for (int j=0; j < sequence1.length(); j++) {
			retVal = sequence1.charAt(j) - sequence2.charAt(j);
			if (retVal != 0) {
				return retVal;
			}
		}

		return 0;
	}

	
	private boolean bothLetters(char char1, char char2) {
		return Character.isLetter(char1) && Character.isLetter(char2);
	}
	
	
	private int compareLetterSequence() {
		StringBuilder sequence1 = letterSequence(char1, string1, str1Length, cursor1);
		StringBuilder sequence2 = letterSequence(char2, string2, str2Length, cursor2);
		
		int retVal = sequence1.toString().compareTo(sequence2.toString());
		if (retVal == 0) {
			cursor1 = cursor1 + sequence1.length();
			cursor2 = cursor2 + sequence2.length();
		}

		return retVal;
	}


	/**
	 * extracts a complete sequence of letters from the passed string till the next digit or the end of the string
	 * @param ch            first letter which is the starting point of the sequence
	 * @param str           the string from which the sequence is extracted
	 * @param strLength     the length of the passed string
	 * @param cursor        index of the first digit position
	 * @return 				sequence of letters	
	 */
	private StringBuilder letterSequence(char ch, String str, int strLength, int cursor) {
		StringBuilder result = new StringBuilder();         // StringBuilder is fastest for String operations
		result.append(ch);
		cursor = cursor + 1;

		for (int i=cursor; i<strLength; i++) {
			ch = str.charAt(cursor);

			if (Character.isLetter(ch)) {
				result.append(ch);
				cursor = cursor + 1;

			} else {
				break;
			}
		}

		return result;
	}
	
	
	private int compareDigitAndLetter() {
		// the one with the digit sequence comes before the one with the letter sequence,
		if (Character.isDigit(char1)) {
			return -1;
		} else {
			return 1;
		}
	}
}
