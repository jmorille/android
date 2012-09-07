package eu.ttbox.smstraker.domain.person;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.TextView;
import eu.ttbox.smstraker.domain.Person;
import eu.ttbox.smstraker.domain.person.PersonDatabase.PersonColumns;

public class PersonHelper {

	boolean isNotInit = true;
	public int idIdx = -1;
	public int nameIdx = -1;
	public int phoneIdx = -1;

	public PersonHelper initWrapper(Cursor cursor) {
		idIdx = cursor.getColumnIndex(PersonColumns.KEY_ID);
		nameIdx = cursor.getColumnIndex(PersonColumns.KEY_NAME);
		phoneIdx = cursor.getColumnIndex(PersonColumns.KEY_PHONE);

		isNotInit = false;
		return this;
	}

	public Person getEntity(Cursor cursor) {
		if (isNotInit) {
			initWrapper(cursor);
		}
		Person user = new Person();
		user.setId(idIdx > -1 ? cursor.getLong(idIdx) : -1);
		user.setName(nameIdx > -1 ? cursor.getString(nameIdx) : null);
		user.setPhone(phoneIdx > -1 ? cursor.getString(phoneIdx) : null);
		return user;
	}

	private PersonHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
		view.setText(cursor.getString(idx));
		return this;
	}

	public PersonHelper setTextPersonId(TextView view, Cursor cursor) {
		return setTextWithIdx(view, cursor, idIdx);
	}

	public String getPersonIdAsString(Cursor cursor) {
		return cursor.getString(idIdx);
	}

	public long getPersonId(Cursor cursor) {
		return cursor.getLong(idIdx);
	}

	public PersonHelper setTextPersonName(TextView view, Cursor cursor) {
		return setTextWithIdx(view, cursor, nameIdx);
	}

	public PersonHelper setTextPersonPhone(TextView view, Cursor cursor) {
		return setTextWithIdx(view, cursor, phoneIdx);
	}

	public String getPersonPhone(Cursor cursor) {
		return cursor.getString(phoneIdx);
	}

	public static ContentValues getContentValues(Person user) {
		ContentValues initialValues = new ContentValues();
		if (user.id > -1) {
			initialValues.put(PersonColumns.KEY_ID, Long.valueOf(user.id));
		}
		initialValues.put(PersonColumns.KEY_NAME, user.name);
		initialValues.put(PersonColumns.KEY_PHONE, user.phone);
		return initialValues;
	}

}
