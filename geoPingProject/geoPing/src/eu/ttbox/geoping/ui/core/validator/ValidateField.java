package eu.ttbox.geoping.ui.core.validator;

import android.widget.TextView;

public interface ValidateField {

    /**
     * Add a new validator for fields attached
     * 
     * @param validator
     *            {@link Validator} : The validator to attach
     */
    ValidateField addValidator(Validator validator);

    /**
     * Function called when the {@link Form} validation
     * 
     * @param value
     *            {@link String} : value to validate
     * @return true if all validators are valid false if a validator is invalid
     */
    boolean isValid(CharSequence value);

    /**
     * Returns the error message displayed on the connected component
     * 
     * @return {@link String} : the message to display
     */
    String getMessages();

    /**
     * Function recovering the field attached to our validator
     * 
     * @return {@link android.widget.TextView} : The fields attached
     */
    TextView getSource();
}
