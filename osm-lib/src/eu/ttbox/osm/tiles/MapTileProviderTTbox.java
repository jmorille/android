package eu.ttbox.osm.tiles;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileDownloaderTTbox;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import eu.ttbox.osm.tiles.chains.MapTileProviderArrayTTbox;
import eu.ttbox.osm.tiles.core.MapTileFilesystemProviderTTbox;

public class MapTileProviderTTbox extends MapTileProviderArrayTTbox //
        implements IMapTileProviderCallback {

    private static final String TAG = "MapTileProviderTTbox";

    // private static final Logger logger =
    // LoggerFactory.getLogger(MapTileProviderTtbox.class);

    private ComponentCallbacks memoryCleanerCallback2;
    private Context context;

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext, int cacheSizeInBytes) {
        this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE, cacheSizeInBytes);
    }

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext, final ITileSource pTileSource, int cacheSizeInBytes) {
        this(pContext, new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext), pTileSource, cacheSizeInBytes);

    }

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */

    public MapTileProviderTTbox(final Context pContext, final IRegisterReceiver pRegisterReceiver, final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource,
            int cacheSizeInBytes) {
        super(pTileSource, pRegisterReceiver, cacheSizeInBytes);
        this.context = pContext;
        // super(pTileSource, pRegisterReceiver);
        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProviderTTbox fileSystemProvider = new MapTileFilesystemProviderTTbox(pRegisterReceiver, pTileSource, 
        		OpenStreetMapTileProviderConstants.ONE_WEEK , aNetworkAvailablityCheck );
        mTileProviderList.add(fileSystemProvider);

        // final MapTileFileArchiveProvider archiveProvider = new
        // MapTileFileArchiveProvider(
        // pRegisterReceiver, pTileSource);
        // mTileProviderList.add(archiveProvider);

        final MapTileDownloaderTTbox downloaderProvider = new MapTileDownloaderTTbox(pTileSource, tileWriter, aNetworkAvailablityCheck);
        mTileProviderList.add(downloaderProvider);
        // Memory Management
         initMemoryListener(pContext);
    }

    @SuppressLint("NewApi")
    public void initMemoryListener(final Context pContext) { 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            memoryCleanerCallback2 = new MemoryCacheCleanerCallback2();
            pContext.registerComponentCallbacks(memoryCleanerCallback2); 
        } else {
            // FIXME How to register onLowMemory ?
//            memoryCleanerCallback2 = new MemoryCacheCleanerCallback();
//            pContext.registerComponentCallbacks(memoryCleanerCallback2); 
        }
    }

    @SuppressLint("NewApi")
    private void detachMemoryListener() {
        if (memoryCleanerCallback2 != null) {
            context.unregisterComponentCallbacks(memoryCleanerCallback2);
        }
    }

    @Override
    public void detach() {
        super.detach();
        detachMemoryListener(); 
    }

    public class MemoryCacheCleanerCallback implements ComponentCallbacks {

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override
        public void onLowMemory() {
            cacheEvictAll();
        }

    }

    public class MemoryCacheCleanerCallback2 extends MemoryCacheCleanerCallback implements ComponentCallbacks2 {

        @Override
        public void onTrimMemory(int level) {
            if (level >= TRIM_MEMORY_MODERATE) { // 60
                Log.i(TAG, "###############################################");
                Log.i(TAG, "### Clear all cache on TrimMemory Event " + level);
                Log.i(TAG, "###############################################");
                cacheEvictAll();
            } else if (level >= TRIM_MEMORY_BACKGROUND) { // 40
                Log.i(TAG, "###############################################");
                Log.i(TAG, "### Clear 1/2 cache on TrimMemory Event " + level);
                Log.i(TAG, "###############################################");
                cacheTrimToSize(cacheSize() / 2);
            }
        }
    }

    public void cacheEvictAll() {
        cache.evictAll();
    }

    public void cacheTrimToSize(int maxSize) {
        cache.trimToSize(maxSize);
    }

    public int cacheSize() {
        return cache.size();
    }

}
