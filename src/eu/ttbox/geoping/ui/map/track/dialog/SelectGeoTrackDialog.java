package eu.ttbox.geoping.ui.map.track.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.Person;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

/**
 * @see http
 *      ://developer.android.com/resources/tutorials/views/hello-gridview.html
 * @author jmorille
 * 
 */
public class SelectGeoTrackDialog extends AlertDialog {

    private static final String TAG = "SelectGeoTrackDialog";

    private static final int GEOTRACK_SELECT_LOADER = R.id.config_id_geotrack_select_person_loader;

    private final OnSelectPersonListener mCallBack;

    // Service
    private LoaderManager loaderManager;
    private GeoTrackSelectPersonListAdapter listAdapter;

    /**
     * The callback used to indicate the user is done selecting the favorite
     * Icon.
     */
    public interface OnSelectPersonListener {

        void onSelectPerson(Person person);
    }

    public SelectGeoTrackDialog(Context context, LoaderManager loaderManager, OnSelectPersonListener callBack) {
        this(context, 0, loaderManager, callBack);
    }

    public SelectGeoTrackDialog(Context context, int theme, LoaderManager loaderManager, OnSelectPersonListener callBack) {
        super(context, theme);
        this.loaderManager = loaderManager;
        this.mCallBack = callBack;
        // Init
        Context themeContext = getContext();
        // setTitle(R.string.dialog_custum_favorite_icon);
        setCancelable(true);
        setIcon(0);
        setCanceledOnTouchOutside(true);
        setButton(BUTTON_NEGATIVE, themeContext.getText(android.R.string.cancel), (OnClickListener) null);

        // View
        LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.map_geotrack_select_dialog, null);
        setView(view);

        // List Of icons
        ListView gridview = (ListView) view;
        listAdapter = new GeoTrackSelectPersonListAdapter(themeContext, null, android.support.v4.widget.SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        gridview.setAdapter(listAdapter);
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Person favicon = (Person) parent.getItemAtPosition(position);
                if (mCallBack != null) {
                    mCallBack.onSelectPerson(favicon);
                }
                cancel();
            }
        });
        // Query
        loaderManager.initLoader(GEOTRACK_SELECT_LOADER, null, geoTrackLoaderCallback);
    }

    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> geoTrackLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = String.format("%s ASC", PersonColumns.KEY_NAME);
            String selection = null;
            String[] selectionArgs = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getContext(), PersonProvider.Constants.CONTENT_URI_PERSON, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            int resultCount = cursor.getCount();
            Log.d(TAG, String.format("onLoadFinished with %s results", resultCount));
            listAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            listAdapter.swapCursor(null);
        }

    };

}
