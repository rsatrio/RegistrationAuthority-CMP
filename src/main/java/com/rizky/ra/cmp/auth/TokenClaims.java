package com.rizky.ra.cmp.auth;

import java.security.Principal;

public class TokenClaims implements Principal{
    
    String email,role,id;

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String getName() {
      
        return "Email";
    }
    
    

}
