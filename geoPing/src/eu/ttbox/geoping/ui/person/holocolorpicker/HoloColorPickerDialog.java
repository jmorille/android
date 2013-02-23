package eu.ttbox.geoping.ui.person.holocolorpicker;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.person.holocolorpicker.ColorPicker.OnColorChangedListener;

public class HoloColorPickerDialog extends DialogFragment implements OnColorChangedListener {

    private ColorPicker picker;
    private SVBar svBar;
    private OpacityBar opacityBar;

    private OnColorChangedListener onColorChangedListener;

    private static String COLOR = "COLOR";

    // ===========================================================
    // Listener
    // ===========================================================
    
    public static HoloColorPickerDialog newInstance(int color) {
        HoloColorPickerDialog frag = new HoloColorPickerDialog();
        Bundle args = new Bundle();
        args.putInt(COLOR, color);
        frag.setArguments(args);
        return frag;
    }

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.holo_colorpicker, container, false);
        // Title
        getDialog().setTitle(R.string.color_picker);
        // Binding
        picker = (ColorPicker) v.findViewById(R.id.holocolorpicker_picker);
        svBar = (SVBar) v.findViewById(R.id.holocolorpicker_svbar);
        opacityBar = (OpacityBar) v.findViewById(R.id.holocolorpicker_opacitybar);
        // Listener
        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.setOnColorChangedListener(this);
        // Init values
        int color = getArguments().getInt(COLOR);
        picker.setOldCenterColor(color);
        picker.setColor(color);
        
        // Button
        // button.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // text.setTextColor(picker.getColor());
        // picker.setOldCenterColor(picker.getColor());
        // }
        // });

        return v;
    }

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onColorChanged(int color) {
        if (onColorChangedListener != null)
            onColorChangedListener.onColorChanged(color);
    }
}
