package eu.ttbox.geoping.encoder;

/**
 * <a href="https://developers.google.com/maps/documentation/ios/urlscheme">Ios Scheme</a>
 */
public class GoogleMapUriEncoderHelper {
/*
    public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params) {
        return encodeSmsMessage(action, params, null);
    }

    public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params, TextEncryptor textEncryptor) {
        StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE_7BITS);
        sb.append("https://maps.google.fr/maps?");
        String latLngString = getLatLngAsString(params);
        sb.append("q=").append("loc:").append(latLngString);
        sb.append("Z=17");
        return sb.toString();
    }

    private static String getLatLngAsString(Bundle params) {
        String latLngString = null;
        int latE6 = params.getInt(GeoTrackDatabase.GeoTrackColumns.COL_LATITUDE_E6, Integer.MIN_VALUE);
        int lngE6 = params.getInt(GeoTrackDatabase.GeoTrackColumns.COL_LONGITUDE_E6, Integer.MIN_VALUE);
        boolean isGeoE6 = (latE6 > Integer.MIN_VALUE) && (lngE6 > Integer.MIN_VALUE);
        if (isGeoE6) {
            double lat = latE6 / AppConstants.E6;
            double lng = lngE6 / AppConstants.E6;
            latLngString = String.format(Locale.US, "%.6f,%.6f", lat, lng);
        }
        return latLngString;
    }
*/


}
