package eu.ttbox.geoping.test.service.backup;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.test.AndroidTestCase;
import android.test.mock.MockCursor;
import android.util.Log;
import eu.ttbox.geoping.service.backup.PairingBackupHelper;

public class PairingBackupHelperTest extends AndroidTestCase {

	public static final String TAG = "PairingBackupHelperTest";

	public void testEncodeMessage() {
	
		// FileDescriptor fd = null;
		// BackupDataInput dataIn = new BackupDataInput(fd);
		// BackupDataInputStream data = new BackupDataInputStream(dataIn);

		final ArrayList<HashMap<String, Object>> lines = new ArrayList<HashMap<String, Object>>();
		lines.add(getCursorLine(1));
		lines.add(getCursorLine(2));
		lines.add(getCursorLine(3));
		MockCursor cursor = new MockCursor() {
			private HashMap<String, Object> line;
			private int index = 0;

			private int colIndexLast = 0;
			private HashMap<String, Integer> colIndexes = new HashMap<String, Integer>();
			private HashMap<Integer, String> idenxCols = new HashMap<Integer, String>();

			@Override
			public int getCount() {
				return lines.size();
			}

			public boolean moveToNext() {
				boolean isNext = index < getCount();
				if (isNext) {
					line = lines.get(index);
					Log.d(TAG, "moveToNext Line : " + index + " / " + line);
					index++;
 				} else {
					line = null;
 					Log.d(TAG, "moveToNext Line : " + index + " / " + line);
				}
				return isNext;
			}

			public int getColumnIndex(String columnName) {
				int columnIndex = -1;
				if (!colIndexes.containsKey(columnName)) {
					columnIndex = colIndexLast;
					colIndexes.put(columnName, columnIndex);
					idenxCols.put(columnIndex, columnName);
					// Increment for next
					colIndexLast++;
				}
				return columnIndex;
			}

			public String getString(int columnIndex) {
				String colName = idenxCols.get(columnIndex);
				return (String)line.get(colName);
			}

			public int getInt(int columnIndex) {
				String colName = idenxCols.get(columnIndex);
				Log.d(TAG, "Column " + colName+"  = " + columnIndex);
				return (Integer)line.get(colName);
			}

			public long getLong(int columnIndex) {
				String colName = idenxCols.get(columnIndex);
				return (Long)line.get(colName);
			}
		};

		
		// Service
		PairingBackupHelper service = new PairingBackupHelper(getContext()); 
		ByteArrayOutputStream data = service.copyTable(cursor, PairingBackupHelper.stringColums, PairingBackupHelper.intColums, PairingBackupHelper.longColums);
		
	}

	private HashMap<String, Object> getCursorLine(int idx) {
		HashMap<String, Object> line = new HashMap<String, Object>();
		for (String colName : PairingBackupHelper.stringColums) {
			String colValue = "val : " + colName + "-" + idx;
			line.put(colName, colValue);
		}
		for (String colName : PairingBackupHelper.intColums) {
			Integer colValue = Integer.valueOf(idx);
			line.put(colName, colValue);
		}
		for (String colName : PairingBackupHelper.longColums) {
			Long colValue = Long.valueOf(idx);
			line.put(colName, colValue);
		}
		return line;
	}
}
