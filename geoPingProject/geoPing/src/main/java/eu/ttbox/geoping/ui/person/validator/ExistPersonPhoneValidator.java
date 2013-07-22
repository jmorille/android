package eu.ttbox.geoping.ui.person.validator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase;


public class ExistPersonPhoneValidator extends AbstractExistRecordValidator  {


    public ExistPersonPhoneValidator(Context context, String entityId) {
        super(context,   entityId);
    }

    public Cursor createSearchCursor(CharSequence value) {
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
        return cursor;
    }

}
