package de.blogspot.soahowto.ppm;

import com.google.appengine.repackaged.com.google.common.base.MoreObjects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Properties;

import static de.blogspot.soahowto.ppm.ListSelectionUtils.selectWithTopBias;

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
				int size = Integer.parseInt(MoreObjects.firstNonNull(req.getParameter("size"), "500"));
				int top = Integer.parseInt(MoreObjects.firstNonNull(req.getParameter("top"), "0"));
				int topSize = Integer.parseInt(MoreObjects.firstNonNull(req.getParameter("topSize"), "0"));
				String id = flickrService.createOrUpdatePhotoSet(title, selectWithTopBias(flickrService.getAllPhotoIds(), size, top, topSize));
				resp.getWriter().println("Successfully created/updated photo set with id " + id);
			} catch (RuntimeException e) {
				resp.getWriter().println("Failed due to exception " + e);
				e.printStackTrace(resp.getWriter());
			}
		}
	}

}