package org.osmdroid.tileprovider.modules;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

public class MapTileDownloaderDetails extends MapTileModuleProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    private static final String TAG = "MapTileDownloaderDetails";
    private static final Logger logger = LoggerFactory.getLogger(TAG);

    public static final int DEFAULT_NUMBER_OF_TILE_DOWNLOAD_THREADS = 1;
    public static final boolean DEBUGMODE = true;

    // ===========================================================
    // Fields
    // ===========================================================

    private final IFilesystemCache mFilesystemCache;

    private OnlineTileSourceBase mTileSource;

    private final INetworkAvailablityCheck mNetworkAvailablityCheck;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MapTileDownloaderDetails(final ITileSource pTileSource) {
        this(pTileSource, null, null);
    }

    public MapTileDownloaderDetails(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache) {
        this(pTileSource, pFilesystemCache, null);
    }

    public MapTileDownloaderDetails(final ITileSource pTileSource, final IFilesystemCache pFilesystemCache, final INetworkAvailablityCheck pNetworkAvailablityCheck) {
        super(DEFAULT_NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);

        mFilesystemCache = pFilesystemCache;
        mNetworkAvailablityCheck = pNetworkAvailablityCheck;
        setTileSource(pTileSource);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public ITileSource getTileSource() {
        return mTileSource;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean getUsesDataConnection() {
        return true;
    }

    @Override
    protected String getName() {
        return "Online Tile Download Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "downloader";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    };

    @Override
    public int getMinimumZoomLevel() {
        return (mTileSource != null ? mTileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
    }

    @Override
    public int getMaximumZoomLevel() {
        return (mTileSource != null ? mTileSource.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
    }

    @Override
    public void setTileSource(final ITileSource tileSource) {
        // We are only interested in OnlineTileSourceBase tile sources
        if (tileSource instanceof OnlineTileSourceBase) {
            mTileSource = (OnlineTileSourceBase) tileSource;
        } else {
            // Otherwise shut down the tile downloader
            mTileSource = null;
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private HttpParams httpParams;
    private HttpClient httpClient;

    private HttpParams getHttpParams() {
        if (httpParams == null) {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "utf-8");
            this.httpParams = params;
        }
        return httpParams;
    }

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            DefaultHttpClient client = new DefaultHttpClient(getHttpParams());
            this.httpClient = client;
        }
        return httpClient;

    }

    private class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {

            if (mTileSource == null) {
                return null;
            }

            InputStream in = null;
            OutputStream out = null;
            final MapTile tile = aState.getMapTile();

            try {

                if (mNetworkAvailablityCheck != null && !mNetworkAvailablityCheck.getNetworkAvailable()) {
                    if (DEBUGMODE) {
                        logger.debug("Skipping " + getName() + " due to NetworkAvailabliltyCheck.");
                    }
                    return null;
                }

                final String tileURLString = mTileSource.getTileURLString(tile);
                Log.d(TAG, "Starting downloading url " + tileURLString);
                if (DEBUGMODE) {
                    logger.debug("Downloading Maptile from url: " + tileURLString);
                }

                if (TextUtils.isEmpty(tileURLString)) {
                    return null;
                }

                final HttpClient client =  getHttpClient();
                final HttpUriRequest head = new HttpGet(tileURLString);
                final HttpResponse response = client.execute(head);
                // Check to see if we got success
                final org.apache.http.StatusLine line = response.getStatusLine();
                if (line.getStatusCode() != HttpURLConnection.HTTP_OK) {
                    logger.warn("Problem downloading MapTile: " + tile + " HTTP response: " + line);
                    head.abort();
                     return null;
                }
                // "Date", "ETag", "Expires", "X-Cache"
                Header headerDate = response.getFirstHeader("Date");
                // {link
                // http://en.wikipedia.org/wiki/List_of_HTTP_header_fields}
                if (headerDate != null && headerDate.getValue() != null) {
                    // Header: Date => values: Thu, 30 Aug 2012 08:46:40 GMT
                    // 08-30 10:46:39.131: D/o*.o*.t*.m*.MapTileDow*(9237):
                    // Header: Content-Length => values: 15730
                    // 08-30 10:46:39.131: D/o*.o*.t*.m*.MapTileDow*(9237):
                    // Header: Content-Length => values: 15730
                    // 08-30 10:46:39.131: D/o*.o*.t*.m*.MapTileDow*(9237):
                    // Header: X-Cache => values: MISS from
                    // orm.openstreetmap.org
                    // 08-30 10:46:39.131: D/o*.o*.t*.m*.MapTileDow*(9237):
                    // Header: X-Cache-Lookup => values: MISS from
                    // orm.openstreetmap.org:3128
                    // 08-30 10:46:39.131: D/o*.o*.t*.m*.MapTileDow*(9237):
                    // Header: Cache-Control => values: max-age=162683
                }
                // 08-30 10:46:39.131: D/o*.o*.t*.m*.MapTileDow*(9237): Header:
                // ETag => values: "a77abdde936a24d3d4eb9911ac571375"

                // HeaderIterator itHeader = response.headerIterator();
                // while (itHeader.hasNext()) {
                // Header header = itHeader.nextHeader();
                // logger.debug("Header: " +header.getName() +
                // " => values: " + header.getValue());
                // }
                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    head.abort();
                     logger.warn("No content downloading MapTile: " + tile);
                    return null;
                }
                Log.d(TAG, "Entity content encoding : " + entity.getContentEncoding().getValue());
                in = entity.getContent();

                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
                StreamUtils.copy(in, out);
                out.flush();
                final byte[] data = dataStream.toByteArray();
                final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                entity.consumeContent();
               
                // Save the data to the filesystem cache
                if (mFilesystemCache != null) {
                    mFilesystemCache.saveFile(mTileSource, tile, byteStream);
                    byteStream.reset();
                }
                Log.d(TAG, "   Ending downloading url " + tileURLString);
                final Drawable result = mTileSource.getDrawable(byteStream);
                Log.d(TAG, "   Ending Drawable result " + result);
                 return result;
            } catch (final UnknownHostException e) {
                // no network connection so empty the queue
                logger.warn("UnknownHostException downloading MapTile: " + tile + " : " + e);
                throw new CantContinueException(e);
            } catch (final LowMemoryException e) {
                // low memory so empty the queue
                logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
                throw new CantContinueException(e);
            } catch (final FileNotFoundException e) {
                logger.warn("Tile not found: " + tile + " : " + e);
            } catch (final IOException e) {
                logger.warn("IOException downloading MapTile: " + tile + " : " + e);
            } catch (final Throwable e) {
                logger.error("Error downloading MapTile: " + tile, e);
            } finally {
                StreamUtils.closeStream(in);
                StreamUtils.closeStream(out);
            }

            return null;
        }

        @Override
        protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
            removeTileFromQueues(pState.getMapTile());
            // don't return the tile because we'll wait for the fs provider to
            // ask for it
            // this prevent flickering when a load of delayed downloads complete
            // for tiles
            // that we might not even be interested in any more
            pState.getCallback().mapTileRequestCompleted(pState, null);
        }

    }

}
