package eu.ttbox.geoping.ui.core.validator.validator;

import android.content.Context;

import java.util.regex.Pattern;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;

public class EmailValidator implements Validator {

    private int mErrorMessage = R.string.validator_email;
 
    private String mDomainName = "";

    Pattern mPattern;

    public EmailValidator( ) {
        super(); 
    }

    /**
     * Lets say that the email address must be valid for such domain. This
     * function only accepts strings of Regexp
     * 
     * @param name
     *            Regexp Domain Name
     * 
     *            example : gmail.com
     */
    public void setDomainName(String name) {
        mDomainName = name;
        if (mDomainName != null && mDomainName.length() > 0) {
            mPattern = Pattern.compile(".+@" + mDomainName);
        } else {
            mPattern = Pattern.compile(".+@.+\\.[a-z]+");
        }
    }

    @Override
    public boolean isValid(CharSequence charseq) {
        if (charseq.length() > 0) {
            return mPattern.matcher(charseq).matches();
        } else {
            return true;
        }
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }

}
