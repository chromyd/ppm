package de.blogspot.soahowto.ppm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListSelectionUtils {
	/**
	 * Creates a list that selects items randomly (that is every list item is equally likely to be taken).
	 *
	 * @param list list to take items from
	 * @param size number of items to take
	 * @return a new list containing at most the specified number of items
	 */
	public static List<String> selectRandom(List<String> list, int size) {
		List<String> listCopy = new ArrayList<>(list);
		Collections.shuffle(listCopy);
		return listCopy.subList(0, size);
	}

	/**
	 * Creates a list that randomly selects items from the top of the list and then randomly selects items from the
	 * remainder.
	 *
	 * @param list    list to take items from
	 * @param size    number of items to take in total
	 * @param top     number of items to constitute the 'top'
	 * @param topSize number of items to take from the 'top'
	 * @return a new list containing at most the specified number of items which are selected by picking from the top
	 * and from the remainder of the list
	 */
	public static List<String> selectWithTopBias(List<String> list, int size, int top, int topSize) {
		List<String> topList = new ArrayList<>(list.subList(0, top));
		List<String> remainderList = new ArrayList<>(list.subList(top, list.size()));
		Collections.shuffle(topList);
		Collections.shuffle(remainderList);
		List<String> resultList = topList.subList(0, topSize);
		resultList.addAll(remainderList.subList(0, size - topSize));
		return resultList;
	}
}
