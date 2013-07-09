package eu.ttbox.osm.tiles.chains;

import org.osmdroid.tileprovider.MapTile;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class TilesLruCacheTTbox extends LruCache<MapTile, Drawable>{

	private static final String TAG = "TilesLruCacheTTbox";

//	private static final  int ONE_TILE_SIZE_BYTE = 256*256;

//	public TilesLruCacheTTbox() {
//		this(90); 
//	}
	
	public TilesLruCacheTTbox(int maxSize) {
		super(maxSize); 
		Log.i(TAG, "Init Cache Size " + maxSize + " Bytes");

	}

	@Override
	protected int sizeOf(MapTile key , Drawable value) {
		//int valueSize =  value.getRowBytes() * value.getHeight();
        int valueSize =  value.getIntrinsicWidth()  * value.getIntrinsicHeight() * 4;
		Log.d(TAG, "Cache Bitmap Size " + valueSize + " Bytes");
		
		return valueSize;
	}
	

	public void ensureCapacity(final int aCapacity) {
//		Log.w(TAG, "Ignore Tile cache increased from " + maxSize() + " to " + aCapacity);
		//		if (aCapacity > mCapacity) {
//			Log.i("Tile cache increased from " + mCapacity + " to " + aCapacity);
//			mCapacity = aCapacity;
//		}
	}
}
