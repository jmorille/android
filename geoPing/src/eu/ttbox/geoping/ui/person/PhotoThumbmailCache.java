package eu.ttbox.geoping.ui.person;

import android.content.ComponentCallbacks2;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

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
		this.cacheTrimToSize(maxSize);
	}

	public int cacheSize() {
		return this.size();
	}

    // ===========================================================
    // Photo Loader
    // ===========================================================

    /**
     * Pour plus de details sur l'intégration dans les contacts consulter
     * <ul>
     * <li>item_photo_editor.xml</li>
     * <li>com.android.contacts.editor.PhotoEditorView</li>
     * <li>com.android.contacts.detail.PhotoSelectionHandler</li>
     * <li>com.android.contacts.editor.ContactEditorFragment.PhotoHandler</li>
     * </ul>
     *
     * @param contactId
     */
    public void loadPhoto(Context context, ImageView photoImageView,String contactId, final String phone) {
        Bitmap photo = null;
        boolean isContactId = !TextUtils.isEmpty(contactId);
        boolean isContactPhone = !TextUtils.isEmpty(phone);
        // Search in cache
        if (photo == null && isContactId) {
            photo = this.get(contactId);
        }
        if (photo == null && isContactPhone) {
            photo = this.get(phone);
        }
        // Set Photo
        if (photo != null) {
            photoImageView.setImageBitmap(photo);
        } else if (isContactId || isContactPhone) {
            // Cancel previous Async
            final PhotoLoaderAsyncTask oldTask = (PhotoLoaderAsyncTask) photoImageView.getTag();
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            // Load photos
            PhotoLoaderAsyncTask newTask = this.getPhotoLoaderAsyncTask(context,   photoImageView);
            photoImageView.setTag(newTask);
            newTask.execute(contactId, phone);
        }

    }

    /**
     * Pour plus de details sur l'intégration dans les contacts consulter
     * <ul>
     * <li>item_photo_editor.xml</li>
     * <li>com.android.contacts.editor.PhotoEditorView</li>
     * <li>com.android.contacts.detail.PhotoSelectionHandler</li>
     * <li>com.android.contacts.editor.ContactEditorFragment.PhotoHandler</li>
     * </ul>
     *
     * @param contactId
     */
    public void loadPhoto(Context context, PhotoEditorView photoImageView, String contactId, final String phone) {
        Bitmap photo = null;
        boolean isContactId = !TextUtils.isEmpty(contactId);
        boolean isContactPhone = !TextUtils.isEmpty(phone);
        // Search in cache
        if (photo == null && isContactId) {
            photo = this.get(contactId);
        }
        if (photo == null && isContactPhone) {
            photo = this.get(phone);
        }
        // Set Photo
        if (photo != null) {
            photoImageView.setValues(photo, true);
        } else if (isContactId || isContactPhone) {
            // Cancel previous Async
            final PhotoLoaderAsyncTask oldTask = (PhotoLoaderAsyncTask) photoImageView.getTag();
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            // Load photos
            PhotoLoaderAsyncTask newTask = this.getPhotoLoaderAsyncTask(context,photoImageView);
            photoImageView.setTag(newTask);
            newTask.execute(contactId, phone);
        }

    }

    // ===========================================================
    // PhotoLoaderAsyncTask
    // ===========================================================

    public PhotoLoaderAsyncTask getPhotoLoaderAsyncTask(Context context, ImageView holder) {
        return new PhotoLoaderAsyncTask(context, this, holder);
    }

    public PhotoLoaderAsyncTask getPhotoLoaderAsyncTask(Context context, PhotoEditorView holder) {
        return new PhotoLoaderAsyncTask(context, this, holder);
    }

    public class PhotoLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView holder;
        private PhotoEditorView holderEditor;

        private final Context mContext;
        private final PhotoThumbmailCache mCache;

        private PhotoLoaderAsyncTask(Context context, PhotoThumbmailCache cache, ImageView holder) {
            super();
            this.holder = holder;
            this.mContext = context;
            this.mCache = cache;
        }

        private PhotoLoaderAsyncTask(Context context, PhotoThumbmailCache cache, PhotoEditorView holder) {
            super();
            this.holderEditor = holder;
            this.mContext = context;
            this.mCache = cache;
        }

        @Override
        protected void onPreExecute() {
//             if (holder!=null) {
//                holder.setTag(this);
//            }
//            if (holderEditor!=null) {
//                holderEditor.setTag(this);
//            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String contactIdSearch = params[0];
            String phoneSearch = null;
            if (params.length > 1) {
                phoneSearch = params[1];
            }
            Bitmap result = ContactHelper.openPhotoBitmap(mContext, mCache, contactIdSearch, phoneSearch);
            Log.d(TAG, "PhotoLoaderAsyncTask load photo : " + (result != null));
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (holder!=null && holder.getTag() == this) {
                holder.setImageBitmap(result);
                holder.setTag(null);
                Log.d(TAG, "PhotoLoaderAsyncTask onPostExecute photo : " + (result != null));
            }
            if (holderEditor!=null && holderEditor.getTag() == this) {
                holderEditor.setImageBitmap(result);
                holderEditor.setTag(null);
                Log.d(TAG, "PhotoLoaderAsyncTask onPostExecute photo : " + (result != null));
            }
        }
    }


}
