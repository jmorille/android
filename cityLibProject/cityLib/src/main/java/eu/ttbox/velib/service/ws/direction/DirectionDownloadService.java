package eu.ttbox.velib.service.ws.direction;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirection;
import eu.ttbox.velib.service.ws.direction.parser.GoogleDirectionParser;

/**
 * Api {@link https://developers.google.com/maps/documentation/directions/?hl=fr-FR}
 * 
 * sample @see http://stackoverflow.com/questions/5608601/android-google-maps-and-drawing-route
 *  
 * 
 */
public class DirectionDownloadService {

	private static final String TAG = DirectionDownloadService.class.getSimpleName();

	private static final String URL_PATTERN = "http://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&sensor=false&units=%s";
	private static final String LATLNG_PATTER = "%s,%s";

	private Context context;

	public DirectionDownloadService(Context context) {
		super();
		this.context = context;
	}

	public GoogleDirection getDirection(double originLat, double originLng, double destLat, double destLng) {
		// Url
		String origin = String.format(LATLNG_PATTER, originLat, originLng);
		String dest = String.format(LATLNG_PATTER, destLat, destLng);
		String urlString = String.format(URL_PATTERN, origin, dest, GoogleUnitSystemsEnum.metric.name());
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, String.format("Starting downloading Google destination from url [%s]", urlString));
		}
		// Do Download
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET"); 
			urlConnection.setReadTimeout(AppConstants.CONNECTION_TIMEOUT);
			urlConnection.setConnectTimeout(AppConstants.CONNECTION_TIMEOUT);
			urlConnection.addRequestProperty("Accept-Encoding", "gzip" );
			try {
				// if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, String.format( "HttpURLConnection for url %s" , url ));
				urlConnection.connect();
				int responseCode = urlConnection.getResponseCode();
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, String.format("Connect to url %s : %s", url, responseCode));
					Map<String,List<String>> headers = urlConnection.getHeaderFields();
					if(headers!=null) {
						for (String key : headers.keySet()) {
							List<String> header = headers.get(key);
							Log.d(TAG, String.format("Http Response header %s : %s", key, header));
						}
					}
				}
				 
				GoogleDirection directions = null;
				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream content = new BufferedInputStream(urlConnection.getInputStream(), 1024);
					String contentEncoding = urlConnection.getHeaderField("Content-Encoding");
					if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) { 
						content = new GZIPInputStream(content, 1024);
					}
					
					GoogleDirectionParser httpWsParser = new GoogleDirectionParser();
					directions = httpWsParser.parseInputStream(content);
					content.close();
				} else {
					Log.e(TAG, String.format("Could not connect to url %s : %s", url, urlConnection.getResponseCode()));
					// Toast.makeText(context, "Coult not connect to carto url "
					// +execute.getStatusLine() , Toast.LENGTH_SHORT).show();
				}
				return directions;
			} finally {
				urlConnection.disconnect();
			}
		} catch (IOException e) {
			Log.e(TAG, String.format("Could not connect to url %s with error : %s", urlString, e.getMessage()));
			throw new RuntimeException(String.format("Could not connect to url %s : %s", urlString, e.getMessage()), e);
		} catch (JSONException e) {
			Log.e(TAG, String.format("Could not parse url %s with error : %s", urlString, e.getMessage()));
			throw new RuntimeException(String.format("Could not parse url %s : %s", urlString, e.getMessage()), e);
		}
		// return null;

	}
}
