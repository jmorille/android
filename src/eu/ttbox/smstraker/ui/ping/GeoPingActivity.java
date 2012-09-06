package eu.ttbox.smstraker.ui.ping;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import eu.ttbox.smstraker.R;
import eu.ttbox.smstraker.R.id;
import eu.ttbox.smstraker.R.layout;
import eu.ttbox.smstraker.core.Intents;

public class GeoPingActivity extends Activity {

    
    private ListView listView;
    private Button addPersonButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geoping);
        // Bind values
        listView = (ListView) findViewById(R.id.track_person_list);
        addPersonButton = (Button) findViewById(R.id.add_track_person_button);
        addPersonButton.setOnClickListener(new OnClickListener() {
             @Override
            public void onClick(View v) {
                Intent intent = Intents.addTrackerPerson(GeoPingActivity.this);
               startActivity(intent);
             }
        });
    }
    
    
    
}
