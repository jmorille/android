package eu.ttbox.velib.ui.slidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.ttbox.velib.R;

public class SlindingMenuItemView extends RelativeLayout {

    private static final String TAG = "SlindingMenuItemView";

    private ImageView selector;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SlindingMenuItemView(Context context) {
        this(context, null);
    }

    public SlindingMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlindingMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.slidingmenu_item_view, this, true);
        
        // Binding
        ImageView icon = (ImageView) v.findViewById(R.id.slidingmenu_item_icon);
        TextView title = (TextView) v.findViewById(R.id.slidingmenu_item_title);
        selector = (ImageView) v.findViewById(R.id.slidingmenu_item_selector_icon);

        // Attribute
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlindingMenuItemView, 0, 0);
        try {
            String mShowText = a.getString(R.styleable.SlindingMenuItemView_text);
            title.setText(mShowText);
            Drawable iconDrawable = a.getDrawable(R.styleable.SlindingMenuItemView_src);
            icon.setImageDrawable(iconDrawable);
        } finally {
            a.recycle();
        }
    }

    // ===========================================================
    // Accessors
    // ===========================================================

    public void setSlidingMenuSelectedVisible(boolean isSelected) {
        if (isSelected) {
            selector.setVisibility(View.VISIBLE);
        } else {
            selector.setVisibility(View.GONE);
        }
    }
    public boolean isSlidingMenuSelectedVisible( ) {
        return selector.getVisibility() == View.VISIBLE;
    }
}
