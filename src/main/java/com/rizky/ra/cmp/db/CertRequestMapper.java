package com.rizky.ra.cmp.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import com.rizky.ca.cmp.db.model.CertRequest;
import com.rizky.ca.cmp.db.model.UserData;



public class CertRequestMapper implements RowMapper<CertRequest>{

    @Override
    public CertRequest map(ResultSet rs, StatementContext ctx) throws SQLException {

        CertRequest certReq=new CertRequest();
        certReq.setEmail(rs.getString("email"));
        certReq.setCommon_name(rs.getString("common_name"));
        certReq.setCountry(rs.getString("country"));
        certReq.setId(rs.getString("id"));
        certReq.setIdentity(rs.getBlob("identity_proof"));
        certReq.setRequest_date(rs.getDate("request_date"));
        certReq.setState(rs.getString("state"));
        certReq.setStatus(rs.getString("status"));
        certReq.setUser_id(rs.getString("user_id"));
        certReq.setKeypass(rs.getString("keypass"));
        
        
        return certReq;
    }

}
