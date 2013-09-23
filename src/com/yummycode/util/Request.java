package com.yummycode.util;

public class Request
{
    private String id = null;
    private String account = null;
    private String type = null;
    private String range = null;
    private String description = null;
    private String visibility = null;
    private String longitude = null;
    private String latitude = null;
    
    public Request() {
    }
    
    public Request(String id, String account, String type, String range, String description, String visibility, String longitude, String latitude) {
        this.id = id;
        this.account = account;
        this.type = type;
        this.range = range;
        this.description = description;
        this.visibility = visibility;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
