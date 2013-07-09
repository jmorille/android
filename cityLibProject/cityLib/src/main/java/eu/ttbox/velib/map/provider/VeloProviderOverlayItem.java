package eu.ttbox.velib.map.provider;

import org.osmdroid.views.overlay.OverlayItem;

import eu.ttbox.velib.model.VelibProvider;

public class VeloProviderOverlayItem extends OverlayItem {

	private VelibProvider velibProvider;

	public VeloProviderOverlayItem(VelibProvider velibProvider) {
		super( velibProvider.getName(), velibProvider.getUrlCarto(), velibProvider.asGeoPoint());
		this.velibProvider = velibProvider;
	}

	public VelibProvider getVelibProvider() {
		return velibProvider;
	}

}
