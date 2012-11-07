package eu.ttbox.velib.ui.help;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.DateUtils;
import eu.ttbox.velib.map.station.bubble.BubbleOverlayView;
import eu.ttbox.velib.model.Station;

public class HelpStationDispoBubbleActivity extends Activity {

	private BubbleOverlayView balloonView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_station_dispo_bubble);
		initStationDispoHelp();

	}

	private void initStationDispoHelp() {
		balloonView = new BubbleOverlayView<Station>(this, null, 0);
		// Define Data
		Station station = new Station();
		station.setName("PLACE DE L'HOTEL DE VILLE");
		station.setNumber("4017");
		station.setAddress("7 PLACE DE L'HOTEL DE VILLE -");
		station.setFullAddress("7 PLACE DE L'HOTEL DE VILLE - 75004 PARIS");
		station.setOpen(true);
		station.setLatitude(48.85713805311015d);
		station.setLongitude(2.35121100822012d);
		station.setStationParking(73);
		station.setStationCycle(42);
		station.setFavory(true);
		// Update
		long now = System.currentTimeMillis();
		station.setVeloUpdated(now - DateUtils.MILLIS_PER_MINUTE - DateUtils.MILLIS_PER_SECOND * 37);
		balloonView.setData(station, now);

		// Insert into screen
		LinearLayout container = (LinearLayout) findViewById(R.id.help_station_dispobubble_container);
		container.addView(balloonView);
	}

}
