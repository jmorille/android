package eu.ttbox.geoping.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;
import eu.ttbox.geoping.service.slave.receiver.PhoneAuthorizeTypeEnum;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.person.PersonEditActivity;

public class Intents {

    public static final String TAG = "Intents";

    public static final String ACTION_SMS_GEOPING_REQUEST_SENDER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST_SENDER";
    public static final String ACTION_SMS_GEOPING_REQUEST_HANDLER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST_HANDLER";
    public static final String ACTION_SMS_GEOPING_RESPONSE_HANDLER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_RESPONSE_HANDLER";

    public static final String ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE = "eu.ttbox.geoping.ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE";

    
    public static final String ACTION_NEW_GEOTRACK_INSERTED = "eu.ttbox.geoping.ACTION_NEW_GEOTRACK_INSERTED";

    public static final String EXTRA_SMS_PHONE = "EXTRA_SMS_PHONE";
    public static final String EXTRA_SMS_PARAMS = "EXTRA_SMS_PARAMS";
    public static final String EXTRA_SMS_ACTION = "EXTRA_SMS_ACTION";

    public static final String EXTRA_AUTHORIZE_PHONE_TYPE_ORDINAL = "EXTRA_AUTHORIZE_PHONE_TYPE_ORDINAL";

    public static final String EXTRA_EXPECTED_ACCURACY = "EXPECTED_ACCURACY";

    //

    // Person Edit
    public static Intent addTrackerPerson(Context context) {
        return new Intent(context, PersonEditActivity.class) //
                .setAction(Intent.ACTION_INSERT);
    }

    public static Intent editPersone(Context context, String entityId) {
        Uri entityUri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PERSON, String.format("/%s", entityId));
        return new Intent(context, PersonEditActivity.class) //
                .setAction(Intent.ACTION_EDIT).putExtra(EXTRA_SMS_PHONE, entityId).setData(entityUri);
    }

    // ===========================================================
    // map Intent
    // ===========================================================

    public static Intent newGeoTrackInserted(Uri geoTrackData, ContentValues values) {
        String userId = values.getAsString(GeoTrackColumns.COL_PHONE_NUMBER);
        Log.d(TAG, "Create Intent action for New GeoTrack for user " + userId);
        // create
        Intent intent = new Intent(Intents.ACTION_NEW_GEOTRACK_INSERTED);
        intent.setData(geoTrackData);//
        intent.putExtra(EXTRA_SMS_PHONE, userId);
        return intent;
    }

    public static Intent showOnMap(Context context, Uri geoTrackData, ContentValues values) {
        String userId = values.getAsString(GeoTrackColumns.COL_PHONE_NUMBER);
        Log.d(TAG, "Create Intent action for New GeoTrack for user " + userId);
        // create
        Intent intent = new Intent(context, ShowMapActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(geoTrackData);//
        intent.putExtra(EXTRA_SMS_PHONE, userId);
        return intent;
    }

    // ===========================================================
    // GeoPing Master
    // ===========================================================

    // Sms Producer
    public static Intent sendSmsGeoPingRequest(Context context, String phoneNumber) {
        return new Intent(context, GeoPingMasterService.class) //
                .setAction(ACTION_SMS_GEOPING_REQUEST_SENDER)//
                .putExtra(EXTRA_SMS_PHONE, phoneNumber);
    }

    // Sms Producer
    public static Intent displayGeotrackOnMap(Context context, String phoneNumber) {
        return new Intent(context, GeoPingMasterService.class) //
                .setAction(ACTION_SMS_GEOPING_REQUEST_SENDER)//
                .putExtra(EXTRA_SMS_PHONE, phoneNumber);
    }

    // ===========================================================
    // GeoPing Slave
    // ===========================================================

    // Register Phone
    public static Intent authorizePhone(Context context, String phone, Bundle params , PhoneAuthorizeTypeEnum authorizeType) {
         // create
        Intent intent = new Intent(context, GeoPingSlaveService.class);
        intent.setAction(ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE);
        intent.putExtra(EXTRA_SMS_PHONE, phone);
        intent.putExtra(EXTRA_SMS_PARAMS, params);
        intent.putExtra(EXTRA_AUTHORIZE_PHONE_TYPE_ORDINAL, authorizeType.ordinal()); 
        return intent;
    }

    // Sms Consumer
    public static Intent consumeSmsGeoPingRequestHandler(Context context, GeoPingMessage msg) {
        Log.d(TAG, String.format("Create Intent from %s", msg));
        Intent intent = new Intent(context, GeoPingSlaveService.class) //
                .setAction(ACTION_SMS_GEOPING_REQUEST_HANDLER);//
        if (msg.params != null && !msg.params.isEmpty()) {
            intent.putExtra(EXTRA_SMS_PARAMS, msg.params);
        }
        intent.putExtra(EXTRA_SMS_ACTION, msg.action);
        intent.putExtra(EXTRA_SMS_PHONE, msg.phone); //
        return intent;
    }

    public static Intent consumerSmsGeoPingResponsetHandler(Context context, GeoPingMessage msg) {
        Intent intent = new Intent(context, GeoPingMasterService.class) //
                .setAction(ACTION_SMS_GEOPING_RESPONSE_HANDLER);
        if (msg.params != null && !msg.params.isEmpty()) {
            intent.putExtra(EXTRA_SMS_PARAMS, msg.params);
        }
        intent.putExtra(EXTRA_SMS_ACTION, msg.action);
        intent.putExtra(EXTRA_SMS_PHONE, msg.phone); //
        return intent;
    }

}
