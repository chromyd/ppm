package de.blogspot.soahowto.ppm;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

@Ignore
public class TestFlickrService {
	public static final String PHOTO_ID1 = "12028509755";
	public static final String PHOTO_SET_TITLE = "test";
	private FlickrService flickrService = new FlickrService();

	@Test
	public void getAllPhotoIds() {
		List<String> photoIds = flickrService.getAllPhotoIds();
		assertThat(photoIds.contains(PHOTO_ID1));
		assertThat(photoIds.size()).isGreaterThan(FlickrService.PER_PAGE_VALUE);
	}

	@Test
	public void createFindDeletePhotoSet() {
		List<String> photoIds = flickrService.getAllPhotoIds();
		Collections.shuffle(photoIds);
		String id1 = flickrService.createOrUpdatePhotoSet(PHOTO_SET_TITLE, photoIds.subList(0, 10));
		String id2 = flickrService.findPhotoSet(PHOTO_SET_TITLE);
		assertThat(id2).isEqualTo(id1);
		flickrService.deletePhotoSet(id2);
	}

	@Test
	public void findPhotoSet() {
		assertThat(flickrService.findPhotoSet("best")).isNotNull();
	}
}
