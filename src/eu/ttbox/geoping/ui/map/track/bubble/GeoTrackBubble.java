package eu.ttbox.geoping.ui.map.track.bubble;

import eu.ttbox.geoping.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class GeoTrackBubble extends FrameLayout  {

	private final static String TAG = "GeoTrackBubble";

	// Config
	private int DEFAULT_BUBBLE_WIDTH = 300; 

	// Display
	private LinearLayout layout;

	public GeoTrackBubble(Context context) {
		super(context);
		layout = new LinearLayout(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.map_geotrack_bubble, layout);
		
		
		// Init fields
//		this.coordTextView = (TextView) v.findViewById(R.id.map_mylocation_dialogView_coord);
//		this.accuracyTextView = (TextView) v.findViewById(R.id.map_mylocation_dialogView_accuracy);
//		this.addressTextView = (TextView) v.findViewById(R.id.map_mylocation_dialogView_address);
//		this.altitudeTextView = (TextView) v.findViewById(R.id.map_mylocation_dialogView_altitude);
//		this.altitudeBlock = v.findViewById(R.id.map_mylocation_dialogView_block_altitude);
//		this.speedTextView = (TextView) v.findViewById(R.id.map_mylocation_dialogView_speed);
		// Frame
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		params.width = DEFAULT_BUBBLE_WIDTH;
		addView(layout, params);
	}


}
