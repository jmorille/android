package eu.ttbox.smstraker;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import eu.ttbox.smstraker.activity.AbstractSmsTrackerActivity;
import eu.ttbox.smstraker.core.AppConstant;
import eu.ttbox.smstraker.domain.GeoTrack;
import eu.ttbox.smstraker.domain.GeoTrackSmsMsg;
import eu.ttbox.smstraker.domain.GeoTrackerProvider;
import eu.ttbox.smstraker.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.smstraker.domain.geotrack.GeoTrackHelper;
import eu.ttbox.smstraker.service.GeoTrackingService;
import eu.ttbox.smstraker.service.SmsMsgActionHelper;
import eu.ttbox.smstraker.service.SmsMsgEncryptHelper;

public class GeoTrakerActivity extends AbstractSmsTrackerActivity implements OnClickListener, LocationListener {

    private static final String TAG ="GeoTrakerActivity";

    private SharedPreferences appPreferences; 
	private LocationManager lManager;
	private Location location;
	private String choix_source = "";

	private GeoTrackDatabase trackingBDD;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// On sp�cifie que l'on va avoir besoin de g�rer l'affichage du cercle
		// de chargement
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.geotracker);

		// On r�cup�re le service de localisation
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		trackingBDD = new GeoTrackDatabase(this);
		
		// Initialisation de l'�cran
		reinitialisationEcran();

		// On affecte un �couteur d'�v�nement aux boutons
		findViewById(R.id.choix_source).setOnClickListener(this);
		findViewById(R.id.obtenir_position).setOnClickListener(this);
		findViewById(R.id.afficherAdresse).setOnClickListener(this);
		findViewById(R.id.sendSmsLoc).setOnClickListener(this);
	}

	// M�thode d�clencher au clique sur un bouton
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.choix_source:
			choisirSource();
			break;
		case R.id.obtenir_position:
			obtenirPosition();
			break;
		case R.id.afficherAdresse:
			afficherAdresse();
			break;
		case R.id.sendSmsLoc:
			if (location == null) {
				obtenirPosition();
			}
			if (location != null) {
				sendSms(location);
			}
			break;
		case R.id.startService: {
			Intent intentService = new Intent(this,GeoTrackingService.class); 
			startService(intentService);
			break;
		}
		case R.id.stopService: {
			Intent intentService = new Intent(this,GeoTrackingService.class); 
			stopService(intentService);
			break;
		}
		default:
			break;
		}
	}

	// R�initialisation de l'�cran
	private void reinitialisationEcran() {
		((TextView) findViewById(R.id.latitude)).setText("0.0");
		((TextView) findViewById(R.id.longitude)).setText("0.0");
		((TextView) findViewById(R.id.altitude)).setText("0.0");
		((TextView) findViewById(R.id.accuracy)).setText("");
		((TextView) findViewById(R.id.adresse)).setText("");
		((TextView) findViewById(R.id.extras)).setText("");

		findViewById(R.id.obtenir_position).setEnabled(false);
		findViewById(R.id.afficherAdresse).setEnabled(false);
	}

	private void choisirSource() {
		reinitialisationEcran();

		// On demande au service la liste des sources disponibles.
		List<String> providers = lManager.getProviders(true);

		final String[] sources = providers.toArray(new String[providers.size()]);

		// On affiche la liste des sources dans une fen�tre de dialog
		// Pour plus d'infos sur AlertDialog, vous pouvez suivre le guide
		// http://developer.android.com/guide/topics/ui/dialogs.html
		new AlertDialog.Builder(this).setItems(sources, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				findViewById(R.id.obtenir_position).setEnabled(true);
				// on stock le choix de la source choisi
				choix_source = sources[which];
				// on ajoute dans la barre de titre de l'application le
				// nom de la source utilis�
				setTitle(String.format("%s - %s", getString(R.string.app_name), choix_source));
			}
		}).create().show();
	}

	/**
	 * @see http 
	 *      ://developer.android.com/guide/topics/location/obtaining-user-location
	 *      .html
	 */
	private void obtenirPosition() {
		// on d�marre le cercle de chargement
		setProgressBarIndeterminateVisibility(true);

		// On demande au service de localisation de nous notifier tout
		// changement de position
		// sur la source (le provider) choisie, toute les minutes
		// (60000millisecondes).
		// Le param�tre this sp�cifie que notre classe impl�mente
		// LocationListener et recevra
		// les notifications.
		lManager.requestLocationUpdates(choix_source, 60000, 0, this);
		// Location lastKnownLocation =
		// lManager.getLastKnownLocation(choix_source);
	}

	private void afficherLocation() {
		// On affiche les informations de la position a l'�cran
		((TextView) findViewById(R.id.latitude)).setText(String.valueOf(location.getLatitude()));
		((TextView) findViewById(R.id.longitude)).setText(String.valueOf(location.getLongitude()));
		((TextView) findViewById(R.id.altitude)).setText(String.valueOf(location.getAltitude()));
		((TextView) findViewById(R.id.accuracy)).setText(String.valueOf(location.getAccuracy()));
		// Extras
		Bundle extras = location.getExtras();
		StringBuffer sb = new StringBuffer();
		for (String key : extras.keySet()) {
			String extraVal = extras.getString(key);
			Log.i(getClass().getName(), "Extra " + key + " = " + extraVal);
			sb.append(key).append(" : ").append(extraVal).append("/n");
		}
		((TextView) findViewById(R.id.extras)).setText(String.valueOf(sb.toString()));

	}

	private void afficherAdresse() {
		setProgressBarIndeterminateVisibility(true);

		// Le geocoder permet de r�cup�rer ou chercher des adresses
		// gr�ce � un mot cl� ou une position
		Geocoder geo = new Geocoder(this);
		try {
			// Ici on r�cup�re la premiere adresse trouv� gr�ce � la position
			// que l'on a r�cup�r�
			List<Address> adresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

			if (adresses != null && adresses.size() == 1) {
				Address adresse = adresses.get(0);
				// Si le geocoder a trouver une adresse, alors on l'affiche
				((TextView) findViewById(R.id.adresse)).setText(String.format("%s, %s %s", adresse.getAddressLine(0), adresse.getPostalCode(), adresse.getLocality()));
			} else {
				// sinon on affiche un message d'erreur
				((TextView) findViewById(R.id.adresse)).setText("L'adresse n'a pu �tre d�termin�e");
			}
		} catch (IOException e) {
			e.printStackTrace();
			((TextView) findViewById(R.id.adresse)).setText("L'adresse n'a pu �tre d�termin�e");
		}
		// on stop le cercle de chargement
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Lorsque la position change...
		Log.i("Tuto g�olocalisation", "La position a chang�.");
		// ... on stop le cercle de chargement
		setProgressBarIndeterminateVisibility(false);
		// ... on active le bouton pour afficher l'adresse
		findViewById(R.id.afficherAdresse).setEnabled(true);
		// ... on sauvegarde la position
		this.location = location;
		// ... on l'affiche
		afficherLocation();
		// ... et on sp�cifie au service que l'on ne souhaite plus avoir de mise
		// � jour
		lManager.removeUpdates(this);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Lorsque la source (GSP ou r�seau GSM) est d�sactiv�
		Log.i("Tuto g�olocalisation", "La source a �t� d�sactiv�");
		// ...on affiche un Toast pour le signaler � l'utilisateur
		Toast.makeText(this, String.format("La source \"%s\" a �t� d�sactiv�", provider), Toast.LENGTH_SHORT).show();
		// ... et on sp�cifie au service que l'on ne souhaite plus avoir de mise
		// � jour
		lManager.removeUpdates(this);
		// ... on stop le cercle de chargement
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("Tuto g�olocalisation", "La source a �t� activ�.");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("Tuto g�olocalisation", "Le statut de la source a chang�.");
	}

	private void sendSms(Location location) {
		final String KEY_SMS_PHONE_NUMBER = "smsPhoneNumber";
		final String KEY_SMS_USE = "smsUse";
		final String KEY_LOCAL_SAVE = "localSave";
		
		// Local Persist
		boolean saveLocal = appPreferences.getBoolean(KEY_LOCAL_SAVE, false);
		if (saveLocal) {
		    // Inser Local
			GeoTrack geoPoint = new GeoTrack(AppConstant.LOCAL_DB_KEY, location);
            ContentValues values =   GeoTrackHelper.getContentValues(geoPoint);
            getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
            // Read all points
			
			Log.i(TAG, "Open DB");
			trackingBDD.open(); 
//			trackingBDD.insertTrackPoint(geoPoint); 
			List<GeoTrack> points = trackingBDD.getTrakPointWithTitre(AppConstant.LOCAL_DB_KEY); 
			Toast.makeText(this, String.format("insertTrackPoint with id  \"%s\" ", ""+points.size ()), Toast.LENGTH_SHORT).show(); 
			trackingBDD.close();
		}
		// Sms
		boolean useSms = appPreferences.getBoolean(KEY_SMS_USE, false);
		if (useSms) {
			String destinationAddress = appPreferences.getString(KEY_SMS_PHONE_NUMBER, null);
			if (destinationAddress != null && destinationAddress.length() > 0) {
			    GeoTrackSmsMsg geoTrackMsg =   SmsMsgActionHelper.geoLocMessage(location);
				String smsMsg = SmsMsgEncryptHelper.encodeSmsMessage(geoTrackMsg);
				if (smsMsg!=null && smsMsg.length()>0) {
					SmsManager.getDefault().sendTextMessage(destinationAddress, null, smsMsg, null, null);
				}
			} else {
				Log.w("SMS Sender", "No SMS destination, define preference key " + KEY_SMS_PHONE_NUMBER);
			}
		}
	}

}
