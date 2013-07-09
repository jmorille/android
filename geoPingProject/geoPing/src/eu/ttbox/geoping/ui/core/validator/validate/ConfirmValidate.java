package eu.ttbox.geoping.ui.core.validator.validate;

import android.content.Context;
import android.widget.TextView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.ValidateField;
import eu.ttbox.geoping.ui.core.validator.Validator;

public class ConfirmValidate implements ValidateField {

    private TextView mField1;
    private TextView mField2;
    private Context mContext;
    private TextView source;
    private int mErrorMessage = R.string.validator_confirm;

    public ConfirmValidate(TextView field1, TextView field2) {
        this.mField1 = field1;
        this.mField2 = field2;
        source = mField2;
        mContext = field1.getContext();
    }

    @Override
    public boolean isValid(CharSequence value) {
        if (mField1.getText().length() > 0 && mField1.getText().equals(mField2.getText())) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public String getMessages() {
        return mContext.getString(mErrorMessage);
    }


    @Override
    public ConfirmValidate addValidator(Validator validator) {
        return this;
    }

    @Override
    public TextView getSource() {
        return source;
    }


}
