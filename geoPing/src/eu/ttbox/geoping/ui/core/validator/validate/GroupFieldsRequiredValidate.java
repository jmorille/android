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
public class GroupFieldsRequiredValidate implements ValidateField {

    private TextView mField1;
    private TextView mField2;
    private Context mContext;

    private TextView source;

    private String mErrorMessage;

    ValidateTextView field1Validator;
    ValidateTextView field2Validator;

    public GroupFieldsRequiredValidate(TextView field1, TextView field2) {
        this(new ValidateTextView(field1).addValidator(new NotEmptyValidator()),//
                new ValidateTextView(field2).addValidator(new NotEmptyValidator())//
        );
    }

    public GroupFieldsRequiredValidate(ValidateTextView field1Validator, ValidateTextView field2Validator) {
        this.field1Validator = field1Validator;
        this.field2Validator = field2Validator;
        source = field1Validator.getSource();
        mContext = source.getContext();
    }

    @Override
    public boolean isValid(CharSequence value) {
        boolean isValidField1 = field1Validator.isValid(mField1.getText());
        boolean isValidField2 = field2Validator.isValid(mField2.getText());
        if (isValidField1 || isValidField2) {
            if (!isValidField1) {
                mErrorMessage = field1Validator.getMessages();
                source = field1Validator.getSource();
            } else if (!isValidField2) {
                mErrorMessage = field2Validator.getMessages();
                source = field2Validator.getSource();
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getMessages() {
        return mErrorMessage;
    }

    @Override
    public GroupFieldsRequiredValidate addValidator(Validator validator) {
        return this;
    }

    @Override
    public TextView getSource() {
        return source;
    }

}
