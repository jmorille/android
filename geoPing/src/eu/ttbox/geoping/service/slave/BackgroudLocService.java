package eu.ttbox.geoping.service.slave;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * {link http://blog.gregfiumara.com/archives/82}
 * 
 */
public class BackgroudLocService extends Service {

	private static final String TAG = "BackgroudLocService";

	private final IBinder binder = new LocalBinder();  

	// Service
	private TelephonyManager telephonyManager;

	@Override
	public void onCreate() {
        super.onCreate();
        Log.i(TAG, "************************************");
		Log.i(TAG, "*** BackgroudLocService Created ***");
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	    super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "BackgroudLocService -- onStartCommand()");
        // Service
        this.telephonyManager =  (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
        this.telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS  | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        sendGsmBrodcat(0, 0);
        sendGsmBrodcat( telephonyManager.getCellLocation());
        return START_STICKY;
    }

	/*
	 * In Android 2.0 and later, onStart() is depreciated. Use onStartCommand()
	 * instead, or compile against API Level 5 and use both.
	 * http://android-developers
	 * .blogspot.com/2010/02/service-api-changes-starting-with.html
	 * 
	 * @Override public void onStart(Intent intent, int startId) { Log.v(TAG,
	 * "BackgroudLocService -- onStart()"); }
	 */

	@Override
	public void onDestroy() {
		Log.i(TAG, "BackgroudLocService Destroyed");
		//Service
		 this.telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		 super.onDestroy();
	}

	// ===========================================================
	// Listener
	// ===========================================================

	private PhoneStateListener phoneStateListener = new PhoneStateListener() {
		/**
		 * Callback invoked when device cell location changes.
		 * bug @see {link http://code.google.com/p/android/issues/detail?id=10931}
		 */
		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
			Log.d(TAG, "onCellLocationChanged : " + location);
			sendGsmBrodcat(location);
		}
		
		@Override 
	    public void onSignalStrengthsChanged(SignalStrength phone_sig){
			super.onSignalStrengthsChanged(phone_sig);

		}
		
	};
	
	private void sendGsmBrodcat(CellLocation location) {
	    Log.d(TAG, "onCellLocationChanged : " + location);
         if (location instanceof GsmCellLocation) {
             String networkOperator = telephonyManager.getNetworkOperator();
             if (networkOperator != null) {
                 // Mobile Country Code
                  int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                  // Mobile Network Code
                 int mnc = Integer.parseInt(networkOperator.substring(3));
                 Log.d(TAG, String.format("networkOperator mcc=%s / mnc=%s", mcc, mnc));
             }
      
            GsmCellLocation gsmLocation = (GsmCellLocation)location;
            int cid = gsmLocation.getCid();
            int lac = gsmLocation.getLac();
             sendGsmBrodcat(cid, lac);
        }
	}
	
	private void sendGsmBrodcat(int cid, int lac) {
	    Log.i(TAG, String.format("Send broadcats (cid, lac) = (%s, %s)", cid, lac));
	    Intent intent = new Intent("EVENT_GSM");
        intent.putExtra("cid", cid);
        intent.putExtra("lac", lac);
        sendBroadcast(intent);
	}

	// ===========================================================
	// Binder
	// ===========================================================

	public class LocalBinder extends Binder {
		public BackgroudLocService getService() {
			return BackgroudLocService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
}
