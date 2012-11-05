package eu.ttbox.geoping.domain.model;

import android.content.ContentValues;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;

public enum SmsLogTypeEnum {

	SEND, RECEIVE;

	public int getCode() {
		return ordinal();
	}

	public static SmsLogTypeEnum getByCode(int code) {
		if (code < 0) {
			return null;
		}
		return SmsLogTypeEnum.values()[code];
	}

	public ContentValues writeTo(ContentValues values) {
		ContentValues val = values != null ? values : new ContentValues();
		val.put(SmsLogColumns.COL_SMSLOG_TYPE, getCode());
		return val;
	}
}
