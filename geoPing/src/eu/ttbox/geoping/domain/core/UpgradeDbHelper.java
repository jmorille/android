package eu.ttbox.geoping.domain.core;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;

public class UpgradeDbHelper {

	private static final String TAG = "UpgradeDbHelper";

	public static String[] concatAllCols(String[]... columns) {
		int allColSize = 0;
		// Compute Size
		for (String[] colArray : columns) {
			allColSize += colArray.length;
		}
		return concatAllCols(allColSize, columns);
	}

	public static String[] concatAllCols(int resultSize, String[]... groupColumns) {
		String[] columns = new String[resultSize];
		int dstPos = 0;
		for (String[] colArray : groupColumns) {
			System.arraycopy(colArray, 0, columns, dstPos, colArray.length);
			dstPos += colArray.length;
		}
		return columns;
	}

	public static int[] convertColToIdx(Cursor cursor, String[] columNames) {
		int[] colIdx = new int[columNames.length];
		int i = 0;
		for (String colName : columNames) {
			colIdx[i++] = cursor.getColumnIndex(colName);
		}
		return colIdx;
	}

	public static ArrayList<ContentValues> copyTable(SQLiteDatabase db, String oldTable, String[] stringColums, String[] intColums, String[] longColums) {
		ArrayList<ContentValues> allRows = null;
		Cursor cursor = null;
		try {
			// Init Columns Arrays
			int columnSize = stringColums.length + intColums.length + longColums.length;
			String[] columns = concatAllCols(columnSize, stringColums, intColums, longColums);
			// Do copy Table
			cursor = db.query(oldTable, columns, null, null, null, null, null);
			// ContentResolver cr = context.getContentResolver();
			allRows = new ArrayList<ContentValues>(cursor.getCount());
			while (cursor.moveToNext()) {
				Log.d(TAG, "Upgrading database :  Read pairing ");
				ContentValues values = readCursorToContentValues(cursor, stringColums, intColums, longColums);
				// Insert Data
				allRows.add(values);
				// cr.insert(PairingProvider.Constants.CONTENT_URI, values);
				Log.d(TAG, "Upgrading database : memory copy of row values : " + values);
			}
			Log.i(TAG, "Upgrading database : memory copy of " + allRows.size() + " rows. ");
		} catch (RuntimeException e) {
			Log.e(TAG, "Upgrading database Error during memory copy of Table " + oldTable + " : " + e.getMessage(), e);
			// allRows = null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return allRows;
	}

	public static ContentValues readCursorToContentValues(Cursor cursor, String[] stringColums, String[] intColums, String[] longColums) {
		ContentValues values = new ContentValues(stringColums.length + intColums.length + longColums.length);
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
		return values;
	}

	public static JSONObject readCursorToJson(Cursor cursor, String[] stringColums, String[] intColums, String[] longColums) {
		JSONObject values = new JSONObject();
		try {
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
		} catch (JSONException e) {
//		TODO	GeoPingApplication.getInstance().tracker().
			Log.e(TAG, "Error Writing Json : " + e.getMessage(), e);
			values = null;
		}
		return values;
	}

	public static int insertOldRowInNewTable(SQLiteDatabase db, ArrayList<ContentValues> oldRows, String newTableName) {
		int resultCount = 0;
		if (oldRows != null && !oldRows.isEmpty()) {
			try {
				db.beginTransaction();
				for (ContentValues values : oldRows) {
					resultCount += db.insertOrThrow(PairingDatabase.TABLE_PAIRING_FTS, null, values);
					Log.d(TAG, "Upgrading database : inserting memory copy of row values : " + values);
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
