package ch.wenkst.sw_utils.natural_sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.natural_sort.NaturalSortComparator;

public class NaturalSortComparatorTest {
	
	/**
	 * test the natural sort comparator
	 */
	@Test
	@DisplayName("natural sort")
	public void naturalSortTest() {
		// sort the string list, capital letters are ignored
		List<String> testWordList = Arrays.asList(
				"Room3",
				"room10",
				"room11",
				"room4.8",
				"Room15.7",
				"doom8",
				"1",
				"11",
				"2",
				"5.3",
				"doom11",
				"doom1",
				"roomNumber"
		);
		
		String[] sortedWordArr = {
				"1",
				"2",
				"5.3",
				"11",
				"doom1",
				"doom8",
				"doom11",
				"Room3",
				"room4.8",
				"room10",
				"room11",
				"Room15.7",
				"roomNumber"
		};
				
		// Collections.sort(testWordList, new NaturalSortComparator(true));
		Collections.sort(testWordList, new NaturalSortComparator(true));
		
		String[] testWordArr = testWordList.toArray(new String[testWordList.size()]);
		Assertions.assertArrayEquals(testWordArr, sortedWordArr, "sorted words, capital letters ignored");
		
		
		
		// sort the string list, capital letters are not ignored
		testWordList = Arrays.asList(
				"Room3",
				"room3",
				"Room10",
				"room10",
				"room11",
				"Room11",
				"room4.8",
				"Room15.7",
				"doom8",
				"1",
				"11",
				"2",
				"5.3",
				"doom11",
				"doom1",
				"roomNumber",
				"roomnumber"
		);
		
		sortedWordArr = new String[] {
				"1",
				"2",
				"5.3",
				"11",
				"Room3",
				"Room10",
				"Room11",
				"Room15.7",
				"doom1",
				"doom8",
				"doom11",
				"room3",
				"room4.8",
				"room10",
				"room11",
				"roomNumber",
				"roomnumber"
		};
		
		
		Collections.sort(testWordList, new NaturalSortComparator(false));
		testWordArr = testWordList.toArray(new String[testWordList.size()]);
		Assertions.assertArrayEquals(testWordArr, sortedWordArr, "sorted words, capital letters not ignored");		
	}
}
