package eu.ttbox.geoping.domain.core;

import android.content.Context;

/**
 * This class monitors the change of country.
 * <p>
 * {@link #getCountryIso()} is used to get the ISO 3166-1 two letters country
 * code of current country.
 * @see com.android.providers.contacts.CountryMonitor 
 */
public class CountryMonitor {

    private String mCurrentCountryIso = "FR";
    private Context mContext;

    public CountryMonitor(Context context) {
        mContext = context;
    }

    /**
     * Get the current country code
     *
     * @return the ISO 3166-1 two letters country code of current country.
     */
    public synchronized String getCountryIso() {
        if (mCurrentCountryIso == null) {
//            final CountryDetector countryDetector =
//                    (CountryDetector) mContext.getSystemService(Context.COUNTRY_DETECTOR);
//            mCurrentCountryIso = countryDetector.detectCountry().getCountryIso();
//            countryDetector.addCountryListener(new CountryListener() {
//                public void onCountryDetected(Country country) {
//                    mCurrentCountryIso = country.getCountryIso();
//                }
//            }, Looper.getMainLooper());
        }
        return mCurrentCountryIso;
    }
    
    
}
