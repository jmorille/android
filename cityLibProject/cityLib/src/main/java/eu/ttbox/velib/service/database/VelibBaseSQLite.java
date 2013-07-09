package eu.ttbox.velib.service.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import eu.ttbox.velib.service.database.Velo.VeloColumns;

public class VelibBaseSQLite extends SQLiteOpenHelper {

	private static final String CREATE_BDD = new StringBuffer(512).append("CREATE TABLE ").append(StationDatabase.TABLE_VELIB).append(" (")//
			.append(VeloColumns.COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")//
			.append(VeloColumns.COL_PROVIDER).append(" TEXT NOT NULL, ")// /
			.append(VeloColumns.COL_NUMBER).append(" TEXT NOT NULL, ")// /
			.append(VeloColumns.COL_LATITUDE_E6).append(" INTEGER NOT NULL, ")// /
			.append(VeloColumns.COL_LONGITUDE_E6).append(" INTEGER NOT NULL, ")// /
			.append(VeloColumns.COL_STATION_TOTAL).append(" INTEGER, ")//
			.append(VeloColumns.COL_STATION_CYCLE).append(" INTEGER, ")//
			.append(VeloColumns.COL_STATION_PARKING).append(" INTEGER, ")//
			.append(VeloColumns.COL_STATION_TICKET).append(" INTEGER, ")//
			.append(VeloColumns.COL_STATION_UPDATE_TIME).append(" INTEGER, ")//
			.append(VeloColumns.COL_NAME).append(" INTEGER NOT NULL, ")// /
			.append(VeloColumns.COL_ADDRESS).append(" TEXT NOT NULL, ")// /
			.append(VeloColumns.COL_OPEN).append(" INTEGER, ")//
			.append(VeloColumns.COL_BONUS).append(" INTEGER, ")//
			.append(VeloColumns.COL_FAVORY).append(" INTEGER, ")//
			.append(VeloColumns.COL_FAVORY_TYPE).append(" TEXT, ")//
			.append(VeloColumns.COL_ALIAS_NAME).append(" TEXT, ")//
			.append(VeloColumns.COL_FULLADDRESS).append(" TEXT ")//
			.append(" );").toString();

	// Index
	private static final String CREATE_INDEX_PATTERN = "CREATE INDEX IF NOT EXISTS %s on " + StationDatabase.TABLE_VELIB + " (%s);";

	private static final String INDEX_VELIB_ADDRESS = "IDX_VELIB_ADDRES";
	private static final String INDEX_VELIB_NAME = "IDX_VELIB_ADDRES";
	private static final String INDEX_VELIB_ALIASNAME = "IDX_VELIB_ADDRES";

	public VelibBaseSQLite(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// mDatabase = db;
		db.execSQL(CREATE_BDD);
		db.execSQL(String.format(CREATE_INDEX_PATTERN, INDEX_VELIB_ADDRESS, VeloColumns.COL_ADDRESS));
		db.execSQL(String.format(CREATE_INDEX_PATTERN, INDEX_VELIB_NAME, VeloColumns.COL_NAME));
		db.execSQL(String.format(CREATE_INDEX_PATTERN, INDEX_VELIB_ALIASNAME, VeloColumns.COL_ALIAS_NAME));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(String.format("DROP TABLE %s ;", StationDatabase.TABLE_VELIB));
		for (String idxName : new String[] { INDEX_VELIB_ADDRESS, INDEX_VELIB_NAME, INDEX_VELIB_ALIASNAME }) {
			db.execSQL(String.format("DROP INDEX IF EXISTS %s ;", idxName));
		}

		onCreate(db);
	}

}
