package eu.ttbox.geoping.service.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.content.Context;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ttbox.geoping.domain.core.UpgradeDbHelper;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

/**
 * http://blog.stylingandroid.com/archives/781
 * 
 */
public class PairingBackupHelper implements BackupHelper {

	private static final String TAG = "DbBackupHelper";

	public static final String DB_PAIRING_KEY = "DB_PAIRING_KEY";

	private static final int IS_BUFFER = 1024;

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
		ByteArrayOutputStream dataCopy = copyTable(stringColums, intColums, longColums);
		// Out Values
		// ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
		// DataOutputStream outWriter = new DataOutputStream(bufStream);
		//
		if (dataCopy != null) {
			try {
				byte[] dataBytes = dataCopy.toByteArray();
				data.writeEntityHeader(DB_PAIRING_KEY, dataBytes.length);
				data.writeEntityData(dataBytes, dataBytes.length);
			} catch (IOException e) {
				Log.e(TAG, "Error during Backup Data : " + e.getMessage(), e);
			}
		}
	}

	public ByteArrayOutputStream copyTable(String[] stringColums, String[] intColums, String[] longColums) {
		ByteArrayOutputStream bufStream = null;
		// Do copy Table
		Cursor cursor = null;
		try {
			int columnSize = stringColums.length + intColums.length + longColums.length;
			String[] columns = UpgradeDbHelper.concatAllCols(columnSize, stringColums, intColums, longColums);
			cursor = pairingDatabase.queryEntities(columns, null, null, null);
			if (cursor.getCount() > 0) {
				bufStream = new ByteArrayOutputStream(IS_BUFFER);
				GZIPOutputStream gzipOut = new GZIPOutputStream(bufStream, IS_BUFFER);
				// Json writer
				JsonFactory f = new JsonFactory();
				JsonGenerator g = f.createGenerator(gzipOut, JsonEncoding.UTF16_BE);
				while (cursor.moveToNext()) {
					g.writeStartObject();
					UpgradeDbHelper.readCursorToJson(g, cursor, stringColums, intColums, longColums);
					g.writeEndObject();
				}
				g.close(); // important: will force flushing of output, close
							// underlying output stream
				gzipOut.close();
				bufStream.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "Backup IOException : " + e.getMessage(), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bufStream;
	}

	@Override
	public void restoreEntity(BackupDataInputStream data) {
		String key = data.getKey();
		Log.d(TAG, "got entity '" + key + "' size=" + data.size());
		if (DB_PAIRING_KEY.equals(key)) {
			try {
				JsonFactory f = new JsonFactory();
				GZIPInputStream gzipIs = new GZIPInputStream(data, IS_BUFFER);
				JsonParser p = f.createJsonParser(gzipIs);
				while (p.nextToken() != JsonToken.END_OBJECT) {
					
				}
				ObjectMapper mapper = new ObjectMapper();
				
				// Close
				p.close();
				gzipIs.close();
			} catch (IOException e) {
				Log.e(TAG, "Backup IOException : " + e.getMessage(), e);
			}
		}
		Log.i(TAG, "----- onRestore End");
	}

	@Override
	public void writeNewStateDescription(ParcelFileDescriptor newState) {

	}

}
