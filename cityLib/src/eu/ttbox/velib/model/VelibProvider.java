package eu.ttbox.velib.model;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import android.graphics.Color;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.geo.GeoPointProvider;
import eu.ttbox.velib.service.download.VeloServiceParser;
import eu.ttbox.velib.service.geo.GeoUtils;
import eu.ttbox.velib.service.provider.VelibServiceProviderAdpater;

/**
 * @see http://fr.wikipedia.org/wiki/Cyclocity
 * @see http://www.jcdecaux.be/fr/corporate/cyclocity-chiffres.cfm
 * @see http://www.jcdecaux.com/fr/Innovation-Design/Cyclocity-R
 * @see http://fr.wikipedia.org/wiki/Liste_des_syst%C3%A8mes_de_v%C3% A9los_en_libre_service_en_France
 * @see http://www.java2s.com/Open-Source/Android/UnTagged/veloid/com.xirgonium.android.manager.htm 
 * 
 */
public enum VelibProvider implements GeoPointProvider {

	// For Test http://%s/service/carto
	FR_AMIENS("FR, Amiens (80) - Velam", 49.9d, 2.3d, //
			"www.velam.amiens.fr", VelibServiceProviderAdpater.CycloCity, Color.GREEN, //
			new String[] { "http://www.velam.amiens.fr/service/carto", "http://www.velam.amiens.fr/service/stationdetails/amiens/%s" }, //
			new double[] { 49.88779293086958d, 2.282665702482666d, 49.90194190292968d, 2.30769408025555d } //
	), //
	FR_BESANCON("FR, Besancon (25) - Velocité", 47.24306d, 6.02194d, //
			"www.velocite.besancon.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "http://www.velocite.besancon.fr/service/carto", "http://www.velocite.besancon.fr/service/stationdetails/besancon/%s" }, //
			new double[] { 47.230795159817774d, 6.00651997961184d, 47.24731887757154d, 6.035338060598549 }//
	), //
	FR_CERGY_PONTOISE("FR, Cergy-Pontoise (95) - VélO2", 49.036111d, 2.063056d, //
			"www.velo2.cergypontoise.fr", VelibServiceProviderAdpater.CycloCity, Color.GREEN,//
			new String[] { "http://www.velo2.cergypontoise.fr/service/carto", "http://www.velo2.cergypontoise.fr/service/stationdetails/cergy/%s" }, //
			new double[] { 49.011215819514526d, 2.002507134593567d, 49.0522974873d, 2.11045997988d } //
			, new double[] { 40d, 0d, 60d, 10d } // 49.011215819514526d, 2.002507134593567d, 49.0522974873d, 2.11045997988
	), //
	FR_CRETEIL("FR, Créteil (94) - Cristolib", 48.791111d, 2.462778d, //
			"www.cristolib.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "http://www.cristolib.fr/service/carto", "http://www.cristolib.fr/service/stationdetails/creteil/%s" }, //
			new double[] { 48.76603756422197d, 2.446248652158302d, 48.7988350967829d, 2.4713148552260713d } //
			, new double[] { 40d, 2.39d, 50d, 13d } // 48.76603756422197d, 2.446248652158302d, 48.7988350967829d, 2.4713148552260713d
	), //
		// FR_LYON http://www.velov.grandlyon.com/
	FR_MARSEILLE("FR, Marseille (13) - Levelo", 43.296346d, 5.369889d, //
			"www.levelo-mpm.fr", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.levelo-mpm.fr/service/carto", "http://www.levelo-mpm.fr/service/stationdetails/marseille/%s" }, //
			new double[] { 43.23984252335505d, 5.349696475467313d, 43.31181964585524d, 5.406000803377506d } //
			, new double[] { 40d, 0d, 43.5d, 10d }), // 43.23984252335505d, 5.349696475467313d, 43.31181964585524d, 5.406000803377506d
	FR_MULHOUSE("FR, Mulhouse (68) - Velocité", 47.749481d, 7.33994d, //
			"www.velocite.mulhouse.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "http://www.velocite.mulhouse.fr/service/carto", "http://www.velocite.mulhouse.fr/service/stationdetails/mulhouse/%s" }, //
			new double[] { 47.733210486448705d, 7.308432590232066d, 47.76040353431892d, 7.35513626875887d } //
	), //
	FR_NANCY("FR, Nancy (54) - velostan", 48.6936d, 6.1846d, //
			"www.velostanlib.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "http://www.velostanlib.fr/service/carto", "http://www.velostanlib.fr/service/stationdetails/nancy/%s" }, //
			new double[] { 48.666721193895995d, 6.162844844269929d, 48.70087814850168d, 6.204735639873545d } //
	), //
	FR_NANTES("FR, Nantes (44) - Bicloo", 47.21806d, -1.55278d, //
			"www.bicloo.nantesmetropole.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "https://abo-nantes.cyclocity.fr/service/carto", "https://abo-nantes.cyclocity.fr/service/stationdetails/nantes/%s" }, //
			new double[] { 47.18921204751304d, -1.578689264310988d, 47.24629806842349d, -1.525933451925082d } //
	), //
		// FR_NICE("FR, Nice (44) - Velobleu", 43.68922539772765d, 7.202911376953125d, "www.velobleu.org", VelibServiceProviderAdpater.CycloCity, Color.BLUE,
		// new String[]{"http://www.velobleu.org/oybike/stands.nsf/getSite?openagent&site=nice&format=json&key=C14844AC3256CEB4E039D81971E31E63"}), //
	FR_PARIS("FR, Paris (75) - Velib", 48.856578d, 2.351828d, //
			"www.velib.paris.fr", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.velib.paris.fr/service/carto", "http://www.velib.paris.fr/service/stationdetails/paris/%s" }, //
			new double[] { 48.795772971485285d, 2.225205253470467d, 48.91508160115182d, 2.480224080865348d } // Boundy Box
	), //
	FR_SAINT_DENIS("FR, Saint-Denis (93) - Velcom", 48.936389d, 2.354722d, //
			"www.velcom.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "https://abo-plainecommune.cyclocity.fr/service/carto",
					"https://abo-plainecommune.cyclocity.fr/service/stationdetails/plainecommune/%s" }, //
			new double[] { 48.90400993613635d, 2.338086833359049d, 48.945238792177285d, 2.392932205694168d } //
	), //
	FR_ROUEN("FR, Rouen (76) - Cy'clic", 49.443889d, 1.103333d, //
			"cyclic.rouen.fr", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://cyclic.rouen.fr/service/carto", "http://cyclic.rouen.fr/service/stationdetails/rouen/%s" }, //
			new double[] { 49.42174920022087d, 1.063998154832871d, 49.44820751633492d, 1.117546476849879d } // Boundy Box
	), //
	FR_TOULOUSE("FR, Toulouse (31) - Vélô Toulouse", 43.604482d, 1.443962d, //
			"www.velo.toulouse.fr", VelibServiceProviderAdpater.CycloCity, Color.RED, //
			new String[] { "http://www.velo.toulouse.fr/service/carto", "http://www.velo.toulouse.fr/service/stationdetails/toulouse/%s" }, //
			new double[] { 43.55724369515301d, 1.40230245215212d, 43.642833569777416d, 1.48781984362323d } //
	), //
		// Other cont
	AU_BRISBANE("AU, Brisbane - CityCycle", -27.466667d, 153.033333d, //
			"www.citycycle.com.au", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.citycycle.com.au/service/carto", "http://www.citycycle.com.au/service/stationdetails/brisbane/%s" }, //
			new double[] { -27.499634d, 152.990655d, -27.448096d, 153.053644d } //
			, new double[] { -30d, 100d, -10d, 200d } //
	), //
		// Single file format
		// AU_VIENNE("AU, Vienne - CityBike Wien", -d, d, //
	// "www.citybikewien.at", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
	// new String[] { "http://dynamisch.citybikewien.at/citybike_xml.php", null }, //
	// new double[] { -d} //
	// , new double[] { -30d, 100d, -10d, 200d } //
	// ), //
	BE_BRUXELLE("BE, Bruxelle - Villo", 50.833d, 4.333d, //
			"www.villo.be", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.villo.be/service/carto", "http://www.villo.be/service/stationdetails/bruxelles/%s" }, //
			new double[] { 50.812489d, 4.303281d, 50.885885d, 4.408635d } //
	), //
	IR_DUBLIN("IR, Dublin - Dublinbikes ", 53.343418d, -6.267612d, //
			"www.dublinbikes.ie", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.dublinbikes.ie/service/carto", "http://www.dublinbikes.ie/service/stationdetails/dublin/%s" }, //
			new double[] { 53.330091d, -6.278214d, 53.359246d, -6.245575d } //
	), //
	SV_SUEDEN("SV, Suèden - Styr & Ställ", 57.7d, 11.933333d, //
			"www.goteborgbikes.se", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.goteborgbikes.se/service/carto", "http://www.goteborgbikes.se/service/stationdetails/goteborg/%s" }, //
			new double[] { 57.689983d, 11.947047d, 57.711547d, 11.995107d } //
	), //
	SL_LJUBLJANA("BE, Ljubljana - Bicikelj", 46.051425d, 14.505964d, //
			"en.bicikelj.si", VelibServiceProviderAdpater.CycloCity, Color.GREEN, //
			new String[] { "http://en.bicikelj.si/service/carto", "http://en.bicikelj.si/service/stationdetails/ljubljana/%s" }, //
			new double[] { 46.040213d, 14.486699d, 46.08259d, 14.534156d } //
	), //
		//
	LX_LUXEMBOURG("LX, Luxembourg - Vel'OH!", 49.61d, 6.13333d,//
			"www.veloh.lu", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.veloh.lu/service/carto", "http://www.veloh.lu/service/stationdetails/luxembourg/%s" }, //
			new double[] { 49.57337d, 6.07227d, 49.63713d, 6.1741d } //
	), //

	ES_SANTANDER("ES, Santander - Tusbic", 43.511111d, -3.586111d, //
			"www.tusbic.es", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.tusbic.es/service/carto", "http://www.tusbic.es/service/stationdetails/santander/%s" }, //
			new double[] { 43.452813054952315d, -3.871390682982487d, 43.47847181103079d, -3.773275975579615d } //
	), //
	ES_SEVILLA("ES, Sevilla - Sevici", 37.3833d, -5.99655d,//
			"www.sevici.es", VelibServiceProviderAdpater.CycloCity, Color.MAGENTA, //
			new String[] { "http://www.sevici.es/service/carto", "http://www.sevici.es/service/stationdetails/seville/%s" }, //
			new double[] { 37.320417860390016d, -6.012005274166835d, 37.42475843174024d, -5.908921914235877d } //
			, new double[] { 10d, -20d, 50d, 20d } // 37.320417860390016d, -6.012005274166835d, 37.42475843174024d, -5.908921914235877d
	), //
	ES_VALENCIA("ES, Valencia - Valenbisi", 39.466667d, -0.366667d, //
			"www.valenbisi.es", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://www.valenbisi.es/service/carto", "http://www.valenbisi.es/service/stationdetails/valence/%s" }, //
			new double[] { 39.43979598762512d, -0.426354265293528d, 39.50141318615774d, -0.323491937905993d } //
			, new double[] { 10d } // 39.43979598762512d, -0.426354265293528d, 39.50141318615774d, -0.323491937905993d
	), //
	JP_TOYAMA("JP, Toyama - Cyclocity", 36.695833d, 137.213611d, //
			"en.cyclocity.jp", VelibServiceProviderAdpater.CycloCity, Color.BLUE, //
			new String[] { "http://en.cyclocity.jp/service/carto", "http://en.cyclocity.jp/service/stationdetails/toyama/%s" }, //
			new double[] { 36.688891d, 137.208727d, 36.705842d, 137.220853d } //
	); //

	//https://montreal.bixi.com//data/bikeStations.xml
	// CA_MONTREAL("CA, Montreal - Bixi", 45.516672d, -73.650005d, //
	// "montreal.bixi.com", VelibServiceProviderAdpater.Bixi, Color.RED, //
	// new String[] { "http://montreal.bixi.com/maps/statajax", null }, //
	// new double[] { 0d, 0d, 0d, 0d } //
	// );

	// Angers http://www.irigo.fr/velo/velocite-plus/ ??? http://www.keolis-angers.fr/pages/index.php?page=irigo_plus&srub=irigo_plus_veloCite ??
	// http://www.citybikewien.at/
	// :http://dynamisch.citybikewien.at/citybike_xml.php
	// Aix en provence http://www.vhello.fr/misc/closed.html
	// ES Cordue http://www.ayuncordoba.es/
	// http://www.citybikewien.at/ => http://dynamisch.citybikewien.at/citybike_xml.php
	// ES http://www.gijon.es/

	// OTHER BIXI https://montreal.bixi.com/data/bikeStations.xml

	private final static String TAG = "VelibProvider";

	final String name;

	final double latitude;
	final double longitude;
	// minLatE6,minLngE6, maxLatE6, maxLngE6
	double[] boundyBoxE6;
	final double[] excludeBoundyBox;

	final String serviceHostName;
	final String[] servicePattenrsUrls;
	final VelibServiceProviderAdpater serviceProviderAdpater;
	final int color;
	GeoPoint cachedGeoPoint;

	private long lockServiceDownloadTime = -1;

	VelibProvider(String name, double latitude, double longitude, String serviceHostName, VelibServiceProviderAdpater serviceProviderAdpater) {
		this(name, latitude, longitude, serviceHostName, serviceProviderAdpater, Color.BLUE, null, null);
	}

	VelibProvider(String name, double latitude, double longitude, //
			String serviceHostName, VelibServiceProviderAdpater serviceProviderAdpater, int color,//
			String[] servicePattenrsUrls, double[] boundyBox) {
		this(name, latitude, longitude, //
				serviceHostName, serviceProviderAdpater, color,//
				servicePattenrsUrls, boundyBox, null);
	}

	VelibProvider(String name, double latitude, double longitude, //
			String serviceHostName, VelibServiceProviderAdpater serviceProviderAdpater, int color,//
			String[] servicePattenrsUrls, double[] boundyBox, double[] excludeBoundyBox) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.excludeBoundyBox = excludeBoundyBox;

		this.boundyBoxE6 = new double[] { //
		boundyBox[0] * AppConstants.E6, //
				boundyBox[1] * AppConstants.E6, //
				boundyBox[2] * AppConstants.E6, //
				boundyBox[3] * AppConstants.E6 };

		this.serviceHostName = serviceHostName;
		this.serviceProviderAdpater = serviceProviderAdpater;
		this.color = color;
		this.servicePattenrsUrls = servicePattenrsUrls;
		// Cache Data
		this.cachedGeoPoint = new GeoPoint((int) (latitude * 1000000), (int) (longitude * 1000000));
	}

	public GeoPoint asGeoPoint() {
		return cachedGeoPoint;
	}

	public static VelibProvider[] getVelibProviders() {
		return VelibProvider.values();
	}

	public static VelibProvider getVelibProvider(String providerName) {
		return VelibProvider.valueOf(providerName);
	}

	public static VelibProvider getVelibProvider(int provider) {
		return VelibProvider.values()[provider];
	}

	public static ArrayList<VelibProvider> getVelibProviderInBoundyBox(GeoPoint lastKnownLocationAsGeoPoint) {
		ArrayList<VelibProvider> providers = new ArrayList<VelibProvider>();
		if (lastKnownLocationAsGeoPoint != null) {
			for (VelibProvider testedProvider : VelibProvider.values()) {
				boolean isGeoPointInBoundyBox = GeoUtils.isGeoPointInBoundyBox(testedProvider.getBoundyBoxE6(), lastKnownLocationAsGeoPoint);
				if (isGeoPointInBoundyBox) {
					providers.add(testedProvider);
				}
			}
		}
		return providers;
	}

	public String getName() {
		return name;
	}

	public int getProvider() {
		return this.ordinal();
	}

	public String getProviderName() {
		return this.name();
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public GeoPoint getCachedGeoPoint() {
		return cachedGeoPoint;
	}

	public String getServiceHostName() {
		return serviceHostName;
	}

	public double[] getExcludeBoundyBox() {
		return excludeBoundyBox;
	}

	public double[] getExcludeBoundyBoxE6() {
		return GeoUtils.getBoundyBoxToBoundyBoxE6(excludeBoundyBox);
	}

	public String getUrlDipso(String stationId) {
		String url;
		if (servicePattenrsUrls != null && servicePattenrsUrls[1] != null) {
			url = serviceProviderAdpater.getUrlDipsoWithUrlPattern(servicePattenrsUrls[1], stationId);
		} else {
			url = serviceProviderAdpater.getUrlDipsoWithHostname(this.serviceHostName, stationId);
		}
		return url;
	}

	public String getUrlCarto() {
		String url;
		if (servicePattenrsUrls != null && servicePattenrsUrls[0] != null) {
			url = serviceProviderAdpater.getUrlCartoWithUrl(servicePattenrsUrls[0]);
		} else {
			url = serviceProviderAdpater.getUrlCartoWithHostname(this.serviceHostName);
		}
		return url;
	}

	public VeloServiceParser getVeloServiceParser() {
		return serviceProviderAdpater.getVeloServiceParser();
	}

	public double[] getBoundyBoxE6() {
		return boundyBoxE6;
	}

	public double[] getBoundyBox() {
		double[] boundyBox = null;
		if (isBoundyBoxE6()) {
			 boundyBox = new double[4];
			 for (int i=0; i<4; i++) {
				 boundyBox[i] = boundyBoxE6[i] / AppConstants.E6;
			 }
		}
		return boundyBox;
	}

	
	public boolean isBoundyBoxE6() {
		return boundyBoxE6 != null;
	}

	public void setBoundyBoxE6(double[] boundyBoxE6) {
		this.boundyBoxE6 = boundyBoxE6;
	}

	public GeoPoint getBoundyBoxMinAsGeoPoint() {
		GeoPoint pointMin = new GeoPoint((int) boundyBoxE6[0], (int) boundyBoxE6[1]);
		return pointMin;
	}

	public GeoPoint getBoundyBoxMaxAsGeoPoint() {
		GeoPoint pointMax = new GeoPoint((int) boundyBoxE6[2], (int) boundyBoxE6[3]);
		return pointMax;
	}

	public boolean isUnLockServiceDownloadTime() {
		return lockServiceDownloadTime < 0;
	}

	public long getLockServiceDownloadTime() {
		return lockServiceDownloadTime;
	}

	public void unLockServiceDownloadTime() {
		lockServiceDownloadTime = -1;
	}

	public void lockServiceDownloadTime(long nowInMs) {
		lockServiceDownloadTime = nowInMs;
	}

	@Override
	public boolean isFavory() {
		return false;
	}

	public int getColor() {
		return this.color;
	}
	
	

}
