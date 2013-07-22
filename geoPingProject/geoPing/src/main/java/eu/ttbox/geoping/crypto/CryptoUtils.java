package eu.ttbox.geoping.crypto;

import java.util.UUID;

import eu.ttbox.geoping.encoder.params.helper.LongEncoded;

public class CryptoUtils {

    private CryptoUtils() {
    }

    public static String generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        StringBuilder sb = new StringBuilder(23);
        // Most
        long mostSigBits = Math.abs(uuid.getMostSignificantBits());
        String msbStr = LongEncoded.toString(mostSigBits, LongEncoded.MAX_RADIX);
        appendAndPadToSize(sb, msbStr, 11);
        // Separator
//        sb.append('-');
        // Least
        long leastSigBits = Math.abs(uuid.getLeastSignificantBits());
        String lsbStr = LongEncoded.toString(leastSigBits, LongEncoded.MAX_RADIX);
        appendAndPadToSize(sb, lsbStr, 11);
        // Id
     
        return sb.toString();
    }

    private static void appendAndPadToSize(StringBuilder builder, String msbStr, int expectedSize) {
        builder.append(msbStr);
        if (msbStr.length() < expectedSize) {
            int diff = expectedSize - msbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        } 
    }
}
