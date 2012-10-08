package eu.ttbox.geoping.test.core.phone;

import eu.ttbox.geoping.core.PhoneNumberUtils;
import android.test.AndroidTestCase;
import android.util.Log;

public class PhoneNumberUtilsTest extends AndroidTestCase {

    private static final String TAG = "PhoneNumberUtilsTest";

    public void testNormalizeNumber() {
        String[] phoneNumbers = new String[] { //
        "06 01 02 03 04", //
                "+336 01 02 03 04" };
        for (String phoneNumber : phoneNumbers) {
            String normalizePhone = PhoneNumberUtils.normalizeNumber(phoneNumber);
            Log.d(TAG, String.format("Phone %s normalize to %s", phoneNumber, normalizePhone));
        }
    }


    public void testMinMatch() { 
            String[] phoneNumbers = new String[] { //
            "06 01 02 03 04", //
                    "+336 01 02 03 04" };
            for (String phoneNumber : phoneNumbers) {
                String normalizePhone = PhoneNumberUtils.toCallerIDMinMatch(phoneNumber);
                Log.d(TAG, String.format("Phone %s MinMatch to %s", phoneNumber, normalizePhone));
            }
    }

}
