package eu.ttbox.geoping.ui.about;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.person.colorpicker.ColorPickerDialog;
import eu.ttbox.geoping.ui.person.holocolorpicker.ColorPicker;
import eu.ttbox.geoping.ui.person.holocolorpicker.OpacityBar;
import eu.ttbox.geoping.ui.person.holocolorpicker.SVBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {

    private WebView webView;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Custum View
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View view = factory.inflate(R.layout.about, null);
        doBindingView(view);

        // Create Dialg
        return new AlertDialog.Builder(getActivity())
        // .setIcon(R.drawable.alert_dialog_icon
                .setView(view) //
                // .setTitle(R.string.color_picker)//
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // TODO
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // TODO
                    }
                }).create();
    }

    private void doBindingView(View v) {
        // Bindings
        webView = (WebView) v.findViewById(R.id.html_view);
        webView.setWebViewClient(new WebViewClient());
        //
        final TextView versionView = (TextView) v.findViewById(R.id.version_view);
        versionView.setText(versionName());
    }

    // ===========================================================
    // Intent
    // ===========================================================

    

    public void handleIntent(Intent intent) {
        if (intent != null) {
            IntentAbout.loadIntentContent(intent, getActivity(), webView);
        }
    }

    // ===========================================================
    // Business
    // ===========================================================

    private String versionName() {
        return ((GeoPingApplication) getActivity().getApplication()).version();
    }

}
