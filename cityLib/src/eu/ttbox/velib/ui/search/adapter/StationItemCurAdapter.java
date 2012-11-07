package eu.ttbox.velib.ui.search.adapter;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.map.station.drawable.StationDispoIcView;
import eu.ttbox.velib.model.FavoriteIconEnum;

public class StationItemCurAdapter extends SimpleCursorAdapter {

	private static final String TAG = "StationItemCurAdapter";

	protected String[] mOriginalFrom;

	protected int[] mFrom;

	protected int[] mTo;

	public Location lastLoc;

	public StationItemCurAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, Location lastLoc) {
		super(context, layout, c, from, to);
		// super(context, layout, c, from, to, flags);// For Ics
		// init
		this.lastLoc = lastLoc;
		this.mOriginalFrom = from;
		this.mTo = to;
		findColumns(from);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewBinder binder = getViewBinder();
		final int count = mTo.length;
		final int[] from = mFrom;
		final int[] to = mTo;

		for (int i = 0; i < count; i++) {
			int viewToId = to[i];
			final View v = view.findViewById(viewToId);
			if (v != null) {
				boolean bound = false;
				int cursorFromId = from[i];
				if (binder != null) {
					bound = binder.setViewValue(v, cursor, cursorFromId);
				}

				if (!bound) {
					String text = cursor.getString(cursorFromId);
					if (text == null) {
						text = "";
					}
					// Log.i(TAG, "bindView from [" + from[i] + "]  == text [" +
					// text + "]");
					if (v instanceof CheckBox) {
						setViewCheckBox((CheckBox) v, text);
					} else if (v instanceof TextView) {
						setViewText((TextView) v, text);
					} else if (v instanceof ImageView) {
						setViewFavoriteImage((ImageView) v, text);
					} else if (v instanceof StationDispoIcView) {
						setViewStationDispo((StationDispoIcView) v, text);
					} else {
						Log.e(TAG, "Not manage type " + v);
						// throw new
						// IllegalStateException(v.getClass().getName() +
						// " is not a " +
						// " view that can be bounds by this SimpleCursorAdapter");
					}
				}
			}
		}
	}

 
	
	public void setViewFavoriteImage(ImageView v, String value) {
		if (value == null || value.length() < 1) {
			v.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
			FavoriteIconEnum favoriteType = FavoriteIconEnum.getFromName(value);
			if (favoriteType==null) {
				favoriteType = FavoriteIconEnum.DEFAULT_ICON;
			}
			v.setImageResource(favoriteType.getImageResource());
		}
	}

	private void setViewCheckBox(CheckBox v, String value) {
		boolean isChecked = "1".equals(value) ? true : false;
		v.setChecked(isChecked);
	}

	private long getDistanceInMeter(int latE6, int lngE6) {
		long distInM = -1;
		if (lastLoc != null) {
			float[] results = new float[3];
			Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), latE6 / AppConstants.E6, lngE6 / AppConstants.E6, results);
			distInM = (long) results[0];
		}
		return distInM;
	}

	private long getDistanceInMeter(String latLngE6) {
		long distInM = -1;
		int separatorIdx = latLngE6.indexOf("#");
		if (separatorIdx > 0) {
			int textSize = latLngE6.length();
			String latE6String = latLngE6.substring(0, separatorIdx);
			String lngE6String = latLngE6.substring(separatorIdx + 1, textSize);
			int latE6 = Integer.parseInt(latE6String);
			int lngE6 = Integer.parseInt(lngE6String);
			return getDistanceInMeter(latE6, lngE6);
		}
		return distInM;
	}

	private void setViewStationDispo(StationDispoIcView v, String value) {
		// Log.d(TAG, "setViewStationDispo for [" + value + "]");
		int separatorIdx = value.indexOf("#");
		if (separatorIdx > 0) {
			int textSize = value.length();
			String cycle = value.substring(0, separatorIdx);
			String parking = value.substring(separatorIdx + 1, textSize);
			int cycleCount = Integer.parseInt(cycle);
			int parkingCount = Integer.parseInt(parking);
			v.setStationCycle(cycleCount);
			v.setStationParking(parkingCount);
			// v.setStationFavory(true);
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Create a map from an array of strings to an array of column-id integers
	 * in mCursor. If mCursor is null, the array will be discarded.
	 * 
	 * @param from
	 *            the Strings naming the columns of interest
	 */
	private void findColumns(String[] from) {
		Cursor mCursor = getCursor();
		if (mCursor != null) {
			int i;
			int count = from.length;
			if (mFrom == null || mFrom.length != count) {
				mFrom = new int[count];
			}
			for (i = 0; i < count; i++) {
				mFrom[i] = mCursor.getColumnIndexOrThrow(from[i]);
				// if (Log.isLoggable(TAG, Log.DEBUG)) {
				// Log.i(TAG, "findColumns for [" + from[i] + "]  == [" +
				// mFrom[i] + "]");
				// }
			}
		} else {
			mFrom = null;
		}
	}

}
