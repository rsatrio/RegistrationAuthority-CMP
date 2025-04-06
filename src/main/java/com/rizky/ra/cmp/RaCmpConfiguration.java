package com.rizky.ra.cmp;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class RaCmpConfiguration extends Configuration {


        
    private String urlCmp,dnRoot;
    private String keyStorePath,keyPass,keyAlias;
    
    private String jwtIssuer;
    private String sharedSecretRa;
    private int jwtExpirationMinutes=60;


    private final SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();
    
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
    

    

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration getSwagger() {
        return swagger;
    }

   

    public String getUrlCmp() {
        return urlCmp;
    }

    public void setUrlCmp(String urlCmp) {
        this.urlCmp = urlCmp;
    }

    public String getDnRoot() {
        return dnRoot;
    }

    public void setDnRoot(String dnRoot) {
        this.dnRoot = dnRoot;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public void setDatabase(DataSourceFactory database) {
        this.database = database;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public int getJwtExpirationMinutes() {
        return jwtExpirationMinutes;
    }

    public void setJwtExpirationMinutes(int jwtExpirationMinutes) {
        this.jwtExpirationMinutes = jwtExpirationMinutes;
    }

    public String getSharedSecretRa() {
        return sharedSecretRa;
    }

    public void setSharedSecretRa(String sharedSecretRa) {
        this.sharedSecretRa = sharedSecretRa;
    }
    
    
   
    
}
