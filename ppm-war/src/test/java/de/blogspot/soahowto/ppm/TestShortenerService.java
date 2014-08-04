package de.blogspot.soahowto.ppm;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class TestShortenerService {
	private ShortenerService shortenerService = new ShortenerService();

	@Test
	public void shorten() {
		String longUrl = "http://www.flickr.com";
		String shortUrl = shortenerService.shortenUrl(longUrl);
		System.out.println("Short URL: " + shortUrl);
		Assertions.assertThat(longUrl.length()).isGreaterThan(shortUrl.length());
	}
}
