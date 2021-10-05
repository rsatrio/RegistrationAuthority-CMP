package com.rizky.ra.cmp.db;

import java.sql.Blob;
import java.util.Date;
import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import com.rizky.ca.cmp.db.model.UserCertificates;

public interface UserCertificatesDAO {
    
    @SqlUpdate("insert into user_certificates(id,request_id,user_id,status,serial_number,"
            + "key_length,subject_dn,request_date,pkcs12,certificate,expired_date) "
            + "values(?,?,?,?,?,?,?,?,?,?,?)")
    void insertData(String id,String reqId,String userId,String status,String serialNum,
            String keyLength,String subjectDn,Date requestDate,
            Blob pkcs12,Blob certificate,Date expiredDate);
    
    @SqlQuery("select * from user_certificates where user_id=?")
    @RegisterRowMapper(UserCertificatesMapper.class)
    List<UserCertificates> getAllDataByUserId(String userId);
    
    @SqlQuery("select * from user_certificates where id=?")
    @RegisterRowMapper(UserCertificatesMapper.class)
    UserCertificates getAllDataById(String id);
    
    @SqlUpdate("update user_certificates set status=? where id=?")
    void updateStatusCert(String status,String id);
    
    @SqlQuery("select a.id,a.request_id,a.user_id,a.status,a.serial_number,a.key_length,a.subject_dn,a.request_date,a.expired_date,b.email " + 
            " from user_certificates a, users b" + 
            " where a.user_id=b.user_id")
    @RegisterRowMapper(UserCertificatesMapperLite.class)
    List<UserCertificates> getAllCertAdmin();
    
    @SqlQuery("select count(1) from user_certificates where user_id=? and status=?")
    int countCertStatusUser(String user_id,String status);
    
    @SqlQuery("select count(1) from user_certificates where status=?")
    int countCertStatusAdmin(String status);
    
    

}
