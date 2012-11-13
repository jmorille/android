package eu.ttbox.velib.ui.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.DateUtils;
import eu.ttbox.velib.map.station.bubble.BubbleOverlayView;
import eu.ttbox.velib.model.Station;

public class HelpStationDispoBubbleActivity extends Fragment {

 

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.help_station_dispo_bubble, container, false);
		initStationDispoHelp(v);
		return v;
	}

	private void initStationDispoHelp(View v) {
		BubbleOverlayView balloonView = new BubbleOverlayView<Station>(this.getActivity(), null, 0);
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
		LinearLayout container = (LinearLayout) v.findViewById(R.id.help_station_dispobubble_container);
		container.addView(balloonView);
	}

}
