package eu.ttbox.velib.search;

import android.content.SearchRecentSuggestionsProvider;

public class StationRecentSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

	public final static String AUTHORITY = "eu.ttbox.velib.search.StationRecentSearchRecentSuggestionsProvider";
	public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

	public StationRecentSearchRecentSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

}