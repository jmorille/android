package eu.ttbox.geoping.ui.core.validator;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Form Validation Class
 *
 * Immediately, only works with EditText
 * 
 * @author throrin19
 * 
 * @version 1.0
 *
 */
public class Form {

	protected ArrayList<ValidateField> mValidates = new ArrayList<ValidateField>();
	
	/**
	 * Function adding Validates to our form
	 * @param validate
     *   {@link ValidateField} Validate to add
	 */
	public Form addValidates(ValidateField validate){
		this.mValidates.add(validate);
		return this;
	}
	
	/**
	 * Called to validate our form.
     * If an error is found, it will be displayed in the corresponding field.
	 * @return
	 * 		boolean :   true if the form is valid
     *                  false if the form is invalid
	 */
	public boolean validate(){
		boolean result = true;
		Iterator<ValidateField> it = this.mValidates.iterator();
		while(it.hasNext()){
			ValidateField validator = it.next();
			TextView field = validator.getSource();
			field.setError(null);
			if(!validator.isValid(field.getText() )){
				result = false;
				field.setError(validator.getMessages());
			}
		}
		return result;
	}
}
