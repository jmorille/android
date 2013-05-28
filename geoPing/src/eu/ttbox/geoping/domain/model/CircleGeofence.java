package eu.ttbox.geoping.domain.model;

import com.google.android.gms.location.Geofence;

import eu.ttbox.osm.core.AppConstants;

/**
 * A single Geofence object, defined by its center (latitude and longitude position) and radius.
 */
public class CircleGeofence {
    // Instance variables
    public   long id = -1;
    public   String mRequestId; // Mandatory
    public   int mLatitudeE6; // Mandatory
    public   int mLongitudeE6; // Mandatory
    public   float mRadius; // Mandatory
    public long mExpirationDuration = Geofence.NEVER_EXPIRE;
    public int mTransitionType;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitudeE6 Latitude of the Geofence's center. The value is not checked for validity.
     * @param longitudeE6 Longitude of the Geofence's center. The value is not checked for validity.
     * @param radius Radius of the geofence circle. The value is not checked for validity
     * @param expiration Geofence expiration duration in milliseconds The value is not checked for
     * validity.
     * @param transition Type of Geofence transition. The value is not checked for validity.
     */ 
    public CircleGeofence(
            String geofenceId,
            int latitudeE6,
            int longitudeE6,
            float radius,
            long expiration,
            int transition) {
        // Set the instance fields from the constructor

        // An identifier for the geofence
        this.mRequestId = geofenceId;

        // Center of the geofence
        this.mLatitudeE6 = latitudeE6;
        this.mLongitudeE6 = longitudeE6;

        // Radius of the geofence, in meters
        this.mRadius = radius;

        // Expiration time in milliseconds
        this.mExpirationDuration = expiration;

        // Transition type
        this.mTransitionType = transition;
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
     * @return A SimpleGeofence ID
     */
    public String getRequestId() {
        return mRequestId;
    }

    public int getLatitudeE6() {
        return mLatitudeE6;
    }
    public int getLongitudeE6() {
        return mLongitudeE6;
    }


    /**
     * Get the geofence latitude
     * @return A latitude value
     */
    public double getLatitude() {
        return mLatitudeE6 / AppConstants.E6;
    }

    /**
     * Get the geofence longitude
     * @return A longitude value
     */
    public double getLongitude() {
        return mLongitudeE6/ AppConstants.E6;
    }

    /**
     * Get the geofence radius
     * @return A radius value
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * Get the geofence expiration duration
     * @return Expiration duration in milliseconds
     */
    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    /**
     * Get the geofence transition type
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                       .setRequestId(getRequestId())
                       .setTransitionTypes(mTransitionType)
                       .setCircularRegion(
                               getLatitude(),
                               getLongitude(),
                               getRadius())
                       .setExpirationDuration(mExpirationDuration)
                       .build();
    }
}