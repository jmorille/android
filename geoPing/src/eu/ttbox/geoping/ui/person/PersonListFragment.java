package eu.ttbox.geoping.ui.person;

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
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;

public class PersonListFragment extends Fragment {

    private static final String TAG = "PersonListFragment";

    private static final int PERSON_LIST_LOADER = R.id.config_id_person_list_loader;

    // Constant
    private static final String PERSON_SORT_DEFAULT = String.format("%s DESC, %s DESC", PersonColumns.COL_NAME, PersonColumns.COL_PHONE);

    private static final int EDIT_ENTITY = 0;

    // binding
    private ListView listView;

    // init
    private PersonListAdapter listAdapter;

    private final AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Log.w(TAG, "OnItemClickListener on Item at Position=" + position + " with id=" + id);
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            PersonHelper helper = new PersonHelper().initWrapper(cursor);
            String entityId = helper.getPersonIdAsString(cursor);
            onEditEntityClick(entityId);
        }
    };
    
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        Log.d(TAG, "onActivityCreated");
//       
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.track_person_list, container, false);
        Log.d(TAG, "onCreateView");
        // Bindings
        listView = (ListView) v.findViewById(android.R.id.list);
        listView.setEmptyView(v.findViewById(android.R.id.empty));
        // Button
        Button addPersonButton = (Button) v.findViewById(R.id.add_track_person_button);
        addPersonButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddEntityClick(v);
            }
        });
       
        // init
        listAdapter = new PersonListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(mOnClickListener);
        // Empty List
        // emptyListView = (TextView) v.findViewById(android.R.id.empty);
        // listView.setEmptyView(emptyListView);
        Log.d(TAG, "Binding end");
        // Intents
        getActivity().getSupportLoaderManager().restartLoader(PERSON_LIST_LOADER, null, personLoaderCallback);

        return v;
    }

    public void onAddEntityClick(View v) {
        Intent intent = Intents.editPerson(getActivity(), null);
        startActivityForResult(intent, EDIT_ENTITY);
    }

    public void onEditEntityClick(String entityId) {
        Intent intent = Intents.editPerson(getActivity(), entityId);
        startActivityForResult(intent, EDIT_ENTITY);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
        case (EDIT_ENTITY):
            if (resultCode == Activity.RESULT_OK) {
                reloadDataList();
            }
        }
    }

    public void reloadDataList() {
        getActivity().getSupportLoaderManager().restartLoader(PERSON_LIST_LOADER, null, personLoaderCallback);
    }

    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> personLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = PERSON_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            String queryString = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), PersonProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
           
            // Display List
            listAdapter.swapCursor(cursor);
            cursor.setNotificationUri(getActivity().getContentResolver(), PersonProvider.Constants.CONTENT_URI);
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
