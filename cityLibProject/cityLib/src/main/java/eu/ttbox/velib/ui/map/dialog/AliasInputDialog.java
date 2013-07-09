package eu.ttbox.velib.ui.map.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.widget.EditText;
import eu.ttbox.velib.R;

public class AliasInputDialog extends AlertDialog implements OnClickListener {

	private final OnAliasInputListener mCallBack;

	private EditText aliaseditText;

	/**
	 * The callback used to indicate the user is done selecting the favorite Icon.
	 */
	public interface OnAliasInputListener {

		void onAliasInput(String alias);
	}

	public AliasInputDialog(Context context, OnAliasInputListener callBack) {
		this(context, 0, callBack);
	}

	public AliasInputDialog(Context context, int theme, OnAliasInputListener callBack) {
		super(context, theme);
		this.mCallBack = callBack;
		// Init
		Context themeContext = getContext();
		setTitle(R.string.dialog_custum_alias);
		setCancelable(true);
		setIcon(0);
		setCanceledOnTouchOutside(true);
		setButton(BUTTON_POSITIVE, themeContext.getText(R.string.valid), this);
		setButton(BUTTON_NEGATIVE, themeContext.getText(R.string.cancel), (OnClickListener) null);

		// Create View
		aliaseditText = new EditText(getContext());
		aliaseditText.setHint(R.string.dialog_custum_alias_hint);
		setView(aliaseditText);

	}

	public void setAliasInput(String alias) {
		aliaseditText.setText(alias);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mCallBack != null) {
			Editable aliasEditable = aliaseditText.getText();
			String value = aliasEditable != null ? aliaseditText.getText().toString().trim() : null;
			if (value != null && value.length() < 1) {
				value = null;
			}
			mCallBack.onAliasInput(value);
		}

	}

}
