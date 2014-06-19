package de.blogspot.soahowto.ppm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.google.appengine.repackaged.com.google.common.base.Stopwatch;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.com.google.common.hash.Hashing;
import com.google.cloud.sql.jdbc.internal.Charsets;
import jersey.repackaged.com.google.common.base.Objects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class PhotosetServlet extends HttpServlet {
	public static final String FLICKR_REST_URL = "https://api.flickr.com/services/rest/";
	public static final String AUTH_TOKEN = "auth_token";
	public static final String API_KEY = "api_key";
	public static final String METHOD = "method";
	public static final String PER_PAGE = "per_page";
	public static final String FORMAT = "format";

	private final Properties properties;

	public PhotosetServlet() {
		properties = new Properties();
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("testing") != null) {
			resp.setContentType(MediaType.TEXT_PLAIN);
			resp.getWriter().println("Hello, this is a testing servlet. \n\n");
			Properties p = System.getProperties();
			p.list(resp.getWriter());
		} else {
			resp.setContentType(MediaType.TEXT_PLAIN);
			properties.load(getClass().getResourceAsStream("/photoset.properties"));
			switch (Strings.nullToEmpty(req.getParameter("action"))) {
				case "create": {
					Client client = ClientBuilder.newClient();
					Map<String, Object> params = newParamMap()
							.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
							.put(API_KEY, properties.getProperty(API_KEY))
							.put(METHOD, "flickr.photosets.create")
							.put("title", "daily500")
							.put("description", "auto created on " + new Date())
							.put("primary_photo_id", "12028509755")
							.map();
					String response = addSignedParams(client.target(FLICKR_REST_URL), params)
							.request().get(String.class);
					resp.getWriter().println("Response:\n" + response);
					break;
				}
				case "jsond": {
					Client client = ClientBuilder.newClient();
					Map<String, Object> params = newParamMap()
							.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
							.put(API_KEY, properties.getProperty(API_KEY))
							.put(METHOD, "flickr.photos.getWithoutGeoData")
							.put(PER_PAGE, 5)
							.put(FORMAT, "json")
							.map();
					String response = addSignedParams(client.target(FLICKR_REST_URL), params)
							.request().get(String.class);
					resp.getWriter().println("Response:\n" + response);
					break;
				}
				case "json": {
					Stopwatch watch = Stopwatch.createStarted();
					int page = 0;
					int numPages = -1;
					int perPage = Integer.valueOf(Objects.firstNonNull(req.getParameter(PER_PAGE), "500"));
					do {
						Client client = ClientBuilder.newClient();
						Map<String, Object> params = newParamMap()
								.put(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
								.put(API_KEY, properties.getProperty(API_KEY))
								.put(METHOD, "flickr.photos.getWithoutGeoData")
								.put(PER_PAGE, perPage)
								.put("page", ++page)
								.put(FORMAT, "json")
								.map();
						String response = addSignedParams(client.target(FLICKR_REST_URL), params)
								.request().get(String.class);
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
									// use parser.getValueAsString()
									break;
							}
						}
						parser.close();
					} while (page < numPages);
					resp.getWriter().println("Read " + page + " pages with " + perPage + " items per page.");
					resp.getWriter().println("Time " + watch);
					break;
				}
				default: {
					Client client = ClientBuilder.newClient();
					String response = client.target(FLICKR_REST_URL)
							.queryParam(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
							.queryParam(API_KEY, properties.getProperty(API_KEY))
							.queryParam(METHOD, "flickr.photos.getWithoutGeoData")
							.queryParam("page", 12)
							.queryParam("api_sig", "fb5c9d5d4f8e82d8e51ac00a29bac55d")
							.request().get(String.class);
					resp.getWriter().println("Response:\n" + response);
				}
			}
		}
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