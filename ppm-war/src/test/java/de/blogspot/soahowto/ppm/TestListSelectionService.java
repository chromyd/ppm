package de.blogspot.soahowto.ppm;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.blogspot.soahowto.ppm.ListSelectionUtils.selectRandom;
import static de.blogspot.soahowto.ppm.ListSelectionUtils.selectWithTopBias;
import static org.fest.assertions.api.Assertions.assertThat;

public class TestListSelectionService {
	private List<String> inList = new ArrayList<>();
	private List<String> outList;

	@Before
	public void setUp() {
		for (int i = 0; i < 100; ) {
			inList.add("" + ++i);
		}
	}

	@Test
	public void randomSelection() {
		System.out.println("Random list:   " + (outList = selectRandom(inList, 10)));
		assertThat(outList).hasSize(10);
	}

	@Test
	public void everythingFromTop() {
		System.out.println("Top 10 only:   " + (outList = selectWithTopBias(inList, 10, 10, 10)));
		assertThat(outList).containsOnly(range(0, 10));
	}

	@Test
	public void everythingFromBottom() {
		System.out.println("Bottom 10 only:   " + (outList = selectWithTopBias(inList, 10, 90, 0)));
		assertThat(outList).containsOnly(range(90, 100));
	}

	@Test
	public void nothingFromTop() {
		System.out.println("Skip top 10:   " + (outList = selectWithTopBias(inList, 20, 10, 0)));
		assertThat(outList).doesNotContain(range(0, 10));
	}

	@Test
	public void mixed() {
		System.out.println("Contain 6/10:  " + (outList = selectWithTopBias(inList, 10, 10, 6)));
		assertThat(outList).hasSize(10);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void sizeTooBig() {
		selectRandom(inList, 105);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void topSizeTooBig() {
		selectWithTopBias(inList, 20, 10, 15);
	}

	@Test(expected = IllegalArgumentException.class)
	public void topSizeGreaterThanSize() {
		selectWithTopBias(inList, 20, 30, 30);
	}

	private String[] range(int from, int to) {
		String[] r = new String[to - from];
		return inList.subList(from, to).toArray(r);
	}
}
