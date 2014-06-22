package de.blogspot.soahowto.ppm;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PhotosetServlet extends HttpServlet {
	private FlickrService flickrService = new FlickrService();

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType(MediaType.TEXT_PLAIN);
		if (req.getParameter("testing") != null) {
			resp.getWriter().println("Hello, this is a testing servlet. \n\n");
			Properties p = System.getProperties();
			p.list(resp.getWriter());
		} else {
			try {
				String title = req.getParameter("title");
				int size = Integer.parseInt(req.getParameter("size"));
				List<String> photoIds = flickrService.getAllPhotoIds();
				Collections.shuffle(photoIds);
				String id = flickrService.createPhotoSet(title, photoIds.subList(0, size));
				resp.getWriter().println("Successfully created/updated photo set with id " + id);
			} catch (RuntimeException e) {
				resp.getWriter().println("Failed due to exception " + e);
				e.printStackTrace(resp.getWriter());
			}
		}
	}

}