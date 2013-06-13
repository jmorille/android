package eu.ttbox.geoping.ui.core.validator.validator;

import android.content.Context;

import java.util.regex.Pattern;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;

public class NumberValidator  implements Validator {

 
    private static final Pattern mPattern = Pattern.compile("[+-]?[\\d,]*\\.?\\d*");

    private int mErrorMessage = R.string.validator_not_number;
 
    @Override
    public boolean isValid(CharSequence value) { 
        return mPattern.matcher(value).matches();
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }

}
