package eu.ttbox.smstraker.core;

import android.content.Context;
import android.content.Intent;
import eu.ttbox.smstraker.service.sender.GeoPingSmsSenderService;
import eu.ttbox.smstraker.ui.person.AddPersonActivity;

public class Intents {

	public static final String ACTION_ADD_TRACKER_PERSON = "eu.ttbox.smstraker.ACTION_ADD_TRACKER_PERSON";

	public static final String ACTION_SMS_GEOPING = "eu.ttbox.smstraker.ACTION_SMS_GEOPING";

	public static final String EXTRA_SMS_PHONE_NUMBER = "SMS_PHONE_NUMBER";

	public static Intent addTrackerPerson(Context context) {
		return new Intent(context, AddPersonActivity.class) //
				.setAction(ACTION_ADD_TRACKER_PERSON);
	}

	public static Intent sensGeoPing(Context context, String phoneNumber) {
		return new Intent(context, GeoPingSmsSenderService.class) //
				.setAction(ACTION_SMS_GEOPING)//
				.putExtra(EXTRA_SMS_PHONE_NUMBER, phoneNumber);
	}

}
