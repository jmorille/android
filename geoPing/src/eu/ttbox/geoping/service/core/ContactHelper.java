package eu.ttbox.geoping.service.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

/**
 * @see Woking With Contact {link http://www.higherpass.com/Android/Tutorials/Working-With-Android-Contacts/}
 */
public class ContactHelper {

	private static final String TAG = "ContactHelper";

	private static final String PERMISSION_READ_CONTACTS = "android.permission.READ_CONTACTS";

    public static Bitmap openPhotoBitmap(Context context,String contactId) {
    	if (contactId==null) {
    		return null;
    	}
    	Long contactIden = Long.valueOf(contactId);
    	return openPhotoBitmap(context, contactIden);
    }
    public static Bitmap openPhotoBitmap(Context context,long contactId) {
        Bitmap photo = null;
        InputStream is = openPhoto(context, Long.valueOf(contactId));
        if (is != null) {
            photo = BitmapFactory.decodeStream(is);
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close Contact Photo Input Stream");
            }
        }
        return photo;
    }

    public static InputStream openPhoto(Context context,long contactId) {
    	Log.d(TAG, "Open Photo for COntact Id : " + contactId );
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri, new String[] { Contacts.Photo.PHOTO }, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }
    
    

    public static ContactVo searchContactForPhone(Context context, String phoneNumber) {
        String contactName = null;
        long contactId = -1l;
        if (isPermissionReadContact(context)) {
            Log.d(TAG, String.format("Search Contact Name for Phone [%s]", phoneNumber));
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cur = context.getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID }, null, null, null);
            try {
                if (cur != null && cur.moveToFirst()) {
                    contactName = cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                    contactId = cur.getLong(cur.getColumnIndexOrThrow(PhoneLookup._ID));
                }
            } finally {
                cur.close();
            }
        }
        Log.d(TAG, String.format("Found Contact %s Name for Phone [%s] : %s", contactId, phoneNumber, contactName));
        ContactVo result = null;
        if (contactId != -1l) {
            result = new ContactVo(contactId, contactName);
        }
        return result;
    }
    

    private static boolean isPermissionReadContact(Context context) {
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(PERMISSION_READ_CONTACTS, context.getPackageName());
    }

    
}
