package eu.ttbox.geoping.domain.person;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.core.CountryMonitor;
import eu.ttbox.geoping.domain.message.MessageDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;

public class PersonOpenHelper extends SQLiteOpenHelper {

	private static final String TAG = "PersonOpenHelper";

	public static final String DATABASE_NAME = "person.db";
	public static final int DATABASE_VERSION = 5;
	
    // ===========================================================
    // Table
    // ===========================================================

	private static final String FTS_TABLE_CREATE_USER = "CREATE VIRTUAL TABLE " + PersonDatabase.TABLE_PERSON_FTS + //
			" USING fts3 " //
			+ "( " + PersonDatabase.PersonColumns.COL_NAME //
			+ ", " + PersonDatabase.PersonColumns.COL_PHONE //
            + ", " + PersonDatabase.PersonColumns.COL_PHONE_NORMALIZED //
            + ", " + PersonDatabase.PersonColumns.COL_PHONE_MIN_MATCH //
			+ ", " + PersonDatabase.PersonColumns.COL_COLOR //
			+ ", " + PersonDatabase.PersonColumns.COL_CONTACT_ID//
            + ", " + PersonDatabase.PersonColumns.COL_PAIRING_TIME // 
			+ ");";
	

	private static final String FTS_TABLE_CREATE_MESSAGE = "CREATE VIRTUAL TABLE " + MessageDatabase.TABLE_MESSAGE_FTS + //
			" USING fts3 " //
			+ "( " + MessageDatabase.MessageColumns.COL_NAME //
			+ ", " + MessageDatabase.MessageColumns.COL_PHONE //
			+ ", " + MessageDatabase.MessageColumns.COL_COLOR //
			+ ");";

    // ===========================================================
    // Index
    // ===========================================================
 
	private static final String INDEX_PERSON_AK = "IDX_PERSON_AK";
	private static final String CREATE_INDEX_PERSON_AK = "CREATE INDEX IF NOT EXISTS " + INDEX_PERSON_AK + " on " + PersonDatabase.TABLE_PERSON_FTS + "(" //
			+ PersonDatabase.PersonColumns.COL_PHONE //
			+ ");";

	private static final String INDEX_MESSAGE_AK = "IDX_MESSAGE_AK";
	private static final String CREATE_INDEX_MESSAGE_AK = "CREATE INDEX IF NOT EXISTS " + INDEX_MESSAGE_AK + " on " + MessageDatabase.TABLE_MESSAGE_FTS + "(" //
			+ MessageDatabase.MessageColumns.COL_PHONE //
			+ ");";

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
		mDatabase.execSQL(FTS_TABLE_CREATE_MESSAGE);
		// Index
//		db.execSQL(CREATE_INDEX_PERSON_AK);
//		db.execSQL(CREATE_INDEX_MESSAGE_AK);
		// new PersonDbBootstrap(mHelperContext, mDatabase).loadDictionary();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		// Index
//		db.execSQL("DROP INDEX  IF EXISTS " + INDEX_PERSON_AK + ";");
//		db.execSQL("DROP INDEX  IF EXISTS " + INDEX_MESSAGE_AK + ";");
		// Table
		db.execSQL("DROP TABLE IF EXISTS " + MessageDatabase.TABLE_MESSAGE_FTS);
		db.execSQL("DROP TABLE IF EXISTS " + PersonDatabase.TABLE_PERSON_FTS);
		onCreate(db);
	}

    public Object getCurrentCountryIso() {
         return countryMonitor.getCountryIso();
    }

}
