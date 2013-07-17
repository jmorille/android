package eu.ttbox.velib.core;

import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.content.Context;
import android.content.Intent;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.osm.OsmMapTilesDownloadService;
import eu.ttbox.velib.ui.search.SearchableVeloActivity;

public class Intents {

    public static final String ACTION_OSM_MAPTILES_DOWNLOAD = "eu.ttbox.velib.ACTION_OSM_MAPTILES_DOWNLOAD";
    public static final String ACTION_OSM_MAPTILES_DOWNLOAD_STOP = "eu.ttbox.velib.ACTION_OSM_MAPTILES_DOWNLOAD_STOP";

    public static final String EXTRA_TILESOURCE = "EXTRA_TILESOURCE";
    public static final String EXTRA_ZOOM_MIN = "EXTRA_ZOOM_MIN";
    public static final String EXTRA_ZOOM_MAX = "EXTRA_ZOOM_MAX";
    public static final String EXTRA_BOUNDYBOX = "EXTRA_BOUNDYBOX";

    public static final String EXTRA_VELIB_PROVIDER = "EXTRA_VELIB_PROVIDER";

    public static Intent downloadMapTiles(Context context, ITileSource tileSource, int minZoom, int maxZoom, double[] boundyBox) {
        Intent intent = new Intent(context, OsmMapTilesDownloadService.class) //
                .setAction(ACTION_OSM_MAPTILES_DOWNLOAD);//
        intent.putExtra(EXTRA_ZOOM_MIN, minZoom).putExtra(EXTRA_ZOOM_MAX, maxZoom);
        intent.putExtra(EXTRA_BOUNDYBOX, boundyBox);
        if (tileSource != null) {
            intent.putExtra(EXTRA_TILESOURCE, tileSource.name());
        }
        return intent;
    }

    public static Intent downloadMapTilesStop(Context context) {
        return new Intent(context, OsmMapTilesDownloadService.class) //
                .setAction(ACTION_OSM_MAPTILES_DOWNLOAD_STOP);
    }
    
    
    public static Intent searchVelo(Context context, VelibProvider velibProvider) {
        Intent intent =   new Intent(context, SearchableVeloActivity.class) //
         .setAction(SearchableVeloActivity.ACTION_VIEW_FAVORITE); //
        if (velibProvider!=null) {
            intent.putExtra(EXTRA_VELIB_PROVIDER, velibProvider.ordinal());
        }
        return intent;
    }
}
