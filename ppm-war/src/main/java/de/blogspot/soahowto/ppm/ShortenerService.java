package de.blogspot.soahowto.ppm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import java.io.IOException;

public class ShortenerService {
	public static final String SHORTENER_REST_URL = "https://www.googleapis.com/urlshortener/v1/url";

	/**
	 * Shortens a long URL.
	 *
	 * @param longUrl the long URL to be shortened
	 * @return the short URL
	 */
	public String shortenUrl(String longUrl) {
		String response = ClientBuilder.newClient().target(SHORTENER_REST_URL).request()
				.post(Entity.json("{\"longUrl\": \"" + longUrl + "\"}"), String.class);
		try (JsonParser parser = new JsonFactory().createParser(response)) {
			while (parser.nextToken() != null) {
				if ("id".equals(parser.getCurrentName())) {
					parser.nextToken();
					return parser.getValueAsString();
				}
			}
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		throw new TechnicalException("Failed to retrieve the short URL from the response");
	}
}
