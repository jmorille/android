package eu.ttbox.geoping.domain.core;

import java.util.ArrayList;

import eu.ttbox.geoping.domain.pairing.PairingDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UpgradeDbHelper {

     
    private static final String TAG = "UpgradeDbHelper";
    

    public static ArrayList<ContentValues> copyTable(SQLiteDatabase db, String oldTable,   String[] stringColums, String[] intColums, String[] longColums) {
        ArrayList<ContentValues> allRows = null;
        try {
            // Init Columns Arrays
            int stringColumSize = stringColums.length;
            int intColumSize = intColums.length;
            int longColumSize = longColums.length;
            String[] columns = new String[stringColumSize + intColumSize + longColumSize];
            int dstPos = 0;
            for (String[] colArray : new String[][] { stringColums, intColums, longColums }) {
                System.arraycopy(colArray, 0, columns, dstPos, colArray.length);
                dstPos += colArray.length;
            }
            // Debug
            for (String column : columns) {
                Log.d(TAG, "Upgrading database :  Select Column : " + column);
            }
            // Do copy Table
            Cursor cursor = db.query(oldTable, columns, null, null, null, null, null);
            // ContentResolver cr = context.getContentResolver();
            allRows = new ArrayList<ContentValues>(cursor.getCount());
            while (cursor.moveToNext()) {
                Log.d(TAG, "Upgrading database :  Read pairing ");
                ContentValues values = new ContentValues(columns.length);
                // Read String
                for (String colName : stringColums) {
                    String colValue = cursor.getString(cursor.getColumnIndex(colName));
                    values.put(colName, colValue);
                }
                // Read Int
                for (String colName : intColums) {
                    int colValue = cursor.getInt(cursor.getColumnIndex(colName));
                    values.put(colName, colValue);
                }
                // Read Long
                for (String colName : longColums) {
                    long colValue = cursor.getLong(cursor.getColumnIndex(colName));
                    values.put(colName, colValue);
                }
                // Insert Data
                allRows.add(values);
                // cr.insert(PairingProvider.Constants.CONTENT_URI, values);
                Log.d(TAG, "Upgrading database : memory copy of row values : " + values   );
            }
            Log.i(TAG, "Upgrading database : memory copy of " + allRows.size() + " rows. "  );
        } catch (RuntimeException e) {
            Log.e(TAG, "Upgrading database Error during memory copy of Table " + oldTable + " : " + e.getMessage(), e);
            // allRows = null;
        }
        return allRows;
    }
    
    public static int insertOldRowInNewTable(SQLiteDatabase db, ArrayList<ContentValues> oldRows , String newTableName) {
		  int resultCount = 0 ;
		if (oldRows != null && !oldRows.isEmpty()) {
			try { 
              db.beginTransaction();
				for (ContentValues values : oldRows) { 
				    resultCount += db.insertOrThrow(PairingDatabase.TABLE_PAIRING_FTS, null, values);
					Log.d(TAG, "Upgrading database : inserting memory copy of row values : " + values   );
				}
				db.setTransactionSuccessful();
			} catch (RuntimeException e) {
				Log.e(TAG, "Upgrading database Error during inserting the copy in Table : " + e.getMessage(), e);
			} finally {
			    db.endTransaction();
			}
		}
		return resultCount;
	}
    
}
