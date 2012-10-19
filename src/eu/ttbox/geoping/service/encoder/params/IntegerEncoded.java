package eu.ttbox.geoping.service.encoder.params;

/**
 * {link
 * http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java.lang/Integer
 * .java.html}
 * 
 * 
 */
public class IntegerEncoded {

    /**
     * All possible chars for representing a number as a String
     */
   public final static char[] ALPHABET = { //
    '0', '1', '2', '3', '4', '5', //
            '6', '7', '8', '9', 'a', 'b', //
            'c', 'd', 'e', 'f', 'g', 'h', //
            'i', 'j', 'k', 'l', 'm', 'n', //
            'o', 'p', 'q', 'r', 's', 't', //
            'u', 'v', 'w', 'x', 'y', 'z' //
            , 'A', 'B', //
            'C', 'D', 'E', 'F', 'G', 'H', //
            'I', 'J', 'K', 'L', 'M', 'N', //
            'O', 'P', 'Q', 'R', 'S', 'T', //
            'U', 'V', 'W', 'X', 'Y', 'Z' //
    };

    /**
     * The minimum radix used for conversions between characters and integers.
     */
    public static final int MIN_RADIX = 2;

    /**
     * The maximum radix used for conversions between characters and integers.
     */
    public static final int MAX_RADIX = ALPHABET.length;

    public static String toString(int i, int radix) {

        if (radix < MIN_RADIX || radix > MAX_RADIX)
            radix = 10;

        /* Use the faster version */
        if (radix == 10) {
            return Integer.toString(i);
        }

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = ALPHABET[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = ALPHABET[-i];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (33 - charPos));
    }
    
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return Integer.valueOf(parseInt(s, radix));
        }

    public static int parseInt(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < IntegerEncoded.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than IntegerEncoded.MIN_RADIX");
        }

        if (radix > IntegerEncoded.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than IntegerEncoded.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, max = s.length();
        int limit;
        int multmin;
        int digit;

        if (max > 0) {
            if (s.charAt(0) == '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw invalidInt(s);
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit =  digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw invalidInt(s);
                }
                if (result < multmin) {
                    throw invalidInt(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw invalidInt(s);
                }
                result -= digit;
            }
        } else {
            throw invalidInt(s);
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else { /* Only got "-" */
                throw invalidInt(s);
            }
        } else {
            return -result;
        }
    }

    public static int digit(int codePoint, int radix) {
        if (radix < MIN_RADIX || radix > MAX_RADIX) {
            return -1;
        }
        if (codePoint < 128) {
            // Optimized for ASCII
            int result = -1;
            if ('0' <= codePoint && codePoint <= '9') {
                result = codePoint - '0';
            } else if ('a' <= codePoint && codePoint <= 'z') {
                result = 10 + (codePoint - 'a');
            } else if ('A' <= codePoint && codePoint <= 'Z') {
                result = 36 + (codePoint - 'A');
            }
            return result < radix ? result : -1;
        }
        return Character.digit(codePoint, radix);
    }
    
    private static NumberFormatException invalidInt(String s) {
        throw new NumberFormatException("Invalid Int: \"" + s + "\"");
    }

}
