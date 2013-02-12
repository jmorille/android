package eu.ttbox.geoping.service.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ttbox.geoping.domain.core.UpgradeDbHelper;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

/**
 * http://blog.stylingandroid.com/archives/781
 * 
 */
public class PairingBackupHelper implements BackupHelper {

	private static final String TAG = "PairingBackupHelper";

	public static final String BACKUP_KEY_PAIRING_DB = "BACKUP_KEY_PAIRING_DB";

	private static final int IS_BUFFER = 1024;

	private Context context;
	private PairingDatabase pairingDatabase;

	// Data to copy
	public static final String[] stringColums = new String[] { //
	PairingColumns.COL_NAME, PairingColumns.COL_PHONE, PairingColumns.COL_PHONE_NORMALIZED, PairingColumns.COL_PHONE_MIN_MATCH //
	};
	public static final String[] intColums = new String[] { //
	PairingColumns.COL_AUTHORIZE_TYPE, PairingColumns.COL_SHOW_NOTIF // init
	};
	public static final String[] longColums = new String[] { PairingColumns.COL_PAIRING_TIME };

	public interface BackupInsertor {

		long insertEntity(ContentValues values);
	}

	public PairingBackupHelper(Context ctx) {
		super();
		this.context = ctx;
		this.pairingDatabase = new PairingDatabase(ctx);
	}
	
	@Override
	public void writeNewStateDescription(ParcelFileDescriptor newState) {
	 Log.d(TAG, "--- --------------------------------- ---");
	 Log.d(TAG, "--- writeNewStateDescription          ---");
	 Log.d(TAG, "--- --------------------------------- ---");
	}


	@Override
	public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
		Log.i(TAG, "-------------------------------------------------");
		Log.i(TAG, "--- performBackup : key =  " + BACKUP_KEY_PAIRING_DB);
		// Doing copy
		ByteArrayOutputStream dataCopy = copyTable(stringColums, intColums, longColums);
		if (dataCopy != null) {
			try {
				byte[] dataBytes = dataCopy.toByteArray();
				data.writeEntityHeader(BACKUP_KEY_PAIRING_DB, dataBytes.length);
				data.writeEntityData(dataBytes, dataBytes.length);
				Log.i(TAG, "performBackup Entity '" + BACKUP_KEY_PAIRING_DB + "' size=" + dataBytes.length);
			} catch (IOException e) {
				Log.e(TAG, "Error during Backup Data : " + e.getMessage(), e);
			}
		}
		Log.i(TAG, "----- performBackup End : key =  " + BACKUP_KEY_PAIRING_DB);
		Log.i(TAG, "-------------------------------------------------");

	}

	public ByteArrayOutputStream copyTable(String[] stringColums, String[] intColums, String[] longColums) {
		ByteArrayOutputStream bufStream = null;
		Cursor cursor = null;
		try {
//			int columnSize = stringColums.length + intColums.length + longColums.length;
//			String[] columns = UpgradeDbHelper.concatAllCols(columnSize, stringColums, intColums, longColums);
			 String[] columns = PairingColumns.ALL_COLS;
			cursor = pairingDatabase.queryEntities(columns, null, null, null);
//			bufStream = copyTable(cursor, stringColums, intColums, longColums);
			 bufStream = copyTable(cursor, columns, null, null);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bufStream;
	}

	public ByteArrayOutputStream copyTable(Cursor cursor, String[] stringColums, String[] intColums, String[] longColums) {
		ByteArrayOutputStream bufStream = null;
		// Do copy Table
		try {

			if (cursor.getCount() > 0) {
				bufStream = new ByteArrayOutputStream(IS_BUFFER);
				GZIPOutputStream gzipOut = new GZIPOutputStream(bufStream, IS_BUFFER);
				// Json writer
				JsonFactory f = new JsonFactory();
				JsonGenerator g = f.createGenerator(gzipOut, JsonEncoding.UTF16_BE);
				// StringWriter buf = new StringWriter();
				// JsonGenerator g = f.createGenerator(buf );
				g.writeStartArray();
//				PairingHelper helper = new PairingHelper().initWrapper(cursor);
				while (cursor.moveToNext()) {
					g.writeStartObject();
//					ContentValues values = helper.getContentValues(helper.getEntity(cursor));
//					UpgradeDbHelper.writeLineToJson(g, values);
					 UpgradeDbHelper.readCursorToJson(g, cursor, stringColums,  intColums, longColums);
					g.writeEndObject();
				}
				g.writeEndArray();
				g.close(); // important: will force flushing of output, close
							// underlying output stream
				// Close Streams
				gzipOut.close();
				bufStream.close();
				// Log.i(TAG, buf.toString());
			}
		} catch (IOException e) {
			Log.e(TAG, "Backup IOException : " + e.getMessage(), e);
		}
		return bufStream;
	}

	@Override
	public void restoreEntity(BackupDataInputStream data) {
		String key = data.getKey();
		Log.i(TAG, "-------------------------------------------------");
		Log.i(TAG, "--- restore Entity '" + key + "' size=" + data.size());

		if (BACKUP_KEY_PAIRING_DB.equals(key)) {
			final List<String> validColumns = Arrays.asList(PairingColumns.ALL_COLS);
			BackupInsertor dbInsertor = new BackupInsertor() {

				@Override
				public long insertEntity(ContentValues values) {
					ContentValues filteredValues = UpgradeDbHelper.filterContentValues(values, validColumns);
					return pairingDatabase.insertEntity(filteredValues);
				}
			};
			readBackup(data, validColumns, dbInsertor);
		}
		Log.i(TAG, "----- restoreEntity End");
		Log.i(TAG, "-------------------------------------------------");
	}

	public void readBackup(InputStream data, List<String> allValidColumns, BackupInsertor dbInsertor) {
		Log.d(TAG, "read Backup : " + data);
		try {
			JsonFactory f = new JsonFactory();
			GZIPInputStream gzipIs = new GZIPInputStream(data, IS_BUFFER);
			ObjectMapper mapper = new ObjectMapper();
			JsonParser jp = f.createJsonParser(gzipIs);
			// advance stream to START_ARRAY first:
			jp.nextToken();
			// Read Objects
			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			while (jp.nextToken() == JsonToken.START_OBJECT) {
				HashMap<String, Object> jsonMap = mapper.readValue(jp, typeRef);
				ContentValues values = UpgradeDbHelper.convertJsonMapAsContentValues(jsonMap, allValidColumns);
				// Insert
				long entityId = dbInsertor.insertEntity(values);
				Log.d(TAG, "Backup Pairing Line : new Inserting entity id " + entityId);
			}
			// Close
			jp.close();
			gzipIs.close();
		} catch (IOException e) {
			Log.e(TAG, "Backup IOException : " + e.getMessage(), e);
		}
	}


}
