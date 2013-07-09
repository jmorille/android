package eu.ttbox.geoping.service.slave.receiver;

public enum AuthorizePhoneTypeEnum {
    NO, // 0
    YES, // 1
    NEVER, // 2
    ALWAYS; // 3

    public static AuthorizePhoneTypeEnum getByOrdinal(int typeOrdinal) {
        if (typeOrdinal < 0 || typeOrdinal > AuthorizePhoneTypeEnum.values().length) {
            return null;
        }
        return AuthorizePhoneTypeEnum.values()[typeOrdinal];
    }
}
