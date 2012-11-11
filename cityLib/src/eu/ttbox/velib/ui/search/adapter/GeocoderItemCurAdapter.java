package eu.ttbox.velib.ui.search.adapter;

import android.content.Context;
import android.database.Cursor;
import android.location.LocationListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import eu.ttbox.velib.R;

public class GeocoderItemCurAdapter extends ResourceCursorAdapter {

	private static final String TAG = "GeonameItemCurAdapter";

	public GeocoderItemCurAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery); 
	}

	
	 

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		ViewHolder holder = (ViewHolder) view.getTag();
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		// Then populate the ViewHolder
		ViewHolder holder = new ViewHolder();
		holder.addressText = (TextView) view.findViewById(R.id.geocoder_list_item_address);
//		holder.lastnameText = (TextView) view.findViewById(R.id.user_list_item_lastname);
//		holder.matriculeText = (TextView) view.findViewById(R.id.user_list_item_matricule);
		// and store it inside the layout.
		view.setTag(holder);
		return view;

	}

	static class ViewHolder {
		TextView	addressText ;
	}
	
}
