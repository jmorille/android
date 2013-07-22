package eu.ttbox.geoping.ui.smslog;

import android.content.Intent;
import android.content.res.TypedArray;
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;

public class SmsLogListFragment extends Fragment {

    private static final String TAG = "SmsLogListFragment";

    private static final int SMSLOG_LIST_LOADER = R.id.config_id_smsLog_list_loader;

    // Constant

    private static final int EDIT_ENTITY = 0;

    // binding
    private ListView listView;

    // Instance
    private boolean isDisplayContactDetail = true;


    // Intents
    public static class Intents {
        public static final String EXTRA_SMS_PHONE = eu.ttbox.geoping.core.Intents.EXTRA_SMS_PHONE;
        public static final String EXTRA_GEOFENCE_REQUEST_ID = "eu.ttbox.geoping.core.Intents.EXTRA_GEOFENCE_REQUEST_ID";
        public static final String EXTRA_INOUT_GOING_TYPE = "EXTRA_INOUT_GOING_TYPE";
        public static final String EXTRA_SIDE_DBCODE = "EXTRA_SIDE_DBCODE";
    }

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

    public SmsLogListFragment() {
        super();
    }

    public SmsLogListFragment( boolean isDisplayContactDetail ) {
        super();
        this.isDisplayContactDetail = isDisplayContactDetail;
    }

    @Override
    public void onInflate(android.app.Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        // Your code here to process the attributes
        final TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.SmsLogListFragment);
        this.isDisplayContactDetail =  a.getBoolean(R.styleable.SmsLogListFragment_isDisplayContactDetail, true);
        a.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.smslog_list, container, false);
        Log.d(TAG, "onCreateView");
        // Bindings
        listView = (ListView) v.findViewById(android.R.id.list);
        listView.setEmptyView(v.findViewById(android.R.id.empty));

        // init
        listAdapter = new SmsLogListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, isDisplayContactDetail);
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
        getActivity().getSupportLoaderManager().initLoader(SMSLOG_LIST_LOADER, getArguments(), smsLogLoaderCallback);
    }

    // ===========================================================
    // Observer
    // ===========================================================

    /**
     * <a href="https://bitbucket.org/craigleehi/google-i-o-2012-app/src/f4fd7504d43b/android/src/com/google/android/apps/iosched/ui/ExploreFragment.java>ExploreFragment</a>
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
        Intent intent = new Intent(getActivity(), SmsLogViewActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(SmsLogProvider.Constants.getContentUri(entityId));
        startActivityForResult(intent, EDIT_ENTITY);
    }

    private void deleteAllSmsLog() {
        int deleteCount = getActivity().getContentResolver().delete(SmsLogProvider.Constants.CONTENT_URI, null, null);
    }

    // @Override
    // public void onActivityResult(int reqCode, int resultCode, Intent data) {
    // super.onActivityResult(reqCode, resultCode, data);
    //
    // switch (reqCode) {
    // case (EDIT_ENTITY):
    // if (resultCode == Activity.RESULT_OK) {
    // getActivity().getSupportLoaderManager().restartLoader(SMSLOG_LIST_LOADER,
    // null, smsLogLoaderCallback);
    // }
    // }
    // }

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
            String sortOrder = SmsLogDatabase.SMSLOG_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            Uri searchUri = SmsLogProvider.Constants.CONTENT_URI;
            // Check for Entity Binding
            if (args != null) {
                if (args.containsKey(Intents.EXTRA_SMS_PHONE)) {
                    String phoneNumber = args.getString(Intents.EXTRA_SMS_PHONE);
                    searchUri =  SmsLogProvider.Constants.getContentUriPhoneFilter(phoneNumber);
                } else if (args.containsKey(Intents.EXTRA_GEOFENCE_REQUEST_ID)) {
                    String requestId = args.getString(Intents.EXTRA_GEOFENCE_REQUEST_ID);
                    searchUri =  SmsLogProvider.Constants.getContentUriRequestId(requestId);
                }
                if (args.containsKey(Intents.EXTRA_SIDE_DBCODE)) {
                    int isTypeSend = args.getInt(Intents.EXTRA_SIDE_DBCODE, -1);
                    if (isTypeSend > -1) {
                        selection = String.format("%s = ?", SmsLogColumns.COL_SMS_SIDE);
                        selectionArgs = new String[] { String.valueOf(isTypeSend) };
                    }
                } else if (args.containsKey(Intents.EXTRA_INOUT_GOING_TYPE)) {
                    int isTypeSend = args.getInt(Intents.EXTRA_INOUT_GOING_TYPE, 0);
                    if (isTypeSend > 0) {
                        selection = String.format("%s > 0", SmsLogColumns.COL_SMSLOG_TYPE);
                    } else if (isTypeSend < 0) {
                        selection = String.format("%s < 0", SmsLogColumns.COL_SMSLOG_TYPE);
                    }
                } 
            }

            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), searchUri, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            // Display List
            listAdapter.changeCursor(cursor);
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
            listAdapter.changeCursor(null);

        }

    };

}
