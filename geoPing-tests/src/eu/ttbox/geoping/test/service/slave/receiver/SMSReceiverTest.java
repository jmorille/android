package eu.ttbox.geoping.test.service.slave.receiver;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
import eu.ttbox.geoping.service.slave.receiver.SMSReceiver;

public class SMSReceiverTest extends AndroidTestCase {

    private SMSReceiver mReceiver;
    private TestContext mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mReceiver = new SMSReceiver();
        mContext = new TestContext();
    }

    
    public void testStartActivity()
    {
        Intent intent = new Intent(SMSReceiver.ACTION_RECEIVE_SMS);
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, "01234567890");
     //TODO   intent.putExtra(SMSReceiver.EXTRA_PDUS, value);
        
        
        mReceiver.onReceive(mContext, intent);        
        assertEquals(1, mContext.getReceivedIntents().size());
        assertNull(mReceiver.getResultData());

        Intent receivedIntent = mContext.getReceivedIntents().get(0);
        assertNull(receivedIntent.getAction());
        assertEquals("01234567890", receivedIntent.getStringExtra("phoneNum"));
        assertTrue((receivedIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0);
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
