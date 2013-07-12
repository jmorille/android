package eu.ttbox.geoping.ui.gcm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import eu.ttbox.geoping.R;

public class GcmSendMessageActivity extends FragmentActivity {

    private static final String TAG = "RegisterActivity";

    // Instance
    private TextView msgTextView;
    private EditText msgToSend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm_send_activity);

        msgTextView = (TextView) findViewById(R.id.msgView);
        msgToSend = (EditText) findViewById(R.id.msgView);
    }


    public void onClickSendButton(View v) {
        String textToSend = msgToSend.getText().toString();

    }




}
