package eu.ttbox.velib.ui.search;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.core.Intents;
import eu.ttbox.velib.search.StationRecentSearchRecentSuggestionsProvider;
import eu.ttbox.velib.ui.preference.VelibPreferenceActivity;

/**
 * General configuration of Search Dialog: @see
 * http://developer.android.com/guide/topics/search/search-dialog.html
 * 
 * For sample implementation :
 * 
 * @see http 
 *      ://developer.android.com/resources/samples/SearchableDictionary/src/com
 *      /example/android/searchabledict/SearchableDictionary.html
 * 
 *      For howto use cursor:
 * 
 * @see http://mobile.tutsplus.com/tutorials/android/android-sdk_loading-
 *      data_cursorloader/
 */
public class SearchableVeloActivity extends FragmentActivity {

    private static final String TAG = "SearchableVeloActivity";

    public static final String ACTION_VIEW_FAVORITE = "eu.ttbox.velib.ui.search.ACTION_VIEW_FAVORITE";

    private SearchableVeloFragment searchFragment;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_station_activity);
        // handle Intent
        handleIntent(getIntent());
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof SearchableVeloFragment) {
            searchFragment = (SearchableVeloFragment) fragment;
        }
    }

    // ===========================================================
    // Handle Intent
    // ===========================================================

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "handleIntent for action : " + intent.getAction());
        }

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            setTitle(R.string.menu_search);
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, StationRecentSearchRecentSuggestionsProvider.AUTHORITY, StationRecentSearchRecentSuggestionsProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            // Log.d(TAG,
            // "---------------------------------------------------------");
            // Log.d(TAG, "ContentProvider Search suggest for " + query);
            // Log.d(TAG,
            // "---------------------------------------------------------");
            searchFragment.doSearch(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            setTitle(R.string.menu_search);
            // Handle a suggestions click (because the suggestions all use
            // ACTION_VIEW)
            // Log.i(TAG, "*********** TODO show result intent"+ intent);
            Uri data = intent.getData();
            CharSequence userQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY);
            if (userQuery != null) {
                searchFragment.doSearch(userQuery.toString());
            }
            // Show Result;
            searchFragment.startMapActivityWithData(data);
        } else if (ACTION_VIEW_FAVORITE.equals((intent.getAction()))) {
            setTitle(R.string.menu_favorite);
            Integer velibProvider = intent.getIntExtra(Intents.EXTRA_VELIB_PROVIDER, -1);
            searchFragment.doSearchFavorite(velibProvider);
        } else {
            Log.w(TAG, "Not Handle Intent for for Action : " + intent.getAction());
        }
    }

    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Création d'un MenuInflater qui va permettre d'instancier un Menu XML
        // en un objet Menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        // SearchManager searchManager = (SearchManager)
        // getSystemService(Context.SEARCH_SERVICE);
        // SearchView searchView = (SearchView)
        // menu.findItem(R.id.menu_search).getActionView();
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // searchView.setIconifiedByDefault(false);
        // }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuOptions: {
            Intent intentOption = new Intent(this, VelibPreferenceActivity.class);
            startActivity(intentOption);
            return true;
        }
        case R.id.menu_search: {
            onSearchRequested();
            Log.d(TAG, "---------------  onSearchRequested  ---------------------------");
            return true;
        }
        case R.id.menuMap: {
            Intent intentMap = new Intent(this, VelibMapActivity.class);
            startActivity(intentMap);
            return true;
        }
        case R.id.menuQuit: {
            finish();
            return true;
        }
        }
        return false;
    }

}
