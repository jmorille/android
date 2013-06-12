package eu.ttbox.geoping.domain.model;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import com.google.android.gms.location.Geofence;

import eu.ttbox.osm.core.AppConstants;

/**
 * A single Geofence object, defined by its center (latitude and longitude
 * position) and radius.
 */
public class CircleGeofence {
    // Instance variables
    public long id = -1l;
    public String name;
    public String requestId; // Mandatory
    private int latitudeE6; // Mandatory
    private int longitudeE6; // Mandatory
    public int radiusInMeters; // Mandatory
    public long expirationDuration = Geofence.NEVER_EXPIRE;
    public int transitionType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

    public IGeoPoint cachedGeoPoint;
//    private double latitude;
//    private double longitude;

    public CircleGeofence() {
        super();
    }

    public CircleGeofence(IGeoPoint center, int radiusInMeters) {
        super();
        this.radiusInMeters = radiusInMeters;
        setCenter(center);
    }

    public CircleGeofence(CircleGeofence other) {
        super();
        this.id = other.id;
        this.name = other.name;
        this.requestId = other.requestId;
        this.latitudeE6 = other.latitudeE6;
        this.longitudeE6 = other.longitudeE6;
        this.radiusInMeters = other.radiusInMeters;
        this.expirationDuration = other.expirationDuration;
        this.transitionType = other.transitionType;
    }

    /**
     * @param geofenceId
     *            The Geofence's request ID
     * @param latitudeE6
     *            Latitude of the Geofence's center. The value is not checked
     *            for validity.
     * @param longitudeE6
     *            Longitude of the Geofence's center. The value is not checked
     *            for validity.
     * @param radius
     *            Radius of the geofence circle. The value is not checked for
     *            validity
     * @param expiration
     *            Geofence expiration duration in milliseconds The value is not
     *            checked for validity.
     * @param transition
     *            Type of Geofence transition. The value is not checked for
     *            validity.
     */
    public CircleGeofence(String geofenceId, int latitudeE6, int longitudeE6, int radius, long expiration, int transition) {
        // Set the instance fields from the constructor

        // An identifier for the geofence
        this.requestId = geofenceId;

        // Center of the geofence
        this.latitudeE6 = latitudeE6;
        this.longitudeE6 = longitudeE6;

        // Radius of the geofence, in meters
        this.radiusInMeters = radius;

        // Expiration time in milliseconds
        this.expirationDuration = expiration;

        // Transition type
        this.transitionType = transition;
    }

    // Instance field getters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the geofence ID
     * 
     * @return A SimpleGeofence ID
     */
    public String getRequestId() {
        return requestId;
    }

    public int getLatitudeE6() {
        return latitudeE6;
    }

    public int getLongitudeE6() {
        return longitudeE6;
    }

    public IGeoPoint getCenterAsGeoPoint() {
        IGeoPoint center = cachedGeoPoint;
        if (center == null) {
            center = new GeoPoint(latitudeE6, longitudeE6, 0);
            this.cachedGeoPoint = center;
        }
        return center;
    }

    public CircleGeofence setCenter(IGeoPoint center) {
        this.latitudeE6 = center.getLatitudeE6();
        this.longitudeE6 = center.getLongitudeE6();
        this.cachedGeoPoint = center;
        return this;
    }

    public CircleGeofence setRequestId(String mRequestId) {
        this.requestId = mRequestId;
        return this;
    }

    public CircleGeofence setLatitudeE6(int mLatitudeE6) {
        this.latitudeE6 = mLatitudeE6;
        this.cachedGeoPoint = null;
        return this;
    }

    public CircleGeofence setLongitudeE6(int mLongitudeE6) {
        this.longitudeE6 = mLongitudeE6;
        this.cachedGeoPoint = null;
        return this;
    }

    public CircleGeofence setRadiusInMeters(int mRadius) {
        this.radiusInMeters = mRadius;
        return this;
    }

    public CircleGeofence setExpirationDuration(long mExpirationDuration) {
        this.expirationDuration = mExpirationDuration;
        return this;
    }

    public CircleGeofence setTransitionType(int mTransitionType) {
        this.transitionType = mTransitionType;
        return this;
    }

    public CircleGeofence setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the geofence latitude
     * 
     * @return A latitude value
     */
    public double getLatitude() {
        return latitudeE6 / AppConstants.E6;
    }

    /**
     * Get the geofence longitude
     * 
     * @return A longitude value
     */
    public double getLongitude() {
        return longitudeE6 / AppConstants.E6;
    }

    /**
     * Get the geofence radius
     * 
     * @return A radius value
     */
    public int getRadiusInMeters() {
        return radiusInMeters;
    }

    /**
     * Get the geofence expiration duration
     * 
     * @return Expiration duration in milliseconds
     */
    public long getExpirationDuration() {
        return expirationDuration;
    }

    /**
     * Get the geofence transition type
     * 
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return transitionType;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     * 
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder().setRequestId(requestId)//
                .setCircularRegion(getLatitude(), getLongitude(), getRadiusInMeters())//
                .setTransitionTypes(transitionType)//
                .setExpirationDuration(expirationDuration)//
                .build();
    }

    public boolean isGeofence(Geofence testGeofence) {
        return testGeofence.getRequestId().equals(requestId);
    }

    @Override
    public String toString() {
        return "CircleGeofence [id=" + id //
                + ", name=" + name //
                + ", latitudeE6=" + latitudeE6 + ", longitudeE6=" + longitudeE6 //
                + ", radius=" + radiusInMeters //
                + ", requestId=" + requestId //
                + ", transitionType=" + transitionType //
                + ", expirationDuration=" + expirationDuration //
                + "]";
    }

}
