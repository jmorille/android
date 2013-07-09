package eu.ttbox.geoping.service.accountsync;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.Track;


public class GeopingSyncAdpater  extends AbstractThreadedSyncAdapter {

    private static final String TAG = "GeopingSyncAdpater";

    private ContactsSyncAdapterService paramContactsSyncAdapterService;

    public GeopingSyncAdpater(ContactsSyncAdapterService paramContactsSyncAdapterService, Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.paramContactsSyncAdapterService = paramContactsSyncAdapterService;
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync for account : " + account );
        try {
            GeopingSyncContactHelper.performSync(getContext(), account, extras, authority, provider, syncResult);
        } catch (OperationCanceledException e) {
            Log.e(TAG,"Error on onPerformSync : "   + e.getMessage(), e );
        }
    }




}
