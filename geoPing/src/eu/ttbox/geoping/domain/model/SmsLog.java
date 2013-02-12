package eu.ttbox.geoping.domain.model;

import java.util.Date;

import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;

public class SmsLog {

	public static final long UNSET_TIME = -1l;

	public long id =  AppConstants.UNSET_ID;
	public long time = UNSET_TIME;
	public SmsLogTypeEnum smsLogType;
	public SmsMessageActionEnum action;
	public String phone;
	public String message; 
	
	public SmsLog setId(long id) {
		this.id = id;
		return this;
	}

	public long getTime() {
		return time;
	}

	public Date getTimeAsDate() {
		if (time == UNSET_TIME) {
			return null;
		}
		Date timeAsDate = new Date(time);
		return timeAsDate;
	}

	public SmsLog setTime(long time) {
		this.time = time;
		return this;
	}
 
	public SmsLog setMessage(String name) {
		this.message = name;
		return this;
	}
	public SmsLog setPhone(String phone) {
		this.phone = phone;
		return this;
	}

  
	public SmsLog setSmsLogType(SmsLogTypeEnum smsLogType) {
		this.smsLogType = smsLogType;
		return this;
	}

	public SmsLog setAction(SmsMessageActionEnum action) {
		this.action = action;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("SmsLog [");
		sb.append("id=").append(id)//
				.append(", phone=").append(phone)//
				.append(", action=").append(action)//
				.append(", logType=").append(smsLogType)//
				.append(", message=").append(message)//
				// .append(", time=").append(time) //
				.append(", time=").append(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", time));

		sb.append("]");
		return sb.toString();
	}

}
