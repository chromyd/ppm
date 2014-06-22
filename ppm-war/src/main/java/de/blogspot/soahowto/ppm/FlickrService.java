package de.blogspot.soahowto.ppm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.google.appengine.repackaged.com.google.common.base.Joiner;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.com.google.common.hash.Hashing;
import com.google.cloud.sql.jdbc.internal.Charsets;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.*;

public class FlickrService {
	public static final String FLICKR_REST_URL = "https://api.flickr.com/services/rest/";
	public static final String AUTH_TOKEN = "auth_token";
	public static final String API_KEY = "api_key";
	public static final String METHOD = "method";
	public static final String PER_PAGE = "per_page";
	public static final int PER_PAGE_VALUE = 500;
	public static final String FORMAT = "format";
	private final Properties properties;

	public FlickrService() {
		properties = new Properties();
		try {
			properties.load(getClass().getResourceAsStream("/photoset.properties"));
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}

	/**
	 * Returns a list of all ids of all photos belonging to the authenticated user.
	 *
	 * @return list of all photo ids
	 */
	public List<String> getAllPhotoIds() {
		int page = 0;
		int numPages = -1;
		List<String> result = new ArrayList<>();
		do {
			Client client = ClientBuilder.newClient();
			Map<String, Object> params = newParamMap()
					.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
					.put(API_KEY, properties.getProperty(API_KEY))
					.put(METHOD, "flickr.photos.getWithoutGeoData")
					.put(PER_PAGE, PER_PAGE_VALUE)
					.put("page", ++page)
					.put(FORMAT, "json")
					.map();
			String response = addSignedParams(client.target(FLICKR_REST_URL), params).request().get(String.class);
			verify(response);
			try (JsonParser parser = createParser(response)) {
				while (parser.nextToken() != null) {
					switch (Strings.nullToEmpty(parser.getCurrentName())) {
						case "page":
							parser.nextToken();
							if (page != parser.getIntValue()) {
								throw new TechnicalException("Page mismatch: on page #" + parser.getIntValue() +
										" but expected to be on #" + page);
							}
							break;
						case "pages":
							parser.nextToken();
							numPages = parser.getIntValue();
							break;
						case "id":
							parser.nextToken();
							result.add(parser.getValueAsString());
							break;
					}
				}
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		} while (page < numPages);
		return result;
	}

	/**
	 * Creates a new photo set.
	 *
	 * @param title    Title of the photo set
	 * @param photoIds list of photo ids to include in the photo set
	 * @return The id of the new photo set
	 */
	public String createPhotoSet(String title, List<String> photoIds) {
		if (photoIds.isEmpty()) {
			throw new TechnicalException("The list of photo IDs must not be empty");
		}
		String id = findPhotoSet(title);
		if (id == null) {
			id = createPhotoSet(title, photoIds.get(0));
		}
		editPhotoSet(id, photoIds);
		return id;
	}

	/**
	 * Creates a new photo set.
	 *
	 * @param title          Title of the photo set
	 * @param primaryPhotoId primary photo id to associate with the photo set
	 * @return The id of the new photo set
	 */
	public String createPhotoSet(String title, String primaryPhotoId) {
		Client client = ClientBuilder.newClient();
		Map<String, Object> params = newParamMap()
				.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
				.put(API_KEY, properties.getProperty(API_KEY))
				.put(METHOD, "flickr.photosets.create")
				.put("title", title)
				.put("description", "auto created on " + new Date())
				.put("primary_photo_id", primaryPhotoId)
				.put(FORMAT, "json")
				.map();
		String response = addSignedParams(client.target(FLICKR_REST_URL), params).request().get(String.class);
		verify(response);
		try (JsonParser parser = createParser(response)) {
			while (parser.nextToken() != null) {
				if ("id".equals(parser.getCurrentName())) {
					parser.nextToken();
					return parser.getValueAsString();
				}
			}
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		throw new TechnicalException("Failed to retrieve ID of the new photo set");
	}

	/**
	 * Edits a new photo set.
	 *
	 * @param id       id of the photo set
	 * @param photoIds photo ids that the photo set comprises of
	 */
	public void editPhotoSet(String id, List<String> photoIds) {
		if (photoIds.isEmpty()) {
			throw new TechnicalException("The list of photo IDs must not be empty");
		}
		Client client = ClientBuilder.newClient();
		Map<String, Object> params = newParamMap()
				.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
				.put(API_KEY, properties.getProperty(API_KEY))
				.put(METHOD, "flickr.photosets.editPhotos")
				.put("photoset_id", id)
				.put("primary_photo_id", photoIds.get(0))
				.put("photo_ids", Joiner.on(',').join(photoIds))
				.put(FORMAT, "json")
				.map();
		String response = addSignedParams(client.target(FLICKR_REST_URL), params).request().get(String.class);
		verify(response);
	}

	/**
	 * Finds id of a photo set with the specified title.
	 *
	 * @param title title of the photo set to find
	 * @return id of the photo set found or null if no photo set with the specified name exists
	 */
	public String findPhotoSet(String title) {
		Client client = ClientBuilder.newClient();
		Map<String, Object> params = newParamMap()
				.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
				.put(API_KEY, properties.getProperty(API_KEY))
				.put(METHOD, "flickr.photosets.getList")
				.put(FORMAT, "json")
				.map();
		String response = addSignedParams(client.target(FLICKR_REST_URL), params).request().get(String.class);
		verify(response);
		try (JsonParser parser = createParser(response)) {
			String provisionalId = null;
			boolean inTitle = false;
			while (parser.nextToken() != null) {
				switch (Strings.nullToEmpty(parser.getCurrentName())) {
					case "id":
						parser.nextToken();
						provisionalId = parser.getValueAsString();
						break;
					case "title":
						parser.nextToken();
						inTitle = true;
						break;
					case "_content":
						if (inTitle && title.equals(parser.getValueAsString())) {
							return provisionalId;
						}
						break;
					default:
						inTitle = false;
				}
			}
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return null;
	}

	private void verify(String response) {
		boolean success = false;
		int code = 0;
		String message = "Success response not recognized";
		try (JsonParser parser = createParser(response)) {
			while (parser.nextToken() != null) {
				switch (Strings.nullToEmpty(parser.getCurrentName())) {
					case "stat":
						parser.nextToken();
						success = "ok".equalsIgnoreCase(parser.getValueAsString());
						break;
					case "code":
						parser.nextToken();
						code = parser.getIntValue();
						break;
					case "message":
						parser.nextToken();
						message = parser.getValueAsString();
						break;
				}
			}
			if (!success) {
				throw new TechnicalException("REST call failed - " + code + ": " + message);
			}
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}

	private JsonParser createParser(String response) throws IOException {
		// extracts the text inside jsonFlickrApi(...) from the response
		return new JsonFactory().createParser(response.substring(14, response.length() - 1));
	}

	private WebTarget addSignedParams(WebTarget target, Map<String, Object> params) {
		StringBuilder signature = new StringBuilder(properties.getProperty("secret"));
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			signature.append(entry.getKey()).append(entry.getValue());
			target = target.queryParam(entry.getKey(), entry.getValue());
		}
		return target.queryParam("api_sig", md5(signature.toString()));
	}

	private String md5(String text) {
		return Hashing.md5().newHasher().putString(text, Charsets.US_ASCII).hash().toString();
	}

	private FluentMapDecorator<String, Object> newParamMap() {
		return new FluentMapDecorator<>(new TreeMap<String, Object>());
	}
}
