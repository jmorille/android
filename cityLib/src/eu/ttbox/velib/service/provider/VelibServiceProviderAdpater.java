package eu.ttbox.velib.service.provider;

import eu.ttbox.velib.service.download.VeloServiceParser;
import eu.ttbox.velib.service.download.bixi.BixiServiceParser;
import eu.ttbox.velib.service.download.cyclocity.CyclocityServiceParser;

public enum VelibServiceProviderAdpater {

	CycloCity("http://%s/service/carto", "http://%s/service/stationdetails/%s") {
		@Override
		public VeloServiceParser createVeloServiceParser() {
			return new CyclocityServiceParser();
		}
	},

	Bixi("http://%s/data/bikeStations.xml", null) {
		@Override
		public VeloServiceParser createVeloServiceParser() {
			return new BixiServiceParser();
		}
	},

	VeloBleu("http://%s/oybike/stands.nsf/getsite?openagent&site=nice&format=json&key=diolev", "http://%s/service/stationdetails/%s") {
		@Override
		public VeloServiceParser createVeloServiceParser() {
			return new CyclocityServiceParser();
		}
	};

	private final String serviceCartoUrlPattern;
	private final String serviceDispoUrlPattern;

	private VeloServiceParser veloServiceParser;

	VelibServiceProviderAdpater(String serviceCartoUrlPattern, String serviceDispoUrlPattern) {
		this.serviceCartoUrlPattern = serviceCartoUrlPattern;
		this.serviceDispoUrlPattern = serviceDispoUrlPattern;
	}

	// public DefaultHandler getHandler(int provider) {
	// throw new RuntimeException("Not implemented XML Service Handler");
	// }

	public String getUrlCartoWithHostname(String hostName) {
		String url = String.format(serviceCartoUrlPattern, hostName);
		return url;
	}

	public String getUrlCartoWithUrl(String urlDispoUrl) {
		return urlDispoUrl;
	}

	public String getUrlDipsoWithHostname(String hostName, String stationId) {
		String url = String.format(serviceDispoUrlPattern, hostName, stationId);
		return url;
	}

	public String getUrlDipsoWithUrlPattern(String urlDispoUrlPattern, String stationId) {
		String url = String.format(urlDispoUrlPattern, stationId);
		return url;
	}

	public VeloServiceParser getVeloServiceParser() {
		if (veloServiceParser == null) {
			veloServiceParser = createVeloServiceParser();
		}
		return veloServiceParser;
	}

	protected VeloServiceParser createVeloServiceParser() {
		throw new RuntimeException("Not implemented");
	}
}
