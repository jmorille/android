package eu.ttbox.geoping.domain.person;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PersonOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "PersonOpenHelper";

    public static final String DATABASE_NAME = "personDb.db";
    public static final int DATABASE_VERSION = 2;

    /*
     * Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for
     * "rowid"
     */
    private static final String FTS_TABLE_CREATE_USER = "CREATE VIRTUAL TABLE " + PersonDatabase.TABLE_PERSON_FTS + //
            " USING fts3 " //
            + "( " + PersonDatabase.PersonColumns.COL_NAME // 
            + ", " + PersonDatabase.PersonColumns.COL_PHONE // 
            + ", " + PersonDatabase.PersonColumns.COL_COLOR //  
            + ");";

   
    
    private final Context mHelperContext;
    private SQLiteDatabase mDatabase;

    PersonOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mHelperContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mDatabase.execSQL(FTS_TABLE_CREATE_USER); 
//        new PersonDbBootstrap(mHelperContext, mDatabase).loadDictionary(); 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + PersonDatabase.TABLE_PERSON_FTS); 
        onCreate(db);
    }

    

}
