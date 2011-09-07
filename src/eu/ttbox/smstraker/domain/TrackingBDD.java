package eu.ttbox.smstraker.domain;

import java.util.ArrayList;
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
		// On créer la BDD et sa table
		maBaseSQLite = new TrackingBaseSQLite(context, NOM_BDD, null, VERSION_BDD);
	}

	public void open() {
		// on ouvre la BDD en écriture
		bdd = maBaseSQLite.getWritableDatabase();
	}

	public void close() {
		// on ferme l'accès à la BDD
		bdd.close();
	}

	public SQLiteDatabase getBDD() {
		return bdd;
	}

	public long insertTrackPoint(TrackPoint geoPoint) {
		// Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = convertAsContentValues(geoPoint);
		// on insère l'objet dans la BDD via le ContentValues
		return bdd.insert(TABLE_TRACK_POINT, null, values);
	}

	private ContentValues convertAsContentValues(TrackPoint point) {
		// on lui ajoute une valeur associé à une clé (qui est le nom de la
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
		// La mise à jour d'un livre dans la BDD fonctionne plus ou moins comme
		// une insertion
		// il faut simple préciser quelle livre on doit mettre à jour grâce à
		// l'ID
		ContentValues values = convertAsContentValues(point);
		return bdd.update(TABLE_TRACK_POINT, values, COL_ID + " = " + id, null);
	}

	public int removeTrakPointWithID(int id) {
		// Suppression d'un livre de la BDD grâce à l'ID
		return bdd.delete(TABLE_TRACK_POINT, COL_ID + " = " + id, null);
	}

	public List<TrackPoint> getTrakPointWithTitre(String userId) {
		// Récupère dans un Cursor les valeur correspondant à un livre contenu
		// dans la BDD (ici on sélectionne le livre grâce à son titre)
		// String whereClause =COL_USERID + " = \"" + userId + "\"";
//		String whereClause = String.format("%s=\"%s\"", COL_USERID, userId);
//		Cursor c = bdd.query(TABLE_TRACK_POINT, ALL_COLS,whereClause, null , null, null, COL_TIME);
		Cursor c = bdd.query(TABLE_TRACK_POINT, ALL_COLS, COL_USERID + " = ?", new String[] {userId} , null, null, COL_TIME);
//		Cursor c = bdd.query(TABLE_TRACK_POINT, ALL_COLS, null, null, null, null, COL_TIME);
		return cursorToLivre(c);
	}

	// Cette méthode permet de convertir un cursor en un livre
	private List<TrackPoint> cursorToLivre(Cursor c) {
		// si aucun élément n'a été retourné dans la requête, on renvoie null
		List<TrackPoint> points = new ArrayList<TrackPoint>(c.getCount());
		if (c.getCount() == 0)
			return points;

		// Sinon on se place sur le premier élément
		// c.moveToFirst();
		while (c.moveToNext()) { 
			// On créé un livre
			TrackPoint point = new TrackPoint();
			// on lui affecte toutes les infos grâce aux infos contenues dans le
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
