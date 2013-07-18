package eu.ttbox.geoping.web.kml;


import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * https://github.com/mbrookes/kml_polygon/blob/master/lib/kml_polygon.rb
 * https://code.google.com/p/kmlcircle/source/browse/trunk/python/kmlcircle.py
 * #
 * # Examples
 * #
 * # puts KmlPolygon::kmlStar(45,45, 70000, 50000)
 * # puts KmlPolygon::kmlRegularPolygon(50, 50, 70000)
 */
public class KmlPolygon {

    // constant to convert to degrees
    private static double DEGREES = 180.0 / Math.PI;
    // constant to convert to radians
    private static double RADIANS = Math.PI / 180.0;
    // Mean Radius of Earth in meters
    private static double EARTH_MEAN_RADIUS = 6371.0 * 1000;

    /**
     * Convert [x,y,z] on unit sphere
     * back to [longitude, latitude])
     */
    public static double[] toEarth(double[] point) {
        double lon = point[0] == 0.0 ? Math.PI / 2.0 : Math.atan(point[1] / point[0]);
        double lat = Math.PI / 2.0 - Math.acos(point[2]);

        // select correct branch of arctan
        if (point[0] < 0.0d) {
            lon = point[1] <= 0.0 ? -(Math.PI - lon) : Math.PI + lon;
        }
        double[] result = new double[]{lon * DEGREES, lat * DEGREES};

        return result;
    }

    /**
     * convert long, lat IN RADIANS to (x,y,z)
     *
     * @return
     */
    public static double[] toCart(double longitude, double latitude) {
        double theta = longitude;
        // spherical coordinate use "co-latitude", not "lattitude"
        // lattiude = [-90, 90] with 0 at equator
        // co-latitude = [0, 180] with 0 at north pole
        double phi = Math.PI / 2.0 - latitude;
        return new double[]{Math.cos(theta) * Math.sin(phi), Math.sin(theta) * Math.sin(phi), Math.cos(phi)};
    }

    /**
     * rotate point pt, around unit vector vec by phi radians
     * http://blog.modp.com/2007/09/rotating-point-around-vector.html
     */
    public static  double[] rotatePoint(double[] vec, double[] pt, double phi) {
        //remap vector for sanity
        double u = vec[0];
        double v = vec[1];
        double w = vec[2];
        double x = pt[0];
        double y = pt[1];
        double z = pt[2];

        double a = u * x + v * y + w * z;
        double d = Math.cos(phi);
        double e = Math.sin(phi);

        return new double[]{(a * u + (x - a * u) * d + (v * z - w * y) * e),
                (a * v + (y - a * v) * d + (w * x - u * z) * e),
                (a * w + (z - a * w) * d + (u * y - v * x) * e)};
    }

    /**
     * spoints -- get raw list of points in longitude,latitude format
     * <p/>
     * radius: radius of polygon in meters
     * sides:  number of sides
     * rotate: rotate polygon by number of degrees
     * <p/>
     * Returns a list of points comprising the object
     */
    public static double[][] spoints(double lon, double lat, double radius, Integer _sides, Double _rotate) {
        int sides = _sides == null ? _sides : 20;
        int rotate = _rotate == null ? 0 : _rotate.intValue();

        double rotate_radians = rotate * RADIANS;

        // compute longitude degrees (in radians) at given latitude
        double r = radius / (EARTH_MEAN_RADIUS * Math.cos(lat * RADIANS));

        double[] vector = toCart(lon * RADIANS, lat * RADIANS);
        double[] point = toCart(lon * RADIANS + r, lat * RADIANS);
        // TODO Check ind i and slides +2
        double[][] points = new double[sides + 1][2];
        for (int i = 0; i < sides; i++) {
            points[i] = toEarth(rotatePoint(vector, point, rotate_radians + (2.0 * Math.PI / sides) * i));
        }

        //connect to starting point exactly
        // not sure if required, but seems to help when
        // the polygon is not filled
        points[sides] = points[0];
        return points;
    }

    /**
     * Output points formatted as a KML string
     * <p/>
     * You may want to edit this function to change "extrude" and other XML nodes.
     */
    public static String pointsToKml(double[][] points) {
        StringBuilder kml_string = new StringBuilder();
        kml_string.append("<Polygon>\n");
        kml_string.append("  <outerBoundaryIs><LinearRing><coordinates>\n");

        // int pointSize = points.length;
//        for (int i= 0; i <pointSize ; i++) {
        //          double[] point = points[i];
        for (double[] point : points) {
            kml_string.append("    ").append(point[0]).append(',').append(point[1]).append("\n");
        }

        kml_string.append("  </coordinates></LinearRing></outerBoundaryIs>\n");
        kml_string.append("</Polygon>\n");

        // kml_string.append(  "  <extrude>1</extrude>\sides"
        // kml_string.append(  "  <altitudeMode>clampToGround</altitudeMode>\sides"
        return kml_string.toString();
    }

    public static Polygon pointsToKmlPolygon(double[][] points) {
        LinearRing linearRing = KmlFactory.createLinearRing();
        List<Coordinate> coords = linearRing.getCoordinates();

        for (double[] point : points) {
            coords.add(new Coordinate(point[0], point[1]));
        }
         // kml_string *.append(  "  <extrude>1</extrude>\sides"
        // kml_string *.append(  "  <altitudeMode>clampToGround</altitudeMode>\sides"
        Polygon polygon = KmlFactory.createPolygon()
                .withOuterBoundaryIs( KmlFactory.createBoundary().withLinearRing(linearRing));
         return polygon;
    }

    /**
     * kml_regular_polygon    - Regular polygon
     * <p/>
     * (lon, lat)            - center point in decimal degrees
     * radius                - radius in meters
     * segments              - number of sides, > 20 looks like a circle (optional, default: 20)
     * rotate                - rotate polygon by number of degrees (optional, default: 0)
     * <p/>
     * Returns a string suitable for adding into a KML file.
     */
    public static String kmlRegularPolygon(double lon, double lat, double radius, Integer segments, Double rotate) {
        return pointsToKml(spoints(lon, lat, radius, segments, rotate));
    }

    /**
     * kml_star - Make a "star" or "burst" pattern
     * <p/>
     * (lon, lat)            - center point in decimal degrees
     * radius                - radius in meters
     * innner_radius         - radius in meters, typically < outer_radius
     * segments              - number of "points" on the star (optional, default: 10)
     * rotate                - rotate polygon by number of degrees (optional, default: 0)
     * <p/>
     * Returns a string suitable for adding into a KML file.
     */
    public static String kmlStar(double lon, double lat, double radius, double inner_radius, Integer _segments, Double rotate) {
        int segments = _segments == null ? 10 : _segments.intValue();
        double[][] outer_points = spoints(lon, lat, radius, segments, rotate);
        double[][] inner_points = spoints(lon, lat, inner_radius, segments, rotate + 180.0 / segments);

        // interweave the radius and inner_radius points
        // I'm sure there is a better way
        int outerPointSize = outer_points.length;
        int pointSize = outerPointSize + outerPointSize - 1;
        double[][] points = new double[pointSize][2];
        int idx = 0;
        for (int i = 0; i < outerPointSize; i++) {
            points[idx++] = outer_points[i];
            // MTB - Drop the last overlapping point leaving start and end points connecting
            // (resulting output differs from orig, but is more correct)
            if (idx < pointSize) {
                points[idx++] = inner_points[i];
            }

        }
        return pointsToKml(points);
    }

    /**
     * kml_regular_polygon    - Regular polygon
     * <p/>
     * (lon, lat)            - center point in decimal degrees
     * radius                - radius in meters
     * segments              - number of sides, > 20 looks like a circle (optional, default: 20)
     * rotate                - rotate polygon by number of degrees (optional, default: 0)
     * <p/>
     * Returns a string suitable for adding into a KML file.
     */
    public static Polygon kmlRegularPolygonKml(double lon, double lat, double radius, Integer segments, Double rotate) {
        return pointsToKmlPolygon(spoints(lon, lat, radius, segments, rotate));
    }

    /**
     * kml_star - Make a "star" or "burst" pattern
     * <p/>
     * (lon, lat)            - center point in decimal degrees
     * radius                - radius in meters
     * innner_radius         - radius in meters, typically < outer_radius
     * segments              - number of "points" on the star (optional, default: 10)
     * rotate                - rotate polygon by number of degrees (optional, default: 0)
     * <p/>
     * Returns a string suitable for adding into a KML file.
     */
    public static Polygon kmlStarPolygon(double lon, double lat, double radius, double inner_radius, Integer _segments, Double rotate) {
        int segments = _segments == null ? 10 : _segments.intValue();
        double[][] outer_points = spoints(lon, lat, radius, segments, rotate);
        double[][] inner_points = spoints(lon, lat, inner_radius, segments, rotate + 180.0 / segments);

        // interweave the radius and inner_radius points
        // I'm sure there is a better way
        int outerPointSize = outer_points.length;
        int pointSize = outerPointSize + outerPointSize - 1;
        double[][] points = new double[pointSize][2];
        int idx = 0;
        for (int i = 0; i < outerPointSize; i++) {
            points[idx++] = outer_points[i];
            // MTB - Drop the last overlapping point leaving start and end points connecting
            // (resulting output differs from orig, but is more correct)
            if (idx < pointSize) {
                points[idx++] = inner_points[i];
            }

        }
        return pointsToKmlPolygon(points);
    }
}
