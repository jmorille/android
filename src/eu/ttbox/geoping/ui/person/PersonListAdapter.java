package eu.ttbox.geoping.ui.person;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.domain.person.PersonHelper;

public class PersonListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private PersonHelper helper;

    private boolean isNotBinding = true;

    public PersonListAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.track_person_list_item, c, flags); // if >10 add
                                                                   // ", flags"
    }

    private void intViewBinding(View view, Context context, Cursor cursor) {
        // Init Cursor
        helper = new PersonHelper().initWrapper(cursor);
        isNotBinding = false;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        if (isNotBinding) {
            intViewBinding(view, context, cursor);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // Bind Value
        helper.setTextPersonName(holder.nameText, cursor)//
                .setTextPersonPhone(holder.phoneText, cursor);
        // Button
        final String entityId = helper.getPersonIdAsString(cursor);
        final String phoneNumber = helper.getPersonPhone(cursor);
        holder.pingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startService(Intents.sendSmsGeoPingRequest(context, phoneNumber));
                // Notif
                NotifToasts.showToastSendGeoPing(context, phoneNumber);

            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.nameText = (TextView) view.findViewById(R.id.person_list_item_name);
        holder.phoneText = (TextView) view.findViewById(R.id.person_list_item_phone);
        holder.pingButton = (Button) view.findViewById(R.id.person_list_item_geoping_button);
        // and store it inside the layout.
        view.setTag(holder);
        return view;

    }

    static class ViewHolder {
        Button pingButton;
        TextView nameText;
        TextView phoneText;
    }
}
