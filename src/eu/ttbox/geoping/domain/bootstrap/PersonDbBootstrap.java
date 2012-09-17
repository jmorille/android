package eu.ttbox.geoping.domain.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.person.PersonDatabase;

public class PersonDbBootstrap {

    private static final String TAG = "PersonDbBootstrap";

    private final Context mHelperContext;
    private SQLiteDatabase mDatabase;

    public PersonDbBootstrap(Context mHelperContext, SQLiteDatabase mDatabase) {
        super();
        this.mHelperContext = mHelperContext;
        this.mDatabase = mDatabase;
    }

    /**
     * Starts a thread to load the database table with words
     */
    public void loadDictionary() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadProducts();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void loadProducts() throws IOException {
        Log.d(TAG, "Loading persons...");
        final Resources resources = mHelperContext.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.persons);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        mDatabase.beginTransaction();
        int insertCount = 0;
        long begin = System.currentTimeMillis();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = TextUtils.split(line, "_");
                if (strings.length < 2)
                    continue;
                long id = addPerson(strings[0].trim(), strings[1].trim());
                Log.d(TAG, String.format("Add Person id %s : name=%s", id, strings[0]));
                if (id < 0) {
                    Log.e(TAG, "unable to add Person : " + strings[0].trim());
                } else {
                    insertCount++;
                }
            }
            mDatabase.setTransactionSuccessful();
            long end = System.currentTimeMillis();
            Log.i(TAG, String.format("Insert %s Persons in %s ms", insertCount, (end - begin)));

        } finally {
            reader.close();
            mDatabase.endTransaction();
        }
        Log.d(TAG, "DONE loading persons.");
    }

    /**
     * Add a word to the dictionary.
     * 
     * @return rowId or -1 if failed
     */
    public long addPerson(String phone, String name) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(PersonDatabase.PersonColumns.COL_NAME, name);
        initialValues.put(PersonDatabase.PersonColumns.COL_PHONE, phone);
        return mDatabase.insert(PersonDatabase.TABLE_PERSON_FTS, null, initialValues);
    }

}
