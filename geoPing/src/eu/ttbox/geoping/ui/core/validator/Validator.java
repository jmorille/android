package eu.ttbox.geoping.ui.core.validator;

import android.content.Context;

/**
 * Class for creating new Validators
 * 
 * @author throrin19
 * 
 */
public interface Validator {

    /**
     * Can check if the value passed in parameter is valid or not.
     * 
     * @param value
     *            {@link String} : the value to validate
     * @return boolean : true if valid, false otherwise.
     */
    boolean isValid(CharSequence value) throws ValidatorException;

    /**
     * Used to retrieve the error message corresponding to the validator.
     * 
     * @return String : the error message
     */
    String getMessage(Context context);
}
