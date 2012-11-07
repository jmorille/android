package eu.ttbox.velib.map.osm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.CloudmadeTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
     * http://alpha.vectors.cloudmade.com/bf761f5e98524bf88ac465bce90002b6/1/256
     * /17/66392/45057.svg?token=df9257032e5b4eddb1d128c8f8b2b68a
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
    public Drawable getDrawable(String aFilePath) {
        Drawable bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(aFilePath);
            bm = getDrawable(stream);
        } catch (Exception e) {
            /*
             * do nothing. If the exception happened on open, bm will be null.
             */
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // do nothing here
                }
            }
        }
        return bm;
    }

    @Override
    public Drawable getDrawable(final InputStream aFileInputStream) {
        // try {
        // default implementation will load the file as a bitmap and create
        // a BitmapDrawable from it
        Log.d(TAG, "Get Drawable Vector");
        SVG svg = SVGParser.getSVGFromInputStream(aFileInputStream);
       Picture pic = svg.getPicture();
       Bitmap bm = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(), Bitmap.Config.ARGB_8888);
       Canvas c = new Canvas(bm);
       pic.draw(c);
       return new BitmapDrawable(bm);
//       return bm;
       
//        Log.d(TAG, "SVG is create");
//        try {
//            PictureDrawable draw = svg.createPictureDrawable();
//            Log.d(TAG, "Get Drawable Vector : " + draw.getBounds());
//            return draw;
//        } catch (java.lang.UnsupportedOperationException e) {
//            Log.e(TAG, e.getMessage());
//            return null;
//        }
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
