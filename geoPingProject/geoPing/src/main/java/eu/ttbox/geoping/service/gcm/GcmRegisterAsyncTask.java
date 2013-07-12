package eu.ttbox.geoping.service.gcm;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.net.URLEncoder;

import eu.ttbox.geoping.deviceinfoendpoint.Deviceinfoendpoint;
import eu.ttbox.geoping.deviceinfoendpoint.model.DeviceInfo;
import eu.ttbox.geoping.ui.gcm.RegisterActivity;

public class GcmRegisterAsyncTask extends AsyncTask<String, Void, String> {

    public static final java.lang.String TAG = "GcmRegisterAsyncTask";



    private Context context;

    public Runnable endInFailure;
    public  Runnable endInSuccess;

    public GcmRegisterAsyncTask(Context context) {
        this.context = context;
    }

    public GcmRegisterAsyncTask(Context context, Runnable endInSuccess, Runnable endInFailure) {
        this.context = context;
        this.endInFailure = endInFailure;
        this.endInSuccess = endInSuccess;
    }



    @Override
    protected String doInBackground(String... params) {
        String registrationId = null;
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            registrationId = gcm.register(GcmRegisterHelper.PROJECT_NUMBER);
            Log.d(TAG, "************************************************************* ");
            Log.d(TAG, "*** register GCM registrationId : " + registrationId);
            Log.d(TAG, "************************************************************* ");

            if (registrationId!=null) {

              //TODO   GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, );

                Deviceinfoendpoint  endpoint = GcmRegisterHelper.getDeviceinfoendpoint(context);

                DeviceInfo deviceInfo = new DeviceInfo();
                endpoint.insertDeviceInfo(
                        deviceInfo
                                .setDeviceRegistrationID(registrationId)
                                .setTimestamp(System.currentTimeMillis())
                                .setDeviceInformation(
                                        URLEncoder
                                                .encode(android.os.Build.MANUFACTURER
                                                        + " "
                                                        + android.os.Build.PRODUCT,
                                                        "UTF-8"))).execute();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error register GCM : " + e.getMessage(), e);
            Log.e(RegisterActivity.class.getName(),
                    "Exception received when attempting to register for Google Cloud "
                            + "Messaging. Perhaps you need to set your virtual device's "
                            + " target to Google APIs? "
                            + "See https://developers.google.com/eclipse/docs/cloud_endpoints_android"
                            + " for more information.", e);
//            showDialog("There was a problem when attempting to register for "
//                    + "Google Cloud Messaging. If you're running in the emulator, "
//                    + "is the target of your virtual device set to 'Google APIs?' "
//                    + "See the Android log for more details.", endInFailure);

        }
        return registrationId;
    }

    @Override
    protected void onPostExecute(String s) {
        if (endInSuccess!=null) {
            endInSuccess.run();
        }
    }

    private void showDialog(String message, final Runnable okAction) {
        AlertDialog myDialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create();
        myDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(okAction != null) {
                    okAction.run();
                }
            }
        });
        myDialog.show();
    }

}
