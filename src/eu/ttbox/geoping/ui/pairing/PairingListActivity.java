package eu.ttbox.geoping.ui.pairing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

public class PairingListActivity extends FragmentActivity {

    private static final String TAG = "GeoPingActivity";

    private static final int PAIRING_LIST_LOADER = R.id.config_id_pairing_list_loader;

    // Constant
    private static final String PAIRING_SORT_DEFAULT = String.format("%s DESC, %s DESC", PairingColumns.COL_NAME, PairingColumns.COL_PHONE);

    private static final int EDIT_ENTITY = 0;

    // binding
    private ListView listView;
  
    // init
    private PairingListAdapter listAdapter;

    private final AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Log.w(TAG, "OnItemClickListener on Item at Position=" + position + " with id=" + id);
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            PairingHelper helper = new PairingHelper().initWrapper(cursor);
            String entityId = helper.getPairingIdAsString(cursor);
            onEditEntityClick(entityId);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing_list);
        // Bindings
        listView = (ListView) findViewById(android.R.id.list);
        
        // init
        listAdapter = new PairingListAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(mOnClickListener);
        Log.d(TAG, "Binding end");
        // Intents
        getSupportLoaderManager().initLoader(PAIRING_LIST_LOADER, null, pairingLoaderCallback);
        handleIntent(getIntent());
    }

    public void onAddEntityClick(View v) {
        Intent intent = Intents.editPairing(PairingListActivity.this, null);
        startActivityForResult(intent, EDIT_ENTITY);
    }

    public void onEditEntityClick(String entityId) {
        Intent intent = Intents.editPairing(PairingListActivity.this, entityId);
        startActivityForResult(intent, EDIT_ENTITY);
    }

    private void onCancelClick() {
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pairing_list, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
            onAddEntityClick(null);
            return true;
        case R.id.menu_cancel:
            onCancelClick();
            return true;
        }
        return false;
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
        case (EDIT_ENTITY):
            if (resultCode == Activity.RESULT_OK) {
                getSupportLoaderManager().restartLoader(PAIRING_LIST_LOADER, null, pairingLoaderCallback);
            }
        }
    }

    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> pairingLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = PAIRING_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            String queryString = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(PairingListActivity.this, PairingProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished");
            // Display List
            listAdapter.swapCursor(cursor);
            cursor.setNotificationUri(getContentResolver(), PairingProvider.Constants.CONTENT_URI );
            // Display Counter
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            listAdapter.swapCursor(null);
        }

    };

}
