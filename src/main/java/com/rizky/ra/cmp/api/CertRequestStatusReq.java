package com.rizky.ra.cmp.api;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

public class CertRequestStatusReq {
    @NotEmpty
    @Length(max = 50)
    String id;
    
    boolean status=false;

    
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    

}
