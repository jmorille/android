package eu.ttbox.geoping.domain.geotrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.TextView;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

public class GeoTrackHelper {

    boolean isNotInit = true;

    public int idIdx = -1;
    public int userIdIdx = -1;
    public int timeIdx = -1;
    public int providerIdx = -1;
    public int latitudeE6Idx = -1;
    public int longitudeE6Idx = -1;
    public int accuracyIdx = -1;

    public int altitudeIdx = -1;
    public int bearingIdx = -1;
    public int speedIdx = -1;

    // public int titreIdx = -1;

    public GeoTrackHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ID);
        userIdIdx = cursor.getColumnIndex(GeoTrackColumns.COL_USERID);
        timeIdx = cursor.getColumnIndex(GeoTrackColumns.COL_TIME);
        providerIdx = cursor.getColumnIndex(GeoTrackColumns.COL_PROVIDER);

        latitudeE6Idx = cursor.getColumnIndex(GeoTrackColumns.COL_LATITUDE_E6);
        longitudeE6Idx = cursor.getColumnIndex(GeoTrackColumns.COL_LONGITUDE_E6);
        accuracyIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ACCURACY);
        altitudeIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ALTITUDE);

        bearingIdx = cursor.getColumnIndex(GeoTrackColumns.COL_BEARING);
        speedIdx = cursor.getColumnIndex(GeoTrackColumns.COL_SPEED);

        isNotInit = false;
        return this;
    }

    public GeoTrack getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        GeoTrack geoTrack = new GeoTrack();
        geoTrack.setId(idIdx > -1 ? cursor.getLong(idIdx) : -1);
        geoTrack.setUserId(userIdIdx > -1 ? cursor.getString(userIdIdx) : null);
        geoTrack.setTime(timeIdx > -1 ? cursor.getLong(timeIdx) : null);
        geoTrack.setProvider(providerIdx > -1 ? cursor.getString(providerIdx) : null);

        geoTrack.setLatitudeE6(latitudeE6Idx > -1 ? cursor.getInt(latitudeE6Idx) : null);
        geoTrack.setLongitudeE6(longitudeE6Idx > -1 ? cursor.getInt(longitudeE6Idx) : null);
        geoTrack.setAccuracy(accuracyIdx > -1 ? cursor.getInt(accuracyIdx) : null);
        geoTrack.setAltitude(altitudeIdx > -1 ? cursor.getDouble(altitudeIdx) : null);

        geoTrack.setBearing(bearingIdx > -1 ? cursor.getFloat(bearingIdx) : null);
        geoTrack.setSpeed(speedIdx > -1 ? cursor.getFloat(speedIdx) : null);

        return geoTrack;
    }

    private GeoTrackHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
        view.setText(cursor.getString(idx));
        return this;
    }

    public GeoTrackHelper setTextId(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, idIdx);
    }

    public String getIdAsString(Cursor cursor) {
        return cursor.getString(idIdx);
    }

    public long getId(Cursor cursor) {
        return cursor.getLong(idIdx);
    }

    public static ContentValues getContentValues(GeoTrack user) {
        ContentValues initialValues = new ContentValues();
        if (user.id > -1) {
            initialValues.put(GeoTrackColumns.COL_ID, Long.valueOf(user.id));
        }
        initialValues.put(GeoTrackColumns.COL_USERID, user.userId);
        initialValues.put(GeoTrackColumns.COL_TIME, user.time);
        initialValues.put(GeoTrackColumns.COL_PROVIDER, user.provider);

        initialValues.put(GeoTrackColumns.COL_LATITUDE_E6, user.getLatitudeE6());
        initialValues.put(GeoTrackColumns.COL_LONGITUDE_E6, user.getLongitudeE6());
        initialValues.put(GeoTrackColumns.COL_ACCURACY, user.accuracy);
        initialValues.put(GeoTrackColumns.COL_ALTITUDE, user.altitude);

        initialValues.put(GeoTrackColumns.COL_BEARING, user.bearing);
        initialValues.put(GeoTrackColumns.COL_SPEED, user.speed);

        return initialValues;
    }
}
