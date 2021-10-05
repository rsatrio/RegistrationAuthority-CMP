package com.rizky.ra.cmp.db;

import java.sql.Blob;
import java.util.Date;
import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import com.rizky.ca.cmp.db.model.CertRequest;

public interface CertificateRequestDAO {

    @SqlUpdate("insert into certificate_request(id,user_id,status,email,common_name,"
            + "country,state,request_date,identity_proof,keypass) values(?,?,?,?,?,?,?,?,?,?)")
    void insertIntoCertRequest(String id,String user_id,String status, String email, String common_name,
            String country,String state,Date requestDate,Blob identityProof,String keyPass);

    @SqlQuery("select * from certificate_request where user_id=?")
    @RegisterRowMapper(CertRequestMapper.class)
    List<CertRequest> getAllDataByUserId(String userId);

    @SqlQuery("select * from certificate_request where id=?")
    @RegisterRowMapper(CertRequestMapper.class)
    CertRequest getAllDataById(String id);
    
    @SqlQuery("select * from certificate_request where id=? and status=?")
    @RegisterRowMapper(CertRequestMapper.class)
    CertRequest getAllDataByIdStatus(String id,String status);

    @SqlQuery("select * from certificate_request where status=?")
    @RegisterRowMapper(CertRequestMapper.class)
    List<CertRequest> getAllRequestByStatus(String status);

    @SqlUpdate("update certificate_request set status=? where id=? and status=?")
    void updateStatus(String status,String id,String oldStatus);
    
    @SqlQuery("select count(1) from certificate_request where user_id=? and status=?")
    int countCertRequestUser(String userId,String status);
    
    @SqlQuery("select count(1) from certificate_request where user_id=?")
    int countAllCertRequestUser(String userId);
    
    @SqlQuery("select count(1) from certificate_request")
    int countAllCertRequestAdmin();
    
    @SqlQuery("select count(1) from certificate_request where status=?")
    int countCertRequestAdmin(String status);

}
