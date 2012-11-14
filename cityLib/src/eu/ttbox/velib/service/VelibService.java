package eu.ttbox.velib.service;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import eu.ttbox.velib.VeloContentProvider.Constants;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.StationDatabase;
import eu.ttbox.velib.service.download.StationDownloadService;
import eu.ttbox.velib.service.geo.GeoUtils;
import eu.ttbox.velib.service.ws.direction.DirectionDownloadService;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirection;

public class VelibService extends Service {

	private final String TAG = getClass().getSimpleName();

	// ConcurrentLinkedQueue ?
	private LinkedBlockingDeque<Station> databaseUpdateQueue = new LinkedBlockingDeque<Station>();
	private LinkedBlockingDeque<Station> downloadUpdateDispoQueue = new LinkedBlockingDeque<Station>();

	private ScheduledThreadPoolExecutor timerDatabase;
	private ScheduledThreadPoolExecutor timerDownload;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public VelibService getService() {
			return VelibService.this;
		}
	}

	StationDatabase velibBDD;

	StationDownloadService downloadService;
	DirectionDownloadService googleDirectionDownloadService;

	Context context;

	// Config
	boolean saveUpdateDispoStationsInDb = true;
	private ArrayList<OnStationDispoUpdated> onStationDispoUpdatedListeners;

	@Override
	public void onCreate() {
		super.onCreate();
		// init
		this.velibBDD = new StationDatabase(getBaseContext());
		this.downloadService = new StationDownloadService(getBaseContext());
		this.googleDirectionDownloadService = new DirectionDownloadService(context);
		this.context = getBaseContext();
		// Download Timer
		timerDownload = new ScheduledThreadPoolExecutor(1);
		// Database Timer
		timerDatabase = new ScheduledThreadPoolExecutor(1);
		// timerDatabase.scheduleAtFixedRate(new ConsumeDatabaseUpdateQueue(), 100, 5000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onDestroy() {
		// Shutdown Databse Timer
		timerDatabase.shutdown();
		try {
			timerDatabase.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log.wtf(TAG, "Could not wait for terminaison of timer Database :" + e.getMessage());
		}
		// CLose Database
		velibBDD.close();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public GoogleDirection getDirections(double originLat, double originLng, double destLat, double destLng) {
		return this.googleDirectionDownloadService.getDirection(originLat, originLng, destLat, destLng);
	}

	public void addOnStationDispoUpdatedListener(OnStationDispoUpdated listener) {
		if (onStationDispoUpdatedListeners == null) {
			onStationDispoUpdatedListeners = new ArrayList<OnStationDispoUpdated>();
		}
		onStationDispoUpdatedListeners.add(listener);
	}

	public void removeOnStationDispoUpdatedListener(OnStationDispoUpdated listener) {
		onStationDispoUpdatedListeners.remove(listener);
	}

	public ArrayList<Station> updateAllStationsByProvider(VelibProvider velibProvider) {
		// Delete
		removeAllStationsByProvider(velibProvider);
		// update
		return getStationsByProvider(velibProvider);
	}

	public int removeAllStationsByProvider(VelibProvider velibProvider) {
		SQLiteDatabase bdd = velibBDD.beginTransaction();
		int deleteCount = velibBDD.removeAllStationsByProvider(bdd, velibProvider);
		Log.i(TAG, String.format("Delete All %s Stations Provider %s", deleteCount, velibProvider));
		velibBDD.commit(bdd);
		bdd.close();
		return deleteCount;
	}

	public ArrayList<Station> getStationsByProvider(final VelibProvider velibProvider) {
		ArrayList<Station> allStations = this.velibBDD.getStationsByProvider(velibProvider);

		// Download Stations
		if (allStations == null || allStations.isEmpty()) {
			final ArrayList<Station> downloadStations = downloadService.donwloadStationsByProvider(velibProvider);
			if (downloadStations != null && !downloadStations.isEmpty()) {
				allStations = downloadStations;
				DatabaseInsertAsync databaseInsertAsync = new DatabaseInsertAsync(velibProvider);
				databaseInsertAsync.execute(downloadStations);
			}
		}
		// Manage Boundy Box
		if (allStations != null && !allStations.isEmpty()) {
			 ArrayList<Station> stationNotInBox = (ArrayList<Station>) GeoUtils.getLatLngE6ProviderNotInBoundyBoxE6(allStations, velibProvider.getExcludeBoundyBoxE6());
			double[] boundyBoxE6 = GeoUtils.getBoundyBoxE6(allStations, velibProvider.getExcludeBoundyBoxE6());
			double[] velibProviderBox = velibProvider.getBoundyBoxE6();
			boolean isRedifineBoundy = GeoUtils.isRedefineBox(velibProviderBox, boundyBoxE6);
//			 boolean isRedifineBoundy = !stationNotInBox.isEmpty();
			// Log.i(TAG, "BoundyBox for VelibProvider " + velibProvider + " is  " + String.format("new double[]{ %sd, %sd, %sd, %sd }",
			// boundyBoxE6[0]/AppConstants.E6, boundyBoxE6[1]/AppConstants.E6, boundyBoxE6[2]/AppConstants.E6, boundyBoxE6[3]/AppConstants.E6 ) );
			if (isRedifineBoundy) {
				// velibProvider.setBoundyBoxE6(boundyBoxE6);
				Log.w(TAG,String.format( "For VelibProvider %s, with %s stations Not In Box", velibProvider, stationNotInBox.size()));
//						"
//				Log.w(TAG,
//						"BoundyBox for VelibProvider "
//								+ velibProvider
//								+ " is  "
//								+ String.format("new double[]{ %sd, %sd, %sd, %sd }", boundyBoxE6[0] / AppConstants.E6, boundyBoxE6[1] / AppConstants.E6,
//										boundyBoxE6[2] / AppConstants.E6, boundyBoxE6[3] / AppConstants.E6));
			}
		}

		// return result
		return allStations;
	}

	public void updateStationnFavorite(Station station) {
		SQLiteDatabase bdd = velibBDD.beginTransaction();
		velibBDD.updateStationnFavorite(bdd, station);
		velibBDD.commit(bdd);
		bdd.close();
	}

	public void updateDispoStationsAsync(final long checkDeltaInMs, final ArrayList<Station> selectedStations, long nowInMs) {
		boolean isAddStation = false;
		for (Station station : selectedStations) {
			// Check updated time
			if ((station.getVeloUpdated() + checkDeltaInMs) < nowInMs) {
				// Check already check
				if (!downloadUpdateDispoQueue.contains(station)) {
					Log.d("download Update offerLast : (%s)", station.getNumber());
					downloadUpdateDispoQueue.offerLast(station);
					isAddStation = true;
				}
			}
		}
		if (isAddStation) {
			timerDownload.submit(new DownloadTaskQueue(checkDeltaInMs));
		}
	}

	public void updateDispoStationsAsyncInPriority(final long checkDeltaInMs, final Station... selectedStations) {
		boolean isAddStation = false;
		for (Station station : selectedStations) {
			downloadUpdateDispoQueue.offerFirst(station);
			isAddStation = true;
		}
		if (isAddStation) {
			timerDownload.submit(new DownloadTaskQueue(checkDeltaInMs));
		}
	}

	private void notifyStationDispoUpdated(Station updatedStation) {
		// Notify other listener
		if (onStationDispoUpdatedListeners != null && !onStationDispoUpdatedListeners.isEmpty() && updatedStation != null) {
			for (OnStationDispoUpdated dispo : onStationDispoUpdatedListeners) {
				dispo.stationDispoUpdated(updatedStation);
			}
		}
	}

	private class DatabaseInsertAsync extends AsyncTask<ArrayList<Station>, Void, Void> {
		VelibProvider velibProvider;

		private DatabaseInsertAsync(VelibProvider velibProvider) {
			this.velibProvider = velibProvider;
		}

		@Override
		protected Void doInBackground(ArrayList<Station>... updatedStations) {
			ArrayList<Station> stations = updatedStations[0];
			// Persist in Db
			long beginTime = System.currentTimeMillis();
			SQLiteDatabase bdd = velibBDD.beginTransaction();
			ContentResolver contentResolver =  getContentResolver();
			for (final Station station : stations) {
				long entityId = velibBDD.insertStation(station);
				Uri entityUri = Uri.withAppendedPath(Constants.CONTENT_URI, String.valueOf(entityId));
				contentResolver.notifyChange(entityUri, null);
			}
			velibBDD.commit(bdd);
			long endTime = System.currentTimeMillis();
			if (Log.isLoggable(TAG, Log.DEBUG))
				Log.d(TAG, "----------------------------------------------------------------");
			if (Log.isLoggable(TAG, Log.DEBUG))
				Log.d(TAG, String.format("Insert in DB %s Stations in %s ms", stations.size(), (endTime - beginTime)));
			if (Log.isLoggable(TAG, Log.DEBUG))
				Log.d(TAG, "----------------------------------------------------------------");
			return null;
		}
	}

	private class DownloadTaskQueue implements Callable<Station> {

		private long checkDeltaInMs;

		public DownloadTaskQueue(long checkDeltaInMs) {
			super();
			this.checkDeltaInMs = checkDeltaInMs;
		}

		@Override
		public Station call() throws Exception {
			if (Log.isLoggable(TAG, Log.DEBUG))
				Log.d(TAG, "DownloadTaskQueue check");
			if (!downloadUpdateDispoQueue.isEmpty()) {
				if (Log.isLoggable(TAG, Log.DEBUG))
					Log.d(TAG, String.format("DownloadTaskQueue with %s stations waiting", downloadUpdateDispoQueue.size()));
				Station station;
				while ((station = downloadUpdateDispoQueue.pollFirst()) != null) {
//				    int stationId = station.getId();
					int updatedStation = downloadService.downloadStationDispo(station, checkDeltaInMs);
					if (updatedStation > 0) {
						// Queue for Save In Db
						databaseUpdateQueue.offerLast(station);
						notifyStationDispoUpdated(station);
					}
				}
				// Ask To Persist
				timerDatabase.submit(consumeDatabaseUpdateQueue);
			}

			return null;
		}
	}

	private Callable<Void>  consumeDatabaseUpdateQueue =new  Callable<Void>() {
		@Override
		public Void call() throws Exception {
			if (!databaseUpdateQueue.isEmpty()) {
				SQLiteDatabase bdd = velibBDD.beginTransaction();
				Station station;
				int stationUpdated = 0;
				ContentResolver contentResolver =  getContentResolver();
				while ((station = databaseUpdateQueue.pollFirst()) != null) {
					velibBDD.updateStationnDispo(bdd, station);
					Uri entityUri = Uri.withAppendedPath(Constants.CONTENT_URI, String.valueOf(station.getId()));
					contentResolver.notifyChange(entityUri, null);
					stationUpdated++;
				}
				velibBDD.commit(bdd);
				bdd.close();
				if (Log.isLoggable(TAG, Log.DEBUG))
					Log.d(TAG, String.format("Database Update %s Stations", stationUpdated));
			}
			return null;
		}
	};
}
