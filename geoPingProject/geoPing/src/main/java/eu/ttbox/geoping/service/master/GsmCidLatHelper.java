package eu.ttbox.geoping.service.master;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class GsmCidLatHelper {

    
    
    protected static final String TAG = "GsmCidLatHelper";

    public static void displayMap(final Context context, final int cellID, final int lac)   {
        Log.i(TAG, "Need Geocoding CidLat : " + cellID + " / " + lac);
        Thread thread = new Thread()
        {
            
            @Override
            public void run() {
                try {
                String geoUri = geocodingCidLat(cellID, lac);
                Log.i(TAG, "Geocoding CidLat : " + geoUri);
                if (geoUri!=null) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geoUri));
                    context.startActivity(intent);
                    
                }  
                } catch (Exception e) {
                    Log.e(TAG, "Error in geocoding CidLat : " + e.getMessage(), e);
                }
            }
        };
        thread.start();
        
    }

    public static String geocodingCidLat(int cellID, int lac) throws Exception {
        String resultGeoUrl = null;
        String urlString = "http://www.google.com/glm/mmap";

        // ---open a connection to Google Maps API---
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.connect();

        // ---write some custom data to Google Maps API---
        OutputStream outputStream = httpConn.getOutputStream();
        writeData(outputStream, cellID, lac);

        // ---get the response---
        InputStream inputStream = httpConn.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // ---interpret the response obtained---
        dataInputStream.readShort();
        dataInputStream.readByte();
        int code = dataInputStream.readInt();
        if (code == 0) {
            double lat = (double) dataInputStream.readInt() / 1000000D;
            double lng = (double) dataInputStream.readInt() / 1000000D;
            dataInputStream.readInt();
            dataInputStream.readInt();
            dataInputStream.readUTF();

            // ---display Google Maps---
            String uriString = "geo:" + lat + "," + lng;
            resultGeoUrl = uriString;
         } 
        return resultGeoUrl;
    }

    private static void  writeData(OutputStream out, int cellID, int lac) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cellID);
        dataOutputStream.writeInt(lac);

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }
}
