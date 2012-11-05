package eu.ttbox.geoping.test.domain;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.model.SmsLog;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;

public class SmsLogProviderTest extends ProviderTestCase2<SmsLogProvider> {

    private static final Uri CONTENT_URI = SmsLogProvider.Constants.CONTENT_URI;

    static final Uri[] validUris = new Uri[] { CONTENT_URI };

    public SmsLogProviderTest() {
        super(SmsLogProvider.class, SmsLogProvider.Constants.AUTHORITY);
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
        SmsLog vo = new SmsLog()//
                .setPhone("0601020304") //
                .setAction(SmsMessageActionEnum.GEOPING_REQUEST)//
                .setTime(java.lang.System.currentTimeMillis()) //
                .setMessage("th7lhawmo,z31,y1e14h,xt3jbc,aa,s0,pg,b1p") //
                .setSmsLogType(SmsLogTypeEnum.SEND);
        ContentValues values =  SmsLogHelper.getContentValues(vo);
        // Insert
        Uri uri = provider.insert(CONTENT_URI, values);
        // Select
        Cursor cursor = provider.query(uri, null, null, null, null);
        try {
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            SmsLogHelper helper = new SmsLogHelper().initWrapper(cursor);
            SmsLog voDb = helper.getEntity(cursor);
            assertEquals(Long.valueOf(uri.getLastPathSegment()).longValue(), voDb.id);
            assertEquals(vo.phone, voDb.phone);
            assertEquals(vo.action, voDb.action);
            assertEquals(vo.time, voDb.time);
            assertEquals(vo.message, voDb.message);
            assertEquals(vo.smsLogType, voDb.smsLogType);
        } finally {
            cursor.close();
        }
    }

    
}
