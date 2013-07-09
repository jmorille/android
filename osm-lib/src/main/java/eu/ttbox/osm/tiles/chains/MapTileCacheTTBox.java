package eu.ttbox.osm.tiles.chains;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.LRUMapTileCache;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileCache;


public class MapTileCacheTTBox extends MapTileCache {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================


    protected TilesLruCacheTTbox mCachedTiles;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileCacheTTBox() {
        this(CACHE_MAPTILECOUNT_DEFAULT);
    }

    /**
     * @param aMaximumCacheSize
     *            Maximum amount of MapTiles to be hold within.
     */
    public MapTileCacheTTBox(final int aMaximumCacheSize) {
        this.mCachedTiles = new TilesLruCacheTTbox(aMaximumCacheSize);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void ensureCapacity(final int aCapacity) {

            mCachedTiles.ensureCapacity(aCapacity);

    }

    public Drawable getMapTile(final MapTile aTile) {

            return this.mCachedTiles.get(aTile);

    }

    public void putTile(final MapTile aTile, final Drawable aDrawable) {
        if (aDrawable != null) {

                this.mCachedTiles.put(aTile, aDrawable);

        }
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public boolean containsTile(final MapTile aTile) {

          return this.mCachedTiles.get(aTile)!=null;

    }

    public void clear() {

            this.mCachedTiles.evictAll();;

    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
