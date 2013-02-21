package eu.ttbox.geoping.domain.core;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

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

	public static void computeMessageDigester(MessageDigest md, ContentValues values,  List<String> ignoreFields) {
	    for (Map.Entry<String, Object> keyVal : values.valueSet()) {
            String colName = keyVal.getKey();
            if (ignoreFields.contains(colName)) {
                Log.i(TAG, "Ignore Column : [" + colName + "] for in Ignore Fields List");
                continue;
            }
            Object colValue = keyVal.getValue();
            byte[]  bytes =  null;
            if (colValue ==null) {
                Log.d(TAG, "Ignore Column : [" + colName + "] for value NULL"  );
            } else  if (colValue instanceof String) {
                  bytes =  ((String) colValue).getBytes(); 
            } else if (colValue instanceof Integer) {
                Integer  val =  ((Integer) colValue) ;
                bytes = new byte[]{val.byteValue()};
            } else if (colValue instanceof Long) {
                Long  val =  ((Long) colValue) ;
             
            } else if (colValue instanceof Double) { 
            } else if (colValue instanceof Boolean) { 
            } else {
                Log.w(TAG, "Ignore Column : [" + colName + "] for type " + (colValue != null ? colValue.getClass() : "null (" + colValue + ")"));
            }
            if (bytes!=null) {
                md.update(bytes);
            }
	    }
	}
	
	public static void writeLineToJson(JsonGenerator g, ContentValues values,  List<String> ignoreFields ) {
		
		for (Map.Entry<String, Object> keyVal : values.valueSet()) {
			String colName = keyVal.getKey();
			if (ignoreFields.contains(colName)) {
				Log.i(TAG, "Ignore Column : [" + colName + "] for in Ignore Fields List");
				continue;
 			}
			
			Object colValue = keyVal.getValue();
			try {
				if (colValue ==null) {
					Log.d(TAG, "Ignore Column : [" + colName + "] for value NULL"  );
				} else  if (colValue instanceof String) {
					g.writeStringField(colName, (String) colValue);
				} else if (colValue instanceof Integer) {
					g.writeNumberField(colName, (Integer) colValue);
				} else if (colValue instanceof Long) {
					g.writeNumberField(colName, (Long) colValue);
				} else if (colValue instanceof Double) {
					g.writeNumberField(colName, (Double) colValue);
				} else if (colValue instanceof Boolean) {
					g.writeBooleanField(colName, (Boolean) colValue);
				} else {
					Log.w(TAG, "Ignore Column : [" + colName + "] for type " + (colValue != null ? colValue.getClass() : "null (" + colValue + ")"));
				}
			} catch (JsonGenerationException e) {
				// TODO GeoPingApplication.getInstance().tracker().
				Log.e(TAG, "Error Writing Json : " + e.getMessage(), e);
			} catch (IOException e) {
				// TODO GeoPingApplication.getInstance().tracker().
				Log.e(TAG, "Error Writing Json : " + e.getMessage(), e);
			}
		}
	}

	public static void readCursorToJson(JsonGenerator g, Cursor cursor, String[] stringColums, String[] intColums, String[] longColums) {
		try {
			// Read String
			if (stringColums != null) {
				for (String colName : stringColums) {
					int colIdx = cursor.getColumnIndex(colName);
					String colValue = cursor.getString(colIdx);
					g.writeStringField(colName, colValue);
				}
			}
			// Read Int
			if (intColums != null) {
				for (String colName : intColums) {
					int colIdx = cursor.getColumnIndex(colName);
					int colValue = cursor.getInt(colIdx);
					g.writeNumberField(colName, colValue);
				}
			}
			// Read Long
			if (longColums != null) {
				for (String colName : longColums) {
					int colIdx = cursor.getColumnIndex(colName);
					long colValue = cursor.getLong(colIdx);
					g.writeNumberField(colName, colValue);
				}
			}
		} catch (JsonGenerationException e) {
			// TODO GeoPingApplication.getInstance().tracker().
			Log.e(TAG, "Error Writing Json : " + e.getMessage(), e);
		} catch (IOException e) {
			// TODO GeoPingApplication.getInstance().tracker().
			Log.e(TAG, "Error Writing Json : " + e.getMessage(), e);
		}
	}

	public static ContentValues convertJsonMapAsContentValues(HashMap<String, Object> jsonMap, List<String> allValidColumns) {
		// Values
		ContentValues values = new ContentValues();
		// Read
		for (String colName : jsonMap.keySet()) {
			if (allValidColumns.contains(colName)) {
				Object colValue = jsonMap.get(colName);
				if (colValue ==null) {
					Log.d(TAG, "Ignore Column : [" + colName + "] for value NULL"  );
				} else  if (colValue instanceof String) {
					values.put(colName, (String) colValue);
				} else if (colValue instanceof Integer) {
					values.put(colName, (Integer) colValue);
				} else if (colValue instanceof Long) {
					values.put(colName, (Long) colValue);
				} else if (colValue instanceof Double) {
					values.put(colName, (Double) colValue);
				} else if (colValue instanceof Boolean) {
					values.put(colName, (Boolean) colValue);
				} else {
					Log.w(TAG, "Ignore Column : [" + colName + "] for type " + (colValue != null ? colValue.getClass() : "null (" + colValue + ")"));
				}
			}
		}
		return values;
	}

 
	
	public static int insertOldRowInNewTable(SQLiteDatabase db, ArrayList<ContentValues> oldRows, String newTableName, List<String> validColumns) {
		int resultCount = 0;
		if (oldRows != null && !oldRows.isEmpty()) {
			try {
				db.beginTransaction();
				for (ContentValues values : oldRows) {
					ContentValues filterValue = values;
					if (validColumns != null) {
						filterValue = filterContentValues(values, validColumns);
					}
					resultCount += db.insertOrThrow(PairingDatabase.TABLE_PAIRING_FTS, null, filterValue);
					Log.d(TAG, "Upgrading database : inserting memory copy of row values : " + filterValue);
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

	public static ContentValues filterContentValues(ContentValues values, List<String> validColumns) {
		ContentValues result = null;
		for (Map.Entry<String, Object> keyVal : values.valueSet()) {
			String key = keyVal.getKey();
			if (!validColumns.contains(key)) {
				if (result == null) {
					result = new ContentValues(values);
				}
				result.remove(key);
			}
		}
		return result == null ? values : result;
	}

}
