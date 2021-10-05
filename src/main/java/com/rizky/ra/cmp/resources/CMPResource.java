package com.rizky.ra.cmp.resources;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.sql.rowset.serial.SerialBlob;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rizky.ca.cmp.common.CMPTools;
import com.rizky.ca.cmp.common.CertRequestStatus;
import com.rizky.ca.cmp.common.CertificateStatus;
import com.rizky.ca.cmp.db.model.CertRequest;
import com.rizky.ca.cmp.db.model.UserCertificates;
import com.rizky.ra.cmp.RaCmpConfiguration;
import com.rizky.ra.cmp.RaCmpSingleton;
import com.rizky.ra.cmp.api.AcceptEnrollCertReq;
import com.rizky.ra.cmp.api.EnrollCertReq;
import com.rizky.ra.cmp.api.StdResponseV1;
import com.rizky.ra.cmp.auth.TokenClaims;
import com.rizky.ra.cmp.db.CertificateRequestDAO;
import com.rizky.ra.cmp.db.UserCertificatesDAO;

import io.dropwizard.auth.Auth;
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
@Path("/v1/cmp/")
@Produces(MediaType.APPLICATION_JSON)
@SwaggerDefinition (securityDefinition =
@SecurityDefinition(oAuth2Definitions=
@OAuth2Definition(key="oauth2",tokenUrl="https://demo.identityserver.io/connect/token"
,authorizationUrl="https://demo.identityserver.io/connect/token",
scopes = @Scope(name = "api",description = "api"),
flow=OAuth2Definition.Flow.APPLICATION)))
public class CMPResource {





    public CMPResource() {

    }


    @POST
    @Path("enrollCert")
    @ApiOperation(value = "Enroll Cert", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="POST")
    @RolesAllowed("admin")
    public StdResponseV1 enrollCert(AcceptEnrollCertReq req,
            @ApiParam(required=false,hidden=true,allowEmptyValue=true) @Auth TokenClaims claim1) {

        StdResponseV1 resp=new StdResponseV1();
        Logger log1=LoggerFactory.getLogger(CMPResource.class);
        Handle jdbi=RaCmpSingleton.getSingleton().getJdbi().open();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();
        try {
            jdbi.begin();
            KeyPairGenerator keygen=KeyPairGenerator.getInstance("RSA");
            keygen.initialize(2048);
            KeyPair keyPair=keygen.generateKeyPair();

            //Get Data
            CertRequest data1=jdbi.attach(CertificateRequestDAO.class).
                    getAllDataByIdStatus(req.getId(), CertRequestStatus.Submitted.toString());


            EnrollCertReq enrollReq=new EnrollCertReq();
            enrollReq.setCommonName(data1.getCommon_name());
            enrollReq.setCountry(data1.getCountry());
            enrollReq.setEmail(data1.getEmail());
            enrollReq.setState(data1.getState());

            byte[] certEncoded=CMPTools.sendCMPEnroll(enrollReq, keyPair, config.getUrlCmp(), config.getDnRoot());

            KeyStore ks=KeyStore.getInstance("pkcs12");
            char[] pwd=data1.getKeypass().toCharArray();
            ks.load(null,pwd);
            X509Certificate x509Cert= (X509Certificate) CertificateFactory
                    .getInstance("X509")
                    .generateCertificate(new ByteArrayInputStream(certEncoded));

            Certificate[] chain=new Certificate[] {x509Cert};
            KeyStore.PrivateKeyEntry privKey=new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), chain);
            ks.setEntry(data1.getEmail(), privKey, new KeyStore.PasswordProtection(pwd));
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            ks.store(bos, pwd);

            //Insert into table

            jdbi.attach(UserCertificatesDAO.class).insertData
            (UUID.randomUUID().toString(),
                    data1.getId(), data1.getUser_id(), 
                    CertificateStatus.Active.toString(), 
                    x509Cert.getSerialNumber().toString(16), 
                    "2048", x509Cert.getSubjectDN().getName(), 
                    new Date(), 
                    new SerialBlob(bos.toByteArray()), 
                    new SerialBlob(certEncoded),
                    x509Cert.getNotAfter());


            //Update request status
            jdbi.attach(CertificateRequestDAO.class).updateStatus(CertRequestStatus.Approved.toString(), 
                    req.getId(),CertRequestStatus.Submitted.toString());

            resp.setStatusOk(true);
            List<String> data2=new LinkedList<String>();
            data2.add(Base64.getEncoder().encodeToString(certEncoded));
            resp.setData(data2);
            jdbi.commit();

        }
        catch(Exception e)  {
            log1.error("Failed to enrollCert",e);
            resp.setStatusOk(false);
            resp.setMessage(e.getMessage());
            jdbi.rollback();

        }
        finally {
            jdbi.close();
        }

        return resp;
    }



    @POST
    @Path("revokeCert")
    @ApiOperation(value = "Revoke Cert", 
    authorizations= @Authorization(scopes=@AuthorizationScope(scope="api", description = "api"), value = "oauth2"),
    notes = "Return results",response=StdResponseV1.class,httpMethod="POST")
    @RolesAllowed("regular")
    public StdResponseV1 revokeCert(AcceptEnrollCertReq req) {

        StdResponseV1 resp=new StdResponseV1();
        Logger log1=LoggerFactory.getLogger(CMPResource.class);
        Handle jdbi=RaCmpSingleton.getSingleton().getJdbi().open();
        RaCmpConfiguration config=RaCmpSingleton.getSingleton().getConfig();
        try {
            UserCertificates cert=jdbi.attach(UserCertificatesDAO.class).getAllDataById(req.getId());
            X509Certificate x509Cert= (X509Certificate) CertificateFactory
                    .getInstance("X509")
                    .generateCertificate(new ByteArrayInputStream(cert.getCertificate().getBytes(1L, (int)cert.getCertificate().length())));
            int result=CMPTools.cmpRevoke(x509Cert.getIssuerDN().getName(), x509Cert.getSubjectDN().getName(), 
                    cert.getSerial_number(), config.getUrlCmp());

            if(result==0)    {
                resp.setStatusOk(true);
                jdbi.attach(UserCertificatesDAO.class).updateStatusCert(CertificateStatus.Revoked.toString(), req.getId());
                jdbi.commit();
            }
            else    {
                resp.setStatusOk(false);
            }
            return resp;

        }
        catch(Exception e)  {
            log1.error("Failed to revoke certificate id {}",req.getId(),e);
            resp.setStatusOk(false);
            jdbi.rollback();
            return resp;
        }
        finally {
            jdbi.close();
        }
    }




}
