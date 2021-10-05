package com.rizky.ra.cmp.api;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

public class EnrollCertReq {
    
    @NotEmpty
    @Length(max = 50,message = "Email length maximum 50 chars")
    @Pattern(regexp = "^(.+)@(.+)$")
    String email;
    
    @NotEmpty
    @Length(max = 30,message = "Password length maximum 30 chars")
    String password;
    
    @NotEmpty
    @Length(max=40)
    String commonName,state,country,identityProof;
    
        
    public String getIdentityProof() {
        return identityProof;
    }

    public void setIdentityProof(String identityProof) {
        this.identityProof = identityProof;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    
    

}
