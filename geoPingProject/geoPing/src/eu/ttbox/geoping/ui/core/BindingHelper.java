package eu.ttbox.geoping.ui.core;


import android.widget.EditText;

public class BindingHelper {

    public static String getEditTextAsValueTrimToNull(EditText view) {
        String nameDirty = view.getText().toString();
        return trimToNull(nameDirty);
    }


    public static String trimToNull(String nameDirty) {
        String name = nameDirty;
        if (name != null) {
            name = name.trim();
            if (name.length() < 1) {
                name = null;
            }
        }
        return name;
    }


}
