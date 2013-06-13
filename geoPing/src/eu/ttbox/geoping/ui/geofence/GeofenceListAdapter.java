package eu.ttbox.geoping.ui.geofence;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;

public class GeofenceListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private static final String TAG = "GeofenceListAdapter";
    private Context context;
    private GeoFenceHelper helper;
    private boolean isNotBinding = true;


    public GeofenceListAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.geofence_list_item, c, flags); // if >10 add
        // ", flags"
        this.context = context;
    }

    private void intViewBinding(View view, Context context, Cursor cursor) {
        // Init Cursor
        helper = new GeoFenceHelper().initWrapper(cursor);
        isNotBinding = false;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if (isNotBinding) {
            intViewBinding(view, context, cursor);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // Bind Value
        helper.setTextAddress(holder.addressText, cursor);
        helper.setTextName(holder.nameText, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.nameText = (TextView) view.findViewById(R.id.geofence_list_item_name);
        holder.addressText = (TextView) view.findViewById(R.id.geofence_list_item_address);
        // and store it inside the layout.
        view.setTag(holder);
        return view;

    }

    static class ViewHolder {
        TextView nameText;
        TextView addressText;
    }

    // ===========================================================
    // Others
    // ===========================================================

}
