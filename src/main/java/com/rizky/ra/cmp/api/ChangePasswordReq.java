package com.rizky.ra.cmp.api;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;



public class ChangePasswordReq {
    @NotEmpty
    @Length(max = 40)
    String oldPassword,newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    

}
