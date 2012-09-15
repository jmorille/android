package eu.ttbox.geoping.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;
import eu.ttbox.geoping.ui.person.PersonEditActivity;

public class Intents {

    public static final String ACTION_SMS_GEOPING_REQUEST_SENDER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST_SENDER";
    public static final String ACTION_SMS_GEOPING_REQUEST_HANDLER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST_HANDLER";
    public static final String ACTION_SMS_GEOPING_RESPONSE_HANDLER = "eu.ttbox.geoping.ACTION_SMS_GEOPING_RESPONSE_HANDLER";

    public static final String ACTION_NEW_GEOTRACK_INSERTED = "eu.ttbox.geoping.ACTION_GEOTRACK";

    public static final String EXTRA_SMS_PHONE = "SMS_PHONE";
    public static final String EXTRA_SMS_ENCODED_MESSAGE = "EXTRA_SMS_ENCODED_MESSAGE";
    public static final String EXTRA_SMS_ACTION = "EXTRA_SMS_ACTION";

    public static final String EXTRA_EXPECTED_ACCURACY = "EXPECTED_ACCURACY";
    public static final String EXTRA_USERID = "EXTRA_USERID";

    //
    public static Intent newGeoTrackInserted(Uri geoTrackData, ContentValues values) {
        // String trackId = geoTrackData.getLastPathSegment();
        String userId = values.getAsString(GeoTrackColumns.COL_USERID);
        return new Intent(ACTION_NEW_GEOTRACK_INSERTED) //
                .setData(geoTrackData)//
                .putExtra(EXTRA_USERID, userId);
    }

    // Person Edit
    public static Intent addTrackerPerson(Context context) {
        return new Intent(context, PersonEditActivity.class) //
                .setAction(Intent.ACTION_INSERT);
    }

    public static Intent editPersone(Context context, String entityId) {
        Uri entityUri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PERSON, String.format("/%s", entityId));
        return new Intent(context, PersonEditActivity.class) //
                .setAction(Intent.ACTION_EDIT).putExtra(EXTRA_USERID, entityId).setData(entityUri);
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
    
    // ===========================================================
    // GeoPing Slave
    // ===========================================================


    // Sms Consumer
    public static Intent consumeSmsGeoPingRequestHandler(Context context, GeoTrackSmsMsg msg) {
        return new Intent(context, GeoPingSlaveService.class) //
                .setAction(ACTION_SMS_GEOPING_REQUEST_HANDLER)//
                .putExtra(EXTRA_SMS_ACTION, msg.action) //
                .putExtra(EXTRA_SMS_ENCODED_MESSAGE, msg.body) //
                .putExtra(EXTRA_SMS_PHONE, msg.phone);
    }

    public static Intent consumerSmsGeoPingResponsetHandler(Context context, GeoTrackSmsMsg msg) {
        return new Intent(context, GeoPingMasterService.class) //
                .setAction(ACTION_SMS_GEOPING_RESPONSE_HANDLER)//
                .putExtra(EXTRA_SMS_ACTION, msg.action) //
                .putExtra(EXTRA_SMS_ENCODED_MESSAGE, msg.body) //
                .putExtra(EXTRA_SMS_PHONE, msg.phone);
    }

}
