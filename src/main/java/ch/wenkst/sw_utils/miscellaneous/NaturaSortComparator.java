package ch.wenkst.sw_utils.miscellaneous;

import java.util.Comparator;


/**
 * comparator that orders strings that contain digits like humans would do:
 * digits before letters and smaller digits before larger digits
 */
public class NaturaSortComparator implements Comparator<String> {
	private boolean isIgnoreCase = false;     	// true if the capital letters should not be before all other letters


	/**
	 * comparator that orders strings like humans would do:
	 * digits before letters and smaller digits before larger digits
	 */
	public NaturaSortComparator() {
		super();       
	}


	/**
	 * comparator that orders strings like humans would do:
	 * digits before letters and smaller digits before larger digits
	 * @param isIgnoreCase         false if capital letters should be before lower case letters
	 */
	public NaturaSortComparator(boolean isIgnoreCase) {
		super();
		this.isIgnoreCase = isIgnoreCase;
	}


	public int compare(String string1, String string2) {
		String str1;
		String str2;

		if (isIgnoreCase) {
			// capital letters are ignored
			str1 = string1.toLowerCase();
			str2 = string2.toLowerCase();

		} else {
			// capital letters are before all other letters
			str1 = string1;
			str2 = string2;
		}


		// check if the passed strings are null
		if (str1 == null || str2 == null) {
			return 0;
		}

		int retVal = 0;         // result of the comparison

		int cursor1 = 0;
		int cursor2 = 0;
		int str1Length = str1.length();
		int str2Length = str2.length();


		// go through both strings and compare pure digits or pure character strings which are parts of the passed strings
		while (cursor1<str1Length && cursor2<str2Length) {
			char ch1 = str1.charAt(cursor1);
			char ch2 = str2.charAt(cursor2);


			// if both are digits, compare the digits
			if (Character.isDigit(ch1) && Character.isDigit(ch2)) {
				// get the two digit sequences
				StringBuilder sequence1 = getDigits(ch1, str1, str1Length, cursor1);
				StringBuilder sequence2 = getDigits(ch2, str2, str2Length, cursor2);


				// compare the length, longer digit sequence is the larger number
				retVal = sequence1.length() - sequence2.length();

				// if the length is equal the first number that is larger matters
				if (retVal == 0) {
					for (int j=0; j < sequence1.length(); j++) {
						retVal = sequence1.charAt(j) - sequence2.charAt(j);
						if (retVal != 0) {
							return retVal;
						}
					}
				}

				// digits are equal, update the cursor positions
				cursor1 = cursor1 + sequence1.length();
				cursor2 = cursor2 + sequence2.length();
			}

			// if both are letter sequences, use the simple string comparator
			else if (Character.isLetter(ch1) && Character.isLetter(ch2)) {
				StringBuilder sequence1 = getLetters(ch1, str1, str1Length, cursor1);
				StringBuilder sequence2 = getLetters(ch2, str2, str2Length, cursor2);
				retVal = sequence1.toString().compareTo(sequence2.toString());

				if (retVal == 0) {
					// letter sequences are equal, update the cursor positions
					cursor1 = cursor1 + sequence1.length();
					cursor2 = cursor2 + sequence2.length();
				}
			}

			// one is a digit sequence, one is a letter sequence
			else {
				// the one with the digit sequence comes before the one with the letter sequence,
				if (Character.isDigit(ch1)) {
					return -1;
				} else {
					return 1;
				}
			}

			// if the retVal is not 0 return it
			if (retVal != 0) {
				return retVal;
			}

		}



		// if no return value so far, the two passed strings are equal, compare the length
		return str1Length - str2Length;


	}



	/**
	 * extracts a complete sequence of digits from the passed string
	 * @param ch            first digit which is the starting point of the sequence
	 * @param str           the string from which the sequence is extracted
	 * @param strLength     the length of the passed string
	 * @param cursor        index of the first digit position
	 * @return 				sequence of digits
	 */
	private StringBuilder getDigits(char ch, String str, int strLength, int cursor) {

		// create the digit sequence with the first character    
		StringBuilder result = new StringBuilder();         // StringBuilder is fastest for String operations
		result.append(ch);
		cursor = cursor + 1;

		// iterate through the rest of the string to get all digits until the next character
		for (int i=cursor; i<strLength; i++) {
			ch = str.charAt(cursor);         // get the next character

			if (Character.isDigit(ch)) {
				// character is a digit, append it to the result
				result.append(ch);
				cursor = cursor + 1;

			} else {
				//character is not a digit, break
				break;
			}
		}

		return result;
	}



	/**
	 * extracts a complete sequence of letters from the passed string
	 * @param ch            first letter which is the starting point of the sequence
	 * @param str           the string from which the sequence is extracted
	 * @param strLength     the length of the passed string
	 * @param cursor        index of the first digit position
	 * @return 				sequence of letters	
	 */
	private StringBuilder getLetters(char ch, String str, int strLength, int cursor) {
		// create the character sequence with the first character    
		StringBuilder result = new StringBuilder();         // StringBuilder is fastest for String operations
		result.append(ch);
		cursor = cursor + 1;

		// iterate through the rest of the string to get all characters until the next digit
		for (int i=cursor; i<strLength; i++) {
			ch = str.charAt(cursor);         // get the next character

			if (Character.isLetter(ch)) {
				// character is a letter, append it to the result
				result.append(ch);
				cursor = cursor + 1;

			} else {
				// character is not a letter, break
				break;
			}
		}

		return result;
	}


}
