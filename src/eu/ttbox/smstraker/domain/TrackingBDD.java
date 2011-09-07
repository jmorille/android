package eu.ttbox.smstraker.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TrackingBDD {

	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "smstracking.db";

	public static final String TABLE_TRACK_POINT = "table_track_point";

	public static final String COL_ID = "ID";
	private static final int NUM_COL_ID = 0;

	public static final String COL_USERID = "USERID";
	private static final int NUM_COL_USERID = 1;

	public static final String COL_TIME = "TIME";
	private static final int NUM_COL_TIME = 2;

	public static final String COL_PROVIDER = "PROVIDER";
	private static final int NUM_COL_PROVIDER = 3;

	public static final String COL_LATITUDE = "LAT";
	private static final int NUM_COL_LATITUDE = 4;

	public static final String COL_LONGITUDE = "LNG";
	private static final int NUM_COL_LONGITUDE = 5;

	public static final String COL_ACCURACY = "ACCURACY";
	private static final int NUM_COL_ACCURACY = 6;

	public static final String COL_ALTITUDE = "ALT";
	private static final int NUM_COL_ALTITUDE = 7;

	public static final String COL_BEARING = "BEARING";
	private static final int NUM_COL_BEARING = 8;

	public static final String COL_SPEED = "SPEED";
	private static final int NUM_COL_SPEED = 9;

	private static final String[] ALL_COLS = new String[] { COL_ID, COL_USERID, COL_TIME, COL_PROVIDER, COL_LATITUDE, COL_LONGITUDE, COL_ACCURACY, COL_ALTITUDE, COL_BEARING, COL_SPEED };

	private SQLiteDatabase bdd;

	private TrackingBaseSQLite maBaseSQLite;

	public TrackingBDD(Context context) {
		// On cr�er la BDD et sa table
		maBaseSQLite = new TrackingBaseSQLite(context, NOM_BDD, null, VERSION_BDD);
	}

	public void open() {
		// on ouvre la BDD en �criture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	public void close() {
		// on ferme l'acc�s � la BDD
		bdd.close();
	}

	public SQLiteDatabase getBDD() {
		return bdd;
	}

	public long insertTrackPoint(TrackPoint geoPoint) {
		// Cr�ation d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = convertAsContentValues(geoPoint);
		// on ins�re l'objet dans la BDD via le ContentValues
		return bdd.insert(TABLE_TRACK_POINT, null, values);
	}

	private ContentValues convertAsContentValues(TrackPoint point) {
		// on lui ajoute une valeur associ� � une cl� (qui est le nom de la
		// colonne dans laquelle on veut mettre la valeur)
		ContentValues values = new ContentValues();
		values.put(COL_TIME, point.getTime());
		values.put(COL_USERID, point.getUserId());
		values.put(COL_PROVIDER, point.getProvider());
		values.put(COL_LATITUDE, point.getLatitude());
		values.put(COL_LONGITUDE, point.getLongitude());
		values.put(COL_ACCURACY, point.getAccuracy());
		values.put(COL_ALTITUDE, point.getAltitude());
		values.put(COL_BEARING, point.getBearing());
		values.put(COL_SPEED, point.getSpeed());
		return values;
	}

	public int updateTrackPoint(int id, TrackPoint point) {
		// La mise � jour d'un livre dans la BDD fonctionne plus ou moins comme
		// une insertion
		// il faut simple pr�ciser quelle livre on doit mettre � jour gr�ce �
		// l'ID
		ContentValues values = convertAsContentValues(point);
		return bdd.update(TABLE_TRACK_POINT, values, COL_ID + " = " + id, null);
	}

	public int removeTrakPointWithID(int id) {
		// Suppression d'un livre de la BDD gr�ce � l'ID
		return bdd.delete(TABLE_TRACK_POINT, COL_ID + " = " + id, null);
	}

	public List<TrackPoint> getTrakPointForToday(String userId ) { 
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		long pointDate = calendar.getTimeInMillis();
		String whereClause =  COL_USERID + " = ? and " + COL_TIME + '>' + pointDate;
		Cursor c = bdd.query(TABLE_TRACK_POINT, ALL_COLS, whereClause, new String[] {userId } , null, null, COL_TIME);
		return cursorToLivre(c);
	}

	public List<TrackPoint> getTrakPointWithTitre(String userId) {
		Cursor c = bdd.query(TABLE_TRACK_POINT, ALL_COLS, COL_USERID + " = ?", new String[] {userId} , null, null, COL_TIME);
		return cursorToLivre(c);
	}

	// Cette m�thode permet de convertir un cursor en un livre
	private List<TrackPoint> cursorToLivre(Cursor c) {
		// si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
		List<TrackPoint> points = new ArrayList<TrackPoint>(c.getCount());
		if (c.getCount() == 0)
			return points;

		// Sinon on se place sur le premier �l�ment
		// c.moveToFirst();
		while (c.moveToNext()) { 
			// On cr�� un livre
			TrackPoint point = new TrackPoint();
			// on lui affecte toutes les infos gr�ce aux infos contenues dans le
			// Cursor
			point.setId(c.getInt(NUM_COL_ID));
			point.setUserId(c.getString(NUM_COL_USERID));
			point.setTime(c.getLong(NUM_COL_TIME));
			point.setProvider(c.getString(NUM_COL_PROVIDER));
			point.setLatitude(c.getDouble(NUM_COL_LATITUDE));
			point.setLongitude(c.getDouble(NUM_COL_LONGITUDE));
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
