package eu.ttbox.smstraker.ui.ping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import eu.ttbox.smstraker.R;
import eu.ttbox.smstraker.core.Intents;
import eu.ttbox.smstraker.domain.PersonProvider;
import eu.ttbox.smstraker.domain.person.PersonDatabase.PersonColumns;

public class GeoPingActivity extends Activity {

	private static final String TAG = "GeoPingActivity";

	private static final int PERSON_LIST_LOADER = R.id.config_id_person_list_loader_started;

	// Constant
	private static final String PERSON_SORT_DEFAULT = String.format("%s DESC, %s DESC", PersonColumns.KEY_NAME, PersonColumns.KEY_PHONE);

	// binding
	private ListView listView;
	private Button addPersonButton;

	// init
	private PersonListAdapter listAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geoping);
		// Bindings
		listView = (ListView) findViewById(R.id.track_person_list);
		addPersonButton = (Button) findViewById(R.id.add_track_person_button);
		addPersonButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = Intents.addTrackerPerson(GeoPingActivity.this);
				startActivity(intent);
			}
		});
		// init
		listAdapter = new PersonListAdapter(this, null, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		listView.setAdapter(listAdapter);

		// Intents
		Intent intent = getIntent();
		if (intent == null) {
			getLoaderManager().initLoader(PERSON_LIST_LOADER, null, orderLoaderCallback);
		} else {
			handleIntent(getIntent());
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	protected void handleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "handleIntent for action : " + intent.getAction());
		}
	}

	private final LoaderManager.LoaderCallbacks<Cursor> orderLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String sortOrder = PERSON_SORT_DEFAULT;
			String selection = null;
			String[] selectionArgs = null;
			String queryString = null;
			// Loader
			CursorLoader cursorLoader = new CursorLoader(GeoPingActivity.this, PersonProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// Display List
			listAdapter.swapCursor(cursor);
			// Display Counter
			int count = 0;
			if (cursor != null) {
				count = cursor.getCount();
			}
			// if (count < 1) {
			// searchResultTextView.setText( R.string.search_no_results );
			// } else {
			// String countString =
			// getResources().getQuantityString(R.plurals.search_results, count,
			// new Object[] { count });
			// searchResultTextView.setText(countString);
			// }

		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// searchResultTextView.setText(R.string.search_instructions);
			listAdapter.swapCursor(null);
		}

	};

}
