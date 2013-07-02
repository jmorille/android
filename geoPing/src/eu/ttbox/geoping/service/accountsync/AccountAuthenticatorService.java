package eu.ttbox.geoping.service.accountsync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AccountAuthenticatorService extends Service {

    private static final String TAG = "AccountAuthenticatorService";

    private GeopingAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.v(TAG, "GeoPing Authentication Service started.");
        mAuthenticator = new GeopingAuthenticator(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "===========================================================");
        Log.d(TAG, "onBind for action : " + intent.getAction()  + " / Intent :  " + intent);
        Log.d(TAG, "===========================================================");

        return mAuthenticator.getIBinder();
    }


}
