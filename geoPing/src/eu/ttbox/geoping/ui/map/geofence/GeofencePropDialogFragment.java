package eu.ttbox.geoping.ui.map.geofence;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.CircleGeofence;


public class GeofencePropDialogFragment extends DialogFragment  {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.geofence_edit_dialog);
        return dialog;
    }

    public static GeofencePropDialogFragment newInstance(CircleGeofence geofence) {
        GeofencePropDialogFragment frag = new GeofencePropDialogFragment();

        return frag;
    }

}
