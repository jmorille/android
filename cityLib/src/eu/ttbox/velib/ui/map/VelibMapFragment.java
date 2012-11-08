package eu.ttbox.velib.ui.map;

import org.osmdroid.util.GeoPoint;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.ttbox.velib.R;

/**
 * @see https://github.com/eskerda/CityBikes/tree/openvelib
 * 
 *      Map Api {@link http
 *      ://www.vogella.com/articles/AndroidLocationAPI/article.html#overview}
 * 
 *      Osm Sample {@link http://code.google.com/p/osmdroid/source/browse/trunk/
 *      OpenStreetMapViewer/src/org/osmdroid/MapActivity.java}
 * 
 *      MVP : {@link http
 *      ://stackoverflow.com/questions/4916209/which-design-patterns-are
 *      -used-on-android/6770903#6770903}
 * @author deostem
 * 
 */
public class VelibMapFragment extends Fragment implements VelibMapView, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "VelibMapFragment";


    // ===========================================================
    // Constructors
    // ===========================================================

       
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map, container, false);
 
         
        return v;
    }
    

    // ===========================================================
    // Listener
    // ===========================================================



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        
    }


    // ===========================================================
    // Drawing
    // ===========================================================


    @Override
    public void mapAnimateTo(GeoPoint geoPoint) {
        // TODO Auto-generated method stub
        
    }
 

}
