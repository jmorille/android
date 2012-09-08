package eu.ttbox.geoping.ui.ping;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class GeoPingActivity extends Activity {

    private static final String TAG = "GeoPingActivity";

    private static final int PERSON_LIST_LOADER = R.id.config_id_person_list_loader_started;

    // Constant
    private static final String PERSON_SORT_DEFAULT = String.format("%s DESC, %s DESC", PersonColumns.KEY_NAME, PersonColumns.KEY_PHONE);

    private static final int SAVE_ENTITY = 0;

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
                startActivityForResult(intent, SAVE_ENTITY);
            }
        });
        // init
        listAdapter = new PersonListAdapter(this, null, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(listAdapter);
        Log.d(TAG, "Binding end");
        // Intents
        getLoaderManager().initLoader(PERSON_LIST_LOADER, null, personLoaderCallback);
        handleIntent(getIntent());
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

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
        case (SAVE_ENTITY):
            if (resultCode == Activity.RESULT_OK) { 
                getLoaderManager().restartLoader(PERSON_LIST_LOADER, null, personLoaderCallback);
            }
        }
    }

    private final LoaderManager.LoaderCallbacks<Cursor> personLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
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
            Log.d(TAG, "onLoadFinished");
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
