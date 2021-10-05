package com.rizky.ra.cmp.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import com.rizky.ca.cmp.db.model.UserCertificates;
import com.rizky.ca.cmp.db.model.UserData;



public class UserCertificatesMapper implements RowMapper<UserCertificates>{

    @Override
    public UserCertificates map(ResultSet rs, StatementContext ctx) throws SQLException {

        UserCertificates userCert=new UserCertificates();
        userCert.setCertificate(rs.getBlob("certificate"));
        userCert.setExpired_date(rs.getDate("expired_date"));
        userCert.setId(rs.getString("id"));
        userCert.setKey_length(rs.getString("key_length"));
        userCert.setPkcs12(rs.getBlob("pkcs12"));
        userCert.setRequest_date(rs.getDate("request_date"));
        userCert.setRequest_id(rs.getString("request_id"));
        userCert.setSerial_number(rs.getString("serial_number"));
        userCert.setStatus(rs.getString("status"));
        userCert.setSubject_dn(rs.getString("subject_dn"));
        userCert.setUser_id(rs.getString("user_id"));        
                
        return userCert;
    }

}
