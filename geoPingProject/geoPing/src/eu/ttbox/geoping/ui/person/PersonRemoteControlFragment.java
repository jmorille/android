package eu.ttbox.geoping.ui.person;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.helper.SmsParamEncoderHelper;


public class PersonRemoteControlFragment extends SherlockFragment {

    private static final String TAG = "PersonRemoteControlFragment";
    // Constant
    private static final int[] buttonIds = new int[]{ //
            R.id.track_person_remote_control_pairingButton //
            , R.id.track_person_remote_control_openButton //
            , R.id.track_person_remote_control_hideButton //
    };
    // Instance
    private Uri entityUri;
    private String entityPhoneNumber;
    // Bindings
    private SparseArray<Button> buttonsMap;
    private PhotoHeaderBinderHelper photoHeader;

    // Cache
    private PhotoThumbmailCache photoCache;

    // ===========================================================
    // OnClick Listener
    // ===========================================================
    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onButtonClick(v);
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.track_person_remote_control, container, false);

        // Binding
        buttonsMap = new SparseArray<Button>(buttonIds.length);
        for (int buttonId : buttonIds) {
            Button localButton = (Button) v.findViewById(buttonId);
            localButton.setOnClickListener(buttonOnClickListener);
            buttonsMap.put(buttonId, localButton);
        }
        photoHeader = new PhotoHeaderBinderHelper(v);
        photoHeader.setBlockSubElementVisible(false);
        // Cache
        photoCache = GeoPingApplication.getInstance().getPhotoThumbmailCache();

        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Load Data
        loadEntity(getArguments());
    }


    // ===========================================================
    // Menu
    // ===========================================================


    // ===========================================================
    // Accessor
    // ===========================================================

    public void loadEntity(Bundle agrs) {
        if (agrs != null && agrs.containsKey(Intents.EXTRA_DATA_URI)) {
//            String entityId = agrs.getString(Intents.EXTRA_PERSON_ID);
            String phone = agrs.getString(Intents.EXTRA_SMS_PHONE);
            Uri entiyUrl = Uri.parse(  agrs.getString(Intents.EXTRA_DATA_URI));
            setEntity(entiyUrl, phone);
        }
    }
    public void setEntity(Uri entityUri, String phoneNumber) {
        this.entityUri = entityUri;
        this.entityPhoneNumber = phoneNumber;
        if (!TextUtils.isEmpty(phoneNumber)) {
            setButtonsVisibility(true);
            // Photo
            if (photoCache!=null) {
               photoCache.loadPhoto(getActivity(), photoHeader.photoImageView, null, entityPhoneNumber);
            }
        } else {
            setButtonsVisibility(false);
        }

    }

    private void setButtonsVisibility(boolean isEnable) {
        if (buttonsMap != null) {
            for (int key : buttonIds) {
                Button localButton = buttonsMap.get(key);
                if (localButton != null) {
                    localButton.setEnabled(isEnable);
                }
            }
        }
    }

    // ===========================================================
    // Action
    // ===========================================================
    public void onButtonClick(View v) {
        Button localButton = buttonsMap.get(v.getId());
        switch (v.getId()) {
            case R.id.track_person_remote_control_pairingButton:
               // Toast.makeText(getActivity(), "Pairing button click", Toast.LENGTH_SHORT).show();
                onPairingClick(v);
                break;
            case R.id.track_person_remote_control_openButton: {
                onOpenApplicationClick(v);
                Toast.makeText(getActivity(), "Open App button click", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.track_person_remote_control_hideButton:
                onTestLongSmsClick(v);
                Toast.makeText(getActivity(), "Send Test Long SMS click", Toast.LENGTH_SHORT).show();
                break;
            default:
                throw new IllegalArgumentException("Not Implemented action for Id : " + v.getId());

        }
    }
    // ===========================================================
    // Command
    // ===========================================================
    public void onOpenApplicationClick(View v) {
        String entityId = entityUri.getLastPathSegment();
        Intent intent = Intents.commandOpenApplication(getActivity(), entityPhoneNumber, entityId);
        getActivity().startService(intent);
    }

    public void onPairingClick(View v) {
        String entityId = entityUri.getLastPathSegment();
        Intent intent = Intents.pairingRequest(getActivity(), entityPhoneNumber, entityId);
        getActivity().startService(intent);
    }

    public void onTestLongSmsClick(View v) {
        Bundle params = new Bundle();
        StringBuffer sb = new StringBuffer(300);
        for (int i = 0; i<30; i ++) {
            sb.append("1234567890");
        }
        SmsMessageLocEnum.GEOFENCE_NAME.writeToBundle(params, sb.toString());
        SmsSenderHelper.sendSmsAndLogIt(getActivity(), SmsLogSideEnum.MASTER, entityPhoneNumber, SmsMessageActionEnum.GEOFENCE_Unknown_transition, params);
    }

}
