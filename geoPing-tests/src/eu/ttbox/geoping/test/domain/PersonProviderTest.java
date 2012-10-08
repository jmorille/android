package eu.ttbox.geoping.test.domain;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.test.ProviderTestCase2;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonHelper;

public class PersonProviderTest extends ProviderTestCase2<PersonProvider> {

    private static final Uri CONTENT_URI = PersonProvider.Constants.CONTENT_URI;

    static final Uri[] validUris = new Uri[] { CONTENT_URI };

    public PersonProviderTest() {
        super(PersonProvider.class, PersonProvider.Constants.AUTHORITY);
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
        Person vo = new Person()//
                .setPhone("0601020304") //
                .setColor(Color.GREEN)//
                .setPairingTime(System.currentTimeMillis())
                .setName("Lena Sjššblom");
        ContentValues values = PersonHelper.getContentValues(vo);
        // Insert
        Uri uri = provider.insert(CONTENT_URI, values);
        // Select
        Cursor cursor = provider.query(uri, null, null, null, null);
        try {
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            PersonHelper helper = new PersonHelper().initWrapper(cursor);
            Person voDb = helper.getEntity(cursor);
            assertEquals(Long.valueOf(uri.getLastPathSegment()).longValue(), voDb.id);
            assertEquals(vo.phone, voDb.phone);
            assertEquals(vo.color, voDb.color);
            assertEquals(vo.name, voDb.name);
            assertEquals(vo.pairingTime, voDb.pairingTime);
        } finally {
            cursor.close();
        }
    }

    public void testInsertAndSearchForPhone() {
        ContentProvider provider = getProvider();
        // Data
        Person vo = new Person()//
        .setPhone("0601020304") //
        .setColor(Color.GREEN)//
        .setPairingTime(System.currentTimeMillis())
        .setName("Lena Sjššblom");
        ContentValues values = PersonHelper.getContentValues(vo);
        // Insert
        Uri uri = provider.insert(CONTENT_URI, values);
        // Select
        Uri searchPhoneUri01 = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode("+33601020304"));
        Uri searchPhoneUri02 = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode("0601020304"));
        for (Uri searchPhoneUri : new Uri[] { searchPhoneUri01, searchPhoneUri02 }) {
            Cursor cursor = provider.query(searchPhoneUri, null, null, null, null);
            try {
                assertEquals(1, cursor.getCount());
                cursor.moveToFirst();
                PersonHelper helper = new PersonHelper().initWrapper(cursor);
                Person voDb = helper.getEntity(cursor);
                assertEquals(Long.valueOf(uri.getLastPathSegment()).longValue(), voDb.id);
                assertEquals(vo.phone, voDb.phone);
                assertEquals(vo.color, voDb.color);
                assertEquals(vo.pairingTime, voDb.pairingTime);
                assertEquals(vo.name, voDb.name);
            } finally {
                cursor.close();
            }
        }
    }

}
