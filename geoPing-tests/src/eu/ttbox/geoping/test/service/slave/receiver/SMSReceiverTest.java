package eu.ttbox.geoping.test.service.slave.receiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import eu.ttbox.geoping.service.slave.receiver.SMSReceiver;
import eu.ttbox.geoping.test.service.encoder.SmsMessageEncoderHelperTest;

public class SMSReceiverTest extends AndroidTestCase {

    private SMSReceiver mReceiver;
    private TestContext mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mReceiver = new SMSReceiver();
        mContext = new TestContext();
    }

    public void tesBadSms() {
        String[] badMessages = new String[] { "Coucou couc", //
                "Tu connais l'appli GeoPing?", //
                "WRY", //
                "geoPing?", //
                "geoPing?W", //
                "geoPing?W(ssaa)" //
        };
        for (String msg : badMessages) {
            Intent intentFakeSms = createFakeSms(getContext(), "01234567890", msg);
            mReceiver.onReceive(mContext, intentFakeSms);
            assertEquals(0, mContext.getReceivedIntents().size());
        }
    }

    public void testStartActivity() {

        Intent intentFakeSms = createFakeSms(getContext(), "01234567890", SmsMessageEncoderHelperTest.MSG_ENCRYPED_LOC);

        mReceiver.onReceive(mContext, intentFakeSms);
        assertEquals(1, mContext.getReceivedIntents().size());
        assertNull(mReceiver.getResultData());

        Intent receivedIntent = mContext.getReceivedIntents().get(0);
        assertNull(receivedIntent.getAction());
        assertEquals("01234567890", receivedIntent.getStringExtra("phoneNum"));
        // assertEquals("01234567890",
        // receivedIntent.getStringExtra(Intents.EXTRA_SMS_ACTION));
        // intent.putExtra(Intents.EXTRA_SMS_PARAMS, msg.params);
        // intent.putExtra(Intents.EXTRA_SMS_ACTION, msg.action);
        // intent.putExtra(Intents.EXTRA_SMS_PHONE, msg.phone); //

        assertTrue((receivedIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0);
    }

    private static Intent createFakeSms(Context context, String sender, String body) {
        byte[] pdu = null;
        byte[] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
        byte[] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
        int lsmcs = scBytes.length;
        byte[] dateBytes = new byte[7];
        Calendar calendar = new GregorianCalendar();
        dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
        dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
        dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
        dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
        dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
        dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
        dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(lsmcs);
            bo.write(scBytes);
            bo.write(0x04);
            bo.write((byte) sender.length());
            bo.write(senderBytes);
            bo.write(0x00);
            bo.write(0x00); // encoding: 0 for default 7bit
            bo.write(dateBytes);
            try {
                String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
                Class cReflectedNFCExtras = Class.forName(sReflectedClassName);
                Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod("stringToGsm7BitPacked", new Class[] { String.class });
                stringToGsm7BitPacked.setAccessible(true);
                byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null, body);
                bo.write(bodybytes);
            } catch (Exception e) {
            }

            pdu = bo.toByteArray();
        } catch (IOException e) {
        }

        Intent intent = new Intent();
        intent.setClassName("com.android.mms", "com.android.mms.transaction.SmsReceiverService");
        intent.setAction("android.provider.Telephony.SMS_RECEIVED");
        intent.putExtra("pdus", new Object[] { pdu });
        intent.putExtra("format", "3gpp");
        // context.startService(intent);
        return intent;
    }

    private static byte reverseByte(byte b) {
        return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
    }

    public class TestContext extends MockContext {
        private List<Intent> mReceivedIntents = new ArrayList<Intent>();

        @Override
        public String getPackageName() {
            return "eu.ttbox.geoping.test";
        }

        @Override
        public void startActivity(Intent xiIntent) {
            mReceivedIntents.add(xiIntent);
        }

        public List<Intent> getReceivedIntents() {
            return mReceivedIntents;
        }
    }
}
