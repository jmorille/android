package eu.ttbox.geoping.ui.admob;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.ttbox.geoping.R;

/**
 * {link https://developers.google.com/mobile-ads-sdk/docs/admob/fundamentals?hl=iw-IL}
  *
 */
public class AdmobFragment extends Fragment {
    // ===========================================================
    // Constructors
    // ===========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.admob, container, false);

        return v;
    }

}
