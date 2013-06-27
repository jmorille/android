package eu.ttbox.geoping.ui.person;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import eu.ttbox.geoping.R;

/**
 * Created by a000cqp on 27/06/13.
 */
public class PersonCommandFragment extends SherlockFragment  {

    private static final String TAG = "PersonCommandFragment";


    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.track_person_edit, container, false);
        return v;
    }

    // ===========================================================
    // Menu
    // ===========================================================



    // ===========================================================
    // Accessor
    // ===========================================================


}
