package eu.ttbox.geoping.ui.map.track.bubble;

import java.util.Locale;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.Person;

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

	public void setData(Person person, GeoTrack geoTrack) {
		// Person
		this.nameTextView.setText(person.name);
		// Track
		if (geoTrack != null) {
			// Date $Time
			String dateString = String.format(getResources().getString(R.string.geotrack_time_dateformat), geoTrack.getTimeAsDate());
			timeTextView.setText(dateString);
			providerTextView.setText( geoTrack.getProvider() ); 
	       	// Coord
			double lat = geoTrack.getLatitude();
			double lng = geoTrack.getLongitude();
			String coordString = String.format(Locale.US, "(%.6f, %.6f)", lat, lng);
			coordTextView.setText(coordString);
			if (displayGeoLoc) {
				coordTextView.setVisibility(VISIBLE);
			} else {
				coordTextView.setVisibility(GONE);
			}
		 
			// Accuracy
			if (geoTrack.hasAccuracy()) {
				accuracyTextView.setText((int) geoTrack.getAccuracy() + "m");
				accuracyTextView.setVisibility(VISIBLE);
			} else {
				accuracyTextView.setText("");
				accuracyTextView.setVisibility(GONE);
			}
			// Altitude
			if (geoTrack.hasAltitude()) {
				altitudeTextView.setText((int) geoTrack.getAltitude() + "m");
				altitudeBlock.setVisibility(VISIBLE);
			} else {
				altitudeTextView.setText("");
				altitudeBlock.setVisibility(GONE);
			}
			// Speed
			if (geoTrack.hasSpeed()) {
				speedTextView.setText((int) geoTrack.getSpeed() + "m/s");
				speedTextView.setVisibility(GONE);
			} else {
				speedTextView.setText("");
				speedTextView.setVisibility(GONE);
			}
			// Address
			setAddress(geoTrack.address);
		} else {
			accuracyTextView.setText("");
			altitudeBlock.setVisibility(GONE);
		}

	}


 
	public void setAddress(String  addr) { 
		if (addr != null && addr.length()>0) {  
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
