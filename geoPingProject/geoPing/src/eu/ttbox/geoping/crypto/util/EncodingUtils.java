package eu.ttbox.geoping.crypto.util;


/**
 * Static helper for encoding data.
 * <p>
 * For internal use only.
 *
 * @author Keith Donald
 */
public class EncodingUtils {

    /**
     * Combine the individual byte arrays into one array.
     */
    public static byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] newArray = new byte[length];
        int destPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, newArray, destPos, array.length);
            destPos += array.length;
        }
        return newArray;
    }

    /**
     * Extract a sub array of bytes out of the byte array.
     * @param array the byte array to extract from
     * @param beginIndex the beginning index of the sub array, inclusive
     * @param endIndex the ending index of the sub array, exclusive
     */
    public static byte[] subArray(byte[] array, int beginIndex, int endIndex) {
        int length = endIndex - beginIndex;
        byte[] subarray = new byte[length];
        System.arraycopy(array, beginIndex, subarray, 0, length);
        return subarray;
    }

    private EncodingUtils() {
    }

}
