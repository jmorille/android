package eu.ttbox.geoping.service.encoder.params;

import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageTypeEnum;



public class SmsType {

    public final String dbFieldName;
    public final SmsMessageTypeEnum wantedWriteType;

    private SmsType(String dbColumn, SmsMessageTypeEnum wantedWriteType) {
        this.dbFieldName = dbColumn;
        this.wantedWriteType = wantedWriteType;
    }

    public static SmsType multiType(String dbColumn, SmsMessageTypeEnum wantedWriteType) {
        return new SmsType(dbColumn, wantedWriteType);
    }



}
