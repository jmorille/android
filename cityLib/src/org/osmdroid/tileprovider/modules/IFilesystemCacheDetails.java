package org.osmdroid.tileprovider.modules;

import java.io.InputStream;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

public interface IFilesystemCacheDetails extends IFilesystemCache {

    boolean saveFile(final ITileSource pTileSourceInfo, MapTile pTile, final InputStream pStream);
}
