package eu.ttbox.smstraker.ui.person;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import eu.ttbox.smstraker.R;

public class AddPersonActivity extends Activity {

	// Bindings
	private EditText nameEditText;
    private EditText phoneEditText; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_person);
        // binding
        nameEditText = (EditText)findViewById(R.id.person_name);
        phoneEditText = (EditText)findViewById(R.id.person_phone); 
    }
    
    
    public void onSaveClick(View v) {
    	
    }

    public void onCancelClick(View v) {
    	
    }

}
