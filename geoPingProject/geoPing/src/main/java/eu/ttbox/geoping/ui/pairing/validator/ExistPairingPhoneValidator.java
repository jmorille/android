package eu.ttbox.geoping.ui.pairing.validator;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.ui.person.validator.AbstractExistRecordValidator;

public class ExistPairingPhoneValidator  extends AbstractExistRecordValidator  {


    public ExistPairingPhoneValidator(Context context, String entityId) {
        super(context,   entityId);
    }

    public Cursor createSearchCursor(CharSequence value) {
        ContentResolver cr = mContext.getContentResolver();
        String[] projection = new String[]{PairingDatabase.PairingColumns.COL_ID};
        // Criteria
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(entityId)) {
            selection = String.format("%s != ?", PairingDatabase.PairingColumns.COL_ID);
            selectionArgs = new String[]{entityId};
        }
        String phoneNumber = value ==null ?null : value.toString();
        Uri uri = PairingProvider.Constants.getUriPhoneFilter( phoneNumber);
        Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        return cursor;
    }
}
