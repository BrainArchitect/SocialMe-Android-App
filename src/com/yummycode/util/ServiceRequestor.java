package com.yummycode.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class ServiceRequestor
{
    public ServiceRequestor() {
    }

    public JSONObject request(String url, List<NameValuePair> params) {
        JSONObject jsonResponse = null;
        
        try {
            // Build a POST request with the provided parameters
            HttpPost postRequest = new HttpPost(url);
            postRequest.setEntity(new UrlEncodedFormEntity(params));

            // Send the request and get the response
            DefaultHttpClient client = SingletonHttpClient.getHttpclient();
            HttpResponse response = client.execute(postRequest);
            HttpEntity responseEntity = response.getEntity();
            InputStream responseContent = responseEntity.getContent();
            
            // Read the response from the stream and store it in a string
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseContent, "ISO-8859-1"), 4);
            StringBuilder sbResponse = new StringBuilder();
            String line;
            
            while((line = responseReader.readLine()) != null) {
                sbResponse.append(line);
            }
            
            responseContent.close();
            
            String strResponse = sbResponse.toString();
            
            // Log response from server to LogCat
            Log.i("SocialMe", strResponse);
            
            jsonResponse = new JSONObject(strResponse);
        }
        catch (UnsupportedEncodingException e) {
            Log.e("SocialMe", "Unsupported encoding while sending POST request");
            e.printStackTrace();
        }
        catch (ClientProtocolException e) {
            Log.e("SocialMe", "Client protocol exception while sending POST request");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.e("SocialMe", "IO exception while sending POST request");
            e.printStackTrace();
        }
        catch (JSONException e) {
            Log.e("SocialMe", "Error parsing JSON response to a JSON object");
            e.printStackTrace();
        }

        return jsonResponse;
    }
}
