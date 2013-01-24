package eu.ttbox.geoping.ui.pairing;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

public class PairingNotificationFragment extends Fragment {

    private static final String TAG = "PairingNotificationFragment";
   
    // Constant
    private static final int PAIRING_NOTIF_LOADER = R.id.config_id_pairing_notif_loader;
    
    // Bindings
    private CompoundButton notifShutdown;
    private CompoundButton notifBatteryLow;
    private CompoundButton notifSimChange;
    private CompoundButton notifPhoneCall;
    private CompoundButton notifPhoneReceive;

    // Alls
    private CompoundButton[]  notifViews ; 
    
    // Instance
    private Uri entityUri;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pairing_notification, container, false);

        // Bindings
        notifShutdown = (CompoundButton) v.findViewById(R.id.pairing_notification_shutdown);
        notifBatteryLow = (CompoundButton) v.findViewById(R.id.pairing_notification_battery_low);
        notifSimChange = (CompoundButton) v.findViewById(R.id.pairing_notification_sim_change);
        notifPhoneCall = (CompoundButton) v.findViewById(R.id.pairing_notification_phone_call);
        notifPhoneReceive = (CompoundButton) v.findViewById(R.id.pairing_notification_phone_reveive);

        // All
        notifViews = new CompoundButton[] {notifShutdown,notifBatteryLow, notifSimChange, notifPhoneCall,notifPhoneReceive  };
       
        for (CompoundButton view : notifViews  ) {
            // Listeners
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    

                }
            });
        }

        // Load Data
        loadEntity(getArguments());
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // ===========================================================
    // Accessor
    // ===========================================================


    // ===========================================================
    // LoaderManager
    // ===========================================================

    private void loadEntity(Bundle agrs) {
        if (agrs != null && agrs.containsKey(Intents.EXTRA_DATA_URI)) {
             getActivity().getSupportLoaderManager().initLoader(PAIRING_NOTIF_LOADER, agrs, pairingNotifLoaderCallback); 
        }
    }
    
    private final LoaderManager.LoaderCallbacks<Cursor> pairingNotifLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String entityId = args.getCharSequence(Intents.EXTRA_DATA_URI).toString();
            Uri entityUri = Uri.parse(entityId);
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), entityUri, null, null, null, null);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished with cursor result count : " + cursor.getCount());
            // Display List
            if (cursor.moveToFirst()) {
                // Data
                PairingHelper helper = new PairingHelper().initWrapper(cursor);
                helper.setCompoundButtonWithIdx(notifShutdown, cursor, helper.notifShutdown); 
                helper.setCompoundButtonWithIdx(notifBatteryLow, cursor, helper.notifBatteryLow);
                helper.setCompoundButtonWithIdx(notifSimChange, cursor, helper.notifSimChange);
                helper.setCompoundButtonWithIdx(notifPhoneCall, cursor, helper.notifPhoneCall);
                helper.setCompoundButtonWithIdx(notifPhoneReceive, cursor, helper.notifPhoneReceive); 
            }
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            for (CompoundButton view : notifViews  ) {
                view.setChecked(false);
            } 
        }

    };

}
