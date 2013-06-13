package eu.ttbox.geoping.ui.geofence;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoFenceProvider;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase.GeoFenceColumns;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;

public class GeofenceListFragment extends Fragment {

	private static final String TAG = "GeofenceListFragment";

	private static final int GEOFENCE_LIST_LOADER = R.id.config_id_geofence_list_loader;

	// Constant
	private static final String GEOFENCE_SORT_DEFAULT = String.format("%s ASC", GeoFenceColumns.COL_NAME );

	private static final int EDIT_ENTITY = 0;

	// binding
	private ListView listView;

	// init
	private GeofenceListAdapter listAdapter;

	private final AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Log.w(TAG, "OnItemClickListener on Item at Position=" + position + " with id=" + id);
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			GeoFenceHelper helper = new GeoFenceHelper().initWrapper(cursor);
			String entityId = helper.getIdAsString(cursor);
			onEditEntityClick(entityId);
		}
	};

    // ===========================================================
    // Constructor
    // ===========================================================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.geofence_list, container, false);
		// Bindings
		listView = (ListView) v.findViewById(android.R.id.list);
		listView.setEmptyView(v.findViewById(android.R.id.empty));
		Button addEntityButton = (Button) v.findViewById(R.id.add_geofence_button);
		Button addEntityButtonHelp = (Button) v.findViewById(R.id.add_geofence_button_help);
		// init
		listAdapter = new GeofenceListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(mOnClickListener);
		// Listener
		OnClickListener addGeofenceOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddEntityClick(v);
			}
		};
		addEntityButton.setOnClickListener(addGeofenceOnClickListener);
		addEntityButtonHelp.setOnClickListener(addGeofenceOnClickListener);
		// Intents
		Log.d(TAG, "Binding end");

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
		// Load data
		getActivity().getSupportLoaderManager().initLoader(GEOFENCE_LIST_LOADER, null, geofenceLoaderCallback);
	}

	public void onAddEntityClick(View v) {
		Intent intent = Intents.editGeofence(getActivity(), null);
		startActivityForResult(intent, EDIT_ENTITY);
	}

	public void onEditEntityClick(String entityId) {
		Intent intent = Intents.editGeofence(getActivity(), entityId);
		startActivityForResult(intent, EDIT_ENTITY);
	}

	protected void handleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "handleIntent for action : " + intent.getAction());
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (EDIT_ENTITY):
			if (resultCode == Activity.RESULT_OK) {
				getActivity().getSupportLoaderManager().restartLoader(GEOFENCE_LIST_LOADER, null, geofenceLoaderCallback);
			}
		}
	}

	// ===========================================================
	// Loader
	// ===========================================================

	private final LoaderManager.LoaderCallbacks<Cursor> geofenceLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader");
			String sortOrder = GEOFENCE_SORT_DEFAULT;
			String selection = null;
			String[] selectionArgs = null;
			String queryString = null;
			// Loader
			CursorLoader cursorLoader = new CursorLoader(getActivity(), GeoFenceProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

			// Display List
			listAdapter.changeCursor(cursor);
			cursor.setNotificationUri(getActivity().getContentResolver(), GeoFenceProvider.Constants.CONTENT_URI);
			// Display Counter
			int count = 0;
			if (cursor != null) {
				count = cursor.getCount();
			}
			Log.d(TAG, "onLoadFinished with result count : " + count);

		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			listAdapter.changeCursor(null);
		}

	};

}
