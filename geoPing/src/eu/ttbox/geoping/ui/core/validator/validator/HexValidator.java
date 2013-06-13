package eu.ttbox.geoping.ui.core.validator.validator;

import android.content.Context;

import java.util.regex.Pattern;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;


public class HexValidator implements Validator {

    /**
     * This is Hex Pattern to verify value.
     */
    private static final Pattern mPattern = Pattern.compile("^(#|)[0-9A-Fa-f]+$");

  
    private int mErrorMessage = R.string.validator_alnum;

    public HexValidator( ) {
        super(); 
    }

    @Override
    public boolean isValid(CharSequence value) {
        return mPattern.matcher(value).matches();
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }
}
