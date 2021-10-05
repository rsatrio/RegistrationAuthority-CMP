package com.rizky.ca.cmp.db.model;

import java.sql.Blob;
import java.util.Date;

public class UserCertificates {
    
    String id,request_id,user_id,status,serial_number,key_length,subject_dn,email;
    Date request_date,expired_date;
    Blob pkcs12,certificate;
    
    
    
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getRequest_id() {
        return request_id;
    }
    public void setRequest_id(String request_id) {
        this.request_id = request_id;
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
    public String getSerial_number() {
        return serial_number;
    }
    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }
    public String getKey_length() {
        return key_length;
    }
    public void setKey_length(String key_length) {
        this.key_length = key_length;
    }
    public String getSubject_dn() {
        return subject_dn;
    }
    public void setSubject_dn(String subject_dn) {
        this.subject_dn = subject_dn;
    }
    public Date getRequest_date() {
        return request_date;
    }
    public void setRequest_date(Date request_date) {
        this.request_date = request_date;
    }
    public Date getExpired_date() {
        return expired_date;
    }
    public void setExpired_date(Date expired_date) {
        this.expired_date = expired_date;
    }
    public Blob getPkcs12() {
        return pkcs12;
    }
    public void setPkcs12(Blob pkcs12) {
        this.pkcs12 = pkcs12;
    }
    public Blob getCertificate() {
        return certificate;
    }
    public void setCertificate(Blob certificate) {
        this.certificate = certificate;
    }
    
    

}
