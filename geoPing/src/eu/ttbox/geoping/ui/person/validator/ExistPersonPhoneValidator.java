package eu.ttbox.geoping.ui.person.validator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase;
import eu.ttbox.geoping.ui.core.validator.Validator;

/**
 * Created by a000cqp on 13/06/13.
 */
public class ExistPersonPhoneValidator implements Validator {

    private int mErrorMessage = R.string.validator_empty;

    private Context mContext;
    private String entityId;

    public ExistPersonPhoneValidator(Context context, String entityId) {
        super();
        this.mContext = context;
        this.entityId = entityId;
    }

    @Override
    public boolean isValid(CharSequence value) {
        ContentResolver cr = mContext.getContentResolver();
        String[] projection = new String[]{PersonDatabase.PersonColumns.COL_ID};
        // Criteria
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(entityId)) {
            selection = String.format("%s != ?", PersonDatabase.PersonColumns.COL_ID);
            selectionArgs = new String[]{entityId};
        }
        String phoneNumber = value ==null ?null : value.toString();
        Uri uri = PersonProvider.Constants.getUriPhoneFilter( phoneNumber);
        Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        int count = 0;
        try {
            count = cursor.getCount();
        } finally {
            cursor.close();
        }
        return (count < 0);
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(mErrorMessage);
    }

}
