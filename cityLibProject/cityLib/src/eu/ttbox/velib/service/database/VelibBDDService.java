package eu.ttbox.velib.service.database;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * @see http://developer.android.com/reference/android/app/Service.html
 * 
 */
public class VelibBDDService extends Service {

	private StationDatabase veloDatabase;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public VelibBDDService getService() {
			return VelibBDDService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// init
		veloDatabase = new StationDatabase(this);
	}

	@Override
	public void onDestroy() {
		veloDatabase.close();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public StationDatabase getVeloDatabase() {
		return veloDatabase;
	}

}
