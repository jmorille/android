package eu.ttbox.velib.map.osm;

import java.io.InputStream;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.CloudmadeTileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;
import eu.ttbox.velib.core.svg.SVG;
import eu.ttbox.velib.core.svg.SVGParser;
 

public class CloudmadeTileSourceVector extends CloudmadeTileSource {

    private static final String TAG = "CloudmadeTileSourceVector";
    private static final Logger logger = LoggerFactory.getLogger(CloudmadeTileSourceVector.class);

    public CloudmadeTileSourceVector(String pName, string pResourceId, int pZoomMinLevel, int pZoomMaxLevel, int pTileSizePixels, String pImageFilenameEnding, String... pBaseUrl) {
        super(pName, pResourceId, pZoomMinLevel, pZoomMaxLevel, pTileSizePixels, pImageFilenameEnding, pBaseUrl);
    }
    
 /**
  *  http://alpha.vectors.cloudmade.com/bf761f5e98524bf88ac465bce90002b6/1/256/17/66392/45057.svg?token=df9257032e5b4eddb1d128c8f8b2b68a

  */
	@Override
	public String getTileURLString(final MapTile pTile) {
		final String tileUrl = super.getTileURLString(pTile);
		  Log.d(TAG, "Tile URL : " + tileUrl);
		return tileUrl;
	}

	

	@Override
	public String localizedName(final ResourceProxy proxy) {
		return "Vector Tiles";
	}

	
    @Override
    public Drawable getDrawable(final InputStream aFileInputStream) {
        // try {
        // default implementation will load the file as a bitmap and create
        // a BitmapDrawable from it
        Log.d(TAG, "Get Drawable Vector");
        SVG svg = SVGParser.getSVGFromInputStream(aFileInputStream);
        PictureDrawable draw =  svg.createPictureDrawable();
        Log.d(TAG, "Get Drawable Vector : " + draw.getBounds() );
        return draw;
        // final Bitmap bitmap = BitmapFactory.decodeStream(aFileInputStream);
        // if (bitmap != null) {
        // return new ExpirableBitmapDrawable(bitmap);
        // }
        // } catch (final OutOfMemoryError e) {
        // logger.error("OutOfMemoryError loading bitmap");
        // System.gc();
        // throw new LowMemoryException(e);
        // }
        // return null;
    }

}
