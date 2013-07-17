package eu.ttbox.velib.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import eu.ttbox.velib.VeloContentProvider;
import eu.ttbox.velib.VeloContentProvider.Constants;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.StationDatabase;
import eu.ttbox.velib.service.download.StationDownloadService;
import eu.ttbox.velib.service.geo.GeoUtils;
import eu.ttbox.velib.service.ws.direction.DirectionDownloadService;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirection;

public class VelibService extends Service {

    private final String TAG = getClass().getSimpleName();
    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    // Service
    SharedPreferences prefs;
    StationDatabase velibBDD;
    StationDownloadService downloadService;
    DirectionDownloadService googleDirectionDownloadService;
    Context context;
    // Config
    boolean saveUpdateDispoStationsInDb = true;
    // ConcurrentLinkedQueue ?
    private LinkedBlockingDeque<Station> databaseUpdateQueue = new LinkedBlockingDeque<Station>();
    private LinkedBlockingDeque<Station> downloadUpdateDispoQueue = new LinkedBlockingDeque<Station>();
    private ScheduledThreadPoolExecutor timerDatabase;
    private ScheduledThreadPoolExecutor timerDownload;
    private ArrayList<OnStationDispoUpdated> onStationDispoUpdatedListeners;
    private Callable<Void> consumeDatabaseUpdateQueue = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            if (!databaseUpdateQueue.isEmpty()) {
                SQLiteDatabase bdd = velibBDD.beginTransaction();
                Station station;
                int stationUpdated = 0;
                ContentResolver contentResolver = getContentResolver();
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

    @Override
    public void onCreate() {
        super.onCreate();
        // init
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

    public ArrayList<Station> getStationsByProviderWithCheckUpdateDate(VelibProvider velibProvider) {
        String updateKey = getProviderStationsLastUpdateKey(velibProvider);
        long lastUpdate = prefs.getLong(updateKey, Long.MIN_VALUE);
        long defaultDeltaTimeInMs = AppConstants.ONE_DAY_IN_MS * prefs.getInt(AppConstants.PREFS_KEY_PROVIDER_DELTA_UPDATE_IN_DAY, 100);
        boolean isToOld = System.currentTimeMillis() - lastUpdate > defaultDeltaTimeInMs;
        return   getStationsByProviderWithCheckUpdateDate(velibProvider, isToOld);
    }

    public ArrayList<Station> getStationsByProviderWithCheckUpdateDate(VelibProvider velibProvider, boolean isToOld) {
        ArrayList<Station> dbStations = this.velibBDD.getStationsByProvider(velibProvider);
        if (dbStations == null || dbStations.isEmpty() || isToOld) {
            Log.i(TAG, "--- ------------------------------------------------------------- ---");
            Log.i(TAG, "--- Download Station for Provider " + velibProvider + " (is too old " + isToOld +  ")" ) ;
            Log.i(TAG, "--- ------------------------------------------------------------- ---");
            final ArrayList<Station> downloadStations = downloadService.donwloadStationsByProvider(velibProvider);
            // Read Datas
            HashMap<String, Station> downloadByNumber = convertStationsAsMapByNumber(downloadStations);
            // Prepare Result
            ArrayList<Station> deleteStations = new ArrayList<Station>();
            ArrayList<Station> updateStations = new ArrayList<Station>();
            if (dbStations != null && !dbStations.isEmpty()) {
                for (Station station : dbStations) { 
                    String key = station.getNumber();
                    Station ref = downloadByNumber.remove(key);
                    if (ref == null) {
                        // Show by deleted
                        deleteStations.add(station);
                    } else if (!isSameStationIdentifier(station, ref)) {
                        station.setNumber(ref.getNumber());
                        station.setName(ref.getName());
                        station.setAddress(ref.getAddress());
                        station.setFullAddress(ref.getFullAddress());
                        station.setLatitudeE6(ref.getLatitudeE6());
                        station.setLongitudeE6(ref.getLongitudeE6());
                        station.setOpen(ref.getOpen());
                        station.setBonus(ref.getBonus());
                        // Ref For update
                        updateStations.add(station);
                    }
                }
                // Update Stations
                if (!updateStations.isEmpty()) {
                    DatabaseUpdtateIdentifierAsync updtateIdentifierAsync = new DatabaseUpdtateIdentifierAsync(velibProvider);
                    updtateIdentifierAsync.execute(updateStations);
                }
                // Delete Stations
                if (!deleteStations.isEmpty()) {
                    DatabaseDeleteStationAsync deleteStationAsync = new DatabaseDeleteStationAsync(velibProvider);
                    deleteStationAsync.execute(deleteStations);
                    dbStations.removeAll(deleteStations);
                 }
                Log.i(TAG, "--- Update Stations : " + updateStations.size() ) ;
                Log.i(TAG, "--- Delete Stations : " + deleteStations.size() ) ;
            }
            // Insert new Stations
            if (dbStations==null) {
                dbStations = new ArrayList<Station>();
            }
            if (!downloadByNumber.isEmpty()) {
                Collection<Station> insertStations = downloadByNumber.values();
                dbStations.addAll(insertStations);
                // Save It in DB
                DatabaseInsertAsync databaseInsertAsync = new DatabaseInsertAsync(velibProvider);
                databaseInsertAsync.execute(downloadStations);
            }
            Log.i(TAG, "--- Insert Stations : " + downloadByNumber.size() ) ;
            Log.i(TAG, "--- ------------------------------------------------------------- ---");

            // Keep Trace of Date Task
            String updateKey = getProviderStationsLastUpdateKey(velibProvider);
            final SharedPreferences.Editor localEdit = prefs.edit();
            localEdit.putLong(updateKey, System.currentTimeMillis());
            localEdit.commit();
        }
        return dbStations;
    }

    private String getProviderStationsLastUpdateKey(VelibProvider velibProvider) {
        String key = new StringBuilder(AppConstants.PREFS_KEY_PROVIDER_LAST_UPDATE_BASE).append(velibProvider.name()).toString();
        return key;
    }

    private boolean isSameStationIdentifier(Station station, Station ref) {
    return station.getNumber().equals(ref.getNumber()) &&
                station.getName().equals(ref.getName()) && //
                station.getAddress().equals(ref.getAddress()) && //
                station.getFullAddress().equals(ref.getFullAddress()) && //
                station.getLatitudeE6() == ref.getLatitudeE6() &&//
                station.getLongitudeE6() == ref.getLongitudeE6() && //
                station.getOpen() == ref.getOpen() && //
                station.getBonus() == ref.getBonus();
    }

    private HashMap<String, Station> convertStationsAsMapByNumber(List<Station> stations) {
        HashMap<String, Station> result = new HashMap<String, Station>();
        if (stations != null && !stations.isEmpty()) {
            for (Station station : stations) {
                String key = station.getNumber();
                result.put(key, station);
            }
        }

        return result;
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
        ArrayList<Station> allStations = getStationsByProviderWithCheckUpdateDate(velibProvider);
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
                Log.w(TAG, String.format("For VelibProvider %s, with %s stations Not In Box", velibProvider, stationNotInBox.size()));
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

    /**
     * Class for clients to access. Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public VelibService getService() {
            return VelibService.this;
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
            ContentResolver contentResolver = getContentResolver();
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

    private class DatabaseUpdtateIdentifierAsync extends AsyncTask<ArrayList<Station>, Void, Void> {
        VelibProvider velibProvider;

        private DatabaseUpdtateIdentifierAsync(VelibProvider velibProvider) {
            this.velibProvider = velibProvider;
        }

        @Override
        protected Void doInBackground(ArrayList<Station>... updatedStations) {
            ArrayList<Station> stations = updatedStations[0];
            // Persist in Db
            long beginTime = System.currentTimeMillis();
            SQLiteDatabase bdd = velibBDD.beginTransaction();
            ContentResolver contentResolver = getContentResolver();
            for (final Station station : stations) {
                long entityId = velibBDD.updateStationnIdentifier(bdd, station);
                Uri entityUri = VeloContentProvider.Constants.getStationUri(station.getId());
                contentResolver.notifyChange(entityUri, null);
            }
            velibBDD.commit(bdd);
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "----------------------------------------------------------------");
            Log.d(TAG, String.format("Update Identifier in DB %s Stations in %s ms", stations.size(), (endTime - beginTime)));
            Log.d(TAG, "----------------------------------------------------------------");
            return null;
        }
    }

    private class DatabaseDeleteStationAsync extends AsyncTask<ArrayList<Station>, Void, Void> {
        VelibProvider velibProvider;

        private DatabaseDeleteStationAsync(VelibProvider velibProvider) {
            this.velibProvider = velibProvider;
        }

        @Override
        protected Void doInBackground(ArrayList<Station>... updatedStations) {
            ArrayList<Station> stations = updatedStations[0];
            // Persist in Db
            long beginTime = System.currentTimeMillis();
            SQLiteDatabase bdd = velibBDD.beginTransaction();
            ContentResolver contentResolver = getContentResolver();
            for (final Station station : stations) {
                long deleteCount  = velibBDD.deleteByEntityId(bdd, station.getId());
                if (deleteCount>-1) {
                    Uri entityUri = VeloContentProvider.Constants.getStationUri(station.getId());
                    contentResolver.notifyChange(entityUri, null);
                }
            }
            velibBDD.commit(bdd);
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "----------------------------------------------------------------");
            Log.d(TAG, String.format("Update Identifier in DB %s Stations in %s ms", stations.size(), (endTime - beginTime)));
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
}
