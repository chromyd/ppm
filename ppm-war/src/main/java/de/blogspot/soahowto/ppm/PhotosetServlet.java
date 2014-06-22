package de.blogspot.soahowto.ppm;

import com.google.appengine.repackaged.com.google.common.base.Stopwatch;
import com.google.appengine.repackaged.com.google.common.base.Strings;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class PhotosetServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("testing") != null) {
			resp.setContentType(MediaType.TEXT_PLAIN);
			resp.getWriter().println("Hello, this is a testing servlet. \n\n");
			Properties p = System.getProperties();
			p.list(resp.getWriter());
		} else {
			resp.setContentType(MediaType.TEXT_PLAIN);
			switch (Strings.nullToEmpty(req.getParameter("action"))) {
				case "create": {
					/*
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
					*/
				}
				case "jsond": {
					/*
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
					*/
				}
				case "json": {
					Stopwatch watch = Stopwatch.createStarted();
					List<String> photos = new FlickrService().getAllPhotos();
					resp.getWriter().println("Got " + photos.size() + " in " + watch);
					break;
				}
				default: {
					/*
					Client client = ClientBuilder.newClient();
					String response = client.target(FLICKR_REST_URL)
							.queryParam(AUTH_TOKEN, properties.getProperty(AUTH_TOKEN))
							.queryParam(API_KEY, properties.getProperty(API_KEY))
							.queryParam(METHOD, "flickr.photos.getWithoutGeoData")
							.queryParam("page", 12)
							.queryParam("api_sig", "fb5c9d5d4f8e82d8e51ac00a29bac55d")
							.request().get(String.class);
					resp.getWriter().println("Response:\n" + response);
					*/
				}
			}
		}
	}

}