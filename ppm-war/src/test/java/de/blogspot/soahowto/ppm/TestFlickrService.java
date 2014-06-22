package de.blogspot.soahowto.ppm;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class TestFlickrService {
	public static final String PHOTO_ID1 = "12028509755";
	public static final String PHOTO_SET_TITLE = "test10";
	private FlickrService flickrService = new FlickrService();

	@Test
	public void getAllPhotoIds() {
		List<String> photoIds = flickrService.getAllPhotoIds();
		Assertions.assertThat(photoIds.contains(PHOTO_ID1));
		Assertions.assertThat(photoIds.size()).isGreaterThan(FlickrService.PER_PAGE_VALUE);
	}

	@Test
	public void createAndFindPhotoSet() {
		List<String> photoIds = flickrService.getAllPhotoIds();
		Collections.shuffle(photoIds);
		String id1 = flickrService.createPhotoSet(PHOTO_SET_TITLE, photoIds.subList(0, 10));
		String id2 = flickrService.findPhotoSet(PHOTO_SET_TITLE);
		Assertions.assertThat(id2).isEqualTo(id1);
	}

	@Test
	public void findPhotoSet() {
		Assertions.assertThat(flickrService.findPhotoSet("best")).isNotNull();
	}
}