package eu.ttbox.geoping.service.accountsync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


public class GeopingSyncAdpater  extends AbstractThreadedSyncAdapter {

    private static final String TAG = "GeopingSyncAdpater";

    private ContactsSyncAdapterService paramContactsSyncAdapterService;

    public GeopingSyncAdpater(ContactsSyncAdapterService paramContactsSyncAdapterService, Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.paramContactsSyncAdapterService = paramContactsSyncAdapterService;
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync ");
    }
}
