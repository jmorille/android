package eu.ttbox.osm.tiles.chains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ActivityManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MapTileProviderArrayTTbox extends MapTileProviderBase {

	private static final String TAG = "MapTileProviderArrayTTbox";

	private final ConcurrentHashMap<MapTile, MapTileRequestState> mWorking;

	private static final Logger logger = LoggerFactory.getLogger(TAG);

	protected final List<MapTileModuleProviderBase> mTileProviderList;

	public final TilesLruCacheTTbox cache;

	// ===========================================================
	// Cache Helper
	// ===========================================================

	public static int getCacheSizeSuggested(ActivityManager activityManager) {
		int memoryClassBytes = activityManager.getMemoryClass() * 1024 * 1024;
		return memoryClassBytes / 8;
	}

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * Creates an {@link MapTileProviderArray} with no tile providers.
	 * 
	 * @param aRegisterReceiver
	 *            a {@link IRegisterReceiver}
	 */
	protected MapTileProviderArrayTTbox(final ITileSource pTileSource, final IRegisterReceiver pRegisterReceiver, int cacheSizeInBytes) {
		this(pTileSource, pRegisterReceiver, new MapTileModuleProviderBase[0], cacheSizeInBytes);
	}

	/**
	 * Creates an {@link MapTileProviderArray} with the specified tile
	 * providers.
	 * 
	 * @param aRegisterReceiver
	 *            a {@link IRegisterReceiver}
	 * @param tileProviderArray
	 *            an array of {@link MapTileModuleProviderBase}
	 */
	public MapTileProviderArrayTTbox(final ITileSource pTileSource, final IRegisterReceiver aRegisterReceiver, final MapTileModuleProviderBase[] pTileProviderArray, int cacheSizeInBytes) {
		super(pTileSource);

		mWorking = new ConcurrentHashMap<MapTile, MapTileRequestState>();
		cache = new TilesLruCacheTTbox(cacheSizeInBytes);

		mTileProviderList = new ArrayList<MapTileModuleProviderBase>();
		Collections.addAll(mTileProviderList, pTileProviderArray);
	}

	// ===========================================================
	// List Provider
	// ===========================================================

	@Override
	public void detach() {
		synchronized (mTileProviderList) {
			for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
				tileProvider.detach();
			}
		}
	}

	@Override
	public Drawable getMapTile(final MapTile pTile) {
		final Drawable tile = mTileCache.getMapTile(pTile);
		if (tile != null && !ExpirableBitmapDrawable.isDrawableExpired(tile)) {
			if (DEBUGMODE) {
				logger.debug("MapTileCache succeeded for: " + pTile);
			}
			return tile;
		} else {
			boolean alreadyInProgress = false;
			// synchronized (mWorking) {
			alreadyInProgress = mWorking.containsKey(pTile);
			// }

			if (!alreadyInProgress) {
				if (DEBUGMODE) {
					logger.debug("Cache failed, trying from async providers: " + pTile);
				}

				final MapTileRequestState state;
				// synchronized (mTileProviderList) {
				final MapTileModuleProviderBase[] providerArray = new MapTileModuleProviderBase[mTileProviderList.size()];
				state = new MapTileRequestState(pTile, mTileProviderList.toArray(providerArray), this);
				// }

				// synchronized (mWorking) {
				// Check again
				alreadyInProgress = mWorking.containsKey(pTile);
				if (alreadyInProgress) {
					return null;
				}

				mWorking.put(pTile, state);
				// }

				final MapTileModuleProviderBase provider = findNextAppropriateProvider(state);
				if (provider != null) {
					provider.loadMapTileAsync(state);
				} else {
					mapTileRequestFailed(state);
				}
			}
			return tile;
		}
	}

	@Override
	public void mapTileRequestCompleted(final MapTileRequestState pState, final Drawable pDrawable) {
		final MapTile tile = pState.getMapTile();
		// synchronized (mWorking) {
		mWorking.remove(tile);
		// }
		if (pDrawable != null) {
			mTileCache.putTile(tile, pDrawable);
		}

		// tell our caller we've finished and it should update its view
		if (mTileRequestCompleteHandler != null) {
			mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_SUCCESS_ID);
		}
		// NOT ??
		super.mapTileRequestCompleted(pState, pDrawable);
	}

	@Override
	public void mapTileRequestFailed(final MapTileRequestState pState) {
		final MapTileModuleProviderBase nextProvider = findNextAppropriateProvider(pState);
		if (nextProvider != null) {
			nextProvider.loadMapTileAsync(pState);
		} else {
			final MapTile tile = pState.getMapTile();
			// synchronized (mWorking) {
			mWorking.remove(tile);
			// }

			if (mTileRequestCompleteHandler != null) {
				mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_FAIL_ID);
			}

			if (DEBUGMODE) {
				logger.debug("MapTile request failed: " + tile);
			}
			super.mapTileRequestFailed(pState);
		}
	}

	/**
	 * We want to not use a provider that doesn't exist anymore in the chain,
	 * and we want to not use a provider that requires a data connection when
	 * one is not available.
	 */
	protected MapTileModuleProviderBase findNextAppropriateProvider(final MapTileRequestState aState) {
		MapTileModuleProviderBase provider = null;
		// The logic of the while statement is
		// "Keep looping until you get null, or a provider that still exists and has a data connection if it needs one,"
		do {
			provider = aState.getNextProvider();
		} while ((provider != null) && (!getProviderExists(provider) || (!useDataConnection() && provider.getUsesDataConnection())));
		return provider;
	}

	public boolean getProviderExists(final MapTileModuleProviderBase provider) {
		synchronized (mTileProviderList) {
			return mTileProviderList.contains(provider);
		}
	}

	@Override
	public int getMinimumZoomLevel() {
		int result = MAXIMUM_ZOOMLEVEL;
		synchronized (mTileProviderList) {
			for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
				if (tileProvider.getMinimumZoomLevel() < result) {
					result = tileProvider.getMinimumZoomLevel();
				}
			}
		}
		return result;
	}

	@Override
	public int getMaximumZoomLevel() {
		int result = MINIMUM_ZOOMLEVEL;
		synchronized (mTileProviderList) {
			for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
				if (tileProvider.getMaximumZoomLevel() > result) {
					result = tileProvider.getMaximumZoomLevel();
				}
			}
		}
		return result;
	}

	@Override
	public void setTileSource(final ITileSource aTileSource) {
		super.setTileSource(aTileSource);
		for (final MapTileModuleProviderBase tileProvider : mTileProviderList) {
			tileProvider.setTileSource(aTileSource);
		}
		clearTileCache();

	}

	// ===========================================================
	// Cache
	// ===========================================================

	@Override
	public void ensureCapacity(final int pCapacity) {
		cache.ensureCapacity(pCapacity);
		super.ensureCapacity(pCapacity);
	}

	@Override
	public void clearTileCache() {
		Log.i(TAG, "Clean All Tiles Cache");
		cache.evictAll();
		Log.i(TAG, "Clean All Tiles Cache : " + cache.size());
		super.clearTileCache();
	}

}
