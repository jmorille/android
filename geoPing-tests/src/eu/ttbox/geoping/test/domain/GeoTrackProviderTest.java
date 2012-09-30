package eu.ttbox.geoping.test.domain;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.Pairing;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

public class GeoTrackProviderTest extends ProviderTestCase2<GeoTrackerProvider> {

    private static final Uri CONTENT_URI = GeoTrackerProvider.Constants.CONTENT_URI;

    private static final Uri[] validUris = new Uri[] { CONTENT_URI };

    public static final String PROVIDER_NETWORK = "network";
    public static final String PROVIDER_GPS = "gps";

    public GeoTrackProviderTest() {
        super(GeoTrackerProvider.class, GeoTrackerProvider.Constants.AUTHORITY);
    }

    private GeoTrack getGeoTrack01(String provider) {
        GeoTrack geoTrack = new GeoTrack() //
                .setProvider(provider)//
                .setLatitudeE6(43158549)//
                .setLongitude(25218546)//
                .setAccuracy(120) //
                .setPhone("+33601020304") //
                .setTime(1347481830000l);
        if (PROVIDER_GPS.equals(provider)) {
            geoTrack.setAccuracy(25) //
                    .setAltitude(124)//
                    .setSpeed(23)//
                    .setBearing(257);
            geoTrack.setAddress("12 rue de la Miarie, 75001 paris");
        }

        return geoTrack;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testQuery() {
        ContentProvider provider = getProvider();
        for (Uri uri : validUris) {
            Cursor cursor = provider.query(uri, null, null, null, null);
            assertNotNull(cursor);
        }
    }

    public void testInsertAndRead() {
        ContentProvider provider = getProvider();
        // Data
        for (GeoTrack vo : new GeoTrack[] { getGeoTrack01(PROVIDER_GPS), getGeoTrack01(PROVIDER_NETWORK)}) {
            GeoTrackHelper helper = new GeoTrackHelper();
            ContentValues values = helper.getContentValues(vo);
            // Insert
            Uri uri = provider.insert(CONTENT_URI, values);
            // Select
            Cursor cursor = provider.query(uri, null, null, null, null);
            try {
                assertEquals(1, cursor.getCount());
                cursor.moveToFirst();
                helper.initWrapper(cursor);
                GeoTrack voDb = helper.getEntity(cursor);
                assertEquals(Long.valueOf(uri.getLastPathSegment()).longValue(), voDb.id);
                assertEquals(vo.phone, voDb.phone);
                assertEquals(vo.getTime(), voDb.getTime());
                assertEquals(vo.getLatitudeE6(), voDb.getLatitudeE6());
                assertEquals(vo.getLongitude(), voDb.getLongitude());
                assertEquals(vo.getAccuracy(), voDb.getAccuracy());
                assertEquals(vo.getAltitude(), voDb.getAltitude());
                assertEquals(vo.getSpeed(), voDb.getSpeed());
                assertEquals(vo.getBearing(), voDb.getBearing());
                assertEquals(vo.address, voDb.address);
            } finally {
                cursor.close();
            }
        }
    }

}
