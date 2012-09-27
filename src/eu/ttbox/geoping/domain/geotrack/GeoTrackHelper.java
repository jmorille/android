package eu.ttbox.geoping.domain.geotrack;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.widget.TextView;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.core.wrapper.BundleWrapper;
import eu.ttbox.geoping.domain.core.wrapper.ContentValuesWrapper;
import eu.ttbox.geoping.domain.core.wrapper.HelperWrapper;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

public class GeoTrackHelper {

    boolean isNotInit = true;

    public int idIdx = -1;
    public int phoneIdIdx = -1;
    public int personIdIdx = -1;
    public int timeIdx = -1;
    public int providerIdx = -1;
    public int latitudeE6Idx = -1;
    public int longitudeE6Idx = -1;
    public int accuracyIdx = -1;

    public int altitudeIdx = -1;
    public int bearingIdx = -1;
    public int speedIdx = -1;

    public int addressIdx = -1;

    // public int titreIdx = -1;

    public GeoTrackHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ID);
        personIdIdx = cursor.getColumnIndex(GeoTrackColumns.COL_PERSON_ID);
        phoneIdIdx = cursor.getColumnIndex(GeoTrackColumns.COL_PHONE_NUMBER);
        timeIdx = cursor.getColumnIndex(GeoTrackColumns.COL_TIME);
        providerIdx = cursor.getColumnIndex(GeoTrackColumns.COL_PROVIDER);

        latitudeE6Idx = cursor.getColumnIndex(GeoTrackColumns.COL_LATITUDE_E6);
        longitudeE6Idx = cursor.getColumnIndex(GeoTrackColumns.COL_LONGITUDE_E6);
        accuracyIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ACCURACY);
        altitudeIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ALTITUDE);

        bearingIdx = cursor.getColumnIndex(GeoTrackColumns.COL_BEARING);
        speedIdx = cursor.getColumnIndex(GeoTrackColumns.COL_SPEED);

        addressIdx = cursor.getColumnIndex(GeoTrackColumns.COL_ADDRESS);
        isNotInit = false;
        return this;
    }

    public GeoTrack getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        GeoTrack geoTrack = new GeoTrack();
        geoTrack.setId(idIdx > -1 ? cursor.getLong(idIdx) : -1);
        geoTrack.setPersonId(personIdIdx > -1 ? cursor.getLong(personIdIdx) : null);
        geoTrack.setPhone(phoneIdIdx > -1 ? cursor.getString(phoneIdIdx) : null);
        geoTrack.setTime(timeIdx > -1 ? cursor.getLong(timeIdx) : -1);
        geoTrack.setProvider(providerIdx > -1 ? cursor.getString(providerIdx) : null);
        if (latitudeE6Idx > -1) {
            geoTrack.setLatitudeE6(cursor.getInt(latitudeE6Idx));
        }
        if (longitudeE6Idx > -1) {
            geoTrack.setLongitudeE6(cursor.getInt(longitudeE6Idx));
        }
        geoTrack.setAccuracy(accuracyIdx > -1 ? cursor.getInt(accuracyIdx) : -1);
        geoTrack.setAltitude(altitudeIdx > -1 ? cursor.getInt(altitudeIdx) : -1);

        geoTrack.setBearing(bearingIdx > -1 ? cursor.getInt(bearingIdx) : -1);
        geoTrack.setSpeed(speedIdx > -1 ? cursor.getInt(speedIdx) : -1);

        geoTrack.setAddress(addressIdx > -1 ? cursor.getString(addressIdx) : null);
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

    public long getPersonId(Cursor cursor) {
        return cursor.getLong(personIdIdx);
    }

    public static ContentValues getContentValues(GeoTrack user) {
        ContentValuesWrapper wrapper = (ContentValuesWrapper) getWrapperValues(user, new ContentValuesWrapper());
        ContentValues initialValues = wrapper.getWrappedValue();
        return initialValues;
    }

    public static Bundle getBundleValues(GeoTrack geoTrack) {
        BundleWrapper wrapper = (BundleWrapper) getWrapperValues(geoTrack, new BundleWrapper());
        Bundle bundle = wrapper.getWrappedValue();
        return bundle;
    }

    private static HelperWrapper<?> getWrapperValues(GeoTrack geoTrack, HelperWrapper<?> initialValues) {
        if (geoTrack.id > -1) {
            initialValues.putLong(GeoTrackColumns.COL_ID, Long.valueOf(geoTrack.id));
        }
        if (geoTrack.hasPersonId()) {
            initialValues.putLong(GeoTrackColumns.COL_PERSON_ID, geoTrack.personId);
        }
        if (geoTrack.hasPhone()) {
            initialValues.putString(GeoTrackColumns.COL_PHONE_NUMBER, geoTrack.phone);
        }
        initialValues.putLong(GeoTrackColumns.COL_TIME, geoTrack.time);
        initialValues.putString(GeoTrackColumns.COL_PROVIDER, geoTrack.provider);
        initialValues.putInt(GeoTrackColumns.COL_LATITUDE_E6, geoTrack.getLatitudeE6());
        initialValues.putInt(GeoTrackColumns.COL_LONGITUDE_E6, geoTrack.getLongitudeE6());
        if (geoTrack.hasAccuracy()) {
            initialValues.putInt(GeoTrackColumns.COL_ACCURACY, geoTrack.accuracy);
        }
        if (geoTrack.hasAltitude()) {
            initialValues.putInt(GeoTrackColumns.COL_ALTITUDE, geoTrack.getAltitude());
        }
        if (geoTrack.hasBearing()) {
            initialValues.putInt(GeoTrackColumns.COL_BEARING, geoTrack.bearing);
        }
        if (geoTrack.hasSpeed()) {
            initialValues.putInt(GeoTrackColumns.COL_SPEED, geoTrack.speed);
        }
        if (geoTrack.hasAddress()) {
            initialValues.putString(GeoTrackColumns.COL_ADDRESS, geoTrack.address);
        }
        return initialValues;
    }

    public static GeoTrack getEntityFromIntent(Intent intent) {
        Bundle initialValues = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
        GeoTrack geoTrack = getEntityFromBundle(initialValues);
        String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
        if (phone!=null && !initialValues.containsKey(GeoTrackColumns.COL_PHONE_NUMBER)) {
            geoTrack.setPhone(phone);
        }
        return geoTrack;
    }
    public static GeoTrack getEntityFromBundle(Bundle initialValues) {
        if (initialValues == null || initialValues.isEmpty()) {
            return null;
        }
        GeoTrack geoTrack = new GeoTrack();
        if (initialValues.containsKey(GeoTrackColumns.COL_ID)) {
            geoTrack.setId(initialValues.getLong(GeoTrackColumns.COL_ID));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_PERSON_ID)) {
            geoTrack.setPersonId(initialValues.getLong(GeoTrackColumns.COL_PERSON_ID));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_PHONE_NUMBER)) {
            geoTrack.setPhone(initialValues.getString(GeoTrackColumns.COL_PHONE_NUMBER));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_PROVIDER)) {
            geoTrack.setProvider(initialValues.getString(GeoTrackColumns.COL_PROVIDER));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_TIME)) {
            geoTrack.setTime(initialValues.getLong(GeoTrackColumns.COL_TIME));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_LATITUDE_E6)) {
            geoTrack.setLatitudeE6(initialValues.getInt(GeoTrackColumns.COL_LATITUDE_E6));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_LONGITUDE_E6)) {
            geoTrack.setLongitudeE6(initialValues.getInt(GeoTrackColumns.COL_LONGITUDE_E6));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_ACCURACY)) {
            geoTrack.setAccuracy(initialValues.getInt(GeoTrackColumns.COL_ACCURACY));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_ALTITUDE)) {
            geoTrack.setAltitude(initialValues.getInt(GeoTrackColumns.COL_ALTITUDE));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_BEARING)) {
            geoTrack.setBearing(initialValues.getInt(GeoTrackColumns.COL_BEARING));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_SPEED)) {
            geoTrack.setSpeed(initialValues.getInt(GeoTrackColumns.COL_SPEED));
        }
        if (initialValues.containsKey(GeoTrackColumns.COL_ADDRESS)) {
            geoTrack.setAddress(initialValues.getString(GeoTrackColumns.COL_ADDRESS));
        }
        return geoTrack;
    }

    public static String getAddressAsString(Address addr) {
        String result = null;
        if (addr != null) {
            StringBuilder addrBuilder = new StringBuilder();
            boolean isNotFist = false;
            for (int i = 0; i < addr.getMaxAddressLineIndex(); i++) {
                if (isNotFist) {
                    addrBuilder.append(", ");
                } else {
                    isNotFist = true;
                }
                String addrLine = addr.getAddressLine(i);
                addrBuilder.append(addrLine);
            }
            result = addrBuilder.toString();
        }
        return result;
    }
}
