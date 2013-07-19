package eu.ttbox.geoping.encoder.model;

public class MessageParamType {

    public final String dbFieldName;
    public final MessageParamTypeEncodingEnum wantedWriteType;

    private MessageParamType(String dbColumn, MessageParamTypeEncodingEnum wantedWriteType) {
        this.dbFieldName = dbColumn;
        this.wantedWriteType = wantedWriteType;
    }

    public static MessageParamType multiType(String dbColumn, MessageParamTypeEncodingEnum wantedWriteType) {
        return new MessageParamType(dbColumn, wantedWriteType);
    }
}
