package eu.ttbox.velib.service.backup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import eu.ttbox.velib.VeloContentProvider;
import eu.ttbox.velib.model.FavoriteIconEnum;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.Velo.VeloColumns;

/**
 * Backup dev guide @see http://developer.android.com/guide/topics/data/backup.html
 * 
 * Sample code @see http://developer.android.com/resources/samples/BackupRestore/src/com/example/android/backuprestore/ExampleAgent.html
 *  
 * 
 */
public class CityLibBackupAgent extends BackupAgentHelper {

	private final String TAG = getClass().getSimpleName();

	private static final int AGENT_VERSION = 1;
	
	private static final String APP_DATA_KEY = "velib_stations";

	// The name of the SharedPreferences file
    static final String PREFS = "user_preferences";

    // A key to uniquely identify the set of backup data
    static final String BACKUP_KEY_PREFS = "CITYLIB_01_PREFS";

    // Object for intrinsic lock
    public static final Object[] sDataLock = new Object[0];


    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
    	// Add Preference Backup
        SharedPreferencesBackupHelper helper = new TestSharedPreferencesBackupHelper(this, PREFS);
        addHelper(BACKUP_KEY_PREFS, helper);
    }


    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        synchronized (CityLibBackupAgent.sDataLock) {
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- onBackup CityLib Backup --- Begin");
            super.onBackup(oldState, data, newState);
            Log.i(TAG, "----- onBackup CityLib   Backup --- End");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
        }
    }


    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        synchronized (CityLibBackupAgent.sDataLock) {
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- onRestore CityLib Backup : Version = " + appVersionCode);
            Log.i(TAG, "----- onRestore CityLib Backup --- Begin");
            super.onRestore(data, appVersionCode, newState);
            Log.i(TAG, "----- onRestore CityLib End --- End");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
        }
    }



	private Cursor getDbStationDataCursor() throws IOException {
		// Database Query
		String[] projection = new String[] { VeloColumns.COL_PROVIDER, VeloColumns.COL_NUMBER, VeloColumns.COL_FAVORY, VeloColumns.COL_FAVORY_TYPE,
				VeloColumns.COL_ALIAS_NAME };
		String sortOrder = String.format("%s ASC, %s ASC", VeloColumns.COL_PROVIDER, VeloColumns.COL_NUMBER);
		String selection = String.format("(%s=1) or (%s >0) or (%s NOTNULL)", VeloColumns.COL_FAVORY, VeloColumns.COL_FAVORY_TYPE,
				VeloColumns.COL_ALIAS_NAME);
//		String selection = String.format("(%s=1)", VeloColumns.COL_FAVORY );

		String[] selectionArgs = null;
		Uri toDbSave = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI, "stations");
		Cursor cursor = getContentResolver().query(toDbSave, projection, selection, selectionArgs, sortOrder);
		return cursor;
	}

	private void saveStationDatas(BackupDataOutput data) throws IOException {
		Cursor cursor = getDbStationDataCursor();
		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {

					int providerid = cursor.getInt(0);
					String stationNumber = cursor.getString(1);
					boolean isFavory = cursor.getInt(2) == 1 ? true : false;
					int favoriTypeId = cursor.getInt(3);
					String aliasName = cursor.getString(4);
					// Field to save
					String providerName = VelibProvider.getVelibProvider(providerid).name();
					String favoriTypeName = "";
					// Init Data
					if (favoriTypeId > 0) {
						favoriTypeName = FavoriteIconEnum.values()[favoriTypeId].name();
					}
					// Create buffer stream and data output stream for our data
					ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
					DataOutputStream outWriter = new DataOutputStream(bufStream);
					// Not null value
					aliasName = aliasName == null ? "" : aliasName;
					favoriTypeName = favoriTypeName == null ? "" : favoriTypeName;
					// Write structured data
					outWriter.writeUTF(providerName);
					outWriter.writeUTF(stationNumber);
					outWriter.writeBoolean(isFavory);
					outWriter.writeUTF(favoriTypeName);
					outWriter.writeUTF(aliasName);
					// Send the data to the Backup Manager via the BackupDataOutput
					byte[] buffer = bufStream.toByteArray();
					int len = buffer.length;
					String backupKey = String.format("%s_%s_%s", APP_DATA_KEY, providerName, stationNumber);
					// Write to
					data.writeEntityHeader(backupKey, len);
					data.writeEntityData(buffer, len);
					Log.i(TAG, String.format("Write Backup data station %s/%s : Favorite %s-%s / alias name %s", providerName, stationNumber, isFavory,
							favoriTypeName, aliasName));
				}
			} finally {
				cursor.close();
			}
		}
	}


	private void restoreStations(BackupDataInput data) throws IOException {
	 	while (data.readNextHeader()) {
			String key = data.getKey();
			int dataSize = data.getDataSize();
			if (key.startsWith(APP_DATA_KEY)) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
				DataInputStream in = new DataInputStream(baStream);
				// Read Data
				String providerName = in.readUTF();
				String stationNumber = in.readUTF();
				boolean isFavory = in.readBoolean();
				String favoriTypeName = in.readUTF();
				String aliasName = in.readUTF();
				// Transform data
				VelibProvider provider = null;
				FavoriteIconEnum favoType = null;
				if (isValidData(providerName)) {
					provider = VelibProvider.getVelibProvider(providerName);
				}
				if (isValidData(favoriTypeName)) {
					favoType = FavoriteIconEnum.getFromName(favoriTypeName);
				}
				// Validate Mandatory Id
				boolean isValidData = provider != null;
				if (isValidData) {
					isValidData = isValidData(stationNumber);
				}
				// Do Save Data
//				if (isValidData) {
					Log.i(TAG, String.format("Read Backup data station %s/%s : Favorite [%s-%s] / aliasname= [%s]", providerName, stationNumber, isFavory,
							favoriTypeName, aliasName));
//				}
			} else {
				 // Curious!  This entity is data under a key we do not
                // understand how to process.  Just skip it.
                data.skipEntityData();
			}
		}
		Log.i(TAG, "----- onRestore End");
	}
	
	private boolean isValidData(String data) {
		return (data != null && data.length() > 0);
	}
	


	
	 void writeStateFile(ParcelFileDescriptor stateFile) throws IOException {
		 
	 }

}
