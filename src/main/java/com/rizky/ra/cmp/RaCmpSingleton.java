package com.rizky.ra.cmp;

import java.security.KeyStore;

import org.jdbi.v3.core.Jdbi;

public class RaCmpSingleton {

    private static RaCmpSingleton singleton1=null;
    private Jdbi jdbi;
    private RaCmpConfiguration config;
    private KeyStore keyStore;
    
    
    private RaCmpSingleton()    {

    }

    public static RaCmpSingleton getSingleton() {
        if(singleton1==null)    {
            singleton1=new RaCmpSingleton();
        }
        return singleton1;
    }
    
    
  
    public KeyStore getKeyStore() {
        return keyStore;
    }

    protected void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    protected void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public RaCmpConfiguration getConfig() {
        return config;
    }

    protected void setConfig(RaCmpConfiguration config) {
        this.config = config;
    }
    
    
    
    

}
