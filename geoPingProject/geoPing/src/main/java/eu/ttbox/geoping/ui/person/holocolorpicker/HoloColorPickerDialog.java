package eu.ttbox.geoping.ui.person.holocolorpicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.person.colorpicker.ColorPickerDialog;
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

    // @Override
    // public View onCreateView(LayoutInflater inflater, ViewGroup container,
    // Bundle savedInstanceState) {
    // View v = inflater.inflate(R.layout.holo_colorpicker, container, false);
    // // Title
    // getDialog().setTitle(R.string.color_picker);
    // // Binding
    // picker = (ColorPicker) v.findViewById(R.id.holocolorpicker_picker);
    // svBar = (SVBar) v.findViewById(R.id.holocolorpicker_svbar);
    // opacityBar = (OpacityBar)
    // v.findViewById(R.id.holocolorpicker_opacitybar);
    // // Listener
    // picker.addSVBar(svBar);
    // picker.addOpacityBar(opacityBar);
    // picker.setOnColorChangedListener(this);
    // // Init values
    // int color = getArguments().getInt(COLOR);
    // picker.setOldCenterColor(color);
    // picker.setColor(color);
    //
    // // Button
    // // button.setOnClickListener(new OnClickListener() {
    // //
    // // @Override
    // // public void onClick(View v) {
    // // text.setTextColor(picker.getColor());
    // // picker.setOldCenterColor(picker.getColor());
    // // }
    // // });
    //
    // return v;
    // }

    private void doBindingView(View v) {
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
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Custum View
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View view = factory.inflate(R.layout.holo_colorpicker, null);
        doBindingView(view);

        // Create Dialg
        return new AlertDialog.Builder(getActivity())
        // .setIcon(R.drawable.alert_dialog_icon
                .setView(view) //
                .setTitle(R.string.color_picker)//
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // ((FragmentAlertDialog)getActivity()).doPositiveClick();
                        ColorPickerDialog.OnColorChangedListener target = (ColorPickerDialog.OnColorChangedListener) getTargetFragment();
                        target.onColorChanged(picker.getColor());
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // ((FragmentAlertDialog)getActivity()).doNegativeClick();
                    }
                }).create();
    }

    // ===========================================================
    // Action
    // ===========================================================

    @Override
    public void onColorChanged(int color) {
        if (onColorChangedListener != null)
            onColorChangedListener.onColorChanged(color);
    }
}
