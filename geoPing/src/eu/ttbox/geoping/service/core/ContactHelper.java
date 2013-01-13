package eu.ttbox.geoping.service.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.Log;

/**
 * @see Woking With Contact {link
 *      http://www.higherpass.com/Android/Tutorials/Working
 *      -With-Android-Contacts/}
 */
public class ContactHelper {

	private static final String TAG = "ContactHelper";

	private static final String PERMISSION_READ_CONTACTS = "android.permission.READ_CONTACTS";


	public static Bitmap openPhotoBitmap(Context context,  PhotoThumbmailCache photoCache, String contactId, String phone) {
		Bitmap photo = null;
		boolean isContactId = !TextUtils.isEmpty(contactId);
		boolean isContactPhone = !TextUtils.isEmpty(phone);
		// Search in cache
		if (photo == null && isContactId) {
			photo = photoCache.get(contactId);
		}
		if (photo == null &&isContactPhone) {
			photo = photoCache.get(phone);
		}
		// Load Photo
		if (photo == null && isContactId) {
			photo = photoCache.loadPhotoLoaderFromContactId(context.getContentResolver(), contactId);
		}
		if (photo == null &&isContactPhone) {
			photo = photoCache.loadPhotoLoaderFromContactPhone(context, phone);
		}
		return photo;
	}
	
//	public static Bitmap openPhotoBitmap(Context context, String contactId) {
//		if (contactId == null) {
//			return null;
//		}
//		Long contactIden = Long.valueOf(contactId);
//		return openPhotoBitmap(context, contactIden);
//	}
//
//	public static Bitmap openPhotoBitmap(Context context, long contactId) {
//		Bitmap photo = null;
//		InputStream is = openPhoto(context, Long.valueOf(contactId));
//		if (is != null) {
//			photo = BitmapFactory.decodeStream(is);
//			try {
//				is.close();
//			} catch (IOException e) {
//				Log.e(TAG, "Could not close Contact Photo Input Stream");
//			}
//		}
//		return photo;
//	}

//	public static Bitmap loadPhotoContact(Context context, String contactId) {
//		if (contactId == null) {
//			return null;
//		}
//		Long contactIden = Long.valueOf(contactId);
//		return loadPhotoContact(context, contactIden);
//	}
//
//	public static Bitmap loadPhotoContact(Context context, long contactId) {
//		ContentResolver cr = context.getContentResolver();
//		Bitmap photo = loadPhotoContact(cr, contactId);
//		if (photo == null) {
//			Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
//			Log.d(TAG, "Search a PhotoId for Contact Uri : " + contactUri);
//			Cursor cursor = cr.query(contactUri, new String[] { ContactsContract.Contacts.PHOTO_ID }, null, null, null);
//
//			try {
//				if (cursor != null && cursor.moveToFirst()) {
//					long photoId = cursor.getLong(0);
//					// if (photoId != null) {
//					photo = loadPhotoContactByPhotoId(cr, photoId);
//					// }
//				}
//			} finally {
//				if (cursor != null)
//					cursor.close();
//			}
//
//		}
//		return photo;
//	}

	public static Bitmap loadPhotoContact(ContentResolver cr, long contactId) {
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		Log.d(TAG, "Search Photo for ContactsContract Contact Uri : " + contactUri);
		InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri);
		if (is == null) {
			Log.d(TAG, "No Photo found for ContactsContract Contact Uri : " + contactUri);
			return null;
		}
		Bitmap photo = BitmapFactory.decodeStream(is); 
		try {
			is.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not close Contact Photo Input Stream");
		}
		return photo;
	}

//	public static Bitmap loadPhotoContactByPhotoId(ContentResolver cr, long photoId) {
//		Log.d(TAG, "Search a Photo for Photo Id : " + photoId);
//
//		Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);
//		Cursor cursor = cr.query(photoUri, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);
//		if (cursor == null) {
//			Log.d(TAG, "No Photo Found for Photo Id : " + photoId);
//
//			return null;
//		}
//		Bitmap photo = null;
//		try {
//			if (cursor != null && cursor.moveToFirst()) {
//				byte[] data = cursor.getBlob(0);
//				if (data != null) {
//					photo = BitmapFactory.decodeByteArray(data, 0, data.length);
//				}
//			}
//		} finally {
//			if (cursor != null)
//				cursor.close();
//		}
//		return photo;
//	}

	public static InputStream openPhoto(Context context, long contactId) {
		Log.d(TAG, "Open Photo for Contact Id : " + contactId);
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

	/**
	 * {@linkplain http://developer.android.com/reference/android/provider/ContactsContract.PhoneLookup.html}
	 * @param context
	 * @param phoneNumber
	 * @return
	 */
	public static ContactVo searchContactForPhone(Context context, String phoneNumber) {
		String contactName = null;
		long contactId = -1l;
		if (isPermissionReadContact(context)) {
			Log.d(TAG, String.format("Search Contact Name for Phone [%s]", phoneNumber));
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
			Cursor cur = context.getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID, PhoneLookup.LOOKUP_KEY }, null, null, null);
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
