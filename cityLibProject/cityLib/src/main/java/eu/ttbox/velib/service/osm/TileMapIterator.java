package eu.ttbox.velib.service.osm;

import java.util.Iterator;

import org.osmdroid.mtp.adt.OSMTileInfo;
import org.osmdroid.mtp.util.Util;
import org.osmdroid.tileprovider.MapTile;

import android.util.Log;

public class TileMapIterator implements Iterator<MapTile> {

	private static final String TAG = "TileMapIterator";

	// Instance
	private int zoomMin;
	private int zoomMax;
	private double latNorth;
	private double lngWest;
	private double latSouth;
	private double lngEast;

	// Start Iteration
	int xMin;
	int yMin;

	// current
	int x;
	int y;
	int zoom;

	// End Iteration
	int xMax;
	int yMax;

	// compute
	private int tilesErrorCount = 0;
	private int tilesTotalCount = 0;
	private int tilesConsumeCount = 0;

	public TileMapIterator(int zoomMin, int zoomMax, double[] boundyBox) {
		super();
		this.zoomMin = zoomMin;
		this.zoomMax = zoomMax;
		this.latNorth = boundyBox[0]; // Math.max(boundyBox[0], boundyBox[2]);
		this.lngWest = boundyBox[1]; // Math.max(boundyBox[1], boundyBox[3]);
		this.latSouth = boundyBox[2]; // Math.min(boundyBox[0], boundyBox[2]);
		this.lngEast = boundyBox[3]; // Math.min(boundyBox[1], boundyBox[3]);
		// init
		init();
	}

	private void init() {
		// Define Starting zoom
		this.zoom = zoomMin;
		// Compute other Limit
		initTileLimitForZoomLevel(this.zoom);
		// Start
		this.x = xMin;
		this.y = yMin;
		// Compute expected
		this.tilesConsumeCount = 0;
		this.tilesErrorCount = 0;
		this.tilesTotalCount = computeTilesCount();
	}

	
	
	public int getTilesErrorCount() {
		return tilesErrorCount;
	}

	public void addTilesErrorCount() {
		this.tilesErrorCount++;
	}

	private void initTileLimitForZoomLevel(int zoom) {
		final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(latNorth, lngWest, zoom);
		final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(latSouth, lngEast, zoom);
		Log.i(TAG, String.format("Zoom Level %s ==> Range %s  //  %s", zoom, upperLeft, lowerRight));
		// Begin
		this.xMin = Math.min(upperLeft.x, lowerRight.x); // upperLeft.x
		this.yMin = Math.min(upperLeft.y, lowerRight.y); // upperLeft.y;
		// End
		this.xMax = Math.max(upperLeft.x, lowerRight.x); // lowerRight.x;
		this.yMax = Math.max(upperLeft.y, lowerRight.y); // lowerRight.y;
		// Init Current
		this.x = xMin;
		this.y = yMin;
		this.zoom = zoom;
	}

	public int computeTilesCount() {
		/* Calculate file-count. */
		int fileCnt = 0;
		for (int z = zoomMin; z <= zoomMax; z++) {
			final OSMTileInfo upperLeft = Util.getMapTileFromCoordinates(latNorth, lngWest, z);
			final OSMTileInfo lowerRight = Util.getMapTileFromCoordinates(latSouth, lngEast, z);
			// Value
			int xMin = Math.min(upperLeft.x, lowerRight.x); // upperLeft.x
			int yMin = Math.min(upperLeft.y, lowerRight.y); // upperLeft.y;
			int xMax = Math.max(upperLeft.x, lowerRight.x); // lowerRight.x;
			int yMax = Math.max(upperLeft.y, lowerRight.y); // lowerRight.y;
			// Delta
			final int dx = xMax - xMin + 1;
			final int dy = yMax - yMin + 1;
			// Original
			// final int dx = lowerRight.x - upperLeft.x + 1;
			// final int dy = lowerRight.y - upperLeft.y + 1;
			fileCnt += dx * dy;
		}
		return fileCnt;
	}

	private boolean isInRangeForX() {
		return x <= xMax && x >= xMin;
	}

	private boolean isInRangeForY() {
		return y <= yMax && y >= yMin;
	}

	private boolean isInRangeForZoom() {
		return zoom <= zoomMax && zoom >= zoomMin;
	}

	@Override
	public boolean hasNext() {
		if (zoom <= zoomMax && x <= xMax && y <= yMax) {
			return true;
		}
		Log.w(TAG, "No hasNext for /" + this.toString());
		return false;
	}

	@Override
	public MapTile next() {
		MapTile next = null;
		if (hasNext()) {
			tilesConsumeCount++;
			next = createCurrent();
		} 
		// Increment Current
		 incrementTilesZoomXY() ;
		return next;
	}
	
	private void incrementTilesZoomXY() {
		y++;
		if (!isInRangeForY()) {
			x++;
			y = yMin;
			// Log.i(TAG, "increment x /" + this.toString() );
			if (!isInRangeForX()) {
				zoom++;
				// Need to recompute the Min-Max Borne
				initTileLimitForZoomLevel(zoom);
			}
		}
	}
	

	public MapTile createCurrent() {
		return new MapTile(zoom, x, y);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	

	public int getTilesTotalCount() {
        return tilesTotalCount;
    }

    public int getTilesConsumeCount() {
        return tilesConsumeCount;
    }

    public int getTilesProgressPercent() {
        int progress = 0;
        if (tilesTotalCount>0) {
            progress = ((tilesConsumeCount*100)/tilesTotalCount);
        }
        return progress;
    }

    @Override
	public String toString() {
		final String SUP_SEP = "<=";
		StringBuilder sb = new StringBuilder();
		sb.append("[Tiles: ").append(tilesConsumeCount).append("/").append(tilesTotalCount).append("]");
		sb.append(" // ");
		sb.append("[Zoom: ").append(zoomMin).append(SUP_SEP).append(zoom).append(SUP_SEP).append(zoomMax)//
				.append(" (").append(isInRangeForZoom()).append(")") //
				.append("]");
		sb.append(" // ");
		sb.append("[X: ").append(xMin).append(SUP_SEP).append(x).append(SUP_SEP).append(xMax) //
				.append(" (").append(isInRangeForX()).append(")") //
				.append("]");
		sb.append(" // ");
		sb.append("[Y: ").append(yMin).append(SUP_SEP).append(y).append(SUP_SEP).append(yMax)//
				.append(" (").append(isInRangeForY()).append(")") //
				.append("]");
		return sb.toString();

		// return "/" + zoom + "/" + x + "/" + y;
	}

}
