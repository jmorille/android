package eu.ttbox.geoping.ui.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.gcm.GcmRegisterAsyncTask;
import eu.ttbox.geoping.service.gcm.GcmUnRegisterAsyncTask;

public class GcmSendMessageActivity extends FragmentActivity {

    private static final String TAG = "RegisterActivity";

    public static final int REGISTER_ACTIVITY_REQ_CODE = 1;

    // Instance
    private TextView msgTextView;
    private EditText msgToSend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm_send_activity);
        // Binding
        msgTextView = (TextView) findViewById(R.id.msgView);
        msgToSend = (EditText) findViewById(R.id.msgView);

        // Registration
//        Intent registerActivity = new Intent(this, RegisterActivity.class);
//        startActivityForResult(registerActivity, REGISTER_ACTIVITY_REQ_CODE);
        GcmRegisterAsyncTask task = new GcmRegisterAsyncTask(this);
        task.execute(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister
        GcmUnRegisterAsyncTask task = new GcmUnRegisterAsyncTask(this);
        task.execute();
    }

    public void onClickSendButton(View v) {
        String textToSend = msgToSend.getText().toString();

    }




}
