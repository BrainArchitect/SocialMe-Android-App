package com.yummycode.api;

import org.json.JSONObject;

public interface SocialMeAPI
{
    public JSONObject smLogin(String email, String password);
    public JSONObject smLogout();
    public JSONObject smRegister(String email, String fullname, String password, String sex, String age, String description);
    public JSONObject smUpdateProfile(String email, String fullname, String password, String sex, String age, String description);
    public JSONObject smResetPassword(String email);
    public JSONObject smPublishRequest(String email, String type, String range, String description, String visibility, String longitude, String latitude);
    public JSONObject smDeleteRequest(String id);
    public JSONObject smRetrieveRequests(String email);
    public JSONObject smRetrieveProfile(String email);
    public JSONObject smRetrieveMatches(String email);
}
