package eu.ttbox.geoping.ui.person;

import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import eu.ttbox.geoping.service.core.ContactHelper;

public class PhotoThumbmailCache extends LruCache<String, Bitmap> {

	private static final String TAG = "PhotoThumbmailCache";

	public PhotoThumbmailCache(int maxSizeBytes) {
		super(maxSizeBytes);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}

	public Bitmap loadPhotoLoaderAsync(ContentResolver cr, String contactId) {
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
