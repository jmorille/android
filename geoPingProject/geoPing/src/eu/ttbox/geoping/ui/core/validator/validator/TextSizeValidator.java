package eu.ttbox.geoping.ui.core.validator.validator;

import android.content.Context;
import android.text.TextUtils;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;

public class TextSizeValidator implements Validator {

    private int mErrorMessage = R.string.validator_textSize_min_max;
    private int mErrorMessageMin = R.string.validator_textSize_min;
    private int mErrorMessageMax = R.string.validator_textSize_max;

    private Integer minSize;
    private Integer maxSize;

    public TextSizeValidator(Integer minSize, Integer maxSize) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    @Override
    public boolean isValid(CharSequence value) {
        int trimSize = value == null ? 0 : TextUtils.getTrimmedLength(value);
        if (maxSize != null && trimSize > maxSize.intValue()) {
            return false;
        }
        if (minSize != null && trimSize < minSize.intValue()) {
            return false;
        }
        return true;
    }

    @Override
    public String getMessage(Context context) {
        if (minSize!=null && maxSize!=null) {
           return context.getString(mErrorMessage, minSize, maxSize);
        } else if (maxSize!=null) {
            return context.getString(mErrorMessageMax,  maxSize);
        } else if (minSize!=null) {
            return context.getString(mErrorMessageMin, minSize );
        }
        return null;
    }

}
