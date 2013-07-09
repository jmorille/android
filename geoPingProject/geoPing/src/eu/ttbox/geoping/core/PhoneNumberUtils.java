package eu.ttbox.geoping.core;

import java.util.regex.Pattern;

import android.content.Context;
import android.util.SparseIntArray;

/**
 * Various utilities for dealing with phone number strings.
 * 
 * @see android.telephony.PhoneNumberUtils
 * 
 */
public class PhoneNumberUtils {

    /*
     * Special characters
     * 
     * (See "What is a phone number?" doc) 'p' --- GSM pause character, same as
     * comma 'n' --- GSM wild character 'w' --- GSM wait character
     */
    public static final char PAUSE = ',';
    public static final char WAIT = ';';
    public static final char WILD = 'N';

    /*
     * Calling Line Identification Restriction (CLIR)
     */
    private static final String CLIR_ON = "*31#+";
    private static final String CLIR_OFF = "#31#+";

    /*
     * TOA = TON + NPI See TS 24.008 section 10.5.4.7 for details. These are the
     * only really useful TOA values
     */
    public static final int TOA_International = 0x91;
    public static final int TOA_Unknown = 0x81;

    static final String LOG_TAG = "PhoneNumberUtils";
    private static final boolean DBG = false;

    /*
     * global-phone-number = ["+"] 1*( DIGIT / written-sep ) written-sep =
     * ("-"/".")
     */
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN = Pattern.compile("[\\+]?[0-9.-]+");

    /** True if c is ISO-LATIN characters 0-9 */
    public static boolean isISODigit(char c) {
        return c >= '0' && c <= '9';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # */
    public final static boolean is12Key(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD */
    public final static boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD;
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , + (no WILD) */
    public final static boolean isReallyDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD, WAIT, PAUSE */
    public final static boolean isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD || c == WAIT || c == PAUSE;
    }

    /**
     * This any anything to the right of this char is part of the post-dial
     * string (eg this is PAUSE or WAIT)
     */
    public final static boolean isStartsPostDial(char c) {
        return c == PAUSE || c == WAIT;
    }

    private static boolean isPause(char c) {
        return c == 'p' || c == 'P';
    }

    private static boolean isToneWait(char c) {
        return c == 'w' || c == 'W';
    }

    /** Returns true if ch is not dialable or alpha char */
    private static boolean isSeparator(char ch) {
        return !isDialable(ch) && !(('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z'));
    }

    /**
     * Compare phone numbers a and b, return true if they're identical enough
     * for caller ID purposes.
     */
    public static boolean compare(String a, String b) {
        return android.telephony.PhoneNumberUtils.compare(a, b);
    }

    /**
     * Compare phone numbers a and b, and return true if they're identical
     * enough for caller ID purposes. Checks a resource to determine whether to
     * use a strict or loose comparison algorithm.
     */
    public static boolean compare(Context context, String a, String b) {
        return android.telephony.PhoneNumberUtils.compare(context, a, b);
    }

    /**
     * Returns the network portion reversed. This string is intended to go into
     * an index column for a database lookup.
     * 
     * Returns null if phoneNumber == null
     */
    public static String getStrippedReversed(String phoneNumber) {
        return android.telephony.PhoneNumberUtils.getStrippedReversed(phoneNumber);
    }

    /**
     * Extracts the network address portion and canonicalizes (filters out
     * separators.) Network address portion is everything up to DTMF control
     * digit separators (pause or wait), but without non-dialable characters.
     * 
     * Please note that the GSM wild character is allowed in the result. This
     * must be resolved before dialing.
     * 
     * Returns null if phoneNumber == null
     */
    public static String extractNetworkPortion(String phoneNumber) {
        return android.telephony.PhoneNumberUtils.extractNetworkPortion(phoneNumber);
    }

    /**
     * Return true iff the network portion of <code>address</code> is, as far as
     * we can tell on the device, suitable for use as an SMS destination
     * address.
     */
    public static boolean isWellFormedSmsAddress(String address) {
        return android.telephony.PhoneNumberUtils.isWellFormedSmsAddress(address);
    }

    /**
     * Returns the rightmost MIN_MATCH (5) characters in the network portion in
     * *reversed* order
     * 
     * This can be used to do a database lookup against the column that stores
     * getStrippedReversed()
     * 
     * Returns null if phoneNumber == null
     */
    public static String toCallerIDMinMatch(String phoneNumber) {
        return android.telephony.PhoneNumberUtils.toCallerIDMinMatch(phoneNumber);
    }

    // ================ Number formatting =========================

    public static String normalizeNumber(String phoneNumber) {
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if ((i == 0 && c == '+') || PhoneNumberUtils.isISODigit(c)) {
                sb.append(c);
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    /**
     * Translates any alphabetic letters (i.e. [A-Za-z]) in the specified phone
     * number into the equivalent numeric digits, according to the phone keypad
     * letter mapping described in ITU E.161 and ISO/IEC 9995-8.
     * 
     * @return the input string, with alpha letters converted to numeric digits
     *         using the phone keypad letter mapping. For example, an input of
     *         "1-800-GOOG-411" will return "1-800-4664-411".
     */
    public static String convertKeypadLettersToDigits(String input) {
        if (input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return input;
        }

        char[] out = input.toCharArray();

        for (int i = 0; i < len; i++) {
            char c = out[i];
            // If this char isn't in KEYPAD_MAP at all, just leave it alone.
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }

        return new String(out);
    }

    /**
     * The phone keypad letter mapping (see ITU E.161 or ISO/IEC 9995-8.) TODO:
     * This should come from a resource.
     */
    private static final SparseIntArray KEYPAD_MAP = new SparseIntArray();
    static {
        KEYPAD_MAP.put('a', '2');
        KEYPAD_MAP.put('b', '2');
        KEYPAD_MAP.put('c', '2');
        KEYPAD_MAP.put('A', '2');
        KEYPAD_MAP.put('B', '2');
        KEYPAD_MAP.put('C', '2');

        KEYPAD_MAP.put('d', '3');
        KEYPAD_MAP.put('e', '3');
        KEYPAD_MAP.put('f', '3');
        KEYPAD_MAP.put('D', '3');
        KEYPAD_MAP.put('E', '3');
        KEYPAD_MAP.put('F', '3');

        KEYPAD_MAP.put('g', '4');
        KEYPAD_MAP.put('h', '4');
        KEYPAD_MAP.put('i', '4');
        KEYPAD_MAP.put('G', '4');
        KEYPAD_MAP.put('H', '4');
        KEYPAD_MAP.put('I', '4');

        KEYPAD_MAP.put('j', '5');
        KEYPAD_MAP.put('k', '5');
        KEYPAD_MAP.put('l', '5');
        KEYPAD_MAP.put('J', '5');
        KEYPAD_MAP.put('K', '5');
        KEYPAD_MAP.put('L', '5');

        KEYPAD_MAP.put('m', '6');
        KEYPAD_MAP.put('n', '6');
        KEYPAD_MAP.put('o', '6');
        KEYPAD_MAP.put('M', '6');
        KEYPAD_MAP.put('N', '6');
        KEYPAD_MAP.put('O', '6');

        KEYPAD_MAP.put('p', '7');
        KEYPAD_MAP.put('q', '7');
        KEYPAD_MAP.put('r', '7');
        KEYPAD_MAP.put('s', '7');
        KEYPAD_MAP.put('P', '7');
        KEYPAD_MAP.put('Q', '7');
        KEYPAD_MAP.put('R', '7');
        KEYPAD_MAP.put('S', '7');

        KEYPAD_MAP.put('t', '8');
        KEYPAD_MAP.put('u', '8');
        KEYPAD_MAP.put('v', '8');
        KEYPAD_MAP.put('T', '8');
        KEYPAD_MAP.put('U', '8');
        KEYPAD_MAP.put('V', '8');

        KEYPAD_MAP.put('w', '9');
        KEYPAD_MAP.put('x', '9');
        KEYPAD_MAP.put('y', '9');
        KEYPAD_MAP.put('z', '9');
        KEYPAD_MAP.put('W', '9');
        KEYPAD_MAP.put('X', '9');
        KEYPAD_MAP.put('Y', '9');
        KEYPAD_MAP.put('Z', '9');
    }

}
