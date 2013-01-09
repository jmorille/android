package eu.ttbox.geoping.ui.person;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.person.PhotoEditorView.EditorListener;

public class PersonListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

	private PersonHelper helper;

	private boolean isNotBinding = true;

	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	public PersonListAdapter(Context context, Cursor c, int flags) {
		super(context, R.layout.track_person_list_item, c, flags);
	}

	private void intViewBinding(View view, Context context, Cursor cursor) {
		// Init Cursor
		helper = new PersonHelper().initWrapper(cursor);
		isNotBinding = false;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {

		if (isNotBinding) {
			intViewBinding(view, context, cursor);
		}
		final ViewHolder holder = (ViewHolder) view.getTag();
		// Bind Value
		final String phoneNumber = helper.getPersonPhone(cursor);
		final String contactId = helper.getContactId(cursor);
		holder.phoneText.setText(phoneNumber);
		// Color
		int color = helper.getPersonColor(cursor);
		Drawable stld = PersonColorDrawableHelper.getListBackgroundColor(color);
		view.setBackgroundDrawable(stld);

		// Button
		helper.setTextPersonName(holder.nameText, cursor);

		holder.pingButton.setEditorListener(new EditorListener() {
			@Override
			public void onRequest(int request) {
				context.startService(Intents.sendSmsGeoPingRequest(context, phoneNumber));
				// Notif
				NotifToasts.showToastSendGeoPingRequest(context, phoneNumber);
			}
		});
		if (contactId != null) {
			executorService.execute(new Runnable() { 
				@Override
				public void run() {
					// TODO cache Photo
					holder.pingButton.setValues(contactId, true); 
				}
			});
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		// Then populate the ViewHolder
		ViewHolder holder = new ViewHolder();
		holder.nameText = (TextView) view.findViewById(R.id.person_list_item_name);
		holder.phoneText = (TextView) view.findViewById(R.id.person_list_item_phone);
		holder.pingButton = (PhotoEditorView) view.findViewById(R.id.person_list_item_geoping_button);
		// Bug
		holder.pingButton.setFocusable(false);
		holder.pingButton.setFocusableInTouchMode(false);
		// and store it inside the layout.
		view.setTag(holder);
		return view;

	}

	static class ViewHolder {
		PhotoEditorView pingButton;
		TextView nameText;
		TextView phoneText;
	}
}
