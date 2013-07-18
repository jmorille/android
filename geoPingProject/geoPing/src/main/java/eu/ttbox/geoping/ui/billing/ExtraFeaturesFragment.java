package eu.ttbox.geoping.ui.billing;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import eu.ttbox.geoping.BuildConfig;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.billing.util.IabHelper;
import eu.ttbox.geoping.service.billing.util.IabResult;
import eu.ttbox.geoping.service.billing.util.Inventory;
import eu.ttbox.geoping.service.billing.util.Purchase;
import eu.ttbox.geoping.service.billing.util.SkuDetails;

public class ExtraFeaturesFragment extends Fragment {

    private static final String TAG = "ExtraFeaturesFragment";

    // Product
    private static final String SKU_NO_AD_PER_YEAR = "NO_AD_PER_YEAR";

    // binding
    private ListView extraListView;


    // The helper object
    private IabHelper mHelper;


    // ===========================================================
    // Billing Listener
    // ===========================================================

    boolean isAdSupressPerYearPurchase = false;

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            Purchase adSupressPerYearPurchase = inventory.getPurchase(SKU_NO_AD_PER_YEAR);
            isAdSupressPerYearPurchase = (adSupressPerYearPurchase != null && verifyDeveloperPayload(adSupressPerYearPurchase));

        }
    };


    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // ===========================================================
    // Constructor
    // ===========================================================

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.extra_features_list, container, false);
        // Bindings
        extraListView = (ListView) v.findViewById(android.R.id.list);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create Adapter
        SkuDetailsListAdapter adapter = createListItems();

        // Binding Menu
        extraListView.setAdapter(adapter);
        extraListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SkuDetails item = (SkuDetails) parent.getItemAtPosition(position);
                Log.i(TAG, "Click on SkuDetails : " + item);
                onClickSkuDetails(item);
            }
        });


        // Create the helper, passing it our context and the public key to verify signatures with
        // -----------------------------------------------------------------------------------------
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getActivity(), getBase64EncodedPublicKey());
        // enable debug logging (for a production application, you should set this to false).
        if (BuildConfig.DEBUG) {
            mHelper.enableDebugLogging(true);
        }

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    private SkuDetailsListAdapter createListItems() {
        SkuDetailsListAdapter adapter = new SkuDetailsListAdapter(getActivity());
        try {
            adapter.add(new SkuDetails("{\"productId\" : \"" + SKU_NO_AD_PER_YEAR +
                    "\", \"type\" : \"inapp\", \"price\" : \"$1.99\" , \"title\" : \"No add in app\", \"description\" : \"Suppress all adds during one year\"  }  "));
            adapter.add(new SkuDetails("{\"productId\" : \"SECU_HIDE_LAUNCHER\", \"type\" : \"inapp\", \"price\" : \"Free\" , \"title\" : \"No icon app launcher\", \"description\" : \"Hide the GeoPing Application in the System\"  }  "));
        } catch (JSONException e) {
            Log.e(TAG, "Error Parsing Json : " + e.getMessage(), e);
        }
        return adapter;

    }


    // ===========================================================
    // Dialog
    // ===========================================================

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }


    public void onClickSkuDetails(SkuDetails skuDetails) {
        if ("SECU_HIDE_LAUNCHER".equals(skuDetails.getSku() )) {
           boolean hideStatus =  ExtraFeatureHelper.enabledSettingLaucherIcon(getActivity(), null);
            if (hideStatus) {
                Toast.makeText(getActivity(), "Show Icon Laucher", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Hide Icon Laucher", Toast.LENGTH_SHORT).show();
            }
        } else  if ("noAddForOneYear".equals(skuDetails.getSku() )) {

        }
    }

    // ===========================================================
    // Billing
    // ===========================================================

    /**
     * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     * <p/>
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    private String getBase64EncodedPublicKey() {
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg6EIpPnbaQ73nK3psbyxspmlEBK4cE9MpDUIS492zPg0h++6tgx7bvSKNK8COrxtDCIUE3A4XxJkLoqxGupdpYBPWdwsNGP67VMDgjLaC2TP8EQRFEHEEZFUuIaY8LPKXsP5QhfEKKFTZxHs/fav0olvVDhZ1MnB+SO6ZbRw/GmZE4ILQMIURn5bypX248OMTwDwrESqVwWKH4165SzM9VeI8/iVAsxnDDG1VfQ8Gnfi4QjyZKG5U9jRyt0iIMnV3LOhkk549Zjv3oLS7R02kcjIfigBztB4P6+MXwZ/5DlN7CKmxn+5IiTACSb4LEoPrekw0DNG+bHaxdpz/fEimQIDAQAB";
        return base64EncodedPublicKey;
    }
    // ===========================================================
    // Other
    // ===========================================================

}
