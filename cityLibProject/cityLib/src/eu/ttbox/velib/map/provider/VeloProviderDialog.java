package eu.ttbox.velib.map.provider;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import eu.ttbox.velib.R;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;

public class VeloProviderDialog extends Dialog {

	public VeloProviderDialog(final Context context, final VelibProvider velibProvider, final VelibService velibService) {
		super(context);
		setContentView(R.layout.velo_provider_dialog);
		// Delete Button
		Button deleteButton = (Button) findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				velibService.removeAllStationsByProvider(velibProvider);
			}
		});
		// Update Button
		Button updateButton = (Button) findViewById(R.id.updateButton);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<Station> stations = velibService.getStationsByProviderWithCheckUpdateDate(velibProvider, true);
				Toast.makeText(context, "Download Stations size " + stations.size(), Toast.LENGTH_SHORT).show();
			}
		});

	}

}
