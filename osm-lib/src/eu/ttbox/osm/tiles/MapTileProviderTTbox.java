package eu.ttbox.osm.tiles;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileDownloaderTTbox;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import eu.ttbox.osm.tiles.chains.MapTileProviderArrayTTbox;

public class MapTileProviderTTbox extends MapTileProviderArrayTTbox implements IMapTileProviderCallback, ComponentCallbacks2 {

    private static final String TAG = "MapTileProviderTTbox";

	// private static final Logger logger = LoggerFactory.getLogger(MapTileProviderTtbox.class);

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext, int cacheSizeInBytes) {
        this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE,   cacheSizeInBytes);
    }

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext, final ITileSource pTileSource, int cacheSizeInBytes) {
        this(pContext, new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
                pTileSource,   cacheSizeInBytes);
     
    }

    @Override
    public void detach() { 
        super.detach();
    }
    
    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext, final IRegisterReceiver pRegisterReceiver,
            final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource, int cacheSizeInBytes) {
        super(pTileSource, pRegisterReceiver,   cacheSizeInBytes);

        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProvider  fileSystemProvider = new MapTileFilesystemProvider (
                pRegisterReceiver, pTileSource, OpenStreetMapTileProviderConstants.ONE_WEEK * 26);
        mTileProviderList.add(fileSystemProvider);

//        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
//                pRegisterReceiver, pTileSource);
//        mTileProviderList.add(archiveProvider);

        final MapTileDownloaderTTbox downloaderProvider = new MapTileDownloaderTTbox(pTileSource, tileWriter,
                aNetworkAvailablityCheck);
        mTileProviderList.add(downloaderProvider);
        // Memory Management
        pContext.registerComponentCallbacks(this);
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) { 
	}

	@Override
	public void onLowMemory() { 
	}

	@Override
	public void onTrimMemory(int level) {  
		if (level >= TRIM_MEMORY_MODERATE) { // 60
			Log.i(TAG, "###############################################");
			Log.i(TAG, "### Clear all cache on TrimMemory Event " + level);
			Log.i(TAG, "###############################################");
			cache.evictAll();
		} else 	if (level >= TRIM_MEMORY_BACKGROUND) { // 40
			Log.i(TAG, "###############################################");
			Log.i(TAG, "### Clear 1/2 cache on TrimMemory Event " + level);
			Log.i(TAG, "###############################################");
			cache.trimToSize(cache.size() / 2);
		}

	}

}
