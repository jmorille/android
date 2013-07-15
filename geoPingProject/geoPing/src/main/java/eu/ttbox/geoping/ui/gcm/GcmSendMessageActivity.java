package eu.ttbox.geoping.ui.gcm;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.Arrays;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.gcm.GcmRegisterAsyncTask;
import eu.ttbox.geoping.service.gcm.GcmUnRegisterAsyncTask;

public class GcmSendMessageActivity extends FragmentActivity {

    public static final int PICK_ACCOUNT_REQUEST = 2;
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final String WEB_CLIENT_ID = "493878400848-pdajjiqn952onnhaj49o0b42pbfhjs87.apps.googleusercontent.com";
    private static final String TAG = "RegisterActivity";
    //
    // Instance
    String accountName;
    GoogleAccountCredential credential;
    // Binding
    private TextView msgTextView;
    private EditText msgToSend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm_send_activity);
        // Binding
        msgTextView = (TextView) findViewById(R.id.msgView);
        msgToSend = (EditText) findViewById(R.id.msgToSend);

        //
       // credential = GoogleAccountCredential.usingAudience(this, "server:client_id:" + WEB_CLIENT_ID);
        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email"));

        //  credential =   GoogleAccountCredential.usingOAuth2(this, )
        //  startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        showGoogleAccountPicker();

        // http://android-developers.blogspot.fr/2012/09/google-play-services-and-oauth-identity.html
//        AccountPicker.newChooseAccountIntent()

        // Registration
//        Intent registerActivity = new Intent(this, RegisterActivity.class);
//        startActivityForResult(registerActivity, REGISTER_ACTIVITY_REQ_CODE);
        //  GcmRegisterAsyncTask task = new GcmRegisterAsyncTask(this);
        // task.execute();
    }

    private void showGoogleAccountPicker() {
        Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);
        startActivityForResult(googlePicker, PICK_ACCOUNT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER: {
                if (data != null && data.getExtras() != null) {
                    accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        GcmRegisterAsyncTask registerTask = new GcmRegisterAsyncTask(GcmSendMessageActivity.this, credential);
                        registerTask.execute();
                    }
                }
            }
            break;
            case PICK_ACCOUNT_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        Log.d(TAG, "Account Name=" + accountName);
                        if (accountName != null) {
                            credential.setSelectedAccountName(accountName);
                            GcmRegisterAsyncTask registerTask = new GcmRegisterAsyncTask(GcmSendMessageActivity.this, credential);
                            registerTask.execute();

                            GoogleAccountCredential.
                        }
                    }
                }
            }
            break;
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister
        GcmUnRegisterAsyncTask task = new GcmUnRegisterAsyncTask(this, credential);
        task.execute();
    }

    public void onClickSendButton(View v) {
        String textToSend = msgToSend.getText().toString();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
//        gcm.send();
    }


}
