package eu.ttbox.geoping.service.encoder.params;

/**
 * {link
 * http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java.lang/Long.java
 * .html}  
 */
public class LongEncoded {

    private static final long MULTMIN_RADIX_TEN = Long.MIN_VALUE / 10;
    private static final long N_MULTMAX_RADIX_TEN = -Long.MAX_VALUE / 10;

    public static final int MAX_RADIX = IntegerEncoded.MAX_RADIX;

    public static String toString(long i, int radix) {
        if (radix < IntegerEncoded.MIN_RADIX || radix > IntegerEncoded.MAX_RADIX)
            radix = 10;
        if (radix == 10)
            return Long.toString(i);
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = IntegerEncoded.ALPHABET[(int) (-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = IntegerEncoded.ALPHABET[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (65 - charPos));
    }

    public static Long valueOf(String s, int radix) throws NumberFormatException {
        return Long.valueOf(parseLong(s, radix));
    }

    public static long parseLong(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < IntegerEncoded.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than IntegerEncoded.MIN_RADIX");
        }
        if (radix > IntegerEncoded.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than IntegerEncoded.MAX_RADIX");
        }

        long result = 0;
        boolean negative = false;
        int i = 0, max = s.length();
        long limit;
        long multmin;
        int digit;

        if (max > 0) {
            if (s.charAt(0) == '-') {
                negative = true;
                limit = Long.MIN_VALUE;
                i++;
            } else {
                limit = -Long.MAX_VALUE;
            }
            if (radix == 10) {
                multmin = negative ? MULTMIN_RADIX_TEN : N_MULTMAX_RADIX_TEN;
            } else {
                multmin = limit / radix;
            }
            if (i < max) {
                digit = IntegerEncoded.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw invalidLong(s);
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = IntegerEncoded.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw invalidLong(s);
                }
                if (result < multmin) {
                    throw invalidLong(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw invalidLong(s);
                }
                result -= digit;
            }
        } else {
            throw invalidLong(s);
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else { /* Only got "-" */
                throw invalidLong(s);
            }
        } else {
            return -result;
        }
    }

    private static NumberFormatException invalidLong(String s) {
        throw new NumberFormatException("Invalid long: \"" + s + "\"");
    }

}
