package com.yummycode.util;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;


public class SingletonHttpClient
{
    private static DefaultHttpClient httpClient = null;

    public static DefaultHttpClient getHttpclient() {
        if (httpClient == null) {
            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
            SingletonHttpClient.setHttpclient(client);
        }
        
        return httpClient;
    }

    public static void setHttpclient(DefaultHttpClient httpClient) {
        SingletonHttpClient.httpClient = httpClient;
    }
}
