package eu.ttbox.geoping.service.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import org.json.JSONObject;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.content.Context;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.JsonWriter;
import android.util.Log;
import eu.ttbox.geoping.domain.core.UpgradeDbHelper;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

/**
 * http://blog.stylingandroid.com/archives/781
 * 
 */
public class PairingBackupHelper implements BackupHelper {

	private static final String TAG = "DbBackupHelper";

	private static final String DB_PAIRING_KEY = "DB_PAIRING_KEY";

	private Context context;
	private PairingDatabase pairingDatabase;

	public PairingBackupHelper(Context ctx) {
		super();
		this.context = ctx;
		this.pairingDatabase = new PairingDatabase(ctx);
	}

	@Override
	public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
		// Data to copy
		String[] stringColums = new String[] { //
		PairingColumns.COL_NAME, PairingColumns.COL_PHONE, PairingColumns.COL_PHONE_NORMALIZED, PairingColumns.COL_PHONE_MIN_MATCH //
		};
		String[] intColums = new String[] { //
		PairingColumns.COL_AUTHORIZE_TYPE, PairingColumns.COL_SHOW_NOTIF // init
		};
		String[] longColums = new String[] { PairingColumns.COL_PAIRING_TIME };
		// Doing copy
		copyTable(stringColums, intColums, longColums);
		// Out Values
		// ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
		// DataOutputStream outWriter = new DataOutputStream(bufStream);
		//
		// byte[] stringBytes = mStringToBackUp.getBytes();
		// data.writeEntityHeader(DB_PAIRING_KEY, stringBytes.length);
		// data.writeEntityData(stringBytes, stringBytes.length);
	}

	public void copyTable(String[] stringColums, String[] intColums, String[] longColums) {
		// Do copy Table
		Cursor cursor = null;
		try {
			int columnSize = stringColums.length + intColums.length + longColums.length;
			String[] columns = UpgradeDbHelper.concatAllCols(columnSize, stringColums, intColums, longColums);
			cursor = pairingDatabase.queryEntities(columns, null, null, null);
			if (cursor.getCount()>0) {
			     
			    ByteArrayOutputStream bufStream = new ByteArrayOutputStream(1024);
			    GZIPOutputStream gzipOut = new GZIPOutputStream(bufStream, 1024);
			    JsonWriter writer = new JsonWriter(new OutputStreamWriter(gzipOut, "UTF-8"));
    			while (cursor.moveToNext()) {
    				JSONObject values = UpgradeDbHelper.readCursorToJson(cursor, stringColums, intColums, longColums);
    				String stringLine = values.toString();
    				byte[] stringBytes = stringLine.getBytes();
    			}
			}
		} catch (IOException e) {
		    Log.e(TAG, "Backup IOException : " + e.getMessage(), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void restoreEntity(BackupDataInputStream data) {
		String key = data.getKey();
		Log.d(TAG, "got entity '" + key + "' size=" + data.size());
//		 if (isKeyInList(key, mPrefGroups)) {
//			 
//		 }
		Log.i(TAG, "----- onRestore End");
	}

	@Override
	public void writeNewStateDescription(ParcelFileDescriptor newState) {

	}

}
