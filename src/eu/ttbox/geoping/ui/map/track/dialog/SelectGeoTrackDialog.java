package eu.ttbox.geoping.ui.map.track.dialog;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay;
import eu.ttbox.geoping.ui.map.track.dialog.GeoTrackSelectPersonListAdapter.OnActivatedPersonListener;

/**
 * 
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

    // Config
//    private HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser;

    /**
     * The callback used to indicate the user is done selecting the favorite
     * Icon.
     */
    public interface OnSelectPersonListener extends OnActivatedPersonListener {
        void onSelectPerson(Person person);
        void onNoPerson(SelectGeoTrackDialog dialog );
    }

    public SelectGeoTrackDialog(Context context, LoaderManager loaderManager, OnSelectPersonListener callBack, HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser) {
        this(context, 0, loaderManager, callBack, geoTrackOverlayByUser);
    }

    public SelectGeoTrackDialog(Context context, int theme, LoaderManager loaderManager, OnSelectPersonListener callBack, HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser) {
        super(context, theme);
        this.loaderManager = loaderManager;
        this.mCallBack = callBack;
//        this.geoTrackOverlayByUser = geoTrackOverlayByUser;
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
        ListView listView = (ListView) view;
        listAdapter = new GeoTrackSelectPersonListAdapter(themeContext, null, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, mCallBack, geoTrackOverlayByUser);
        listView.setAdapter(listAdapter);
        // Listener

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(TAG, String.format("Select personId %s", id));
                if (mCallBack != null) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    PersonHelper helper = new PersonHelper().initWrapper(cursor);
                    Person person = helper.getEntity(cursor);
                    mCallBack.onSelectPerson(person);
                }
                cancel();
            }
        });
        // Query
        loaderManager.initLoader(GEOTRACK_SELECT_LOADER, null, geoTrackPersonLoaderCallback);
    }

    // ===========================================================
    // Service
    // ===========================================================

    private void onNoPerson() {
        dismiss();
        mCallBack.onNoPerson( this );
    }

    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> geoTrackPersonLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = String.format("%s ASC", PersonColumns.COL_NAME);
            String selection = null;
            String[] selectionArgs = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getContext(), PersonProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            int resultCount = cursor.getCount();
            Log.d(TAG, String.format("onLoadFinished with %s results", resultCount));
            listAdapter.swapCursor(cursor);
            // check
            if (resultCount<1) {
                onNoPerson() ;
            }
         }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            listAdapter.swapCursor(null);
        }

    };

}
