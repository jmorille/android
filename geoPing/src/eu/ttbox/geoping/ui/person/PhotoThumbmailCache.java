package eu.ttbox.geoping.ui.person;

import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.core.ContactVo;

public class PhotoThumbmailCache extends LruCache<String, Bitmap> {

	private static final String TAG = "PhotoThumbmailCache";

	public PhotoThumbmailCache(int maxSizeBytes) {
		super(maxSizeBytes);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}

	 
 
	public Bitmap loadPhotoLoaderFromContactId(ContentResolver cr, String contactId) {
		Bitmap photo = this.get(contactId);
		if (photo == null) {
			photo = ContactHelper.loadPhotoContact(cr, Long.valueOf(contactId).longValue());
			if (photo != null) {
				this.put(contactId, photo);
				Log.d(TAG, "Put in cache " + contactId + " : " + photo);
			}

		}
		return photo;
	}

	public Bitmap loadPhotoLoaderFromContactPhone(Context context, String phoneNumber) {
		Bitmap photo = this.get(phoneNumber);
		if (photo == null) {
			ContactVo searchContact = ContactHelper.searchContactForPhone(context,phoneNumber);
			if (searchContact != null &&  searchContact.id>0) {
				photo = loadPhotoLoaderFromContactId(context.getContentResolver(), String.valueOf(searchContact.id));
				if (photo != null) {
					this.put(phoneNumber, photo);
					Log.d(TAG, "Put in cache " + phoneNumber + " : " + photo);
				}
			}

		}
		return photo;
	}

	
	public void onLowMemory() {
		cacheEvictAll();
	}

	public void onTrimMemory(int level) {
		if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) { // 60
			Log.i(TAG, "###############################################");
			Log.i(TAG, "### Clear all cache on TrimMemory Event " + level);
			Log.i(TAG, "###############################################");
			cacheEvictAll();
		} else if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) { // 40
			Log.i(TAG, "###############################################");
			Log.i(TAG, "### Clear 1/2 cache on TrimMemory Event " + level);
			Log.i(TAG, "###############################################");
			cacheTrimToSize(cacheSize() / 2);
		}
	}

	public void cacheEvictAll() {
		this.evictAll();
	}

	public void cacheTrimToSize(int maxSize) {
		this.trimToSize(maxSize);
	}

	public int cacheSize() {
		return this.size();
	}
}
