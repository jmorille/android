package eu.ttbox.geoping.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

	public static final String SENDER_ID = "eu.ttbox.geoping";
	
	public GCMIntentService() {
		super(SENDER_ID); 
	}
  
	@Override
	protected void onError(Context context, String errorId) {
		Log.e(TAG, String.format("onError : %s", errorId));
	}
 

	@Override
	protected void onRegistered(Context context, String regId) {
		Log.d(TAG, String.format("onRegistered : %s", regId));
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.d(TAG, String.format("onUnregistered : %s", regId));
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(TAG, String.format("onMessage : %s", intent));
		
	}
	
}
