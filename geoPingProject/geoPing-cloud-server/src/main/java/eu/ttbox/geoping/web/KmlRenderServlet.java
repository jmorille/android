package eu.ttbox.geoping.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import eu.ttbox.geoping.web.kml.KmlPolygon;


/**
 * <a href="https://code.google.com/p/javaapiforkml/">Java Kml API Lib</a>
 * <a href="http://labs.micromata.de/display/jak/Home">Java Kml API Doc</a>
 */
public class KmlRenderServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(KmlRenderServlet.class.getName());

    private static final String KEY_MESSAGE = "q";

    private static final String CONTENT_TYPE_KML = "application/vnd.google-earth.kml+xml";
    private static final String CONTENT_TYPE_KMZ = "application/vnd.google-earth.kmz";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = req.getParameter(KEY_MESSAGE);

        Kml kml = convertAsKml(message);

        // write response
        resp.setContentType(CONTENT_TYPE_KML);
        resp.setStatus(200);
        kml.marshal(resp.getWriter());
        resp.getWriter().flush();
        resp.flushBuffer();

    }


    private Kml convertAsKml( String message ) {
        double lat = 9.444652669565212;
        double lng = 51.30473589438118;
        double alt = 0;
        int radius = 100;
        // Create <Point> and set values.
        Point point = KmlFactory.createPoint()  //
         .withExtrude(false);
        point.getCoordinates().add(new Coordinate(lat, lng, alt));


// TODO        Polygon circle = KmlPolygon.kmlRegularPolygonKml(lat, lng, radius, null, null);


        // Create <Placemark> and set values.
        Placemark placemark = KmlFactory.createPlacemark() //
         .withName("Java User Group Hessen - JUGH!") //
         .withVisibility(true) //
         .withOpen(true) //
         .withDescription("die Java User Group Hessen")
         .withGeometry(point);

        // Create Kml
        Kml kml = KmlFactory.createKml()
                .withFeature(placemark);
        return kml;
    }
}
