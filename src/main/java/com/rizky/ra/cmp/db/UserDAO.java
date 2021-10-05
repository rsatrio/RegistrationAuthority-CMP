package com.rizky.ra.cmp.db;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import com.rizky.ca.cmp.db.model.UserData;

public interface UserDAO {

    @SqlQuery("select user_id from users")
    List<String> getAllUsers();

    @SqlUpdate("insert into users(user_id,email,password,salt) values(?,?,?,?)")
    void insertInitialReg(String userId,String email,String password,String salt);

    @SqlQuery("select * from users where email=? and active=1 ")
    @RegisterRowMapper(UserDataMapper.class)
    UserData loginQuery(String email);
    
    @SqlUpdate("update users set password=? where email=?")
    void updatePassword(String newPass,String email);
}
