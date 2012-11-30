package eu.ttbox.geoping.ui.map.track.bubble;

import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.ui.person.PersonColorDrawableHelper;
import eu.ttbox.osm.core.ExternalIntents;
import eu.ttbox.osm.ui.map.mylocation.CompassEnum;

public class GeoTrackBubble extends FrameLayout {

	private final static String TAG = "GeoTrackBubble";

	// Config
	private int DEFAULT_BUBBLE_WIDTH = 300;

	// Datas
	private GeoTrack geoTrack;

	// Display
	private LinearLayout layout;
	private TextView nameTextView;
	private TextView providerTextView;

	private TextView timeTextView;
	private TextView coordTextView;
	private TextView accuracyTextView;
	private TextView altitudeTextView;
	private View altitudeBlock;
	private TextView speedTextView;
	private TextView bearingTextView;

    private View batteryBlock;
    private TextView batteryTextView;

	private ImageView navigationImg;
	private ImageView streetviewImg;

	private TextView addressTextView;

	private boolean displayGeoLoc = false;

	public GeoTrackBubble(Context context) {
		super(context);
		layout = new LinearLayout(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.map_geotrack_bubble, layout);

		// Init fields
		this.nameTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_name);
		this.providerTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_provider);
		this.timeTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_time);
		this.coordTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_coord);
		this.accuracyTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_accuracy);
		this.addressTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_address);
		this.altitudeTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_altitude);
		this.altitudeBlock = v.findViewById(R.id.map_geotrack_bubbleView_block_altitude);
		this.speedTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_speed);
		this.bearingTextView = (TextView) v.findViewById(R.id.map_geotrack_bubbleView_bearing);
		
		this.batteryBlock = v.findViewById(R.id.map_geotrack_bubbleView_block_battery);
		this.batteryTextView= (TextView) v.findViewById(R.id.map_geotrack_bubbleView_battery);

		// Button
		streetviewImg = (ImageView) v.findViewById(R.id.map_geotrack_bubbleView_streetview_image);
		streetviewImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startStreetView();
			}
		});
		navigationImg = (ImageView) v.findViewById(R.id.map_geotrack_bubbleView_navigation_image);
		navigationImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startNavigationTo();
			}
		});

		// Frame
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		params.width = DEFAULT_BUBBLE_WIDTH;
		addView(layout, params);
	}

	private boolean isSameGeoTrack(GeoTrack location, GeoTrack other) {
		if (location != null && other != null) {
			boolean isSame = location.getId() == other.getId();
			return isSame;
		} else if (location == null && other == null) {
			return true;
		}
		return false;
	}

	private void startNavigationTo() {
		if (geoTrack != null) {
			ExternalIntents.startActivityNavigationTo(getContext(), geoTrack.getLatitude(), geoTrack.getLongitude());
		}
	}

	private void startStreetView() {
		if (geoTrack != null) {
			ExternalIntents.startActivityStreetView(getContext(), geoTrack.getLatitude(), geoTrack.getLongitude());
		}
	}

	public void setData(Person person, GeoTrack geoTrack) {
		// Person
		this.nameTextView.setText(person.displayName);
		// Track
		boolean hasLatLng = false;
		boolean hasAccuracy = false;
		boolean hasAltitude = false;
		boolean hasSpeed = false;
		boolean hasAddress = false;
		boolean hasProvider = false;
		boolean hasBearing = false;
		boolean hasTime = false;
		boolean hasBattery = false;
		if (geoTrack != null) {
			hasLatLng = geoTrack.hasLatLng();
			hasAccuracy = geoTrack.hasAccuracy();
			hasAltitude = geoTrack.hasAltitude();
			hasSpeed = geoTrack.hasSpeed();
			hasAddress = geoTrack.hasAddress();
			hasProvider = geoTrack.hasProvider();
			hasBearing = geoTrack.hasBearing();
			hasTime = geoTrack.hasTime();
			hasBattery = geoTrack.hasBatteryLevelInPercent();
		}
		// Color
		Drawable colorDrawable = PersonColorDrawableHelper.getBubbleBackgroundColor(person.color);
		setBackgroundDrawable(colorDrawable);
		// GeoTrack
		this.geoTrack = geoTrack;
		// Bind values
     	if (hasTime) {
			// Date $Time
			String dateString = String.format(getResources().getString(R.string.geotrack_time_dateformat), geoTrack.getTimeAsDate());
			timeTextView.setText(dateString);
			timeTextView.setVisibility(VISIBLE);
		} else {
			timeTextView.setVisibility(GONE);
		} 
		if (hasProvider) {
			providerTextView.setText(geoTrack.getProvider());
			providerTextView.setVisibility(VISIBLE);
		} else {
			providerTextView.setVisibility(GONE);
		}
		// Coord
		if (hasLatLng && displayGeoLoc) { 
			double lat = geoTrack.getLatitude();
			double lng = geoTrack.getLongitude();
			String coordString = String.format(Locale.US, "(%.6f, %.6f)", lat, lng);
			coordTextView.setText(coordString);
			coordTextView.setVisibility(VISIBLE);
		} else {
			coordTextView.setVisibility(GONE);
		}
		// Accuracy
		if (hasAccuracy) {
			accuracyTextView.setText(String.format("%s m",(int) geoTrack.getAccuracy() ));
			accuracyTextView.setVisibility(VISIBLE);
		} else {
			accuracyTextView.setText("");
			accuracyTextView.setVisibility(GONE);
		} 
		// Altitude
		if (hasAltitude) {
			altitudeTextView.setText(String.format("%s m", (int) geoTrack.getAltitude()));
			altitudeBlock.setVisibility(VISIBLE);
		} else {
			altitudeTextView.setText("");
			altitudeBlock.setVisibility(GONE);
		}
		// Speed
		if (hasSpeed) {
		    int speedMs = geoTrack.getSpeed();
		    int speedKh = (int)( speedMs * 3.6);
			speedTextView.setText(String.format("%s km/h", speedKh));
			speedTextView.setVisibility(VISIBLE);
		} else {
			speedTextView.setText("");
			speedTextView.setVisibility(GONE);
		}
		// Bearing
		if (hasBearing) {
            String compassAsString = null;
		    float bearing = (float) geoTrack.getBearing();
		    CompassEnum compass = CompassEnum.getCardinalPoint(bearing);
		    if (compass!=null) {
		        compassAsString = compass.getI18nLabelShort(getContext());
		    }
		    bearingTextView.setText(String.format("%s° %s", (int) bearing, compassAsString));
		    bearingTextView.setVisibility(VISIBLE);
		} else {
		    bearingTextView.setText("");
		    bearingTextView.setVisibility(GONE);
		}
		if (hasBattery) {
		    batteryTextView.setText(String.format("%s %", geoTrack.batteryLevelInPercent ));
		    batteryBlock.setVisibility(VISIBLE);
		} else {
		    batteryTextView.setText("");
            batteryBlock.setVisibility(GONE);
		}
		// Address
		if (hasAddress) {
			 setAddress(geoTrack.address);
		} else {
			 setAddress("");
		}  	
	}
 
	public void setAddress(String addr) {
		if (addr != null && addr.length() > 0) {
			addressTextView.setText(addr);
			addressTextView.setVisibility(VISIBLE);
		} else {
			addressTextView.setText("");
			addressTextView.setVisibility(GONE);
		} 
	}

	public void setDisplayGeoLoc(boolean displayGeoLoc) {
		this.displayGeoLoc = displayGeoLoc;
	}

}
