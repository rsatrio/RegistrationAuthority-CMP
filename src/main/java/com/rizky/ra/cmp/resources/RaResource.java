package com.rizky.ra.cmp.resources;



import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.sql.Blob;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.sql.rowset.serial.SerialBlob;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.password4j.Hash;
import com.password4j.Password;
import com.rizky.ca.cmp.common.CMPTools;
import com.rizky.ca.cmp.common.CertRequestStatus;
import com.rizky.ca.cmp.common.CertificateStatus;
import com.rizky.ca.cmp.db.model.CertRequest;
import com.rizky.ca.cmp.db.model.UserCertificates;
import com.rizky.ca.cmp.db.model.UserData;
import com.rizky.ra.cmp.RaCmpConfiguration;
import com.rizky.ra.cmp.RaCmpSingleton;
import com.rizky.ra.cmp.api.CertRequestStatusReq;
import com.rizky.ra.cmp.api.ChangePasswordReq;
import com.rizky.ra.cmp.api.EnrollCertReq;
import com.rizky.ra.cmp.api.LoginRequest;
import com.rizky.ra.cmp.api.RegistrationRequest;
import com.rizky.ra.cmp.api.StdResponseV1;
import com.rizky.ra.cmp.auth.TokenClaims;
import com.rizky.ra.cmp.db.CertificateRequestDAO;
import com.rizky.ra.cmp.db.UserCertificatesDAO;
import com.rizky.ra.cmp.db.UserDAO;
import com.rizky.ra.cmp.serializer.SerializeBlobBase64;
import com.rizky.ra.cmp.serializer.SerializeBlobString;

import io.dropwizard.auth.Auth;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.rsa.RSASigner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.OAuth2Definition;
import io.swagger.annotations.Scope;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;


@Api("API for CMP")
@Path("/v1/ra/")
@Produces(MediaType.APPLICATION_JSON)
@SwaggerDefinition (securityDefinition =
@SecurityDefinition(oAuth2Definitions=
@OAuth2Definition(key="oauth2",tokenUrl="https://demo.identityserver.io/connect/token"
,authorizationUrl="https://demo.identityserver.io/connect/token",
scopes = @Scope(name = "api",description = "api"),
flow=OAuth2Definition.Flow.APPLICATION)))
public class RaResource {


    Logger log1=LoggerFactory.getLogger(RaResource.class);


    public RaResource() {

    }

    @POST
    @Path("registration")
    public StdResponseV1 registerAccount(@Valid @NotNull RegistrationRequest req) {

        StdResponseV1 resp=new StdResponseV1();

        try  {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            Hash pwdHash=Password.hash(req.getPassword())
                    .withBCrypt();


            RaCmpSingleton.getSingleton().getJdbi().onDemand(UserDAO.class)
            .insertInitialReg(UUID.randomUUID().toString(),
                    req.getEmail(), pwdHash.getResult(), null);

            resp.setStatusOk(true);

            return resp;
        }
        catch(Exception e) {
            log1.error("Failed to register {}",req.getEmail(),e);
            resp.setStatusOk(false);

        }

        return resp;
    }

    @POST
    @Path("logon")
    public StdResponseV1 raLogin(@NotNull LoginRequest req) {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();

        try {
            UserData userData=jdbi.onDemand(UserDAO.class).loginQuery(req.getUsername());
            String passwd=userData.getPassword();

            if(Password.check(req.getPassword(), passwd).withBCrypt())  {
                JWT jwt1=new JWT().setIssuer(config.getJwtIssuer())
                        .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                        .setSubject("sukses")
                        .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(config.getJwtExpirationMinutes()))
                        .addClaim("role", userData.getUser_type())
                        .addClaim("id", userData.getUser_id())
                        .addClaim("email", userData.getEmail());
                KeyStore ks=RaCmpSingleton.getSingleton().getKeyStore();

                RSAPrivateKey priv=(RSAPrivateKey) ks.getKey(config.getKeyAlias(), 
                        config.getKeyPass().toCharArray());
                String privKeyPem=CMPTools.privateKeyPem(Base64.getEncoder().encodeToString(priv.getEncoded()));

                Signer signer1=RSASigner.newSHA256Signer(privKeyPem);
                String encodedJwt=JWT.getEncoder().encode(jwt1, signer1);

                List<String> data=new LinkedList<String>();
                data.add(encodedJwt);

                resp.setStatusOk(true);
                resp.setData(data);

                return resp;                
            }
            else    {
                resp.setStatusOk(false);
                return resp;
            }

        }
        catch(Exception e)  {
            log1.error("Failed to login with email {}",req.getUsername(),e);
            resp.setStatusOk(false);
            return resp;
        }


    }

    @POST
    @Path("certReq")
    @ApiOperation(value = "Input Cert Request", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="POST")
    @RolesAllowed("regular")
    public StdResponseV1 inputCertRequest(@NotNull EnrollCertReq req,
            @ApiParam(required=false,hidden=true,allowEmptyValue=true) @Auth TokenClaims claim1) {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();

        try {

            jdbi.onDemand(CertificateRequestDAO.class).insertIntoCertRequest(UUID.randomUUID().toString(),
                    claim1.getId(), 
                    CertRequestStatus.Submitted.toString(), 
                    req.getEmail(), req.getCommonName(), req.getCountry(),
                    req.getState(),
                    new Date(), 
                    new SerialBlob(req.getIdentityProof().getBytes()), 
                    req.getPassword());

            resp.setStatusOk(true);
            return resp;

        }
        catch(Exception e)  {
            log1.error("Failed to add new Certification Request",e);
            resp.setStatusOk(false);
            return resp;
        }

    }

    @GET
    @Path("certReqData")
    @ApiOperation(value = "List All Submitted Request", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="GET")
    @RolesAllowed("admin")
    public StdResponseV1 getCertRequestData() {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();

        try {

            List<CertRequest> certRequests=jdbi.onDemand(CertificateRequestDAO.class).
                    getAllRequestByStatus(CertRequestStatus.Submitted.toString());

            ObjectMapper map1=new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Blob.class, new SerializeBlobString());
            map1.registerModule(module);

            String result=map1.writeValueAsString(certRequests);
            List<String> data=new LinkedList<String>();
            data.add(result);
            resp.setStatusOk(true);
            resp.setData(data);
            return resp;

        }
        catch(Exception e)  {
            log1.error("Failed to add new Certification Request",e);
            resp.setStatusOk(false);
            return resp;
        }

    }

    @POST
    @Path("certReqData")
    @ApiOperation(value = "Update Request", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="POST")
    @RolesAllowed("admin")
    public StdResponseV1 rejectRequestData(@NotNull @Valid CertRequestStatusReq req) {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();
        Handle handle1=jdbi.open();
        try {
            handle1.begin();
            if(!req.isStatus()) {
                handle1.attach(CertificateRequestDAO.class).
                updateStatus(CertRequestStatus.Rejected.toString(), 
                        req.getId(),CertRequestStatus.Submitted.toString());
                handle1.commit();
                resp.setStatusOk(true);
            }
            else    {
                resp.setStatusOk(false);
            }
            return resp;

        }
        catch(Exception e)  {
            log1.error("Failed to update Cert Request Data",e);
            handle1.rollback();
            resp.setStatusOk(false);
            return resp;
        }
        finally {
            handle1.close();
        }

    }


    @GET
    @Path("userCert")
    @ApiOperation(value = "User Cert", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="GET")
    @RolesAllowed("regular")
    public StdResponseV1 getUserCertData(@ApiParam(required=false,hidden=true,allowEmptyValue=true) @Auth TokenClaims claim1) {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();

        try {

            List<UserCertificates> uCert=jdbi.onDemand(UserCertificatesDAO.class).
                    getAllDataByUserId(claim1.getId());
            ObjectMapper map1=new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Blob.class, new SerializeBlobBase64());
            map1.registerModule(module);

            String data2=map1.writeValueAsString(uCert);
            List<String> data1=new LinkedList<String>();
            data1.add(data2);

            resp.setStatusOk(true);
            resp.setData(data1);

            return resp;

        }
        catch(Exception e)  {
            log1.error("Failed to get Certificate Data for user {}",claim1.getId(),e);

            resp.setStatusOk(false);
            return resp;
        }


    }


    @GET
    @Path("adminCert")
    @ApiOperation(value = "List All Certificate", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="GET")
    @RolesAllowed("admin")
    public StdResponseV1 getAdminCertData(@ApiParam(required=false,hidden=true,allowEmptyValue=true) @Auth TokenClaims claim1)  {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();

        try {

            List<UserCertificates> uCert=jdbi.onDemand(UserCertificatesDAO.class).getAllCertAdmin();
            ObjectMapper map1=new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Blob.class, new SerializeBlobBase64());
            map1.registerModule(module);

            String data2=map1.writeValueAsString(uCert);
            List<String> data1=new LinkedList<String>();
            data1.add(data2);

            resp.setStatusOk(true);
            resp.setData(data1);

            return resp;

        }
        catch(Exception e)  {
            log1.error("Failed to get Certificate Data for admin {}",e);

            resp.setStatusOk(false);
            return resp;
        }


    }

    @POST
    @Path("change-password")
    @ApiOperation(value = "Change Password", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Returns Status",response=StdResponseV1.class,httpMethod="POST")
    @RolesAllowed("regular")
    public StdResponseV1 getAdminCertData(@NotNull @Valid ChangePasswordReq req,
            @ApiParam(required=false,hidden=true,allowEmptyValue=true) @Auth TokenClaims claim1)  {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();        

        try {
            UserData userData=jdbi.onDemand(UserDAO.class).loginQuery(claim1.getEmail());
            if(Password.check(req.getOldPassword(), userData.getPassword()).withBCrypt())   {
                String newPass=Password.hash(req.getNewPassword()).withBCrypt().getResult();
                jdbi.onDemand(UserDAO.class).updatePassword(newPass,claim1.getEmail());
                resp.setStatusOk(true);
                return resp;
            }
            else    {
                resp.setStatusOk(false);
                resp.setMessage("Wrong old password");
                return resp;
            }

        }
        catch(Exception e)  {
            log1.error("Failed to change password for user {}",claim1.getEmail());
            resp.setStatusOk(false);
            return resp;
        }


    }

    @GET
    @Path("dashboard")
    @ApiOperation(value = "Dashboard User", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="GET")
    @RolesAllowed({"admin","regular"})
    public StdResponseV1 getDashboardUser(@ApiParam(required=false,hidden=true,allowEmptyValue=true) @Auth TokenClaims claim1)  {

        StdResponseV1 resp=new StdResponseV1();
        Jdbi jdbi=RaCmpSingleton.getSingleton().getJdbi();        

        try {
            List<String> data1=new LinkedList<String>();

            if(claim1.getRole().equalsIgnoreCase("regular"))    { 
                String activeCert=Integer.toString(
                        jdbi.onDemand(UserCertificatesDAO.class).countCertStatusUser(claim1.getId(),CertificateStatus.Active.toString()));
                String revokeCert=Integer.toString(
                        jdbi.onDemand(UserCertificatesDAO.class).countCertStatusUser(claim1.getId(),CertificateStatus.Revoked.toString()));
                String totalRequest=Integer.toString(
                        jdbi.onDemand(CertificateRequestDAO.class).countAllCertRequestUser(claim1.getId()));
                String rejectedRequest=Integer.toString(
                        jdbi.onDemand(CertificateRequestDAO.class).countCertRequestUser(claim1.getId(), CertRequestStatus.Rejected.toString()));
                data1.add(totalRequest);
                data1.add(activeCert);
                data1.add(revokeCert);
                data1.add(rejectedRequest);
            }
            else    {
                String activeCert=Integer.toString(
                        jdbi.onDemand(UserCertificatesDAO.class).countCertStatusAdmin(CertificateStatus.Active.toString()));
                String revokeCert=Integer.toString(
                        jdbi.onDemand(UserCertificatesDAO.class).countCertStatusAdmin(CertificateStatus.Revoked.toString()));
                String totalRequest=Integer.toString(
                        jdbi.onDemand(CertificateRequestDAO.class).countAllCertRequestAdmin());
                String submittedRequest=Integer.toString(
                        jdbi.onDemand(CertificateRequestDAO.class).countCertRequestAdmin( CertRequestStatus.Submitted.toString()));
                data1.add(totalRequest);
                data1.add(activeCert);
                data1.add(revokeCert);
                data1.add(submittedRequest);
            }
            resp.setStatusOk(true);
            resp.setData(data1);
            return resp;

        }
        catch(Exception e)  {
            log1.error("Dashboard data request failed for user {}",claim1.getId());
            resp.setStatusOk(false);
            return resp;
        }
    }


}
