package com.yummycode.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.yummycode.util.ServiceRequestor;

public class SocialMe implements SocialMeAPI
{
//    private static final String serviceEndpoint = "http://192.168.0.18/socialme/socialme.php";
    private static final String serviceEndpoint = "http://service.yummycode.com/mobile/api/socialme.php";
    private ServiceRequestor sr;
    
    public SocialMe() {
        this.sr = new ServiceRequestor();
    }

    @Override
    public JSONObject smLogin(String email, String password) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "login"));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smLogout() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "logout"));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smRegister(String email, String fullname, String password, String sex, String age, String description) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "register"));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("fullname", fullname));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("sex", sex));
        params.add(new BasicNameValuePair("age", age));
        params.add(new BasicNameValuePair("description", description));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smUpdateProfile(String email, String fullname, String password, String sex, String age, String description) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "update_profile"));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("fullname", fullname));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("sex", sex));
        params.add(new BasicNameValuePair("age", age));
        params.add(new BasicNameValuePair("description", description));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smResetPassword(String email) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "reset_password"));
        params.add(new BasicNameValuePair("email", email));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smPublishRequest(String email, String type, String range, String description, String visibility, String longitude, String latitude) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "publish_request"));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("range", range));
        params.add(new BasicNameValuePair("description", description));
        params.add(new BasicNameValuePair("visibility", visibility));
        params.add(new BasicNameValuePair("longitude", longitude));
        params.add(new BasicNameValuePair("latitude", latitude));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smDeleteRequest(String id) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "delete_request"));
        params.add(new BasicNameValuePair("id", id));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smRetrieveRequests(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smRetrieveProfile(String email) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "get_profile"));
        params.add(new BasicNameValuePair("email", email));
        
        return sr.request(serviceEndpoint, params);
    }

    @Override
    public JSONObject smRetrieveMatches(String email) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "get_matches"));
        params.add(new BasicNameValuePair("email", email));
        
        return sr.request(serviceEndpoint, params);
    }
}
