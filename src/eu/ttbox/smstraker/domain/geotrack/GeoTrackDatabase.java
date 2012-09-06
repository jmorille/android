package eu.ttbox.smstraker.domain.geotrack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import eu.ttbox.smstraker.domain.GeoTrack;

public class GeoTrackDatabase {

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "smstracking.db";

	public static final String TABLE_TRACK_POINT = "table_track_point";

	 public static class GeoTrackColumns {
	     public static final String COL_ID = BaseColumns._ID;
	     public static final String COL_USERID = "USERID";
	     public static final String COL_TIME = "TIME";
	     public static final String COL_PROVIDER = "PROVIDER";
	     public static final String COL_LATITUDE_E6 = "LAT_E6";
	     public static final String COL_LONGITUDE_E6 = "LNG_E6";
	     public static final String COL_ACCURACY = "ACCURACY";
	     public static final String COL_ALTITUDE = "ALT";
	     public static final String COL_BEARING = "BEARING";
	     public static final String COL_SPEED = "SPEED";
	     private static final String[] ALL_COLS = new String[] { COL_ID, COL_USERID, COL_TIME, COL_PROVIDER, COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_ACCURACY, COL_ALTITUDE, COL_BEARING, COL_SPEED };

	 }
	 
	private static final int NUM_COL_ID = 0;
 	private static final int NUM_COL_USERID = 1;
 	private static final int NUM_COL_TIME = 2;
 	private static final int NUM_COL_PROVIDER = 3;
 	private static final int NUM_COL_LATITUDE_E6 = 4;
 	private static final int NUM_COL_LONGITUDE_E6 = 5;
 	private static final int NUM_COL_ACCURACY = 6;
 	private static final int NUM_COL_ALTITUDE = 7;
 	private static final int NUM_COL_BEARING = 8;
 	private static final int NUM_COL_SPEED = 9;


	private SQLiteDatabase bdd;

	private GeoTrackOpenHelper maBaseSQLite;

	public GeoTrackDatabase(Context context) {
		// On cr�er la BDD et sa table
		maBaseSQLite = new GeoTrackOpenHelper(context, NOM_BDD, null, VERSION_BDD);
	}

	@Deprecated
	public void open() {
		// on ouvre la BDD en �criture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	@Deprecated
	public void close() {
		// on ferme l'acc�s � la BDD
		bdd.close();
	}


	@Deprecated
	public SQLiteDatabase getBDD() {
		return bdd;
	}

	
	
	private SQLiteDatabase getWritableDatabase() {
		return maBaseSQLite.getWritableDatabase();
	}

	public SQLiteDatabase getReadableDatabase() {
		return maBaseSQLite.getReadableDatabase();
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
	
	public long insertTrackPoint(GeoTrack geoPoint) {
		// Cr�ation d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = convertAsContentValues(geoPoint);
		// on ins�re l'objet dans la BDD via le ContentValues
		return bdd.insert(TABLE_TRACK_POINT, null, values);
	}

	private ContentValues convertAsContentValues(GeoTrack point) {
		// on lui ajoute une valeur associ� � une cl� (qui est le nom de la
		// colonne dans laquelle on veut mettre la valeur)
		ContentValues values = new ContentValues();
		values.put(GeoTrackColumns.COL_TIME, point.getTime());
		values.put(GeoTrackColumns.COL_USERID, point.getUserId());
		values.put(GeoTrackColumns.COL_PROVIDER, point.getProvider());
		values.put(GeoTrackColumns.COL_LATITUDE_E6, point.getLatitudeE6());
		values.put(GeoTrackColumns.COL_LONGITUDE_E6, point.getLongitudeE6());
		values.put(GeoTrackColumns.COL_ACCURACY, point.getAccuracy());
		values.put(GeoTrackColumns.COL_ALTITUDE, point.getAltitude());
		values.put(GeoTrackColumns.COL_BEARING, point.getBearing());
		values.put(GeoTrackColumns.COL_SPEED, point.getSpeed());
		return values;
	}

	public int updateTrackPoint(int id, GeoTrack point) {
		// La mise � jour d'un livre dans la BDD fonctionne plus ou moins comme
		// une insertion
		// il faut simple pr�ciser quelle livre on doit mettre � jour gr�ce �
		// l'ID
		ContentValues values = convertAsContentValues(point);
		return bdd.update(TABLE_TRACK_POINT, values, GeoTrackColumns.COL_ID + " = " + id, null);
	}

	public int removeTrakPointWithID(int id) {
		// Suppression d'un livre de la BDD gr�ce � l'ID
		return bdd.delete(TABLE_TRACK_POINT, GeoTrackColumns.COL_ID + " = " + id, null);
	}

	public List<GeoTrack> getTrakPointForToday(String userId ) { 
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		long pointDate = calendar.getTimeInMillis();
		String whereClause =  GeoTrackColumns.COL_USERID + " = ? and " + GeoTrackColumns.COL_TIME + '>' + pointDate;
		Cursor c = bdd.query(TABLE_TRACK_POINT, GeoTrackColumns.ALL_COLS, whereClause, new String[] {userId } , null, null, GeoTrackColumns.COL_TIME);
		return cursorToLivre(c);
	}

	public List<GeoTrack> getTrakPointWithTitre(String userId) {
		Cursor c = bdd.query(TABLE_TRACK_POINT, GeoTrackColumns.ALL_COLS, GeoTrackColumns.COL_USERID + " = ?", new String[] {userId} , null, null, GeoTrackColumns.COL_TIME);
		return cursorToLivre(c);
	}

	// Cette m�thode permet de convertir un cursor en un livre
	private List<GeoTrack> cursorToLivre(Cursor c) {
		// si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
		List<GeoTrack> points = new ArrayList<GeoTrack>(c.getCount());
		if (c.getCount() == 0)
			return points;

		// Sinon on se place sur le premier �l�ment
		// c.moveToFirst();
		while (c.moveToNext()) { 
			// On cr�� un livre
			GeoTrack point = new GeoTrack();
			// on lui affecte toutes les infos gr�ce aux infos contenues dans le
			// Cursor
			point.setId(c.getInt(NUM_COL_ID));
			point.setUserId(c.getString(NUM_COL_USERID));
			point.setTime(c.getLong(NUM_COL_TIME));
			point.setProvider(c.getString(NUM_COL_PROVIDER));
			point.setLatitudeE6(c.getInt(NUM_COL_LATITUDE_E6));
			point.setLongitudeE6(c.getInt(NUM_COL_LONGITUDE_E6));
			point.setAccuracy(c.getFloat(NUM_COL_ACCURACY));
			point.setAltitude(c.getDouble(NUM_COL_ALTITUDE));
			point.setBearing(c.getFloat(NUM_COL_BEARING));
			point.setSpeed(c.getFloat(NUM_COL_SPEED));
			points.add(point);
		}

		// On ferme le cursor
		c.close();

		// On retourne le livre
		return points;
	}

}
