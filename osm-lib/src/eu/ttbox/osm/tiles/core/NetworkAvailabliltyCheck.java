package eu.ttbox.osm.tiles.core;

import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkAvailabliltyCheck implements INetworkAvailablityCheck   {

	private final ConnectivityManager mConnectionManager;

	public NetworkAvailabliltyCheck(final Context aContext) {
		mConnectionManager = (ConnectivityManager) aContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public NetworkInfo getActiveNetworkInfo() {
		final NetworkInfo networkInfo = mConnectionManager.getActiveNetworkInfo();
		return networkInfo;
	}
	
	 
 	public boolean isConnectedOrConnecting() {
		final NetworkInfo networkInfo = mConnectionManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}
	
 	@Override
 	public boolean getNetworkAvailable() {
		final NetworkInfo networkInfo = mConnectionManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	@Override
	public boolean getWiFiNetworkAvailable() {
		final NetworkInfo wifi = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi != null && wifi.isAvailable();
	}

	@Override
	public boolean getCellularDataNetworkAvailable() {
		final NetworkInfo mobile = mConnectionManager.getActiveNetworkInfo();
		return mobile != null && mobile.getType() == ConnectivityManager.TYPE_MOBILE;
	}

	@Override
	public boolean getRouteToPathExists(final int hostAddress) {
		return (mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_WIFI, hostAddress) || mConnectionManager
				.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, hostAddress));
	}
}
