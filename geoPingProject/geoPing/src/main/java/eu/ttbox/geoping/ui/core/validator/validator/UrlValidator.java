package eu.ttbox.geoping.ui.core.validator.validator;


import android.content.Context;
import android.webkit.URLUtil;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;

public class UrlValidator implements Validator {
    private int mErrorMessage = R.string.validator_url;


    public UrlValidator() {
        super();
    }

    @Override
    public boolean isValid(CharSequence url) {
        if (url.length() > 0) {
            if (URLUtil.isValidUrl(url.toString()))
                return true;
            else
                return false;
        } else {
            return true;
        }
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }

}
