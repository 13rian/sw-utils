package ch.wenkst.sw_utils.natural_sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.BaseTest;

public class NaturalSortComparatorTest extends BaseTest {
	
	@Test
	public void naturalSortIgnoreCase() {
		List<String> listToSort = unsortedWordList();
		Collections.sort(listToSort, new NaturalSortComparator(true));
		String[] arrayToSort = listToSort.toArray(new String[listToSort.size()]);
		String[] arraySorted = sortedWordArrayIgnoreCase();
		Assertions.assertArrayEquals(arraySorted, arrayToSort);
	}
	
	
	@Test 
	public void naturalSortCaseSensitive() {
		List<String> listToSort = unsortedWordList();
		Collections.sort(listToSort, new NaturalSortComparator(false));
		String[] arrayToSort = listToSort.toArray(new String[listToSort.size()]);
		String[] arraySorted = sortedWordArrayCaseSensitive();
		Assertions.assertArrayEquals(arraySorted, arrayToSort);
	}
	
	
	private List<String> unsortedWordList() {
		return Arrays.asList(
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
	}
	
	
	private String[] sortedWordArrayIgnoreCase() {
		return new String[] {
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
	}
	
	
	private String[] sortedWordArrayCaseSensitive() {
		return new String[] {
				"1",
				"2",
				"5.3",
				"11",
				"Room3",
				"Room15.7",
				"doom1",
				"doom8",
				"doom11",
				"room4.8",
				"room10",
				"room11",
				"roomNumber"
		};
	}
}
