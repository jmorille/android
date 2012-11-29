package eu.ttbox.osm.tiles.core;

import java.io.File;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;

public class MapTileFilesystemProviderTTbox  extends MapTileFileStorageProviderBase {

    private static final String TAG = "MapTileFilesystemProviderTTbox";

    
    // ===========================================================
    // Constants
    // ===========================================================

    private static final Logger logger = LoggerFactory.getLogger(TAG);

    // ===========================================================
    // Fields
    // ===========================================================

    private final long mMaximumCachedFileAge;

    private ITileSource mTileSource;

   

    // ===========================================================
    // Service
    // ===========================================================

    private INetworkAvailablityCheck networkAvailablityCheck;
    private TileLoader tileLoader;
    
    
    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileFilesystemProviderTTbox(final IRegisterReceiver pRegisterReceiver, INetworkAvailablityCheck aNetworkAvailablityCheck) {
        this(pRegisterReceiver, TileSourceFactory.DEFAULT_TILE_SOURCE,   aNetworkAvailablityCheck);
    }

    public MapTileFilesystemProviderTTbox(final IRegisterReceiver pRegisterReceiver,
            final ITileSource aTileSource, INetworkAvailablityCheck aNetworkAvailablityCheck) {
        this(pRegisterReceiver, aTileSource, DEFAULT_MAXIMUM_CACHED_FILE_AGE,   aNetworkAvailablityCheck);
    }

    /**
     * Provides a file system based cache tile provider. Other providers can register and store data
     * in the cache.
     *
     * @param pRegisterReceiver
     */
    public MapTileFilesystemProviderTTbox(final IRegisterReceiver pRegisterReceiver,
            final ITileSource pTileSource, final long pMaximumCachedFileAge, INetworkAvailablityCheck aNetworkAvailablityCheck) {
        super(pRegisterReceiver, NUMBER_OF_TILE_FILESYSTEM_THREADS,
                TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);
        mTileSource = pTileSource; 
        mMaximumCachedFileAge = pMaximumCachedFileAge;
        this.networkAvailablityCheck = aNetworkAvailablityCheck;
        this.tileLoader =  new TileLoader();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================


    @Override
    public void detach() { 
        super.detach();
    }
    
    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    protected String getName() {
        return "File System Cache Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "filesystem";
    }

    @Override
    protected Runnable getTileLoader() {
        return  this.tileLoader ; 
    };

    @Override
    public int getMinimumZoomLevel() {
        return mTileSource != null ? mTileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL;
    }

    @Override
    public int getMaximumZoomLevel() {
        return mTileSource != null ? mTileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL;
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource = pTileSource;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private class TileLoader extends MapTileModuleProviderBase.TileLoader {

    
        @Override
        public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {

            if (mTileSource == null) {
                return null;
            }

            final MapTile tile = pState.getMapTile();

            // if there's no sdcard then don't do anything
            if (!getSdCardAvailable()) {
                if (DEBUGMODE) {
                    logger.debug("No sdcard - do nothing for tile: " + tile);
                }
                return null;
            }

            // Check the tile source to see if its file is available and if so, then render the
            // drawable and return the tile
            final File file = new File(TILE_PATH_BASE,
                    mTileSource.getTileRelativeFilenameString(tile) + TILE_PATH_EXTENSION);
            if (file.exists()) {

                try {
                    final Drawable drawable = mTileSource.getDrawable(file.getPath());
//                    Log.d(TAG, "mTileSource getDrawable : " + mTileSource.getClass());
                    // Check to see if file has expired
                    final long now = System.currentTimeMillis();
                    final long lastModified = file.lastModified();
                    boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

                    if (fileExpired) {
                    	fileExpired = !networkAvailablityCheck.getWiFiNetworkAvailable();
                    }
                    if (fileExpired) {
                        if (DEBUGMODE) {
                            logger.debug("Tile expired: " + tile);
                        }
                        drawable.setState(new int[] {ExpirableBitmapDrawable.EXPIRED });
                    }

                    return drawable;
                } catch (final LowMemoryException e) {
                    // low memory so empty the queue
                    logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
                    throw new CantContinueException(e);
                }
            }

            // If we get here then there is no file in the file cache
            return null;
        }
    }

}
