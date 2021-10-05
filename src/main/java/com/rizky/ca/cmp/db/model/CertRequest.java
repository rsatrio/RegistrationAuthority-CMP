package com.rizky.ca.cmp.db.model;

import java.sql.Blob;
import java.util.Date;

public class CertRequest {
    
    String id,user_id,status,email,common_name,country,state,keypass;
    Date request_date;
    Blob identity;
    
    
    public String getKeypass() {
        return keypass;
    }
    
    public void setKeypass(String keypass) {
        this.keypass = keypass;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUser_id() {
        return user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCommon_name() {
        return common_name;
    }
    public void setCommon_name(String common_name) {
        this.common_name = common_name;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public Date getRequest_date() {
        return request_date;
    }
    public void setRequest_date(Date request_date) {
        this.request_date = request_date;
    }
    public Blob getIdentity() {
        return identity;
    }
    public void setIdentity(Blob identity) {
        this.identity = identity;
    }
    
    

}
