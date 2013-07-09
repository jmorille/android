package eu.ttbox.velib.ui.preference.comp;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class LongEditTextPreference extends EditTextPreference {

	public LongEditTextPreference(Context context) {
		super(context);
	}

	public LongEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LongEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		Long defaultValue = null;
		if (defaultReturnValue != null) {
			defaultValue = Long.valueOf(defaultReturnValue);
		} else {
			defaultValue = Long.valueOf(0);
		}
		return String.valueOf(getPersistedLong(defaultValue));
	}

	@Override
	protected boolean persistString(String value) {
		return persistLong(Long.valueOf(value));
	}

}
