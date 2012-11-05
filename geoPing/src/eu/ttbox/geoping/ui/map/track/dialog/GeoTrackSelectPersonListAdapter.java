package eu.ttbox.geoping.ui.map.track.dialog;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay;
import eu.ttbox.geoping.ui.person.PersonColorDrawableHelper;

public class GeoTrackSelectPersonListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private static final String TAG = "GeoTrackSelectPersonListAdapter";

    private PersonHelper helper;

    private boolean isNotBinding = true;
    private HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser;

    private final OnActivatedPersonListener mCallBack;

    public interface OnActivatedPersonListener {
        void onDoRemovePerson(Person person);

        void onDoAddPerson(Person person);
    }

    public GeoTrackSelectPersonListAdapter(Context context, Cursor c, int flags, OnActivatedPersonListener mCallBack, HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser) {
        super(context, R.layout.map_geotrack_select_dialog_list_item, c, flags); // if
                                                                                 // >10
                                                                                 // add
                                                                                 // ", flags"
        this.geoTrackOverlayByUser = geoTrackOverlayByUser;
        this.mCallBack = mCallBack;
    }

    private void intViewBinding(View view, Context context, Cursor cursor) {
        // Init Cursor
        helper = new PersonHelper().initWrapper(cursor);
        isNotBinding = false;
    }
    
 
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        if (isNotBinding) {
            intViewBinding(view, context, cursor);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // Bind Value
        helper.setTextPersonName(holder.nameText, cursor)//
                .setTextPersonPhone(holder.phoneText, cursor);
        // Button
        final Person person = helper.getEntity(cursor);
        final String phoneNumber = helper.getPersonPhone(cursor);
        // Display Status
        boolean isActif = geoTrackOverlayByUser.containsKey(phoneNumber);
        holder.selectedSelector.setChecked(isActif);
        // Color
        int color = helper.getPersonColor(cursor);
        Drawable stld = PersonColorDrawableHelper.getListBackgroundColor(color);
        view.setBackgroundDrawable(stld);
        // Button
        holder.selectedSelector.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCallBack != null) {
                    CheckBox tb = (CheckBox) v;
                    if (tb.isChecked()) {
                        mCallBack.onDoAddPerson(person);
                    } else {
                        mCallBack.onDoRemovePerson(person);
                    }
                }

            }
        });
        // Action
        holder.pingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startService(Intents.sendSmsGeoPingRequest(context, phoneNumber));
                 // Notif
                NotifToasts.showToastSendGeoPingRequest(context, phoneNumber);

            }
        });
        // view.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // context.startActivity(Intents.editPersone(context, entityId));
        // }
        // });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.nameText = (TextView) view.findViewById(R.id.person_list_item_name);
        holder.phoneText = (TextView) view.findViewById(R.id.person_list_item_phone);
        holder.pingButton = (Button) view.findViewById(R.id.person_list_item_geoping_button);
        holder.selectedSelector = (CheckBox) view.findViewById(R.id.person_list_item_status_selected);
        // Backgroup Shape

        // normal.getPaint().setShader(shader);
        // normal.setPadding(7, 3, 7, 5);
        // and store it inside the layout.
        view.setTag(holder);
        return view;
    }

    static class ViewHolder {
        Button pingButton;
        TextView nameText;
        TextView phoneText;
        CheckBox selectedSelector;
        // CustomShapeDrawable backgroud;
    }

     

}
