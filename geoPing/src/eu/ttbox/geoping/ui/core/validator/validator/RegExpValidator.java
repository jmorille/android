package eu.ttbox.geoping.ui.core.validator.validator;

import android.content.Context;

import java.util.regex.Pattern;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;
import eu.ttbox.geoping.ui.core.validator.ValidatorException;

/**
 * This validator test value with custom Regex Pattern.
 */
public class RegExpValidator implements Validator {
   
    private Pattern mPattern;

    private int mErrorMessage = R.string.validator_regexp;

    public RegExpValidator( ) {
        super(); 
    }

    public void setPattern(String pattern){
        mPattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isValid(CharSequence value) throws ValidatorException {
        if(mPattern != null){
            return mPattern.matcher(value).matches();
        }else{
            throw new ValidatorException("You can set Regexp Pattern first");
        }
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }
}
