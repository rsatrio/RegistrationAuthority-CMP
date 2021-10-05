package com.rizky.ra.cmp.api;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

public class AcceptEnrollCertReq {
    @NotEmpty
    @Length(max=50)
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    

}
