package eu.ttbox.geoping.ui.core.validator.validate;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import eu.ttbox.geoping.ui.core.validator.ValidateField;
import eu.ttbox.geoping.ui.core.validator.Validator;
import eu.ttbox.geoping.ui.core.validator.ValidatorException;

public class ValidateTextView implements ValidateField {

    private static final String TAG = "ValidateTextView";
    /**
     * Validator chain
     */
    protected ArrayList<Validator> mValidators = new ArrayList<Validator>();

    /**
     * Validation failure messages
     */
    protected String mMessage = "";

    protected TextView mSource;
    private Context mContext;

    public ValidateTextView(TextView source) {
        this(source, true);
    }

    public ValidateTextView(TextView source, boolean isTextWatcher) {
        this.mSource = source;
        this.mContext = source.getContext();
        if (isTextWatcher) {
            this.mSource.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Clear on Input
                    if (s != null && s.length() > 0 && mSource.getError() != null) {
                        mSource.setError(null);
                    }
                    // Check On input
                    if (!isValid(s)) {
                        mSource.setError(mMessage);
                    }
                }
            });
        }
    }

    /**
     * Adds a validator to the end of the chain
     *
     * @param validator
     */
    public ValidateTextView addValidator(Validator validator) {
        this.mValidators.add(validator);
        return this;
    }

    public ValidateTextView addValidator(Validator... validator) {
        if (validator != null && validator.length > 0) {
            this.mValidators.addAll(Arrays.asList(validator));
        }
        return this;
    }


    @Override
    public boolean isValid(CharSequence value) {
        boolean result = true;
        this.mMessage = "";

        Iterator<Validator> it = this.mValidators.iterator();
        while (it.hasNext()) {
            Validator validator = it.next();
            try {
                if (!validator.isValid(value)) {
                    this.mMessage = validator.getMessage(mContext);
                    result = false;
                    break;
                }
            } catch (ValidatorException e) {
                Log.e(TAG, "ValidatorException : " + e.getMessage(), e);
                this.mMessage = e.getMessage();
                result = false;
                break;
            }
        }

        return result;
    }

    public String getMessages() {
        return this.mMessage;
    }

    public TextView getSource() {
        return this.mSource;
    }

}
