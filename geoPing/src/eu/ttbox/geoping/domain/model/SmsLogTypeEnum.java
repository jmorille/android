package eu.ttbox.geoping.domain.model;

import android.content.ContentValues;
import android.support.v4.util.SparseArrayCompat;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;

public enum SmsLogTypeEnum {

	SEND_REQ(0), //
	SEND_ERROR(1), //
	SEND_ACK(2), //
	SEND_DELIVERY_ACK(3),// 
	RECEIVE(-1);

	public final int code;

	// ===========================================================
	// Constructors
	// ===========================================================

	SmsLogTypeEnum(int code) {
		this.code = code;
	}

	static {
		SmsLogTypeEnum[] enumValues = SmsLogTypeEnum.values();
		SparseArrayCompat<SmsLogTypeEnum> codeMapping = new SparseArrayCompat<SmsLogTypeEnum>(enumValues.length);
		for (SmsLogTypeEnum type : enumValues) {
			int key = type.code;
			if (codeMapping.indexOfKey(key)>=0) {
				throw new RuntimeException("Duplicate SmsLogTypeEnum for code ["+key+"]");
			}
			codeMapping.put(key, type);
		}
		byCodes = codeMapping;
	}
	// ===========================================================
	// Static Accessors
	// ===========================================================

	private final static SparseArrayCompat<SmsLogTypeEnum> byCodes;

	public static SmsLogTypeEnum getByCode(int code) {
		SmsLogTypeEnum codeVal = byCodes.get(code);
		return codeVal;
	}

	// ===========================================================
	// Accessors
	// ===========================================================

	public int getCode() {
		return code;
	}

	public ContentValues writeTo(ContentValues values) {
		ContentValues val = values != null ? values : new ContentValues();
		val.put(SmsLogColumns.COL_SMSLOG_TYPE, getCode());
		return val;
	}
}
