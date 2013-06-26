package eu.ttbox.geoping.ui.core.validator.validator;

import android.content.Context; 

import java.util.Arrays;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;

public class TextCharacterLimitedValidator implements Validator {

    private int mErrorMessage = R.string.validator_textForbidden_notAllowChar;

    private char[] forbidenChar;

    private char foundChar;

    public TextCharacterLimitedValidator(char[] forbidenChar) {
        super();
    }

    @Override
    public boolean isValid(CharSequence value) {
        int valueSize = value ==null? 0 : value.length();
        if (valueSize >0) {
            for (int i=0  ; i<valueSize; i++) {
                char c = value.charAt(i);
               int idx = Arrays.binarySearch(forbidenChar, c);
                if (idx>-1) {
                    foundChar = c;
                    return false;
                }
            }
         }
        return true;
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage, foundChar );

    }

}
