package eu.ttbox.velib.map.direction;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirection;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirectionRoute;
import eu.ttbox.velib.service.ws.direction.parser.GoogleDirectionStatusEnum;

public class DrivingDirectionOverlay extends Overlay {

	private Context context;
	private MapView mapView;

	public DrivingDirectionOverlay(Context context, MapView mapView, GoogleDirection direction) {
		super(context);
		this.context = context;
		this.mapView = mapView;
		// direction
		parseDirection(direction);
	}

	private void parseDirection(GoogleDirection direction) {
		if (GoogleDirectionStatusEnum.OK.equals(direction.status)) {
			for (GoogleDirectionRoute route : direction.routes) {
				for (GeoPoint point : route.polyline) {
					// TODO
				}
			}
		}
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow ) {

//		return super.draw(canvas, mapView, shadow, when);
	}

}
