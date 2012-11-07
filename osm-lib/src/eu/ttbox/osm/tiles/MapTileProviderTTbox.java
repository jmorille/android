package eu.ttbox.osm.tiles;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;

public class MapTileProviderTTbox extends MapTileProviderArray implements IMapTileProviderCallback {

    // private static final Logger logger = LoggerFactory.getLogger(MapTileProviderTtbox.class);

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
    public void detach() { 
        super.detach();
    }
    
    /**
     * Creates a {@link MapTileProviderTTbox}.
     */
    public MapTileProviderTTbox(final IRegisterReceiver pRegisterReceiver,
            final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource) {
        super(pTileSource, pRegisterReceiver);

        final TileWriter tileWriter = new TileWriter();

        final MapTileFilesystemProvider  fileSystemProvider = new MapTileFilesystemProvider (
                pRegisterReceiver, pTileSource, OpenStreetMapTileProviderConstants.ONE_WEEK * 26);
        mTileProviderList.add(fileSystemProvider);

//        final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
//                pRegisterReceiver, pTileSource);
//        mTileProviderList.add(archiveProvider);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(pTileSource, tileWriter,
                aNetworkAvailablityCheck);
        mTileProviderList.add(downloaderProvider);
    }

}
