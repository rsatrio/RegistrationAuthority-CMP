package com.rizky.ra.cmp.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import com.rizky.ca.cmp.db.model.UserData;



public class UserDataMapper implements RowMapper<UserData>{

    @Override
    public UserData map(ResultSet rs, StatementContext ctx) throws SQLException {

        UserData userData=new UserData();
        userData.setEmail(rs.getString("email"));        
        userData.setName(rs.getString("name"));              
        userData.setPassword(rs.getString("password"));
        userData.setCountry(rs.getString("country"));
        userData.setStatus(rs.getString("status"));
        userData.setActive(rs.getBoolean("active"));
        userData.setUser_id(rs.getString("user_id"));
        userData.setUser_type(rs.getString("user_type"));
        userData.setCreation_date(rs.getDate("creation_date"));
        userData.setSalt(rs.getString("salt"));
        userData.setIdentity(rs.getBlob("identity"));
        userData.setPhoto(rs.getBlob("photo"));
        userData.setFailed_login(rs.getInt("failed_login"));        
        userData.setLogin_count(rs.getInt("login_count"));
       
        
        return userData;
    }

}
