package eu.ttbox.geoping.service.accountsync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonHelper;


/**
 * <a href="http://www.c99.org/2010/01/23/writing-an-android-sync-provider-part-2/">Register in System Contact DB</a>
 */
public class GeopingSyncContactHelper {

    private static final String TAG = "GeopingSyncContactHelper";

    public static class Constants {
        public static final String ACCOUNT_TYPE = "eu.ttbox.geoping";
    }

    public static  Account getAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts =  accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Account result  = accounts!=null && accounts.length>0  ? accounts[0] : null;
        return result;
    }

    public static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException
    {
        ContentResolver mContentResolver = context.getContentResolver();
        HashMap<String, Long> localContacts = new HashMap<String, Long>();

        Log.i(TAG, "performSync: " + account.toString());

        // Load the local Last.fm contacts
        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon() //
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name) //
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type) //
                .build();
        Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, ContactsContract.RawContacts.SYNC1 }, null, null, null);
        try {
          while (c1.moveToNext()) {
              String syncId = c1.getString(1);
              Long baseId =  c1.getLong(0);
              localContacts.put(syncId, baseId);
              Log.d(TAG, "ContactsContract : syncId="+ syncId + "  with baseId="+baseId );
          }
        } finally {
            c1.close();
        }
       //
        Cursor cperson = mContentResolver.query(PersonProvider.Constants.CONTENT_URI, null, null, null, null );
        try {
            if (cperson.getCount()>0) {
                PersonHelper helper = new PersonHelper().initWrapper(cperson);
                Log.d(TAG, "==============================================================");

                while (cperson.moveToNext()) {
                    Person person=  helper.getEntity(cperson);

                    Log.d(TAG, "Read Local Person DB : "+ person );
                   //  personName = "ATATA";
                    if (!localContacts.containsKey(person.displayName)) {
                         addContact(context, account,  person, person.displayName );
                    }
                }
                Log.d(TAG, "==============================================================");
            }
        } finally {
            cperson.close();
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

        try {
            if(operationList.size() > 0)
                mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
        } catch (Exception e1) {
            Log.e(TAG, "Exception e : " + e1.getMessage(), e1);
        }
    }

    /**
     * <a herf="http://www.grokkingandroid.com/androids-contentprovideroperation-withbackreference-explained/">ContentProviderOperation Withbackreference explained</a>

     */
    public  static void addContact(Context context,  Account account,   Person person,  String username ) {
         Log.i(TAG, "Adding contact: " + person);

        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

        //Create our RawContact
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type);
        builder.withValue(ContactsContract.RawContacts.SYNC1, username);
        if (  person.contactId !=null) {
            builder.withValue(ContactsContract.RawContacts.CONTACT_ID,  person.contactId);
        }
        operationList.add(builder.build());

        //Create a Data record of common type 'StructuredName' for our RawContact
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, username);

        operationList.add(builder.build());

        //Create a Data record of custom type "vnd.android.cursor.item/vnd.fm.last.android.profile" to display a link to the Last.fm profile
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.eu.ttbox.geoping.person.profile");
        builder.withValue(ContactsContract.Data.DATA1, person.displayName);
        builder.withValue(ContactsContract.Data.DATA2, "Geoping Profile");
        builder.withValue(ContactsContract.Data.DATA3, person.phone);

        operationList.add(builder.build());

        try {
            ContentResolver cr = context.getContentResolver();
            cr.applyBatch(ContactsContract.AUTHORITY, operationList);
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong during creation! " + e);
            e.printStackTrace();
        }
    }


//    private static void updateContactStatus(ArrayList<ContentProviderOperation> operationList, long rawContactId, Track track) {
//        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
//        Uri entityUri = Uri.withAppendedPath(rawContactUri, RawContacts.Entity.CONTENT_DIRECTORY);
//        Cursor c = mContentResolver.query(entityUri, new String[] { RawContacts.SOURCE_ID, Entity.DATA_ID, Entity.MIMETYPE, Entity.DATA1 }, null, null, null);
//        try {
//            while (c.moveToNext()) {
//                if (!c.isNull(1)) {
//                    String mimeType = c.getString(2);
//                    String status = "";
//                    if (track.getNowPlaying() != null && track.getNowPlaying().equals("true"))
//                        status = "Listening to " + track.getName() + " by " + track.getArtist();
//                    else
//                        status = "Listened to " + track.getName() + " by " + track.getArtist();
//
//                    if (mimeType.equals("vnd.android.cursor.item/vnd.fm.last.android.profile")) {
//                        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
//                        builder.withValue(ContactsContract.StatusUpdates.DATA_ID, c.getLong(1));
//                        builder.withValue(ContactsContract.StatusUpdates.STATUS, status);
//                        builder.withValue(ContactsContract.StatusUpdates.STATUS_RES_PACKAGE, "fm.last.android");
//                        builder.withValue(ContactsContract.StatusUpdates.STATUS_LABEL, R.string.app_name);
//                        builder.withValue(ContactsContract.StatusUpdates.STATUS_ICON, R.drawable.icon);
//                        if (track.getDate() != null) {
//                            long date = Long.parseLong(track.getDate()) * 1000;
//                            builder.withValue(ContactsContract.StatusUpdates.STATUS_TIMESTAMP, date);
//                        }
//                        operationList.add(builder.build());
//
//                        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
//                        builder.withSelection(BaseColumns._ID + " = '" + c.getLong(1) + "'", null);
//                        builder.withValue(ContactsContract.Data.DATA3, status);
//                        operationList.add(builder.build());
//                    }
//                }
//            }
//        } finally {
//            c.close();
//        }
//    }

}
