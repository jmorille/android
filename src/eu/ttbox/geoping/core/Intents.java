package eu.ttbox.geoping.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.MainActivity;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;
import eu.ttbox.geoping.service.slave.GeopingNotifSlaveTypeEnum;
import eu.ttbox.geoping.service.slave.receiver.AuthorizePhoneTypeEnum;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.pairing.PairingEditActivity;
import eu.ttbox.geoping.ui.person.PersonEditActivity;

public class Intents {

    public static final String TAG = "Intents";

    public static final String ACTION_SMS_GEOPING_REQUEST_SENDER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST_SENDER";
    public static final String ACTION_SMS_GEOPING_REQUEST_HANDLER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST_HANDLER";
    public static final String ACTION_SMS_GEOPING_RESPONSE_HANDLER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_RESPONSE_HANDLER";

    public static final String ACTION_SMS_PAIRING_RESQUEST = "eu.ttbox.geoping.ACTION_SMS_PAIRING_RESQUEST";
    public static final String ACTION_SMS_PAIRING_RESPONSE = "eu.ttbox.geoping.ACTION_SMS_PAIRING_RESPONSE";

    public static final String ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE = "eu.ttbox.geoping.ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE";

    public static final String ACTION_NEW_GEOTRACK_INSERTED = "eu.ttbox.geoping.ACTION_NEW_GEOTRACK_INSERTED";

    public static final String EXTRA_GEO_E6 = "EXTRA_GEO_E6";
    public static final String EXTRA_SMS_PHONE = "EXTRA_SMS_PHONE";
    public static final String EXTRA_SMS_PARAMS = "EXTRA_SMS_PARAMS";
    public static final String EXTRA_SMS_ACTION = "EXTRA_SMS_ACTION";
    public static final String EXTRA_SMS_USER_ID = "EXTRA_SMS_USER_ID";
    public static final String EXTRA_NOTIFICATION_TYPE_ENUM_ORDINAL = "EXTRA_NOTIFICATION_TYPE";
    public static final String EXTRA_NOTIF_ID = "EXTRA_NOTIF_ID";

    public static final String EXTRA_AUTHORIZE_PHONE_TYPE_ENUM_ORDINAL = "EXTRA_AUTHORIZE_PHONE_TYPE_ORDINAL";

    public static final String EXTRA_EXPECTED_ACCURACY = "EXPECTED_ACCURACY";

    //

    // Person Edit
    public static Intent editPerson(Context context, String entityId) {
        Intent intent = new Intent(context, PersonEditActivity.class);
        if (entityId == null) {
            intent.setAction(Intent.ACTION_INSERT);
        } else {
            Uri entityUri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, String.valueOf(entityId));
            intent.setAction(Intent.ACTION_EDIT).putExtra(EXTRA_SMS_PHONE, entityId).setData(entityUri);
        }
        return intent;
    }

    public static Intent editPairing(Context context, String entityId) {
        Intent intent = new Intent(context, PairingEditActivity.class);
        if (entityId == null) {
            intent.setAction(Intent.ACTION_INSERT);
        } else {
            Uri entityUri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI, String.valueOf(entityId));
            intent.setAction(Intent.ACTION_EDIT).putExtra(EXTRA_SMS_PHONE, entityId).setData(entityUri);
        }
        return intent;
    }

    // ===========================================================
    // map Intent
    // ===========================================================

    public static Intent newGeoTrackInserted(Uri geoTrackData, ContentValues values) {
        String userId = values.getAsString(GeoTrackColumns.COL_PHONE);
        Log.d(TAG, "Create Intent action for New GeoTrack for user " + userId);
        // create
        Intent intent = new Intent(Intents.ACTION_NEW_GEOTRACK_INSERTED);
        intent.setData(geoTrackData);//
        intent.putExtra(EXTRA_SMS_PHONE, userId);
        return intent;
    }

    public static Intent showOnMap(Context context, Uri geoTrackData, ContentValues values) {
        String phone = values.getAsString(GeoTrackColumns.COL_PHONE);
        Log.d(TAG, "Create Intent action for New GeoTrack for user " + phone);
        // create
        Intent intent = new Intent(context, ShowMapActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(geoTrackData);//
        intent.putExtra(EXTRA_SMS_PHONE, phone);
        // Wsg84
        for (String cloneKey : new String[] { GeoTrackColumns.COL_LATITUDE_E6, GeoTrackColumns.COL_LONGITUDE_E6 }) {
            intent.putExtra(cloneKey, values.getAsInteger(cloneKey));
         }
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

    public static Intent pairingRequest(Context context, String phoneNumber, String userId) {
        Long entityId = Long.valueOf(userId);
        return new Intent(context, GeoPingMasterService.class) //
                .setAction(ACTION_SMS_PAIRING_RESQUEST)//
                .putExtra(EXTRA_SMS_USER_ID, entityId)//
                .putExtra(EXTRA_SMS_PHONE, phoneNumber);
    }

    // ===========================================================
    // GeoPing Slave
    // ===========================================================

    // Register Phone
    public static Intent authorizePhone(Context context, String phone, Bundle params, AuthorizePhoneTypeEnum authorizePhoneType, int notificationId, GeopingNotifSlaveTypeEnum notifType) {
        // create
        Intent intent = new Intent(context, GeoPingSlaveService.class);
        intent.setAction(ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE);
        intent.putExtra(EXTRA_SMS_PHONE, phone);
        intent.putExtra(EXTRA_SMS_PARAMS, params);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE_ENUM_ORDINAL, notifType.ordinal());
        intent.putExtra(EXTRA_NOTIF_ID, notificationId);
        intent.putExtra(EXTRA_AUTHORIZE_PHONE_TYPE_ENUM_ORDINAL, authorizePhoneType.ordinal());
        return intent;
    }

    public static Intent sendSmsGeoPingResponse(Context context, String phoneNumber) {
        return new Intent(context, GeoPingSlaveService.class) //
                .setAction(SmsMessageActionEnum.GEOPING_REQUEST.intentAction)//
                .putExtra(EXTRA_SMS_PHONE, phoneNumber);
    }
    
    // ===========================================================
    // Navigation
    // ===========================================================
    public static Intent activityMain(Context context ) {
        return new Intent(context, MainActivity.class) //
                .setAction(Intent.ACTION_VIEW )//
                // TODO Select Page
                 ;
    }

    
    // Sms Consumer

    // public static Intent consumerSmsGeoPingResponsetHandler(Context context,
    // GeoPingMessage msg) {
    // Intent intent = new Intent(context, GeoPingMasterService.class) //
    // .setAction(ACTION_SMS_GEOPING_RESPONSE_HANDLER);
    // if (msg.params != null && !msg.params.isEmpty()) {
    // intent.putExtra(EXTRA_SMS_PARAMS, msg.params);
    // }
    // intent.putExtra(EXTRA_SMS_ACTION, msg.action);
    // intent.putExtra(EXTRA_SMS_PHONE, msg.phone); //
    // return intent;
    // }

    // public static Intent consumeSmsGeoPingRequestHandler(Context context,
    // GeoPingMessage msg) {
    // Log.d(TAG, String.format("Create Intent from %s", msg));
    // return convertForSlaveGeoPingMessage(context, msg,
    // ACTION_SMS_GEOPING_REQUEST_HANDLER);
    // }

    // public static Intent consumerSmsPairingResponsetHandler(Context context,
    // GeoPingMessage msg) {
    // return convertForSlaveGeoPingMessage(context, msg,
    // ACTION_SMS_PAIRING_RESQUEST);
    // }

    // public static Intent convertForSlaveGeoPingMessage(Context context,
    // GeoPingMessage msg, String intentAction) {
    // Log.d(TAG, String.format("Create Intent from %s", msg));
    // Intent intent = new Intent(context, GeoPingSlaveService.class) //
    // .setAction(intentAction);//
    // if (msg.params != null && !msg.params.isEmpty()) {
    // intent.putExtra(EXTRA_SMS_PARAMS, msg.params);
    // }
    // intent.putExtra(EXTRA_SMS_ACTION, msg.action);
    // intent.putExtra(EXTRA_SMS_PHONE, msg.phone); //
    // return intent;
    // }
}
