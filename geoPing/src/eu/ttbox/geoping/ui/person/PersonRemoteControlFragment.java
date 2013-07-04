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

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;


public class PersonRemoteControlFragment extends SherlockFragment {

    private static final String TAG = "PersonRemoteControlFragment";

    // Constant
    private static final  int[] buttonIds = new int[]{ //
            R.id.track_person_remote_control_pairingButton //
            , R.id.track_person_remote_control_openButton //
            , R.id.track_person_remote_control_hideButton //
    };
    // Instance
    private SparseArray<Button>
            buttonsMap ;

    private Uri entityUri;
    private String entityPhoneNumber;

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
        for (int i : buttonIds) {
            Button localButton = (Button) v.findViewById(R.id.track_person_remote_control_hideButton);
            localButton.setOnClickListener(buttonOnClickListener);
            buttonsMap.put(i, localButton);
        }

        return v;
    }

    // ===========================================================
    // Menu
    // ===========================================================


    // ===========================================================
    // Accessor
    // ===========================================================

    public void setEntity(Uri entityUri, String phoneNumber) {
        this.entityUri = entityUri;
        this.entityPhoneNumber = phoneNumber;
        if (!TextUtils.isEmpty(phoneNumber)) {
            setButtonsVisibility(true);
        } else {
            setButtonsVisibility( false);
        }
    }

    private void setButtonsVisibility( boolean isEnable ) {
        for (int key : buttonIds) {
            Button localButton = buttonsMap.get(key);
            if (localButton!=null) {
                localButton.setEnabled(isEnable);
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
                Toast.makeText(getActivity(), "Pairing button click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.track_person_remote_control_openButton:
                Toast.makeText(getActivity(), "Open App button click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.track_person_remote_control_hideButton:
                Toast.makeText(getActivity(), "Hide click", Toast.LENGTH_SHORT).show();
                break;
            default:
                throw new IllegalArgumentException("Not Implemented action for Id : " + v.getId());

        }
    }



    public void onPairingClick(View v) {
        String entityId = entityUri.getLastPathSegment();
        Intent intent = Intents.pairingRequest(getActivity(),entityPhoneNumber , entityId);
        getActivity().startService(intent);
    }

}
