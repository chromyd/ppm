package de.blogspot.soahowto.ppm;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.util.Properties;

public class PhotosetServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (req.getParameter("testing") != null) {
      resp.setContentType("text/plain");
      resp.getWriter().println("Hello, this is a testing servlet. \n\n");
      Properties p = System.getProperties();
      p.list(resp.getWriter());

    } else {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello, " + req.getParameter("name"));
	    Properties p = new Properties();
	    p.load(getClass().getResourceAsStream("/photoset.properties"));
	    resp.getWriter().println("Token starts with " + p.getProperty("auth_token").substring(0,3));
	    Client client = ClientBuilder.newClient();
	    String response = client.target("https://api.flickr.com/services/rest/")
			    .queryParam("auth_token", p.getProperty("auth_token"))
			    .queryParam("api_key", p.getProperty("api_key"))
			    .queryParam("method", "flickr.photos.getWithoutGeoData")
			    .queryParam("page", 12)
			    .queryParam("api_sig", "fb5c9d5d4f8e82d8e51ac00a29bac55d")
			    .request().get(String.class);
	    resp.getWriter().println("Response:\n" + response);
    }
  }
}