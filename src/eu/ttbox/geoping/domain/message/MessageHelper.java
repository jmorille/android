package eu.ttbox.geoping.domain.message;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.TextView;
import eu.ttbox.geoping.domain.Message;
import eu.ttbox.geoping.domain.message.MessageDatabase.MessageColumns;

public class MessageHelper {

    boolean isNotInit = true;
    public int idIdx = -1;
    public int nameIdx = -1;
    public int phoneIdx = -1;
    public int colorIdx = -1;

    public MessageHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(MessageColumns.COL_ID);
        nameIdx = cursor.getColumnIndex(MessageColumns.COL_NAME);
        phoneIdx = cursor.getColumnIndex(MessageColumns.COL_PHONE);
        colorIdx = cursor.getColumnIndex(MessageColumns.COL_COLOR);
        isNotInit = false;
        return this;
    }

    public Message getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        Message user = new Message();
        user.setId(idIdx > -1 ? cursor.getLong(idIdx) : -1);
        user.setName(nameIdx > -1 ? cursor.getString(nameIdx) : null);
        user.setPhone(phoneIdx > -1 ? cursor.getString(phoneIdx) : null);
        user.setColor(colorIdx > -1 ? cursor.getInt(colorIdx) : null);
        return user;
    }

    private MessageHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
        view.setText(cursor.getString(idx));
        return this;
    }

    public MessageHelper setTextMessageId(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, idIdx);
    }

    public String getMessageIdAsString(Cursor cursor) {
        return cursor.getString(idIdx);
    }

    public long getMessageId(Cursor cursor) {
        return cursor.getLong(idIdx);
    }

    public int getMessageColor(Cursor cursor) {
        return cursor.getInt(colorIdx);
    }

    public MessageHelper setTextMessageName(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, nameIdx);
    }

    public MessageHelper setTextMessagePhone(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, phoneIdx);
    }

    public String getMessagePhone(Cursor cursor) {
        return cursor.getString(phoneIdx);
    }

    public static ContentValues getContentValues(Message user) {
        ContentValues initialValues = new ContentValues();
        if (user.id > -1) {
            initialValues.put(MessageColumns.COL_ID, Long.valueOf(user.id));
        }
        initialValues.put(MessageColumns.COL_NAME, user.name);
        initialValues.put(MessageColumns.COL_PHONE, user.phone);
        return initialValues;
    }

}
