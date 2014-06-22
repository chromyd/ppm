package de.blogspot.soahowto.ppm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
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

	public List<String> getAllPhotos() {
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
			String response = addSignedParams(client.target(FLICKR_REST_URL), params)
					.request().get(String.class);
			try {
				// extracts the text inside jsonFlickrApi(...) from the response
				JsonParser parser = new JsonFactory().createParser(response.substring(14, response.length() - 1));
				while (parser.nextToken() != null) {
					switch (Strings.nullToEmpty(parser.getCurrentName())) {
						case "page":
							parser.nextToken();
							if (page != parser.getIntValue()) {
								throw new RuntimeException("Page mismatch: on page #" + parser.getIntValue() +
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
				parser.close();
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		} while (page < numPages);
		return result;
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
