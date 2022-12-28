package de.blogspot.soahowto.ppm;

import com.google.appengine.repackaged.com.google.common.base.MoreObjects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import static de.blogspot.soahowto.ppm.ListSelectionUtils.selectRandom;
import static de.blogspot.soahowto.ppm.ListSelectionUtils.selectRolling;

public class PhotosetServlet extends HttpServlet {
	private FlickrService flickrService = new FlickrService();

	private int getEpochDay() {
		LocalDate now = LocalDate.now();
		LocalDate epoch = LocalDate.ofEpochDay(0);

		return (int)ChronoUnit.DAYS.between(epoch, now);
	}

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
				int modulus = Integer.parseInt(MoreObjects.firstNonNull(req.getParameter("modulus"), "0"));
				List<String> allPhotoIds = flickrService.getAllPhotoIds();
				List<String> photoIds = (modulus == 0) ? selectRandom(allPhotoIds, size) : selectRolling(allPhotoIds, size, modulus, getEpochDay());
				String id = flickrService.createOrUpdatePhotoSet(title, photoIds);
				resp.getWriter().println("Successfully created/updated photo set with id " + id);
			} catch (RuntimeException e) {
				resp.getWriter().println("Failed due to exception " + e);
				e.printStackTrace(resp.getWriter());
			}
		}
	}

}