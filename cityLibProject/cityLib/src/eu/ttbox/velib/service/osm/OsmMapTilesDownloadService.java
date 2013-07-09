package eu.ttbox.velib.service.osm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import eu.ttbox.osm.tiles.MapTileProviderTTbox;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.core.Intents;
import eu.ttbox.velib.service.core.WorkerService;


@TargetApi(11)
public class OsmMapTilesDownloadService extends WorkerService implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "OsmDowloaderService";

    // Current Config
    private MapTileProviderTTbox tileProvider;
    private ITileSource tileSource;

    // Context
    private boolean isWifi = false;
    private boolean isTilesMapIteratorActive = false;
    private TileMapIterator tileMapIterator;
    private Notification  notification;
    // Notification
    RemoteViews remoteView;

    // Service
    private SharedPreferences prefs;
    private ConnectivityManager connectivityManager;
    private NotificationManager notificationManager;
    private IntentFilter wifiIntentFilter;
    // Other
    private final IBinder mBinder = new LocalBinder();

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public OsmMapTilesDownloadService() {
        // super();
        super("OsmMapTilesDownloadService");
    }

    /**
     * Listener to get download Event and Schedule Next One
     */
    private Handler tileSuccesHanlder = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            case MapTile.MAPTILE_SUCCESS_ID:
                // Log.i(TAG, "MAPTILE_SUCCESS_ID : " + msg + " // " +
                // tileMapIterator);
                updateNotificationProgress();
                scheduleNext();
                break;
            case MapTile.MAPTILE_FAIL_ID:
                // Log.i(TAG, "MAPTILE_FAIL_ID : " + msg + " // " +
                // tileMapIterator);
                tileMapIterator.addTilesErrorCount();
                updateNotificationProgress();
                scheduleNext();
                break;
            }
        }
    };

    private void scheduleNext() {
        if (isTilesMapIteratorActive && tileMapIterator != null && tileMapIterator.hasNext()) {
            // Log.d(TAG, "MapTiles schedule Next : " + tileMapIterator );
            // Download it
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    MapTile tileMap = tileMapIterator.next();
                    if (tileMap != null) {
                        // OnlineTileSourceBase onlineTileSource =
                        // (OnlineTileSourceBase) tileSource;
                        // Log.d(TAG, "MapTiles Url : " +
                        // onlineTileSource.getTileURLString(tileMap));
                        Drawable drawable = tileProvider.getMapTile(tileMap);
                    }
                }
            });
        } else {
            
            if (tileMapIterator != null && tileSource instanceof OnlineTileSourceBase) {
                OnlineTileSourceBase onlineTileSource = (OnlineTileSourceBase) tileSource;
                Log.w(TAG, "No MapTiles for Url : " + onlineTileSource.getTileURLString(tileMapIterator.createCurrent()));
            } else {
                Log.w(TAG, "MapTiles NOT schedule : tileMapIterator=" + tileMapIterator);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        // Service
        this.connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        // Application Config
        // TODO tileSource
        setTileSource(null);
        // Network Tests
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        this.isWifi = (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI);
        activateTilesDowload(isWifi);
        wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(connectionChangeReceiver, wifiIntentFilter);
        // Notification
        doInForegroundService();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(connectionChangeReceiver);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    /**
     * @see {link
     *      http://developer.android.com/guide/topics/ui/notifiers/notifications
     *      .html#CustomExpandedView}
     * @see {link
     *      http://united-coders.com/nico-heid/show-progressbar-in-notification
     *      -area-like-google-does-when-downloading-from-android/} source code
     *      {link https://github.com/nheid/unitedcoders-android}
     */
    private void doInForegroundService() {
        Log.i(TAG, "############################################");
        Log.i(TAG, "###     In Foreground OrderService      ####");
        Log.i(TAG, "############################################");
        // Notification notification =
        // new
        // Notification.Builder(getBaseContext()).setContentTitle(getText(R.string.app_name))
        // //
        // .build();
        CharSequence tickerText = getText(R.string.notif_download_tiles_status);
        Intent notificationIntent = new Intent(this, VelibMapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Notif view // getApplicationContext().getPackageName()
        this.remoteView = new RemoteViews("eu.ttbox.velib", R.layout.notif_tiles_download_progress);
        remoteView.setImageViewResource(R.id.status_icon, R.drawable.panneau_obligation_cycles);
        remoteView.setTextViewText(R.id.status_text, getText(R.string.notif_download_tiles_status));
        remoteView.setProgressBar(R.id.status_progress, 100, 0, false);
//        remoteView.setOnClickPendingIntent(R.id.status_progress_stop_button, pendingIntent);
        // Notif
        this.notification = new Notification.Builder(getBaseContext()) //
                .setContentTitle(getText(R.string.app_name)) //
                 .setSmallIcon(R.drawable.panneau_obligation_cycles) //
                .setWhen(System.currentTimeMillis()) //
                .setTicker(tickerText) //
                .setContentIntent(pendingIntent) //
                .setContent(remoteView).getNotification();
        // notification.setLatestEventInfo(this,
        // getText(R.string.notification_title),
        // getText(R.string.notification_message), pendingIntent);
        startForeground(R.id.osm_download_service_notification_started, notification);

    }

    private void updateNotificationProgress() {
        if (remoteView == null) {
            return;
        }
        boolean isProgressUpdated = true;
        // Log.d(TAG, "update Notification Progress with " + tileMapIterator);
        if (tileMapIterator != null) {
            if (tileMapIterator.getTilesConsumeCount() == tileMapIterator.getTilesTotalCount()) {
                remoteView.setProgressBar(R.id.status_progress, tileMapIterator.getTilesTotalCount(), tileMapIterator.getTilesConsumeCount(), false);
                String status = getString(R.string.notif_download_tiles_status_end, tileMapIterator.getTilesConsumeCount(), tileMapIterator.getTilesErrorCount());
                remoteView.setTextViewText(R.id.status_text, status);
                // close notif
                stopForeground(false);
            } else if (tileMapIterator.getTilesConsumeCount() % 100 == 0) {
                remoteView.setProgressBar(R.id.status_progress, tileMapIterator.getTilesTotalCount(), tileMapIterator.getTilesConsumeCount(), false);
                String status = getString(R.string.notif_download_tiles_status_progress, tileMapIterator.getTilesConsumeCount(), tileMapIterator.getTilesTotalCount(),
                        tileMapIterator.getTilesProgressPercent(), tileMapIterator.getTilesErrorCount());
                remoteView.setTextViewText(R.id.status_text, status);
            } else {
                isProgressUpdated = false;
            }
        } else {
            remoteView.setProgressBar(R.id.status_progress, 100, 0, true);
        }
        if (isProgressUpdated) {
            notificationManager.notify(R.id.osm_download_service_notification_started, notification);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void desactivateTilesDowload() {
        Log.d(TAG, "MapTiles desactivate Download : on previous status " + isTilesMapIteratorActive);
        isTilesMapIteratorActive = false;
        // TODO stopForeground(true);
    }

    private void activateTilesDowload(boolean isActif) {
        if (isActif) {
            Log.d(TAG, "MapTiles activate Download : on previous status " + isTilesMapIteratorActive);
            // if (!isTilesMapIteratorActive) {
            isTilesMapIteratorActive = true;
            updateNotificationProgress();
            scheduleNext();
            // }
        } else {
            desactivateTilesDowload();
        }
    }

    //

    private void setTileSource(ITileSource tileSource) {
        ITileSource tileSourceUse = tileSource == null ? TileSourceFactory.DEFAULT_TILE_SOURCE : tileSource;
        // Init MapTilesProvider
        int cacheSizeInBytes = 10000;
        MapTileProviderTTbox tileProvider = new MapTileProviderTTbox(this, tileSourceUse, cacheSizeInBytes);
        tileProvider.setUseDataConnection(true);
        tileProvider.setTileRequestCompleteHandler(tileSuccesHanlder);
        this.tileProvider = tileProvider;
        // Set
        this.tileProvider = tileProvider;
        this.tileSource = tileSourceUse;
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public OsmMapTilesDownloadService getService() {
            return OsmMapTilesDownloadService.this;
        }
    }

    private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // start service
                isWifi = true;
                activateTilesDowload(true);
            } else {
                // stop service
                isWifi = false;
                desactivateTilesDowload();
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Service onReceive action : " + action);
        if (Intents.ACTION_OSM_MAPTILES_DOWNLOAD.equals(action)) {
            Bundle extras = intent.getExtras();
            // Tile sources
            ITileSource tileSource = null;
            String tileSourceName = extras.getString(Intents.EXTRA_TILESOURCE);
            if (tileSourceName != null) {
                tileSource = TileSourceFactory.getTileSource(tileSourceName);
            } else {
                tileSource =  TileSourceFactory.DEFAULT_TILE_SOURCE;
            }
            setTileSource(tileSource);
            // Other params
            int zoomMin = extras.getInt(Intents.EXTRA_ZOOM_MIN, tileSource.getMinimumZoomLevel());
            int zoomMax = extras.getInt(Intents.EXTRA_ZOOM_MAX, tileSource.getMaximumZoomLevel());
            double[] boundyBox = extras.getDoubleArray(Intents.EXTRA_BOUNDYBOX);
            Log.w(TAG, String.format("Intent BoundyBox (%s, %s)  (%s, %s)", boundyBox[0], boundyBox[1], boundyBox[2], boundyBox[3]));
            this.tileMapIterator = new TileMapIterator(zoomMin, zoomMax, boundyBox);
            activateTilesDowload(true);
        } else if (Intents.ACTION_OSM_MAPTILES_DOWNLOAD_STOP.equals(action)) {
            desactivateTilesDowload();

        }
    }
}
