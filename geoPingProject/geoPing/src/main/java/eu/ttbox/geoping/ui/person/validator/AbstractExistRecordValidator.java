package eu.ttbox.geoping.ui.person.validator;

import android.content.Context;
import android.database.Cursor;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.core.validator.Validator;


public abstract  class AbstractExistRecordValidator implements Validator {


    private int mErrorMessage = R.string.validator_exiting_record;

    public Context mContext;
    public String entityId;

    public AbstractExistRecordValidator(Context context, String entityId) {
        super();
        this.mContext = context;
        this.entityId = entityId;
    }


    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean isValid(CharSequence value) {
        Cursor cursor = createSearchCursor(value);
        int count = 0;
        try {
            count = cursor.getCount();
        } finally {
            cursor.close();
        }
        return (count < 1);
    }

    public abstract  Cursor createSearchCursor(CharSequence value);

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }

}
