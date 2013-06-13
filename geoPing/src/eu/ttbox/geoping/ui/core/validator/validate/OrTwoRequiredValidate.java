package eu.ttbox.geoping.ui.core.validator.validate;

import android.content.Context;
import android.widget.TextView;

import eu.ttbox.geoping.ui.core.validator.ValidateField;
import eu.ttbox.geoping.ui.core.validator.Validator;
import eu.ttbox.geoping.ui.core.validator.validator.NotEmptyValidator;

/**
 * Validator class to validate if the fields are empty fields of 2 or not. If
 * one of them is null, no error. If both are nulls: Error
 *
 * @author throrin19
 */
public class OrTwoRequiredValidate implements ValidateField {

    private TextView mField1;
    private TextView mField2;
    private Context mContext;

    private TextView source;

    private String mErrorMessage;

    public OrTwoRequiredValidate(TextView field1, TextView field2) {
        this.mField1 = field1;
        this.mField2 = field2;
        source = mField1;
        mContext = field1.getContext();
    }

    @Override
    public boolean isValid(CharSequence value) {
        ValidateTextView field1Validator = new ValidateTextView(mField1) //
                .addValidator(new NotEmptyValidator());

        ValidateTextView field2Validator = new ValidateTextView(mField2) //
                .addValidator(new NotEmptyValidator());

        if (field1Validator.isValid(mField1.getText()) || field2Validator.isValid(mField2.getText())) {
            return true;
        } else {
            mErrorMessage = field1Validator.getMessages();
            return false;
        }
    }

    @Override
    public String getMessages() {
        return mErrorMessage;
    }

    @Override
    public OrTwoRequiredValidate addValidator(Validator validator) {
        return this;
    }

    @Override
    public TextView getSource() {
        return source;
    }

}
