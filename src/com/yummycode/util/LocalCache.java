package com.yummycode.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class LocalCache
{
    private static Account account = null;
    
    public static void cacheUser(JSONObject response) {
        try {
            JSONObject jsonAccount = response.getJSONObject("account");
            
            String fullname = jsonAccount.getString("fullname");
            String email = jsonAccount.getString("email");
            
            String sex = "Male";
            if(jsonAccount.getString("sex").equals("1")) {
                sex = "Female";
            }
            
            String age = jsonAccount.getString("age");
            String description = jsonAccount.getString("description");
            
            account = new Account(fullname, email, sex, age, description);
            
            JSONArray jsonRequests = jsonAccount.getJSONArray("requests");
            ArrayList<Request> requests = new ArrayList<Request>();
            
            for (int i = 0; i < jsonRequests.length(); i++) {
                JSONObject request = jsonRequests.getJSONObject(i);

                Request newRequest = new Request(request.getString("RequestID"), request.getString("Account"), request.getString("Type"),
                                                 request.getString("Range"), request.getString("Description"), request.getString("Visibility"),
                                                 request.getString("Longitude"), request.getString("Latitude"));
                
                requests.add(newRequest);
            }
            
            account.setRequests(requests);
        }
        catch (JSONException e) {
            Log.e("SocialMe", "Error while storing account information in local cache");
            e.printStackTrace();
        }
    }
    
    public static void updateUserInfo(JSONObject response) {
        try {
            JSONObject jsonAccount = response.getJSONObject("account");
            
            String fullname = jsonAccount.getString("fullname");
            String email = jsonAccount.getString("email");
            
            String sex = "Male";
            if(jsonAccount.getString("sex").equals("1")) {
                sex = "Female";
            }
            
            String age = jsonAccount.getString("age");
            String description = jsonAccount.getString("description");
            
            ArrayList<Request> requests = account.getRequests();
            
            account = new Account(fullname, email, sex, age, description);
            
            account.setRequests(requests);
        }
        catch (JSONException e) {
            Log.e("SocialMe", "Error while storing account information in local cache");
            e.printStackTrace();
        }
    }
    
    public static void clearCache() {
        LocalCache.account = null;
    }
    
    public static void removeRequestByID(String id) {
        ArrayList<Request> requests = account.getRequests();
        
        for(int i = 0; i < requests.size(); i++) {
            if(requests.get(i).getId().equalsIgnoreCase(id)) {
                requests.remove(i);
                break;
            }
        }
    }
    
    public static void addRequest(Request newRequest) {
        account.getRequests().add(newRequest);
    }
    
    public static Account getAccount() {
        return account;
    }

    public static void setEmail(Account account) {
        LocalCache.account = account;
    }
}
