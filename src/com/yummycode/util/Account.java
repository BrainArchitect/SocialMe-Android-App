package com.yummycode.util;

import java.util.ArrayList;

public class Account
{
    private String email = null;
    private String fullname = null;
    private String sex = null;
    private String age = null;
    private String description = null;
    private ArrayList<Request> requests = new ArrayList<Request>();
    
    public Account() {
    }
    
    public Account(String fullname, String email, String sex, String age, String description) {
        this.fullname = fullname;
        this.email = email;
        this.sex = sex;
        this.age = age;
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<Request> requests) {
        this.requests = requests;
    }
}
