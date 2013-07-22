package eu.ttbox.geoping.ui;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.pairing.PairingListActivity;
import eu.ttbox.geoping.ui.person.PersonListActivity;
import eu.ttbox.geoping.ui.prefs.GeoPingPrefActivity;
import eu.ttbox.geoping.ui.smslog.SmsLogListActivity;

public class MenuOptionsItemSelectionHelper {

	public static boolean onOptionsItemSelected(Context context, com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option:
			Intent intentOption = new Intent(context, GeoPingPrefActivity.class);
			context.startActivity(intentOption);
			return true;
		case R.id.menuMap:
			Intent intentMap = new Intent(context, ShowMapActivity.class);
			context.startActivity(intentMap);
			return true;
		case R.id.menu_pairing:
			Intent intentPairing = new Intent(context, PairingListActivity.class);
			context.startActivity(intentPairing);
			return true;
		case R.id.menu_track_person:
			Intent intentGeoPing = new Intent(context, PersonListActivity.class);
			context.startActivity(intentGeoPing);
			return true;
		case R.id.menu_smslog:
			Intent intentSmsLog = new Intent(context, SmsLogListActivity.class);
			context.startActivity(intentSmsLog);
			return true;
		case R.id.menuAppComment:
			Intents.startActivityAppMarket(context);
			return true;

			// case R.id.menuAppShare:
			// Intent intentAppShare = new Intent(Intent.ACTION_SEND);
			// intentAppShare.putExtra(Intent.EXTRA_TEXT,
			// "market://details?id=eu.ttbox.geoping");
			// return true;

		}

		return false;
	}

	public static boolean onOptionsItemSelected(Context context, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option:
			Intent intentOption = new Intent(context, GeoPingPrefActivity.class);
			context.startActivity(intentOption);
			return true;
		case R.id.menuMap:
			Intent intentMap = new Intent(context, ShowMapActivity.class);
			context.startActivity(intentMap);
			return true;
		case R.id.menu_pairing:
			Intent intentPairing = new Intent(context, PairingListActivity.class);
			context.startActivity(intentPairing);
			return true;
		case R.id.menu_track_person:
			Intent intentGeoPing = new Intent(context, PersonListActivity.class);
			context.startActivity(intentGeoPing);
			return true;
		case R.id.menu_smslog:
			Intent intentSmsLog = new Intent(context, SmsLogListActivity.class);
			context.startActivity(intentSmsLog);
			return true;
		}
		return false;
	}
}
