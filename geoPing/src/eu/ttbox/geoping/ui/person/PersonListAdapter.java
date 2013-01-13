package eu.ttbox.geoping.ui.person;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.person.PhotoEditorView.EditorListener;

public class PersonListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

	private static final String TAG = "PersonListAdapter";

	private PersonHelper helper;

	private boolean isNotBinding = true;

	private Context context;
	// Cache
	private PhotoThumbmailCache photoCache;

	// ===========================================================
	// Constructors
	// ===========================================================

	public PersonListAdapter(Context context, Cursor c, int flags ) {
		super(context, R.layout.track_person_list_item, c, flags);
		this.context = context; 
		// Cache
		photoCache = ((GeoPingApplication) context.getApplicationContext()).getPhotoThumbmailCache();

	}

	private void intViewBinding(View view, Context context, Cursor cursor) {
		// Init Cursor
		helper = new PersonHelper().initWrapper(cursor);
		isNotBinding = false;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {

		if (isNotBinding) {
			intViewBinding(view, context, cursor);
		}
		final ViewHolder holder = (ViewHolder) view.getTag();
		// Cancel any pending thumbnail task, since this view is now bound to
		// new thumbnail
		final PhotoLoaderAsyncTask oldTask = holder.photoLoaderAsyncTask;
		if (oldTask != null) {
			oldTask.cancel(false);
		}

		// Value
		final String phoneNumber = helper.getPersonPhone(cursor);
		final String contactId = helper.getContactId(cursor);
		String personName = helper.getPersonDisplayName(cursor);
		int color = helper.getPersonColor(cursor);
		// Bind Value
		holder.phoneText.setText(phoneNumber);
		holder.nameText.setText(personName);
		// Color
		Drawable stld = PersonColorDrawableHelper.getListBackgroundColor(color);
		//
		// if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.JELLY_BEAN) {
		// view.setBackground(stld);
		// } else
		view.setBackgroundDrawable(stld);

		// Photo
		if (!TextUtils.isEmpty(contactId)) {
			Bitmap cachedResult = photoCache.get(contactId);
			if (cachedResult != null) {
				holder.pingButton.setValues(cachedResult, false);
			} else {
				PhotoLoaderAsyncTask newTask = new PhotoLoaderAsyncTask(holder);
				holder.photoLoaderAsyncTask = newTask;
				newTask.execute(contactId);
			}
		}

		// Button
		holder.pingButton.setEditorListener(new EditorListener() {
			@Override
			public void onRequest(int request) {
				context.startService(Intents.sendSmsGeoPingRequest(context, phoneNumber));
			}
		});

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		// Then populate the ViewHolder
		ViewHolder holder = new ViewHolder();
		holder.nameText = (TextView) view.findViewById(R.id.person_list_item_name);
		holder.phoneText = (TextView) view.findViewById(R.id.person_list_item_phone);
		holder.pingButton = (PhotoEditorView) view.findViewById(R.id.person_list_item_geoping_button);
		view.setTag(holder);
		return view;

	}

	static class ViewHolder {
		PhotoEditorView pingButton;
		TextView nameText;
		TextView phoneText;
		PhotoLoaderAsyncTask photoLoaderAsyncTask;
	}

	// ===========================================================
	// Listeners
	// ===========================================================

	public class PhotoLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

		final ViewHolder holder;

		public PhotoLoaderAsyncTask(ViewHolder holder) {
			super();
			this.holder = holder;
		}

		@Override
		protected void onPreExecute() {
			holder.photoLoaderAsyncTask = this;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			final String contactIdSearch = params[0];
			Bitmap result = photoCache.loadPhotoLoaderFromContactId(context.getContentResolver(), contactIdSearch);
			return result;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (holder.photoLoaderAsyncTask == this) {
				holder.pingButton.setValues(result, true);
				holder.photoLoaderAsyncTask = null;
			}
		}
	}

	// ===========================================================
	// Others
	// ===========================================================

}
