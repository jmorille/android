package eu.ttbox.geoping.ui.prefs.comp;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntEditTextPreference extends EditTextPreference {

	public IntEditTextPreference(Context context) {
		super(context);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		int savedValue = getPersistedInt(-1);
		if (savedValue > -1) {
			return String.valueOf(getPersistedInt(-1));
		} else {
			return null;
		}
	}

	@Override
	protected boolean persistString(String value) {
		Integer intVal = Integer.valueOf(value);
		return persistInt(intVal);
	}

}
