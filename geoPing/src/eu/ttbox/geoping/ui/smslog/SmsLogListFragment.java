package eu.ttbox.geoping.ui.smslog;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;

public class SmsLogListFragment extends Fragment {

	private static final String TAG = "SmsLogListFragment";

	private static final int SMSLOG_LIST_LOADER = R.id.config_id_smsLog_list_loader;

	// Constant
	private static final String SMSLOG_SORT_DEFAULT = String.format("%s DESC, %s DESC", SmsLogColumns.COL_TIME, SmsLogColumns.COL_PHONE);

	private static final int EDIT_ENTITY = 0;

	// binding
	private ListView listView;

	// init
	private SmsLogListAdapter listAdapter;

	private final AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Log.w(TAG, "OnItemClickListener on Item at Position=" + position + " with id=" + id);
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			SmsLogHelper helper = new SmsLogHelper().initWrapper(cursor);
			String entityId = helper.getSmsLogIdAsString(cursor);
			onViewEntityClick(entityId);
		}
	};

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.smslog_list, container, false);
		Log.d(TAG, "onCreateView");
		// Bindings
		listView = (ListView) v.findViewById(android.R.id.list);
		listView.setEmptyView(v.findViewById(android.R.id.empty));

		// init
		listAdapter = new SmsLogListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(mOnClickListener);
		Log.d(TAG, "Binding end");
		// Intents
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
		getActivity().getSupportLoaderManager().initLoader(SMSLOG_LIST_LOADER, savedInstanceState, smsLogLoaderCallback);
	}

	/**
	 * 
	 */
//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		activity.getContentResolver().registerContentObserver(SmsLogProvider.Constants.CONTENT_URI, true, mObserver);
//	}
//
//	@Override
//	public void onDetach() {
//		super.onDetach();
//		getActivity().getContentResolver().unregisterContentObserver(mObserver);
//	}

	// ===========================================================
	// Observer
	// ===========================================================

	/**
	 * {@link https://bitbucket.org/craigleehi/google-i-o-2012-app/src/f4fd7504d43b/android/src/com/google/android/apps/iosched/ui/ExploreFragment.java}
	 * 
	 */
	private final ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (getActivity() == null) {
				return;
			}

			Loader<Cursor> loader = getLoaderManager().getLoader(SMSLOG_LIST_LOADER);
			if (loader != null) {
				loader.forceLoad();
			}
		}
	};

	// ===========================================================
	// Other
	// ===========================================================

	public void onViewEntityClick(String entityId) {
		// TODO View
		// Intent intent = Intents.editSmsLog(SmsLogListActivity.this,
		// entityId);
		// startActivityForResult(intent, EDIT_ENTITY);
	}

	private void deleteAllSmsLog() {
		int deleteCount = getActivity().getContentResolver().delete(SmsLogProvider.Constants.CONTENT_URI, null, null);
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (EDIT_ENTITY):
			if (resultCode == Activity.RESULT_OK) {
				getActivity().getSupportLoaderManager().restartLoader(SMSLOG_LIST_LOADER, null, smsLogLoaderCallback);
			}
		}
	}

	public void refreshLoader(Bundle args) {
		getActivity().getSupportLoaderManager().restartLoader(SMSLOG_LIST_LOADER, args, smsLogLoaderCallback);

	}

	// ===========================================================
	// Loader
	// ===========================================================

	private final LoaderManager.LoaderCallbacks<Cursor> smsLogLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader");
			String sortOrder = SMSLOG_SORT_DEFAULT;
			String selection = null;
			String[] selectionArgs = null;
			String queryString = null;
			Uri searchUri = SmsLogProvider.Constants.CONTENT_URI;
			// Check for Entity Binding
			if (args != null && args.containsKey(Intents.EXTRA_SMS_PHONE)) {
				String phoneNumber = args.getString(Intents.EXTRA_SMS_PHONE);
				searchUri = Uri.withAppendedPath(SmsLogProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(phoneNumber));
			}
			// else if (args!=null && args.containsKey(Intents.EXTRA_DATA_URI))
			// {
			// Uri entityUri = (Uri)args.getParcelable(Intents.EXTRA_DATA_URI);
			// Cursor entityCursor =
			// getActivity().getContentResolver().query(entityUri, new String[]
			// {} , null, null, null);
			// try {
			//
			// } finally {
			// entityCursor.close();
			// }
			// }

			// Loader
			CursorLoader cursorLoader = new CursorLoader(getActivity(), searchUri, null, selection, selectionArgs, sortOrder);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

			// Display List
			listAdapter.swapCursor(cursor);
			cursor.setNotificationUri(getActivity().getContentResolver(), SmsLogProvider.Constants.CONTENT_URI);
			// Display Counter
			int count = 0;
			if (cursor != null) {
				count = cursor.getCount();
			}
			Log.d(TAG, "onLoadFinished with result count : " + count);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			listAdapter.swapCursor(null);

		}

	};

}
