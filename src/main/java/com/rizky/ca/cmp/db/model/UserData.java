package com.rizky.ca.cmp.db.model;

import java.sql.Blob;
import java.util.Date;

public class UserData {
    
    private String name,email,password,salt,country,status,user_id,user_type;
    Blob identity,photo;
    Date creation_date;
    boolean active;
    
    int login_count,failed_login;
    
    
    
    
    public int getLogin_count() {
        return login_count;
    }
    public void setLogin_count(int login_count) {
        this.login_count = login_count;
    }
    public int getFailed_login() {
        return failed_login;
    }
    public void setFailed_login(int failed_login) {
        this.failed_login = failed_login;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getUser_id() {
        return user_id;
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    public String getUser_type() {
        return user_type;
    }
    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }
    public Blob getIdentity() {
        return identity;
    }
    public void setIdentity(Blob identity) {
        this.identity = identity;
    }
    public Blob getPhoto() {
        return photo;
    }
    public void setPhoto(Blob photo) {
        this.photo = photo;
    }
    public Date getCreation_date() {
        return creation_date;
    }
    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    
    

}
