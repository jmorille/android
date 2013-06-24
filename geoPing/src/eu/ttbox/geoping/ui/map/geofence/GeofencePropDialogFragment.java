package eu.ttbox.geoping.ui.map.geofence;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.ui.geofence.GeofenceEditFragment;

/**
 * <a href="http://stackoverflow.com/questions/7008183/error-inflating-fragment-in-dialog-the-second-time">Fragment in Dailog</a>
 */
public class GeofencePropDialogFragment extends AlertDialog {

    private FragmentActivity context;

    private GeofenceEditFragment editFragment;

    public GeofencePropDialogFragment(FragmentActivity context) {
        super(context);
        this.context = context;

        setTitle(R.string.menu_geofence);
        setIcon(R.drawable.ic_action_geofence);
        // Custom Button
        setButton( DialogInterface.BUTTON_NEGATIVE , context.getString( android.R.string.cancel),(OnClickListener) null);
        setButton( DialogInterface.BUTTON_POSITIVE  , context.getString(  R.string.menu_save),  new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // View
        Context themeContext = getContext();
        LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.geofence_edit_dialog, null);
        setView(view);


        // Binding
        editFragment =  (GeofenceEditFragment)context.getSupportFragmentManager()
                .findFragmentByTag("fragment_geofence_edit_prop_dialog");
     }


    public static GeofencePropDialogFragment newInstance(FragmentActivity context, CircleGeofence geofence) {
        GeofencePropDialogFragment frag = new GeofencePropDialogFragment(context);
        frag.editFragment.loadEntity(geofence);
        return frag;
    }

    @Override
    protected void onStop() {
        // Remove Fragment
        FragmentTransaction ft2 = context.getSupportFragmentManager()
                .beginTransaction();
         ft2.remove(editFragment );
        ft2.commit();
        // Process Close
        super.onStop();

    }
}
