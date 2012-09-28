package eu.ttbox.geoping.service.slave;

public enum GeopingNotifSlaveTypeEnum {

    PAIRING, //
    GEOPING_REQUEST_CONFIRM, //
    GEOPING_NOTIF;//

    public static GeopingNotifSlaveTypeEnum getByOrdinal(int notifTypeOrdinal) {
        if (notifTypeOrdinal < 0 || notifTypeOrdinal > GeopingNotifSlaveTypeEnum.values().length) {
            return null;
        }
        return GeopingNotifSlaveTypeEnum.values()[notifTypeOrdinal];
    }

}
