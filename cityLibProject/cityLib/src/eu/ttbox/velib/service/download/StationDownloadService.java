package eu.ttbox.velib.service.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.impl.cookie.DateUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
/**
 * <a heref="http://developer.android.com/reference/java/net/HttpURLConnection.html">HttpURLConnection</a>
 * 
 */
public class StationDownloadService {

	private static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    private static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

    private final String TAG = "StationDownloadService";

	private Context context;
	
	private ConnectivityManager connectivityManager;

	public StationDownloadService(Context context) {
		super();
		this.context = context;
		this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public ArrayList<Station> donwloadStationsByProvider(VelibProvider velibProvider) {
		NetworkInfo activeNW = connectivityManager.getActiveNetworkInfo();
		if (!activeNW.isConnectedOrConnecting()) {
			return null;
		}
		// Log.i(TAG, String.format( "Starting downloading stations for Provider %s" , velibProvider));
		String urlString = velibProvider.getUrlCarto();
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, String.format("Starting downloading stations from url [%s]", urlString));
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(AppConstants.CONNECTION_TIMEOUT);
			urlConnection.setConnectTimeout(AppConstants.CONNECTION_TIMEOUT);
			urlConnection.addRequestProperty(HTTP_HEADER_ACCEPT_ENCODING, "gzip" );
 			try {
				// if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, String.format( "HttpURLConnection for url %s" , url ));
//				urlConnection.connect();
				int responseCode = urlConnection.getResponseCode();
//				if (Log.isLoggable(TAG, Log.DEBUG)) {
//					Log.d(TAG, String.format("Connect to url %s : %s", url, responseCode));
//					Map<String,List<String>> headers = urlConnection.getHeaderFields();
//					if(headers!=null) {
//						for (String key : headers.keySet()) {
//							List<String> header = headers.get(key);
//							Log.d(TAG, String.format("Http Response header %s : %s", key, header));
//						}
//					}
//				}
				ArrayList<Station> stations = null;
				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream content = new BufferedInputStream(urlConnection.getInputStream(), 10240);
					String contentEncoding = urlConnection.getHeaderField(HTTP_HEADER_CONTENT_ENCODING);
					if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
//						Log.i(TAG, String.format("Http Response header Content-Encoding : %s",  contentEncoding));
						content = new GZIPInputStream(content, 1024);
					}
					VeloServiceParser veloServiceParser = velibProvider.getVeloServiceParser();
					stations = veloServiceParser.parseInputStreamForStations(content, velibProvider);
					content.close();
				} else {
					Log.e(TAG, String.format("Could not connect to url %s : %s", url, urlConnection.getResponseCode()));
					// Toast.makeText(context, "Coult not connect to carto url "
					// +execute.getStatusLine() , Toast.LENGTH_SHORT).show();
				}
				return stations;
			} finally {
				urlConnection.disconnect();
			}
		} catch (IOException e) {
			Log.w(TAG, String.format("Could not connect to url %s with error : %s", urlString, e.getMessage()));
			throw new RuntimeException(String.format("Could not connect to url %s : %s", urlString, e.getMessage()), e);
		}
		// return null;

	}

	public int updateStationDispo(VelibProvider velibProvider, ArrayList<Station> stations) {
		NetworkInfo activeNW = connectivityManager.getActiveNetworkInfo();
		if (!activeNW.isConnectedOrConnecting()) {
			return 0;
		}
 		ArrayList<Station> updatedStations = donwloadStationsByProvider(velibProvider);
		HashMap<Integer, Station> dispoStations = new HashMap<Integer, Station>(stations.size());
		for (Station station : updatedStations) {
			Integer id = Integer.valueOf(station.getId());
			dispoStations.put(id, station);
		}
		// Update List
		int updatedCount = 0;
		for (Station station : stations) {
			Integer id = Integer.valueOf(station.getId());
			Station dispoStation = dispoStations.get(id);
			if (dispoStation != null) {
				station.setVeloTotal(dispoStation.getVeloTotal());
				station.setStationCycle(dispoStation.getStationCycle());
				station.setStationParking(dispoStation.getStationParking());
				station.setVeloTicket(dispoStation.getVeloTicket());
				station.setVeloUpdated(dispoStation.getVeloUpdated());
				updatedCount++;
			}
		}
		return updatedCount;
	}
   
	private void logResponseHeader(HttpURLConnection urlConnection, String urlString ) {
 		if (Log.isLoggable(TAG, Log.DEBUG)) { 
			Map<String,List<String>> headers = urlConnection.getHeaderFields();
			if(headers!=null) {
				for (String key : headers.keySet()) {
					List<String> header = headers.get(key);  
					Log.d(TAG, String.format("Http download Url :\t %s \t [%s = %s]",   urlString,key, header));
				}
			}
		}
	}
	
	public int downloadStationDispo(Station station, long checkDeltaInMs) {
		int result = 0;
		try {
			long now = System.currentTimeMillis();
			if ((station.getVeloUpdated() + checkDeltaInMs) < now) {
				VelibProvider velibProvider = station.getVeloProvider();
				if (velibProvider.isUnLockServiceDownloadTime()) {
					String urlString = velibProvider.getUrlDipso(station.getNumber());
					try {
						// Open Connection
						URL url = new URL(urlString);
						HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
						// Define Header
						urlConnection.setRequestMethod("GET");
						urlConnection.setReadTimeout(AppConstants.CONNECTION_TIMEOUT);
						urlConnection.setConnectTimeout(AppConstants.CONNECTION_TIMEOUT);
						urlConnection.addRequestProperty(HTTP_HEADER_ACCEPT_ENCODING, "gzip" );
						String lastModified = DateUtils.formatDate(station.getVeloUpdatedDate() , "EEE, d MMM yyyy HH:mm:ss 'GMT'");
						urlConnection.addRequestProperty("If-Modified-Since", lastModified); // Mon, 02 Jul 2010 08:24:42 GMT
						Log.i(TAG, String.format("Http connect  Url :\t %s \t with If-Modified-Since [%s]", urlString, lastModified ));
						try {
//							urlConnection.connect();
							int responseCode = urlConnection.getResponseCode(); 
							if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) { 
								Log.i(TAG, String.format("Http response Url :\t %s \t Response Code HTTP_NOT_MODIFIED", urlString )); 
 								// Set Update Time
								station.setVeloUpdated(System.currentTimeMillis());
							} else  if (responseCode == HttpURLConnection.HTTP_OK) {
								logResponseHeader(urlConnection, urlString);
								String contentEncoding = urlConnection.getHeaderField(HTTP_HEADER_CONTENT_ENCODING);
								InputStream content = new BufferedInputStream(urlConnection.getInputStream(), 1024);
								if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
//									Log.d(TAG, String.format("Http Response header Content-Encoding : %s",  contentEncoding));
									content = new GZIPInputStream(content, 1024);
								}
								try {
									VeloServiceParser veloServiceParser = velibProvider.getVeloServiceParser();
									Station dispo = veloServiceParser.parseInputStreamForStationDispo(content, station);
									if (dispo != null) {
										result = 1;
									}
								} finally {
									content.close();
								}
								if (Log.isLoggable(TAG, Log.INFO)) {
									long end = System.currentTimeMillis(); 
									Log.i(TAG, String.format("Http download Url :\t %s \t Dowloaded Content-Encoding=%s in %s ms.",   urlString, contentEncoding, (end - now)));
								}

							} else {
								Log.e(TAG, "--------------------------------------------------");
								Log.e(TAG, String.format("--- %s : for %s", responseCode, url));
								Log.e(TAG, "--------------------------------------------------");
								if (responseCode == 403) {
									velibProvider.lockServiceDownloadTime(now);
								}
							}
						} finally {
							urlConnection.disconnect();
						}

					} catch (Exception e) {
						Log.w(TAG, String.format("Could not connect to url :\t %s \t error message %s ", urlString, e.getMessage()));
						// throw new RuntimeException("Could not connect to url " +
						// url
						// + " : " + e.getMessage(), e);
					}
				} else {
					// Log.w(TAG, "Download service lock for Velib Provider + " + velibProvider);
				}
			}
		} finally {
			station.setAskRefreshDispo(false);

		}
		return result;
	}

}
