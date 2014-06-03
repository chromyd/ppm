package de.blogspot.soahowto.ppm;

import com.google.appengine.repackaged.com.google.common.hash.Hashing;
import com.google.cloud.sql.jdbc.internal.Charsets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class PhotosetServlet extends HttpServlet {
	private final Properties properties;

	public PhotosetServlet() {
		properties = new Properties();
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println("inside doGet");
		if (req.getParameter("testing") != null) {
			resp.setContentType("text/plain");
			resp.getWriter().println("Hello, this is a testing servlet. \n\n");
			Properties p = System.getProperties();
			p.list(resp.getWriter());
		} else {
			resp.setContentType("text/plain");
			resp.getWriter().println("Hello, " + req.getParameter("name"));
			properties.load(getClass().getResourceAsStream("/photoset.properties"));
			resp.getWriter().println("Token starts with " + properties.getProperty("auth_token").substring(0, 3));
			switch (req.getParameter("name")) {
				case "create": {
					Client client = ClientBuilder.newClient();
					Map<String, String> params = newParamMap()
							.put("auth_token", properties.getProperty("auth_token"))
							.put("api_key", properties.getProperty("api_key"))
							.put("method", "flickr.photosets.create")
							.put("title", "daily500")
							.put("description", "auto created on " + new Date())
							.put("primary_photo_id", "12028509755")
							.map();
					String response = addSignedParams(client.target("https://api.flickr.com/services/rest/"), params)
							.request().get(String.class);
					resp.getWriter().println("Response:\n" + response);
					break;
				}
				default: {
					Client client = ClientBuilder.newClient();
					String response = client.target("https://api.flickr.com/services/rest/")
							.queryParam("auth_token", properties.getProperty("auth_token"))
							.queryParam("api_key", properties.getProperty("api_key"))
							.queryParam("method", "flickr.photos.getWithoutGeoData")
							.queryParam("page", 12)
							.queryParam("api_sig", "fb5c9d5d4f8e82d8e51ac00a29bac55d")
							.request().get(String.class);
					resp.getWriter().println("Response:\n" + response);
				}
			}
		}
	}

	private WebTarget addSignedParams(WebTarget target, Map<String, String> params) {
		StringBuffer signature = new StringBuffer(properties.getProperty("secret"));
		for (Map.Entry<String, String> entry : params.entrySet()) {
			signature.append(entry.getKey()).append(entry.getValue());
			target = target.queryParam(entry.getKey(), entry.getValue());
		}
		System.out.println("Signature: " + signature);
		return target.queryParam("api_sig", md5(signature.toString()));
	}

	private String md5(String text) {
		return Hashing.md5().newHasher().putString(text, Charsets.US_ASCII).hash().toString();
	}

	private FluentMapDecorator<String, String> newParamMap() {
		return new FluentMapDecorator<>(new TreeMap<String, String>());
	}
}