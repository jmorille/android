package eu.ttbox.smstraker.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.smstraker.core.AppConstant;
import eu.ttbox.smstraker.domain.GeoTrack;
import eu.ttbox.smstraker.domain.GeoTrackSmsMsg;
import eu.ttbox.smstraker.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.smstraker.service.receiver.TrackerLocationHelper;

/**
 * @see http://blog.developpez.com/android23/p8571/android/creation-de-service/
 * @author deostem
 * For Conttain
 * @see http://www.java2s.com/Open-Source/Android/Map/osmand/net/osmand/data/Boundary.java.htm
 *
 */
public class GeoTrackingService extends Service implements LocationListener {

	private static final int ONE_MINUTES = 1000 * 60 ;
	 
	private Timer timer; 
	
	 
	private LocationManager lManager;
	
	private GeoTrackDatabase trackingBDD;
	
	private SharedPreferences appPreferences; 
	
	private Location lastLocation;
	
	private int minuteCount = 5;
	
	private final IBinder binder = new LocalBinder();
	
	public class LocalBinder extends Binder {
        public GeoTrackingService getService() {
            return GeoTrackingService.this;
        }
    }
	
	// initialisation des ressources 
	@Override
	public void onCreate() {
		super.onCreate();
		timer = new Timer();  
		// Timer
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		trackingBDD = new GeoTrackDatabase(this);
		//
	    Log.d(this.getClass().getName(), "onCreate"); 
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) { 
	    Log.d(this.getClass().getName(), "onStart"); 
	    timer.scheduleAtFixedRate(new TimerTask() { 
	        public void run() { 
	        	doGeoFix();
	        } 
	    }, 0, ONE_MINUTES*minuteCount); 
	 
	    return START_NOT_STICKY; 
	} 
	
	private void doGeoFix() {
		// Executer de votre tache 
    	List<String> providers = lManager.getProviders(true);
    	String choix_source = providers.get(0);
    	lManager.requestLocationUpdates(choix_source, 60000, 0, GeoTrackingService.this);
	}
	
	@Override 
	public void onDestroy() { 
	    Log.d(this.getClass().getName(), "onDestroy"); 
	    this.timer.cancel(); 
	} 
	
	// connexion client distant 
	@Override
	public IBinder onBind(Intent intent) { 
		return binder;
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		lManager.removeUpdates(this);
		if ( TrackerLocationHelper.isBetterLocation(location, lastLocation)) {
			this.lastLocation = location; 
			sendSms(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	
	private void sendSms(Location location) {
		final String KEY_SMS_PHONE_NUMBER = "smsPhoneNumber";
		final String KEY_SMS_USE = "smsUse";
		final String KEY_LOCAL_SAVE = "localSave";
		
		// Local Persist
		boolean saveLocal = appPreferences.getBoolean(KEY_LOCAL_SAVE, false);
		if (saveLocal) {
			GeoTrack geoPoint = new GeoTrack(AppConstant.LOCAL_DB_KEY, location); 
			trackingBDD.open(); 
			trackingBDD.insertTrackPoint(geoPoint);  
			trackingBDD.close();
		}
		// Sms
		boolean useSms = appPreferences.getBoolean(KEY_SMS_USE, false);
		if (useSms) {
			String destinationAddress = appPreferences.getString(KEY_SMS_PHONE_NUMBER, null);
			if (destinationAddress != null && destinationAddress.length() > 0) {
			    GeoTrackSmsMsg geoTrackMsg =   SmsMsgActionHelper.geoLocMessage(location);
				String smsMsg = SmsMsgEncryptHelper.encodeSmsMessage(geoTrackMsg);
				if (smsMsg!=null && smsMsg.length()>0) {
					SmsManager.getDefault().sendTextMessage(destinationAddress, null, smsMsg, null, null);
				}
			} else {
				Log.d("SMS Sender", "No SMS destination, define preference key " + KEY_SMS_PHONE_NUMBER);
			}
		}
	}
}
