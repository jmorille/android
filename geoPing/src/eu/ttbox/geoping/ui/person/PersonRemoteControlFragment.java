package eu.ttbox.geoping.ui.person;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import eu.ttbox.geoping.R;


public class PersonRemoteControlFragment extends SherlockFragment {

    private static final String TAG = "PersonRemoteControlFragment";

    private SparseArray<Button> buttonsMap ;

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
        int[] buttonIds = new int[]{ //
                R.id.track_person_remote_control_pairingButton //
                , R.id.track_person_remote_control_openButton //
                , R.id.track_person_remote_control_hideButton //
        };
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
}
