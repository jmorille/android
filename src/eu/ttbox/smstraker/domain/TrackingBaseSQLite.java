package eu.ttbox.smstraker.domain;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class TrackingBaseSQLite extends SQLiteOpenHelper {

	private static final String CREATE_BDD = new StringBuffer("CREATE TABLE ").append(TrackingBDD.TABLE_TRACK_POINT).append(" (")//
			.append(TrackingBDD.COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")//
			.append(TrackingBDD.COL_USERID).append(" TEXT NOT NULL, ")// /
			.append(TrackingBDD.COL_PROVIDER).append(" TEXT NOT NULL, ")// /
			.append(TrackingBDD.COL_TIME).append(" INTEGER NOT NULL, ")// /
			.append(TrackingBDD.COL_LATITUDE).append(" REAL NOT NULL, ")// /
			.append(TrackingBDD.COL_LONGITUDE).append(" REAL NOT NULL, ")// /
			.append(TrackingBDD.COL_ACCURACY).append(" REAL NOT NULL, ")// /
			.append(TrackingBDD.COL_ALTITUDE).append(" REAL, ")// /
			.append(TrackingBDD.COL_BEARING).append(" REAL, ")// /
			.append(TrackingBDD.COL_SPEED).append(" REAL ")// /
			.append(" );").toString();

	// Index
	private static final String INDEX_TRACK_POINT_AK = "IDX_TRACKPOINT_AK";
	private static final String CREATE_INDEX_AK = "CREATE INDEX " + INDEX_TRACK_POINT_AK + " on " + TrackingBDD.TABLE_TRACK_POINT + "(" //
			+ TrackingBDD.COL_USERID + ", " //
			+ TrackingBDD.COL_TIME //
			+ ");";

	public TrackingBaseSQLite(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD);
//		db.execSQL(CREATE_INDEX_AK); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// On peut fait ce qu'on veut ici moi j'ai décidé de supprimer la table
		// et de la recréer
		// comme ça lorsque je change la version les id repartent de 0
		db.execSQL("DROP TABLE " + TrackingBDD.TABLE_TRACK_POINT + ";");
//		db.execSQL("DROP INDEX " + INDEX_TRACK_POINT_AK + ";");
		
		onCreate(db);
	}

}
