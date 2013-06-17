package eu.ttbox.geoping;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.helper.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.master.GsmCidLatHelper;
import eu.ttbox.geoping.service.slave.BackgroudLocService;
import eu.ttbox.geoping.ui.AbstractSmsTrackerActivity;

@Deprecated
public class GeoTrakerActivity extends AbstractSmsTrackerActivity implements OnClickListener, LocationListener {

    private static final String TAG = "GeoTrakerActivity";

    private SharedPreferences appPreferences;
    private LocationManager lManager;
    private Location location;
    private String choix_source = "";

    private GeoTrackDatabase trackingBDD;

    TextView latitudeTextView;
    TextView longitudeTextView;
    TextView altitudeTextView;
    TextView accuracyTextView;
    TextView speedTextView;
    TextView bearingTextView;
    TextView adresseTextView;
    TextView extrasTextView;

    TextView gsmCidTextView;
    TextView gsmLacTextView;
 
    // Listener
    private BroadcastReceiver mStatusReceiver;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On spécifie que l'on va avoir besoin de gérer l'affichage du cercle
        // de chargement
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.geotracker);

        // On récupére le service de localisation
        lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        trackingBDD = new GeoTrackDatabase(this);

        // Services
        mStatusReceiver = new StatusReceiver();

        // Initialisation de l'écran
        initBinding();
        reinitialisationEcran();

        // On affecte un écouteur d'événement aux boutons
        findViewById(R.id.choix_source).setOnClickListener(this);
        findViewById(R.id.obtenir_position).setOnClickListener(this);
        findViewById(R.id.afficherAdresse).setOnClickListener(this);
        findViewById(R.id.sendSmsLoc).setOnClickListener(this);
    }
 

    @Override
    public void onResume() {
        super.onResume();
        // Register Listener
        IntentFilter filter = new IntentFilter();
        filter.addAction("EVENT_GSM");
        // Listener
        registerReceiver(mStatusReceiver, filter);
        Log.i(TAG, "###  onResume");
    }

    @Override
    public void onPause() {
        // Listener
        unregisterReceiver(mStatusReceiver);
        Log.i(TAG, "###  onPause");

        super.onPause();
    }

    private void initBinding() {
        latitudeTextView = (TextView) findViewById(R.id.latitude);
        longitudeTextView = (TextView) findViewById(R.id.longitude);
        altitudeTextView = (TextView) findViewById(R.id.altitude);
        accuracyTextView = (TextView) findViewById(R.id.accuracy);
        speedTextView = (TextView) findViewById(R.id.speed);
        bearingTextView = (TextView) findViewById(R.id.bearing);
        adresseTextView = (TextView) findViewById(R.id.adresse);
        extrasTextView = (TextView) findViewById(R.id.extras);
                
       gsmCidTextView = (TextView) findViewById(R.id.gsmCidLac_Cid);
        gsmLacTextView = (TextView) findViewById(R.id.gsmCidLac_Lac);
    }

    // Réinitialisation de l'écran
    private void reinitialisationEcran() {
        latitudeTextView.setText("0.0");
        longitudeTextView.setText("0.0");
        altitudeTextView.setText("0.0");
        accuracyTextView.setText("");
        speedTextView.setText("");
        bearingTextView.setText("");
        adresseTextView.setText("");
        extrasTextView.setText("");
        
        gsmCidTextView.setText("");
        gsmLacTextView.setText("");

        
        findViewById(R.id.obtenir_position).setEnabled(false);
        findViewById(R.id.afficherAdresse).setEnabled(false);
    }

    public void onGeocodingCidLatClick(View v) {
        int cellID = Integer.parseInt(gsmCidTextView.getText().toString());
        int lac = Integer.parseInt(gsmLacTextView.getText().toString());
        try {
            GsmCidLatHelper.displayMap(this, cellID, lac);
        } catch (Exception e) {
            Log.e(TAG, "Error in geocoding CidLat : " + e.getMessage(), e);
         }
    }
    
    
    // Méthode déclencher au clique sur un bouton
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
            Log.d(TAG, "Start Service Button");
            Intent intentService = new Intent(this, BackgroudLocService.class);
            startService(intentService);
            break;
        }
        case R.id.stopService: {
            Intent intentService = new Intent(this, BackgroudLocService.class);
            stopService(intentService);
            break;
        }
        default:
            break;
        }
    }
    
    

    private void choisirSource() {
        reinitialisationEcran();

        // On demande au service la liste des sources disponibles.
        List<String> providers = lManager.getProviders(true);

        final String[] sources = providers.toArray(new String[providers.size()]);

        // On affiche la liste des sources dans une fenétre de dialog
        // Pour plus d'infos sur AlertDialog, vous pouvez suivre le guide
        // http://developer.android.com/guide/topics/ui/dialogs.html
        new AlertDialog.Builder(this).setItems(sources, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                findViewById(R.id.obtenir_position).setEnabled(true);
                // on stock le choix de la source choisi
                choix_source = sources[which];
                // on ajoute dans la barre de titre de l'application le
                // nom de la source utilisé
                setTitle(String.format("%s - %s", getString(R.string.app_name), choix_source));
            }
        }).create().show();
    }

    /**
     *  http://developer.android.com/guide/topics/location/obtaining-user-location.html
     */
    private void obtenirPosition() {
        // on démarre le cercle de chargement
        setProgressBarIndeterminateVisibility(true);

        // On demande au service de localisation de nous notifier tout
        // changement de position
        // sur la source (le provider) choisie, toute les minutes
        // (60000millisecondes).
        // Le paramétre this spécifie que notre classe implémente
        // LocationListener et recevra
        // les notifications.
        lManager.requestLocationUpdates(choix_source, 60000, 0, this);
        // Location lastKnownLocation =
        // lManager.getLastKnownLocation(choix_source);
    }

    private void afficherLocation() {
        // On affiche les informations de la position a l'écran
        latitudeTextView.setText(String.valueOf(location.getLatitude()));
        longitudeTextView.setText(String.valueOf(location.getLongitude()));
        altitudeTextView.setText(String.valueOf(location.getAltitude()));
        accuracyTextView.setText(String.valueOf(location.getAccuracy()));
        speedTextView.setText(String.valueOf(location.getSpeed()));
        bearingTextView.setText(String.valueOf(location.getBearing()));
        // Extras
        Bundle extras = location.getExtras();
        StringBuffer sb = new StringBuffer();
        for (String key : extras.keySet()) {
            String extraVal = extras.getString(key);
            Log.i(getClass().getName(), "Extra " + key + " = " + extraVal);
            sb.append(key).append(" : ").append(extraVal).append("/n");
        }
        extrasTextView.setText(String.valueOf(sb.toString()));

    }

    private void afficherAdresse() {
        setProgressBarIndeterminateVisibility(true);

        // Le geocoder permet de récupérer ou chercher des adresses
        // gréce é un mot clé ou une position
        Geocoder geo = new Geocoder(this);
        try {
            // Ici on récupére la premiere adresse trouvé gréce é la position
            // que l'on a récupéré
            List<Address> adresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (adresses != null && adresses.size() == 1) {
                Address adresse = adresses.get(0);
                // Si le geocoder a trouver une adresse, alors on l'affiche
                adresseTextView.setText(String.format("%s, %s %s", adresse.getAddressLine(0), adresse.getPostalCode(), adresse.getLocality()));
            } else {
                // sinon on affiche un message d'erreur
                adresseTextView.setText("L'adresse n'a pu étre déterminée");
            }
        } catch (IOException e) {
            e.printStackTrace();
            adresseTextView.setText("L'adresse n'a pu étre déterminée");
        }
        // on stop le cercle de chargement
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Lorsque la position change...
        Log.i("Tuto géolocalisation", "La position a changé.");
        // ... on stop le cercle de chargement
        setProgressBarIndeterminateVisibility(false);
        // ... on active le bouton pour afficher l'adresse
        findViewById(R.id.afficherAdresse).setEnabled(true);
        // ... on sauvegarde la position
        this.location = location;
        // ... on l'affiche
        afficherLocation();
        // ... et on spécifie au service que l'on ne souhaite plus avoir de mise
        // é jour
        lManager.removeUpdates(this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Lorsque la source (GSP ou réseau GSM) est désactivé
        Log.i("Tuto géolocalisation", "La source a été désactivé");
        // ...on affiche un Toast pour le signaler é l'utilisateur
        Toast.makeText(this, String.format("La source \"%s\" a été désactivé", provider), Toast.LENGTH_SHORT).show();
        // ... et on spécifie au service que l'on ne souhaite plus avoir de mise
        // é jour
        lManager.removeUpdates(this);
        // ... on stop le cercle de chargement
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("Tuto géolocalisation", "La source a été activé.");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("Tuto géolocalisation", "Le statut de la source a changé.");
    }

    private void sendSms(Location location) {
        final String KEY_SMS_PHONE_NUMBER = "smsPhoneNumber";
        final String KEY_SMS_USE = "smsUse";
        final String KEY_LOCAL_SAVE = "localSave";

        // Local Persist
        boolean saveLocal = appPreferences.getBoolean(KEY_LOCAL_SAVE, false);
        if (saveLocal) {
            // Inser Local
            GeoTrack geoPoint = new GeoTrack(AppConstants.LOCAL_DB_KEY, location);
            ContentValues values = GeoTrackHelper.getContentValues(geoPoint);
            Uri uri = getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
            // Read all points
            Toast.makeText(this, String.format("insertTrackPoint with id  \"%s\" ", "" + uri), Toast.LENGTH_SHORT).show();
        }
        // Sms
        boolean useSms = appPreferences.getBoolean(KEY_SMS_USE, false);
        if (useSms) {
            String destinationAddress = appPreferences.getString(KEY_SMS_PHONE_NUMBER, null);
            if (destinationAddress != null && destinationAddress.length() > 0) {
                GeoTrack geotrack = new GeoTrack(null, location);
                Bundle params = GeoTrackHelper.getBundleValues(geotrack);
                String encoded = SmsMessageIntentEncoderHelper.encodeSmsMessage(SmsMessageActionEnum.LOC, params);
                if (encoded != null && encoded.length() > 0) {
                    SmsManager.getDefault().sendTextMessage(destinationAddress, null, encoded, null, null);
                }
            } else {
                Log.w("SMS Sender", "No SMS destination, define preference key " + KEY_SMS_PHONE_NUMBER);
            }
        }
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive Intent action : " + action);
            if ("EVENT_GSM".equals(action)) {
                int cid = intent.getIntExtra("cid", 0);
                int lac = intent.getIntExtra("lac", 0);
                gsmCidTextView.setText(String.valueOf(cid));
                gsmLacTextView.setText(String.valueOf(lac));
            }
        }
    }
}
