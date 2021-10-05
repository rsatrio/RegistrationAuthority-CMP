package com.rizky.ra.cmp.api;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

public class RegistrationRequest {

    @NotEmpty
    @Length(max = 50,message = "Email length maximum 50 chars")
    @Pattern(regexp = "^(.+)@(.+)$")
    String email;
    
    @NotEmpty
    @Length(max = 30,message = "Password length maximum 30 chars")
    String password;

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
    
    
}
