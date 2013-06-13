package eu.ttbox.geoping.core;

import android.content.Context;
import android.widget.Toast;
import eu.ttbox.geoping.R;

public class NotifToasts {

	@Deprecated
    public static void showToastSendGeoPingRequest(Context context, String phone) {
//        final String formatStr = context.getResources().getString(R.string.toast_notif_sended_geoping_request, phone);
//        Toast.makeText(context, formatStr, Toast.LENGTH_SHORT).show();
    }

    public static void showToastSendGeoPingResponse(Context context, String phone) {
        final String formatStr = context.getResources().getString(R.string.toast_notif_sended_geoping_response, phone);
        Toast.makeText(context, formatStr, Toast.LENGTH_SHORT).show();
    }
    
    public static void showBackupRestored(Context context ) { 
        Toast.makeText(context, R.string.toast_notif_backup_restored, Toast.LENGTH_SHORT).show();
    }

//    public static void validateMissingPhone(Context context ) {
//        Toast.makeText(context, "Missing phone", Toast.LENGTH_SHORT).show();
//    }
}
