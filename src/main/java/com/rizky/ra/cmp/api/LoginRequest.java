package com.rizky.ra.cmp.api;

import com.rizky.ra.cmp.validation.CheckStringContent;

public class LoginRequest {
    
    String username,password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}
