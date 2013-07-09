package eu.ttbox.geoping.ui.geofence;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

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
        String addr = helper.getAddress(cursor);
        if (TextUtils.isEmpty(addr)) {
            // Lat Lng
            double lat = helper.getLatitude(cursor);
            double lng = helper.getLongitude(cursor);
            String coordString = String.format(Locale.US, "(%.6f, %.6f) +/- %s m", lat, lng, helper.getRadiusInMeters(cursor));
            holder.addressText.setText(coordString);
        } else {
            holder.addressText.setText(addr);
        }
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
