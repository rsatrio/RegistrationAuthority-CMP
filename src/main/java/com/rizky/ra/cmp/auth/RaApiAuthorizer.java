package com.rizky.ra.cmp.auth;

import io.dropwizard.auth.Authorizer;

public class RaApiAuthorizer implements Authorizer<TokenClaims> {

    @Override
    public boolean authorize(TokenClaims claim, String role) {
        // TODO Auto-generated method stub
        return claim.getRole().equals(role);
    }

}