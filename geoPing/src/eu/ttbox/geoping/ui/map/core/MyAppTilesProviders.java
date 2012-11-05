package eu.ttbox.geoping.ui.map.core;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.tileprovider.util.CloudmadeUtil;

import android.content.Context;

/**
 * @see http://code.google.com/p/osmdroid/issues/detail?id=135
 * 
 *      Own tile @see
 *      http://stackoverflow.com/questions/8136775/how-can-i-implement
 *      -offline-maps-using-osmdroid-by-saving-map-tiles-images-into DB Tile
 *      Provider @see DatabaseFileArchive
 * 
 * @see http://wiki.openstreetmap.org/wiki/SVG
 * @see http://wiki.openstreetmap.org/wiki/Osmarender/SVG
 * 
 *      Tiles Chages @see http://wiki.openstreetmap.org/wiki/API_v0.5#
 *      Getting_list_of_changed_tiles Disk Usage @see
 *      http://wiki.openstreetmap.org/wiki/Tile_Disk_Usage
 * 
 *      Somes Tiles @see http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 * @author jmorille
 * 
 */
public class MyAppTilesProviders {

    public static final OnlineTileSourceBase MAPQUESTOSM = new XYTileSource( //
            "MapquestOSM", ResourceProxy.string.mapquest_osm, 0, 18, 256, ".png", //
            "http://otile1.mqcdn.com/tiles/1.0.0/osm/", //
            "http://otile2.mqcdn.com/tiles/1.0.0/osm/", //
            "http://otile3.mqcdn.com/tiles/1.0.0/osm/", //
            "http://otile4.mqcdn.com/tiles/1.0.0/osm/");

    public static final OnlineTileSourceBase PISTEMAP = new XYTileSource( //
            "OpenPisteMap", ResourceProxy.string.cyclemap, 0, 17, 256, ".png", //
            "http://tiles.openpistemap.org/contours-only", //
            "http://tiles2.openpistemap.org/landshaded//");

    public static void initTilesSource(Context context) {
        // Remove Tiles
        TileSourceFactory.getTileSources().remove(TileSourceFactory.TOPO);
        TileSourceFactory.getTileSources().remove(TileSourceFactory.MAPQUESTAERIAL);
        TileSourceFactory.getTileSources().remove(TileSourceFactory.BASE);
        TileSourceFactory.getTileSources().remove(TileSourceFactory.HILLS);
        // Add Licence Tiles
        // ------------------
        // only do static initialisation if needed
        // http://developers.cloudmade.com/projects/web-maps-api/examples
        if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
            CloudmadeUtil.retrieveCloudmadeKey(context);
        }
        if (BingMapTileSource.getBingKey().length() == 0) {
            BingMapTileSource.retrieveBingKey(context);
        }
        final BingMapTileSource bmts = new BingMapTileSource(null);
        if (!TileSourceFactory.containsTileSource(bmts.name())) {
            TileSourceFactory.addTileSource(bmts);
        }
        // Add Other Tiles
        // TileSourceFactory.addTileSource(CLOUDMADE_VECTOR_TILES);
        // TileSourceFactory.addTileSource(PISTEMAP);

    }

}
