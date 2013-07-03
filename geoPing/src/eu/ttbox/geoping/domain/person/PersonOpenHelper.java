package eu.ttbox.geoping.domain.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.core.CountryMonitor;
import eu.ttbox.geoping.domain.core.UpgradeDbHelper;
import eu.ttbox.geoping.domain.message.MessageDatabase;
import eu.ttbox.geoping.domain.message.MessageDatabase.MessageColumns;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
/**
 * <ul>
 * <li>Db version 5 : Geoping 0.1.5 (37)</li>
 * <li>Db version 6 : Geoping 0.1.6 (39)</li>
 * </ul>
 *  
 *
 */
public class PersonOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "PersonOpenHelper";

    public static final String DATABASE_NAME = "person.db";
    public static final int DATABASE_VERSION = 6;

    // ===========================================================
    // Table
    // ===========================================================

    private static final String FTS_TABLE_CREATE_USER_V5 = "CREATE VIRTUAL TABLE personFTS" + //
            " USING fts3 " //
            + "( " + PersonColumns.COL_NAME //
            + ", " + PersonColumns.COL_PHONE //
            + ", " + PersonColumns.COL_PHONE_NORMALIZED //
            + ", " + PersonColumns.COL_PHONE_MIN_MATCH //
            + ", " + PersonColumns.COL_COLOR //
            + ", " + PersonColumns.COL_CONTACT_ID//
            + ", " + PersonColumns.COL_PAIRING_TIME //
            + ");";

    private static final String FTS_TABLE_CREATE_USER_V6 = "CREATE TABLE " + PersonDatabase.TABLE_PERSON_FTS //
            + "( " + PersonColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"//
            + ", " + PersonColumns.COL_NAME + " TEXT" //
            + ", " + PersonColumns.COL_PERSON_UUID + " TEXT" //
            + ", " + PersonColumns.COL_EMAIL + " TEXT" //
            // Phone
            + ", " + PersonColumns.COL_PHONE + " TEXT" //
            + ", " + PersonColumns.COL_PHONE_NORMALIZED + " TEXT" //
            + ", " + PersonColumns.COL_PHONE_MIN_MATCH + " TEXT" //
            + ", " + PersonColumns.COL_COLOR + " INTEGER"//
            + ", " + PersonColumns.COL_CONTACT_ID + " INTEGER"//
            + ", " + PersonColumns.COL_PAIRING_TIME + " INTEGER"//
            // Encryption
            + ", " + PersonColumns.COL_ENCRYPTION_PUBKEY + " TEXT"//
            + ", " + PersonColumns.COL_ENCRYPTION_PRIVKEY + " TEXT"//
            + ", " + PersonColumns.COL_ENCRYPTION_REMOTE_PUBKEY + " TEXT"//
            + ", " + PersonColumns.COL_ENCRYPTION_REMOTE_TIME + " INTEGER"//
            + ", " + PersonColumns.COL_ENCRYPTION_REMOTE_WAY + " TEXT"//
            + ");";

    private static final String FTS_TABLE_CREATE_USER = FTS_TABLE_CREATE_USER_V6;

    private static final String FTS_TABLE_CREATE_MESSAGE = "CREATE VIRTUAL TABLE " + MessageDatabase.TABLE_MESSAGE_FTS + //
            " USING fts3 " //
            + "( " + MessageColumns.COL_NAME //
            + ", " + MessageColumns.COL_PHONE //
            + ", " + MessageColumns.COL_COLOR //
            + ");";

    // ===========================================================
    // Index
    // ===========================================================

    private static final String INDEX_PERSON_PHONE_AK = "IDX_PERSON_PHONE_AK";
    private static final String CREATE_INDEX_PERSON_PHONE_AK = "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_PERSON_PHONE_AK + " on " + PersonDatabase.TABLE_PERSON_FTS + "(" //
            + PersonColumns.COL_PHONE_MIN_MATCH + ");";

    // ===========================================================
    // Constructors
    // ===========================================================

    private SQLiteDatabase mDatabase;
    private CountryMonitor countryMonitor;

    public PersonOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //
        countryMonitor = new CountryMonitor(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        // Create
        mDatabase.execSQL(FTS_TABLE_CREATE_USER);
        // mDatabase.execSQL(FTS_TABLE_CREATE_MESSAGE);
        // Index
        db.execSQL(CREATE_INDEX_PERSON_PHONE_AK);
        // db.execSQL(CREATE_INDEX_MESSAGE_AK);
        // new PersonDbBootstrap(mHelperContext, mDatabase).loadDictionary();
    }

    private void onLocalDrop(SQLiteDatabase db) {
        // Index
        db.execSQL("DROP INDEX IF EXISTS " + INDEX_PERSON_PHONE_AK);
        // Table
        db.execSQL("DROP TABLE IF EXISTS " + PersonDatabase.TABLE_PERSON_FTS);

        // Drop Older Tables
        final String[] TO_DROP_TABLE_NAME = new String[] { "messageFTS", "personFTS" // V5
        };
        for (String toDrop : TO_DROP_TABLE_NAME) {
            db.execSQL("DROP TABLE IF EXISTS " + toDrop);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        // Read previous values
        // ----------------------
        ArrayList<ContentValues> oldRows = null;
        if (oldVersion <= 5) {
            String[] stringColums = new String[] { //
            PersonColumns.COL_NAME, PersonColumns.COL_PHONE, PersonColumns.COL_PHONE_NORMALIZED, PersonColumns.COL_PHONE_MIN_MATCH //
                    , PersonColumns.COL_CONTACT_ID //
            };
            String[] intColums = new String[] { //
            PersonColumns.COL_COLOR // init
            };
            String[] longColums = new String[] { PersonColumns.COL_PAIRING_TIME };
            oldRows = UpgradeDbHelper.copyTable(db, "personFTS", stringColums, intColums, longColums);
            // Drop All Table
            db.execSQL("DROP TABLE IF EXISTS personFTS");
        } else {
            oldRows = UpgradeDbHelper.copyTable(db, PersonDatabase.TABLE_PERSON_FTS );
        }

        // Create the new Table
        // ----------------------
        onLocalDrop(db);
        onCreate(db);
        Log.i(TAG, "Upgrading database : Create TABLE  : " + PersonDatabase.TABLE_PERSON_FTS);

        // Insert data in new table
        // ----------------------
        if (oldRows != null && !oldRows.isEmpty() ) {
            List<String> validColumns = Arrays.asList(PersonColumns.ALL_COLS);
            UpgradeDbHelper.insertOldRowInNewTable(db, oldRows, PersonDatabase.TABLE_PERSON_FTS, validColumns);
        }
    }

    public Object getCurrentCountryIso() {
        return countryMonitor.getCountryIso();
    }

}
