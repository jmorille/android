package eu.ttbox.geoping.ui.person;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import eu.ttbox.geoping.R;

public class PhotoHeaderBinderHelper {


    // Binding
    public ImageView photoImageView;

    public ImageView mainActionIconImageView;
    public TextView mainActionNameTextView;

    public ImageView subEltIcon;
    public TextView subEltNameTextView;
    public TextView subEltPhoneTextView;

    public PhotoHeaderBinderHelper( View rootView) {
        // Binding
        this.photoImageView = (ImageView) rootView.findViewById(R.id.header_photo_imageView);

        this.mainActionIconImageView = (ImageView) rootView.findViewById(R.id.header_photo_main_action);
        this.mainActionNameTextView = (TextView) rootView.findViewById(R.id.header_photo_main_name);

        this.subEltIcon = (ImageView) rootView.findViewById(R.id.header_photo_subelt_icon);
        this.subEltNameTextView = (TextView) rootView.findViewById(R.id.header_photo_subelt_action);
        this.subEltPhoneTextView = (TextView) rootView.findViewById(R.id.header_photo_subelt_phone);
    }



}
