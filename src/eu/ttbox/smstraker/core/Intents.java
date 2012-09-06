package eu.ttbox.smstraker.core;

import android.content.Context;
import android.content.Intent;
import eu.ttbox.smstraker.ui.person.AddPersonActivity;

public class Intents {

    public static final String ACTION_ADD_TRACKER_PERSON = "eu.ttbox.smstraker.ACTION_ADD_TRACKER_PERSON";
    
    public static Intent addTrackerPerson(Context context) {
        return new Intent(context, AddPersonActivity.class) //
        .setAction(ACTION_ADD_TRACKER_PERSON);
    }

}
