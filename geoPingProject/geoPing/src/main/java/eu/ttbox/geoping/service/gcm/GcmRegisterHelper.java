package eu.ttbox.geoping.service.gcm;


import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.deviceinfoendpoint.Deviceinfoendpoint;

public class GcmRegisterHelper {

    public static final String PROJECT_NUMBER = "493878400848";

    public static Deviceinfoendpoint getDeviceinfoendpoint(Context context) {
        HttpRequestInitializer httpRequestInitializer =  new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) {
            }
        };
        return getDeviceinfoendpoint(context, httpRequestInitializer);
    }

    public static Deviceinfoendpoint getDeviceinfoendpoint(Context context, HttpRequestInitializer credential) {
        Deviceinfoendpoint.Builder endpointBuilder = new Deviceinfoendpoint.Builder(
                AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
                credential);
        endpointBuilder.setApplicationName(context.getString(R.string.app_name));
        Deviceinfoendpoint  endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
        return endpoint;
    }
}
