package eu.ttbox.geoping.test.domain;

import android.content.ContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import eu.ttbox.geoping.domain.SmsLogProvider;

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

}
