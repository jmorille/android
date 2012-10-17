package eu.ttbox.geoping.service.slave.receiver;

import eu.ttbox.geoping.service.slave.BackgroudLocService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, BackgroudLocService.class);
            i.setAction(Intent.ACTION_BOOT_COMPLETED);
//            context.startService(i);
        }
        
        // This is where you start your service
//        startService(new Intent(aContext, MyService.class);
    }
    
}
