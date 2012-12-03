package eu.ttbox.geoping.ui.pairing;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

public class PairingListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private PairingHelper helper;

    private boolean isNotBinding = true;

    private String contentDescAuthAlways;
    private String contentDescAuthRequest;
    private String contentDescAuthNever;
    
    public PairingListAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.pairing_list_item, c, flags); // if >10 add
                                                                   // ", flags"
    }

    private void intViewBinding(View view, Context context, Cursor cursor) {
        // Init Cursor
        helper = new PairingHelper().initWrapper(cursor);
        isNotBinding = false;
        // Image
        Resources r = context.getResources();
        contentDescAuthAlways = r.getString(R.id.pairing_authorize_type_radio_always);
        contentDescAuthNever = r.getString(R.id.pairing_authorize_type_radio_never);
        contentDescAuthRequest = r.getString(R.id.pairing_authorize_type_radio_ask);
        
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        if (isNotBinding) {
            intViewBinding(view, context, cursor);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // Bind Value 
        final String phoneNumber = helper.getPairingPhone(cursor);
        holder.phoneText.setText(phoneNumber);
        helper.setTextPairingName(holder.nameText, cursor);
        // Button
        holder.pingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startService(Intents.sendSmsGeoPingResponse(context, phoneNumber, true));
                // Notif
                NotifToasts.showToastSendGeoPingResponse(context, phoneNumber);
             }
        });
        // Backgroud
        PairingAuthorizeTypeEnum authType =  helper.getPairingAuthorizeTypeEnum(cursor);
        switch (authType) {
        case AUTHORIZE_ALWAYS:
//            view.setBackgroundResource(R.color.pairing_authorize_type_always);
//            holder.authType.setImageDrawable(  drawableAuthAlways);
            holder.authType.setImageResource(R.drawable.ic_cadenas_ouvert_vert);
            holder.authType.setContentDescription(contentDescAuthAlways);
            break;
        case AUTHORIZE_NEVER:
//            view.setBackgroundResource(R.color.pairing_authorize_type_never);
            holder.authType.setImageResource( R.drawable.ic_cadenas_ferme_rouge);
            holder.authType.setContentDescription(contentDescAuthNever);
             break;
        case AUTHORIZE_REQUEST:
//            view.setBackgroundResource(R.color.pairing_authorize_type_request);
            holder.authType.setImageResource( R.drawable.ic_cadenas_entrouvert_jaune);
            holder.authType.setContentDescription(contentDescAuthRequest);
             break; 
        default:
            holder.authType.setImageDrawable( null);
//            view.setBackgroundResource(android.R.color.transparent);
            break;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.nameText = (TextView) view.findViewById(R.id.pairing_list_item_name);
        holder.phoneText = (TextView) view.findViewById(R.id.pairing_list_item_phone);
        holder.pingButton = (ImageButton) view.findViewById(R.id.pairing_list_item_geoping_button);
        holder.pingButton.setFocusable(false);
        holder.pingButton.setFocusableInTouchMode(false);
        holder.authType = (ImageView) view.findViewById(R.id.pairing_list_item_authType);
        // and store it inside the layout.
        view.setTag(holder);
        return view;

    }

    static class ViewHolder {
        ImageButton pingButton;
        TextView nameText;
        TextView phoneText;
        ImageView authType;
    }
}
