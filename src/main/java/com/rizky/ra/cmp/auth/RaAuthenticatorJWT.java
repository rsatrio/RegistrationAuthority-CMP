package com.rizky.ra.cmp.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.fusionauth.jwks.JSONWebKeySetHelper;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSAVerifier;

import java.security.interfaces.RSAPublicKey;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.container.ResourceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rizky.ra.cmp.RaCmpConfiguration;
import com.rizky.ra.cmp.RaCmpSingleton;

public class RaAuthenticatorJWT  implements Authenticator<String, TokenClaims>{

    RaCmpConfiguration config;
   
    public RaAuthenticatorJWT(RaCmpConfiguration config)   {
        this.config=config;   
    }
    
    Logger log1=LoggerFactory.getLogger(RaAuthenticatorJWT.class);
    @Override
    public Optional<TokenClaims> authenticate(String bearer)
            throws AuthenticationException {

        try {
//            List<JSONWebKey> keys = JSONWebKeySetHelper.retrieveKeysFromJWKS(config.getJwksUrl());
            
            String alias1=RaCmpSingleton.getSingleton().getConfig().getKeyAlias();
            RSAPublicKey pubKey=(RSAPublicKey)RaCmpSingleton.getSingleton().
                    getKeyStore().getCertificate(alias1).getPublicKey();
//            RSAPublicKey pubKey=(RSAPublicKey)JSONWebKey.parse(keys.get(0));
//            log1.info(new String(pubKey.getEncoded()));
            
            Verifier verify1=RSAVerifier.newVerifier(pubKey);
            
            log1.debug("Bearer: "+bearer);
            
            JWT jwt1=JWT.getDecoder().decode(bearer, verify1);

            if(jwt1.expiration.isBefore(ZonedDateTime.now()))   {
                throw new AuthenticationException("Unauthorized");
            }
            TokenClaims claim1=new TokenClaims();
           
            claim1.setEmail(jwt1.getString("email"));
            claim1.setRole(jwt1.getString("role"));
            claim1.setId(jwt1.getString("id"));
                                
            return Optional.of(claim1);
        }
        catch(Exception e)  {
            log1.error("Error authorization JWT",e);

            throw new AuthenticationException("Unauthorized");      
        }
    }


}
