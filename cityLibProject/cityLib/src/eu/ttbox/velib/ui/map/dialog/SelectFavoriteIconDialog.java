package eu.ttbox.velib.ui.map.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import eu.ttbox.velib.R;
import eu.ttbox.velib.model.FavoriteIconEnum;

/**
 * @see http://developer.android.com/resources/tutorials/views/hello-gridview.html
 *  
 */
public class SelectFavoriteIconDialog extends AlertDialog {

	 private final OnSelectFavoriteIconListener mCallBack;
	 
	/**
     * The callback used to indicate the user is done selecting the favorite Icon.
     */
    public interface OnSelectFavoriteIconListener {
 
        void onSelectFavoriteIcon(FavoriteIconEnum favicon);
    }
    
	public SelectFavoriteIconDialog(Context context,  OnSelectFavoriteIconListener callBack) {
		this(context, 0, callBack);
	}

	public SelectFavoriteIconDialog(Context context, int theme,  OnSelectFavoriteIconListener callBack) {
		super(context, theme);
		this.mCallBack = callBack;
		// Init
		Context themeContext = getContext();
		// setTitle(R.string.dialog_custum_favorite_icon);
		setCancelable(true);
		setIcon(0);
		setCanceledOnTouchOutside(true);
		setButton(BUTTON_NEGATIVE, themeContext.getText(R.string.cancel), (OnClickListener) null);

		// View
		LayoutInflater inflater = (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_select_favorite, null);
		setView(view);

		// List Of icons
		GridView gridview = (GridView) view;
		gridview.setAdapter(new ImageAdapter(context));
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				FavoriteIconEnum favicon = (FavoriteIconEnum) parent.getItemAtPosition(position);
				if (mCallBack!=null) {
					mCallBack.onSelectFavoriteIcon(favicon);
				}
				cancel();
 			}
		});
	}

	private static class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return FavoriteIconEnum.values().length;
		}

		public Object getItem(int position) {
			return FavoriteIconEnum.values()[position];
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(FavoriteIconEnum.values()[position].getImageResource());
			return imageView;
		}

	}

}
