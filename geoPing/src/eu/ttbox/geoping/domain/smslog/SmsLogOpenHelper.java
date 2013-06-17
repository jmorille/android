package eu.ttbox.geoping.domain.smslog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
/**
 * <ul>
 * <li>Db version 7 : Geoping 0.1.5 (37)</li>
 * <li>Db version 8 : Geoping 0.1.6 (39)</li>
 * <li>Db version 9 : Geoping 0.2.0 (??) : Add COL_IS_READ & COL_REQUEST_ID</li>
 * </ul>
 *  
 *
 */
public class SmsLogOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "SmsLogOpenHelper";

    public static final String DATABASE_NAME = "smsLog.db";
    public static final int DATABASE_VERSION = 9;

    // ===========================================================
    // Table
    // ===========================================================

    /*
     * Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for
     * "rowid"
     */
    private static final String FTS_TABLE_CREATE = "CREATE TABLE " + SmsLogDatabase.TABLE_SMSLOG_FTS   // 
            + "( " + SmsLogColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"//
            + ", " + SmsLogColumns.COL_TIME + " INTEGER NOT NULL"//
            + ", " + SmsLogColumns.COL_SMSLOG_TYPE + " INTEGER"  //@see SmsLogTypeEnum
            + ", " + SmsLogColumns.COL_ACTION+ " TEXT"// // @see SmsMessageActionEnum
            + ", " + SmsLogColumns.COL_PHONE + " TEXT"// 
            + ", " + SmsLogColumns.COL_PHONE_MIN_MATCH + " TEXT"// 
            + ", " + SmsLogColumns.COL_MESSAGE + " TEXT"//
            + ", " + SmsLogColumns.COL_MESSAGE_PARAMS + " TEXT"//
            + ", " + SmsLogColumns.COL_PARENT_ID  + " INTEGER"// 
            + ", " + SmsLogColumns.COL_SMS_WEIGHT + " INTEGER"//  
            + ", " + SmsLogColumns.COL_SMS_SIDE  + " INTEGER"//
            // Acknowledge
            + ", " + SmsLogColumns.COL_IS_SEND_TIME + " INTEGER"//
            + ", " + SmsLogColumns.COL_IS_DELIVERY_TIME + " INTEGER"//  
            + ", " + SmsLogColumns.COL_IS_READ + " INTEGER"//
            // Geofence
            + ", " + SmsLogColumns.COL_REQUEST_ID  + " TEXT"//
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
