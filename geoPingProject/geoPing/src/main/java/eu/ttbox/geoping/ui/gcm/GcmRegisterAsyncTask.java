package eu.ttbox.geoping.ui.gcm;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class GcmRegisterAsyncTask extends AsyncTask<String, Void, String> {

    public static final java.lang.String TAG = "GCMIntentService";

    public static final String PROJECT_NUMBER = "493878400848";

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
            registrationId = gcm.register(PROJECT_NUMBER);
            Log.d(TAG, "register GCM registrationId : " + registrationId);
        } catch (IOException e) {
            Log.e(TAG, "Error register GCM : " + e.getMessage(), e);
            Log.e(RegisterActivity.class.getName(),
                    "Exception received when attempting to register for Google Cloud "
                            + "Messaging. Perhaps you need to set your virtual device's "
                            + " target to Google APIs? "
                            + "See https://developers.google.com/eclipse/docs/cloud_endpoints_android"
                            + " for more information.", e);
            showDialog("There was a problem when attempting to register for "
                    + "Google Cloud Messaging. If you're running in the emulator, "
                    + "is the target of your virtual device set to 'Google APIs?' "
                    + "See the Android log for more details.", endInFailure);

        }
        return registrationId;
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
