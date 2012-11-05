package eu.ttbox.geoping.ui.smslog;

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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;

public class SmsLogListFragment extends Fragment {

    private static final String TAG = "SmsLogListFragment";

    private static final int PAIRING_LIST_LOADER = R.id.config_id_smsLog_list_loader;

    // Constant
    private static final String PAIRING_SORT_DEFAULT = String.format("%s DESC, %s DESC", SmsLogColumns.COL_TIME, SmsLogColumns.COL_PHONE);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.smslog_list, container, false);

        // Bindings
        listView = (ListView) v.findViewById(android.R.id.list);
        // init
        listAdapter = new SmsLogListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(mOnClickListener);
        Log.d(TAG, "Binding end");
        // Intents
        getActivity().getSupportLoaderManager().initLoader(PAIRING_LIST_LOADER, null, smsLogLoaderCallback);
        return v;
    }

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
                getActivity(). getSupportLoaderManager().restartLoader(PAIRING_LIST_LOADER, null, smsLogLoaderCallback);
            }
        }
    }

    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> smsLogLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = PAIRING_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            String queryString = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), SmsLogProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished");
            // Display List
            listAdapter.swapCursor(cursor);
            cursor.setNotificationUri(getActivity().getContentResolver(), SmsLogProvider.Constants.CONTENT_URI);
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
