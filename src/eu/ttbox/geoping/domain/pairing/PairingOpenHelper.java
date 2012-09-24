package eu.ttbox.geoping.domain.pairing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PairingOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "PairingOpenHelper";

    public static final String DATABASE_NAME = "person.db";
    public static final int DATABASE_VERSION = 1;

    // ===========================================================
    // Table
    // ===========================================================

    private static final String FTS_TABLE_CREATE_PAIRING = "CREATE VIRTUAL TABLE " + PairingDatabase.TABLE_PAIRING_FTS + //
            " USING fts3 " //
            + "( " + PairingDatabase.PairingColumns.COL_NAME //
            + ", " + PairingDatabase.PairingColumns.COL_PHONE //
            + ", " + PairingDatabase.PairingColumns.COL_COLOR //
            + ");";

    // ===========================================================
    // Constructors
    // ===========================================================

    
    private SQLiteDatabase mDatabase;

    PairingOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mDatabase.execSQL(FTS_TABLE_CREATE_PAIRING);
        // new PairingDbBootstrap(mHelperContext, mDatabase).loadDictionary();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + PairingDatabase.TABLE_PAIRING_FTS);
        onCreate(db);
    }

}
