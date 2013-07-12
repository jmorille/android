package eu.ttbox.geoping.service.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import eu.ttbox.geoping.deviceinfoendpoint.Deviceinfoendpoint;


public class GcmUnRegisterAsyncTask extends AsyncTask<String, Void, String> {

    private static String TAG = "GcmUnRegisterAsyncTask";

    private Context context;

    public Runnable endInFailure;
    public Runnable endInSuccess;

    public GcmUnRegisterAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            gcm.unregister();

            Deviceinfoendpoint endpoint = GcmRegisterHelper.getDeviceinfoendpoint(context);


//            try {
               // endpoint.removeDeviceInfo(registrationId).execute();
//            } catch (IOException e) {
//                Log.e(TAG,  "Exception received when attempting to unregister with server at "  + endpoint.getRootUrl(), e);
//            }

        } catch (IOException e) {
            Log.e(TAG, "GCM unregister error : " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (endInSuccess != null) {
            endInSuccess.run();
        }
    }


}
