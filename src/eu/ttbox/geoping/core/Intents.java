package eu.ttbox.geoping.core;

import android.content.Context;
import android.content.Intent;
import eu.ttbox.geoping.service.sender.GeoPingSmsSenderService;
import eu.ttbox.geoping.ui.person.AddPersonActivity;

public class Intents {

	public static final String ACTION_ADD_TRACKER_PERSON = "eu.ttbox.geoping.ACTION_ADD_TRACKER_PERSON";

	public static final String ACTION_SMS_GEOPING_REQUEST = "eu.ttbox.geoping.ACTION_SMS_GEOPING_REQUEST";
    public static final String ACTION_SMS_GEOPING_RESPONSE = "eu.ttbox.geoping.ACTION_SMS_GEOPING_RESPONSE";

    public static final String ACTION_NEW_GEOTRACK_INSERTED = "eu.ttbox.geoping.ACTION_GEOTRACK";

    
	public static final String EXTRA_SMS_PHONE_NUMBER = "SMS_PHONE_NUMBER";
    public static final String EXTRA_EXPECTED_ACCURACY = "EXPECTED_ACCURACY";
    public static final String EXTRA_USERID = "EXTRA_USERID";

    
    public static Intent newGeoTrackInserted(String userId){
        return new Intent(ACTION_NEW_GEOTRACK_INSERTED).putExtra(EXTRA_USERID, userId);
    }
    
	public static Intent addTrackerPerson(Context context) {
		return new Intent(context, AddPersonActivity.class) //
				.setAction(ACTION_ADD_TRACKER_PERSON);
	}

	public static Intent sendGeoPingRequest(Context context, String phoneNumber) {
		return new Intent(context, GeoPingSmsSenderService.class) //
				.setAction(ACTION_SMS_GEOPING_REQUEST)//
				.putExtra(EXTRA_SMS_PHONE_NUMBER, phoneNumber);
	}

	   public static Intent sendGeoPingResponse(Context context, String phoneNumber) {
	        return new Intent(context, GeoPingSmsSenderService.class) //
	                .setAction(ACTION_SMS_GEOPING_RESPONSE)//
	                .putExtra(EXTRA_SMS_PHONE_NUMBER, phoneNumber);
	    }

}
