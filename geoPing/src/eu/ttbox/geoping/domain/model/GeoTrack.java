package eu.ttbox.geoping.domain.model;

import java.util.Date;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.cache.ZoomLevelComputeCache;

public class GeoTrack implements Comparable<GeoTrack>{

 
    
    public long id = AppConstants.UNSET_ID;
    public long personId =  AppConstants.UNSET_ID;

    public String phone;
    public String provider;
    public String address;

    public long time = AppConstants.UNSET_TIME;
    private int latitudeE6;
    private int longitudeE6;

    private boolean hasLatitude = false;
    private boolean hasLlongitude = false;

    // Optionnal
    public int batteryLevelInPercent = -1;
    public String requesterPersonPhone ;
    
    // Optionnal
    private int altitude;
    public int accuracy = -1;
    public int bearing = -1;
    public int speed = -1;

    private boolean hasAltitude = false;
//    private boolean hasAccuracy = false;
//    private boolean hasBearing = false;
//    private boolean hasSpeed = false;

    // Other
    public String titre;

    private GeoPoint cachedGeoPoint;
    private transient ZoomLevelComputeCache cachedZoomLevelComputeCache;

    public GeoTrack() {
    }

    public GeoTrack(String phone, Location loc) {
        this.phone = phone;
        this.provider = loc.getProvider(); 
        setTime(loc.getTime());
        setLatitude(loc.getLatitude());
        setLongitude(loc.getLongitude());
         setAccuracy((int) loc.getAccuracy());
        if (loc.hasAltitude()) {
            setAltitude((int)loc.getAltitude()); 
        }
        if (loc.hasBearing()) {
            setBearing((int) loc.getBearing());
         }
        if (loc.hasSpeed()) {
            setSpeed( (int) loc.getSpeed());
         }
    }

    public Location asLocation() {
        Location loc = new Location(provider);
        loc.setTime(time);
        loc.setLatitude(latitudeE6 / AppConstants.E6);
        loc.setLongitude(longitudeE6 / AppConstants.E6);
        loc.setAccuracy(accuracy);

        loc.setAltitude(altitude);
        loc.setBearing(bearing);
        loc.setSpeed(speed);

        return loc;
    }

    public GeoPoint asGeoPoint() {
        if (cachedGeoPoint == null) {
            cachedGeoPoint = new GeoPoint(latitudeE6, longitudeE6, altitude);
        }
        return cachedGeoPoint;
    }

    public long getId() {
        return id;
    }

    public String getIdAsString() {
        String result = null;
        if (id != -1) {
            result = String.valueOf(id);
        }
        return result;
    }

    public Long getIdAsLong() {
        Long result = null;
        if (id != -1) {
            result = Long.valueOf(id);
        }
        return result;
    }

    public GeoTrack setId(long id) {
        this.id = id;
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public GeoTrack setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public long getTime() {
        return time;
    }

  
    public Date getTimeAsDate() {
        if (time == AppConstants.UNSET_TIME) {
            return null;
        }
        Date timeAsDate = new Date(time);
        return timeAsDate;
    }

    public GeoTrack setTime(long time) {
        this.time = time;
        return this;
    }

    public double getLatitude() {
        return latitudeE6 / AppConstants.E6;
    }

    public GeoTrack setLatitude(double latitude) {
        return setLatitudeE6((int) (latitude * AppConstants.E6));
    }

    public GeoTrack setLatitudeE6(int latitudeE6) {
        this.latitudeE6 = latitudeE6;
        this.hasLatitude = true;
        clearLatLngCache();
        return this;
    }

    public double getLongitude() {
        return longitudeE6 / AppConstants.E6;
    }

    public GeoTrack setLongitude(double longitude) {
        return setLongitudeE6((int) (longitude * AppConstants.E6));
    }

    public GeoTrack setLongitudeE6(int longitudeE6) {
        this.longitudeE6 = longitudeE6;
        this.hasLlongitude = true;
        clearLatLngCache();
        return this;
    }

    public int getLatitudeE6() {
        return latitudeE6;
    }

    public int getLongitudeE6() {
        return longitudeE6;
    }

    public int getAltitude() {
        return altitude;
    }

    public GeoTrack setAltitude(int altitude) {
        this.altitude = (int) altitude;
        this.hasAltitude = true;
        return this;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public GeoTrack setAccuracy(int accuracy) {
        this.accuracy = accuracy;
//        this.hasAccuracy = true;
        return this;
    }

    public int getBearing() {
        return bearing;
    }

    public GeoTrack setBearing(int bearing) {
        this.bearing = bearing;
//        this.hasBearing = true;
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    public GeoTrack setSpeed(int speed) {
        this.speed = speed;
//        this.hasSpeed = true;
        return this;
    }

    public String getTitre() {
        return titre;
    }

    public GeoTrack setTitre(String titre) {
        this.titre = titre;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public GeoTrack setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public GeoTrack setAddress(String address) {
        this.address = address;
        return this;
    }

    public GeoTrack setPersonId(long personId) {
        this.personId = personId;
        return this;
    }

    

    public int getBatteryLevelInPercent() {
        return batteryLevelInPercent;
    }

    public GeoTrack setBatteryLevelInPercent(int batteryLevelInPercent) {
        this.batteryLevelInPercent = batteryLevelInPercent;
        return this;
    }

    public String getRequesterPersonPhone() {
        return requesterPersonPhone;
    }

    public GeoTrack setRequesterPersonPhone(String requesterPersonPhone) {
        this.requesterPersonPhone = requesterPersonPhone;
        return this;
    }
    
    // ===========================================================
    // Setter Value Test
    // ===========================================================


    public boolean hasTime() {
        return  time != AppConstants.UNSET_TIME;
    }
    
    
    public boolean hasProvider() {
        return provider != null;
    }
    
    public boolean hasPersonId() {
        return personId != -1L;
    }

    public boolean hasPhone() {
        return phone != null;
    }

    public boolean hasLatitude() {
        return hasLatitude;
    }

    public boolean hasLongitude() {
        return hasLlongitude;
    }

    public boolean hasAltitude() {
        return hasAltitude;
    }

    public boolean hasLatLng() {
        return hasLatitude && hasLlongitude;
    }


    
    public boolean hasAccuracy() {
        return this.accuracy != -1;
    }

    public boolean hasBearing() {
        return this.bearing != -1;
    }

    public boolean hasSpeed() {
        return this.speed != -1;
    }

    public boolean hasAddress() {
        return this.address != null && this.address.length() > 0;
    }
    
    public boolean hasRequesterPersonPhone() {  
        return this.requesterPersonPhone != null && this.requesterPersonPhone.length() > 0;
    }
    public boolean hasBatteryLevelInPercent() { 
        return this.batteryLevelInPercent > -1;
    }

    // ===========================================================
    // Business
    // ===========================================================

    private void clearLatLngCache() {
        cachedGeoPoint = null;
        cachedZoomLevelComputeCache = null;
    }

    public float computeGroundResolutionInMForZoomLevel(int zoomLevel) {
        if (cachedZoomLevelComputeCache == null) {
            cachedZoomLevelComputeCache = new ZoomLevelComputeCache(getLatitude());
         }
        return cachedZoomLevelComputeCache.computeGroundResolutionInMForZoomLevel(zoomLevel);
    }

    // ===========================================================
    // Override
    // ===========================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("GeoTrack [");
        sb.append("id=").append(id)//
                .append(", phone=").append(phone)//
                .append(", provider=").append(provider)//
                .append(", latitudeE6=").append(latitudeE6)//
                .append(", longitudeE6=").append(longitudeE6)//
                // .append(", time=").append(time) //
                .append(", time=").append(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", time));

        sb.append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(GeoTrack another) {
        long rhs = another.time;
        return time < rhs ? -1 : (time == rhs ? 0 : 1);
     }

  


}
