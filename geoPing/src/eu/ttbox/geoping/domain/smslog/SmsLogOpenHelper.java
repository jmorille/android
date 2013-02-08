package eu.ttbox.geoping.domain.smslog;

import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SmsLogOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "SmsLogOpenHelper";

    public static final String DATABASE_NAME = "smsLog.db";
    public static final int DATABASE_VERSION = 8;

    // ===========================================================
    // Table
    // ===========================================================

    /*
     * Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for
     * "rowid"
     */
    private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + SmsLogDatabase.TABLE_SMSLOG_FTS + //
            " USING fts3 " //
            + "( " + SmsLogColumns.COL_TIME //
            + ", " + SmsLogColumns.COL_SMSLOG_TYPE //
            + ", " + SmsLogColumns.COL_ACTION //
            + ", " + SmsLogColumns.COL_PHONE //
            + ", " + SmsLogColumns.COL_PHONE_NORMALIZED // 
            + ", " + SmsLogColumns.COL_PHONE_MIN_MATCH // 
            + ", " + SmsLogColumns.COL_MESSAGE //
            + ", " + SmsLogColumns.COL_MESSAGE_PARAMS //
            + ", " + SmsLogColumns.COL_PARENT_ID //
            + ", " + SmsLogColumns.COL_SMS_WEIGHT //
            // Acknowledge
            + ", " + SmsLogColumns.COL_IS_SEND_TIME //
            + ", " + SmsLogColumns.COL_IS_DELIVERY_TIME // 
            + ");";

    // ===========================================================
    // Constructors
    // ===========================================================

    private SQLiteDatabase mDatabase;

    SmsLogOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mDatabase.execSQL(FTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + SmsLogDatabase.TABLE_SMSLOG_FTS);
        onCreate(db);
    }

}
