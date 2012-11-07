package eu.ttbox.velib.map.osm.tiles;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;
import android.util.Log;

public class MapTileProviderTTbox extends MapTileProviderArray implements IMapTileProviderCallback {

    // private static final Logger logger = LoggerFactory.getLogger(MapTileProviderTtbox.class);

    private static final String TAG = "MapTileProviderTTbox";

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext) {
        this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE);
    }

    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final Context pContext, final ITileSource pTileSource) {
        this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
                pTileSource);
    }

    @Override
    public   void setTileSource(ITileSource tileSource) {
        super.setTileSource(tileSource);
        Log.d(TAG, "setTileSource : " + tileSource);
    }
    
    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final IRegisterReceiver pRegisterReceiver,
            final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource) {
        super(pTileSource, pRegisterReceiver);

        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProviderTTbox fileSystemProvider = new MapTileFilesystemProviderTTbox(
                pRegisterReceiver, pTileSource);
        mTileProviderList.add(fileSystemProvider);

//        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
//                pRegisterReceiver, pTileSource);
//        mTileProviderList.add(archiveProvider);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(pTileSource, tileWriter,
                aNetworkAvailablityCheck);
        mTileProviderList.add(downloaderProvider);
    }

}
