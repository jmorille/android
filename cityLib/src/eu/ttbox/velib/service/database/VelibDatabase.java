package eu.ttbox.velib.service.database;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.Velo.VeloColumns;

/**
 * @see http ://developer.android.com/resources/samples/SearchableDictionary/src/com /example/android/searchabledict/DictionaryDatabase.html
 * @author jmorille Search by distance @see http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
 * 
 */
public class VelibDatabase {

	private static final int VERSION_BDD = 4;
	private static final String NOM_BDD = "velib.db";

	public static final String TABLE_VELIB = "table_velib";

	public static final String[] ALL_COLS = new String[] { VeloColumns.COL_ID, VeloColumns.COL_PROVIDER, VeloColumns.COL_NUMBER, VeloColumns.COL_LATITUDE_E6,
			VeloColumns.COL_LONGITUDE_E6, VeloColumns.COL_STATION_TOTAL, VeloColumns.COL_STATION_CYCLE, VeloColumns.COL_STATION_PARKING,
			VeloColumns.COL_STATION_TICKET, VeloColumns.COL_STATION_UPDATE_TIME, VeloColumns.COL_NAME, VeloColumns.COL_ADDRESS, VeloColumns.COL_OPEN,
			VeloColumns.COL_BONUS, VeloColumns.COL_FAVORY, VeloColumns.COL_FAVORY_TYPE, VeloColumns.COL_ALIAS_NAME, VeloColumns.COL_FULLADDRESS };

	public static final HashMap<String, String> MAP_PROJECTION_COLUMN = buildColumnMap();

	private final VelibBaseSQLite maBaseSQLite;

	public VelibDatabase(Context context) {
		maBaseSQLite = new VelibBaseSQLite(context, NOM_BDD, null, VERSION_BDD);
	}

	/**
	 * Builds a map for all columns that may be requested, which will be given to the SQLiteQueryBuilder. This is a good way to define aliases for column names,
	 * but must include all columns, even if the value is the key. This allows the ContentProvider to request columns w/o the need to know real column names and
	 * create the alias itself.
	 */
	private static HashMap<String, String> buildColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String col : ALL_COLS) {
			map.put(col, col);
		}
		// Aliases
		addColumnMapAlias(map, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, VeloColumns.COL_ID);
		addColumnMapAlias(map, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, VeloColumns.COL_ID);

		addColumnMapAlias(map, VeloColumns.ALIAS_COL_DISPO_CYCLE_PARKING,
				String.format("%s || '#' || %s", VeloColumns.COL_STATION_CYCLE, VeloColumns.COL_STATION_PARKING));
		addColumnMapAlias(map, VeloColumns.ALIAS_COL_LAT_LNG_E6,
				String.format("%s || '#' || %s", VeloColumns.COL_LATITUDE_E6, VeloColumns.COL_LONGITUDE_E6));
		
		
		return map;
	}

	private static void addColumnMapAlias(HashMap<String, String> map, String alias, String column) {
		map.put(alias, String.format("%s AS %s", column, alias));

	}

	public void close() {
		maBaseSQLite.close();
	}

	// public SQLiteDatabase open() {
	// return maBaseSQLite.getWritableDatabase();
	// }

	// public void close(SQLiteDatabase bdd) {
	// bdd.close();
	// }

	private SQLiteDatabase getWritableDatabase() {
		return maBaseSQLite.getWritableDatabase();
	}

	public SQLiteDatabase getReadableDatabase() {
		return maBaseSQLite.getReadableDatabase();
	}

	public long insertStation(Station geoPoint) {
		ContentValues values = convertAsContentValues(geoPoint);
		long rowId =  getWritableDatabase().insert(TABLE_VELIB, null, values);
		if (rowId>-1) {
			geoPoint.setId((int)rowId);
		}
		return rowId;
	}

	public SQLiteDatabase beginTransaction() {
		SQLiteDatabase bdd = maBaseSQLite.getWritableDatabase();
		bdd.beginTransaction();
		return bdd;
	}

	public void commit(SQLiteDatabase bdd) {
		bdd.setTransactionSuccessful();
		bdd.endTransaction();
	}

	public void endTransaction(SQLiteDatabase bdd) {
		bdd.endTransaction();
	}

	public void updateStationnFavorite(SQLiteDatabase bdd, Station station) {
		ContentValues values = new ContentValues();
		values.put(VeloColumns.COL_FAVORY, station.isFavory());
		values.put(VeloColumns.COL_FAVORY_TYPE, station.getFavoriteTypeId());
		values.put(VeloColumns.COL_ALIAS_NAME, station.getNameAlias());
		// Do Updated
		String whereClause = new StringBuilder(32).append(VeloColumns.COL_ID).append('=').append(station.getId()).toString();
		bdd.update(TABLE_VELIB, values, whereClause, null);
	}

	public void updateStationnDispo(SQLiteDatabase bdd, Station station) {
		ContentValues values = new ContentValues();
		values.put(VeloColumns.COL_STATION_TOTAL, station.getVeloTotal());
		values.put(VeloColumns.COL_STATION_CYCLE, station.getStationCycle());
		values.put(VeloColumns.COL_STATION_PARKING, station.getStationParking());
		values.put(VeloColumns.COL_STATION_TICKET, station.getVeloTicket());
		values.put(VeloColumns.COL_STATION_UPDATE_TIME, station.getVeloUpdated());
		// Do Updated
		String whereClause = new StringBuilder(32).append(VeloColumns.COL_ID).append('=').append(station.getId()).toString();
		bdd.update(TABLE_VELIB, values, whereClause, null);
	}

	private ContentValues convertAsContentValues(Station point) {
		ContentValues values = new ContentValues();
		values.put(VeloColumns.COL_PROVIDER, point.getProvider());
		values.put(VeloColumns.COL_NUMBER, point.getNumber());
		values.put(VeloColumns.COL_NAME, point.getName());
		values.put(VeloColumns.COL_ADDRESS, point.getAddress());
		values.put(VeloColumns.COL_FULLADDRESS, point.getFullAddress());
		values.put(VeloColumns.COL_LATITUDE_E6, point.getLatitudeE6());
		values.put(VeloColumns.COL_LONGITUDE_E6, point.getLongitudeE6());
		values.put(VeloColumns.COL_OPEN, point.getOpen());
		values.put(VeloColumns.COL_BONUS, point.getBonus());
		values.put(VeloColumns.COL_FAVORY, point.isFavory());
		values.put(VeloColumns.COL_FAVORY_TYPE, point.getFavoriteTypeId());
		values.put(VeloColumns.COL_ALIAS_NAME, point.getNameAlias());
		return values;
	}

	public int removeStationWithID(int id) {
		SQLiteDatabase bdd = beginTransaction();
		String whereClause = new StringBuilder(32).append(VeloColumns.COL_ID).append('=').append(id).toString();
		int delCount = bdd.delete(TABLE_VELIB, whereClause, null);
		commit(bdd);
		bdd.close();
		return delCount;
	}

	public int removeAllStationsByProvider(SQLiteDatabase bdd, VelibProvider velibProvider) {
		String whereClause = String.format("%s=%d", VeloColumns.COL_PROVIDER, velibProvider.getProvider());
		int delCount = bdd.delete(TABLE_VELIB, whereClause, null);
		return delCount;
	}

	public ArrayList<Station> getStationsByProvider(VelibProvider velibProvider) {
		SQLiteDatabase bdd = maBaseSQLite.getReadableDatabase();
		final String whereClause = VeloColumns.COL_PROVIDER + "=" + velibProvider.getProvider(); // String.format("%=%1$d",
		// COL_PROVIDER
		// ,provider);
		Cursor c = bdd.query(TABLE_VELIB, ALL_COLS, whereClause, null, null, null, null);
		ArrayList<Station> allStations = cursorToLivre(c);
		bdd.close();
		return allStations;
	}

	public Cursor getSearchStationMatches(String query, String[] columns) {
		// TODO String selection = KEY_WORD + " MATCH ?";
		String selection = VeloColumns.COL_NAME + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };

		return query(selection, selectionArgs, columns, null);
	}

	private Cursor query(String selection, String[] selectionArgs, String[] columns, String sortOrder) {
		/*
		 * The SQLiteBuilder provides a map for all possible columns requested to actual columns in the database, creating a simple column alias mechanism by
		 * which the ContentProvider does not need to know the real column names
		 */
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_VELIB);
		// builder.setProjectionMap(mColumnMap);
		SQLiteDatabase bdd = maBaseSQLite.getReadableDatabase();
		Cursor cursor = builder.query(bdd, columns, selection, selectionArgs, null, null, sortOrder);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	private ArrayList<Station> cursorToLivre(Cursor c) {
		ArrayList<Station> points = null;
		if (c.getCount() == 0) {
			return points;
		} else {
			points = new ArrayList<Station>(c.getCount());
		}
		while (c.moveToNext()) {
			Station point = new Station();
			point.setId(c.getInt(VeloColumns.NUM_COL_ID));
			point.setProvider(c.getInt(VeloColumns.NUM_COL_PROVIDER));
			point.setNumber(c.getString(VeloColumns.NUM_COL_NUMBER));
			point.setName(c.getString(VeloColumns.NUM_COL_NAME));
			point.setAddress(c.getString(VeloColumns.NUM_COL_ADDRESS));
			point.setLatitudeE6(c.getInt(VeloColumns.NUM_COL_LATITUDE_E6));
			point.setLongitudeE6(c.getInt(VeloColumns.NUM_COL_LONGITUDE_E6));
			point.setFullAddress(c.getString(VeloColumns.NUM_COL_FULLADDRESS));
			point.setOpen(c.getInt(VeloColumns.NUM_COL_OPEN) > 0 ? true : false);
			point.setBonus(c.getInt(VeloColumns.NUM_COL_BONUS) > 0 ? true : false);
			point.setFavory(c.getInt(VeloColumns.NUM_COL_FAVORY) > 0 ? true : false);
			point.setFavoriteType(c.getString(VeloColumns.NUM_COL_FAVORY_TYPE));
			point.setNameAlias(c.getString(VeloColumns.NUM_COL_ALIAS_NAME));

			point.setVeloTotal(c.getInt(VeloColumns.NUM_COL_STATION_TOTAL));
			point.setStationCycle(c.getInt(VeloColumns.NUM_COL_STATION_CYCLE));
			point.setStationParking(c.getInt(VeloColumns.NUM_COL_STATION_PARKING));
			point.setVeloTicket(c.getInt(VeloColumns.NUM_COL_STATION_TICKET));
			point.setVeloUpdated(c.getLong(VeloColumns.NUM_COL_STATION_UPDATE_TIME));

			points.add(point);
		}
		// On ferme le cursor
		c.close();
		// On retourne le livre
		return points;
	}

}
