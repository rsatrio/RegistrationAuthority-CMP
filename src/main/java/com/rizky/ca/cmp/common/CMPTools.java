package com.rizky.ca.cmp.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Date;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.CertRepMessage;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIHeaderBuilder;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.cmp.RevDetails;
import org.bouncycastle.asn1.cmp.RevRepContent;
import org.bouncycastle.asn1.cmp.RevReqContent;
import org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509DefaultEntryConverter;
import org.bouncycastle.asn1.x509.X509NameEntryConverter;
import org.bouncycastle.cert.cmp.GeneralPKIMessage;
import org.bouncycastle.cert.cmp.ProtectedPKIMessage;
import org.bouncycastle.cert.cmp.ProtectedPKIMessageBuilder;
import org.bouncycastle.cert.crmf.CertificateRequestMessage;
import org.bouncycastle.cert.crmf.CertificateRequestMessageBuilder;
import org.bouncycastle.cert.crmf.PKMACBuilder;
import org.bouncycastle.cert.crmf.jcajce.JcePKMACValuesCalculator;
import org.bouncycastle.operator.MacCalculator;

import com.rizky.ra.cmp.api.EnrollCertReq;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class CMPTools {

    public  static byte[] sendCMPEnroll(EnrollCertReq req,KeyPair keyPair,
            String urlCmp,String dnRoot) throws Exception {

        CertificateRequestMessageBuilder msgbuilder = new CertificateRequestMessageBuilder(BigInteger.valueOf(1000));
        X509NameEntryConverter dnconverter = new X509DefaultEntryConverter();
        X500Name issuerDN = new X500Name(dnRoot);
        X500Name subjectDN =new X500Name("CN="+req.getCommonName()
        +",emailAddress="+req.getEmail()+",C="+req.getCountry()+",ST="+req.getState());
        msgbuilder.setIssuer(issuerDN);
        msgbuilder.setSubject(subjectDN);
        final byte[]                  bytes = keyPair.getPublic().getEncoded();
        final ByteArrayInputStream    bIn = new ByteArrayInputStream(bytes);
        final ASN1InputStream         dIn = new ASN1InputStream(bIn);
        final SubjectPublicKeyInfo keyInfo = new SubjectPublicKeyInfo((ASN1Sequence)dIn.readObject());
        msgbuilder.setPublicKey(keyInfo);
        GeneralName sender = new GeneralName(subjectDN);
        msgbuilder.setAuthInfoSender(sender);
        // RAVerified POP
        msgbuilder.setProofOfPossessionRaVerified();
        CertificateRequestMessage msg = msgbuilder.build();
        GeneralName recipient = new GeneralName(issuerDN);
        ProtectedPKIMessageBuilder pbuilder = new ProtectedPKIMessageBuilder(sender, recipient);
        pbuilder.setMessageTime(new Date());
        // senderNonce
        byte[] senderNonce = {1, 1, 1, 1, 1, 1};
        byte[] transactionId = new byte [1];
        pbuilder.setSenderNonce(senderNonce);
        // TransactionId
        pbuilder.setTransactionID(transactionId);
        // Key Id used (required) by the recipient to do a lot of stuff
        pbuilder.setSenderKID("KeyId".getBytes());
        org.bouncycastle.asn1.crmf.CertReqMessages msgs = new org.bouncycastle.asn1.crmf.CertReqMessages(msg.toASN1Structure());
        org.bouncycastle.asn1.cmp.PKIBody pkibody = new org.bouncycastle.asn1.cmp.PKIBody(org.bouncycastle.asn1.cmp.PKIBody.TYPE_INIT_REQ, msgs);
        pbuilder.setBody(pkibody);
        JcePKMACValuesCalculator jcePkmacCalc = new JcePKMACValuesCalculator();

        final AlgorithmIdentifier digAlg = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1); // SHA1
        final AlgorithmIdentifier macAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1); // HMAC/SHA1
        jcePkmacCalc.setup(digAlg, macAlg);
        PKMACBuilder macbuilder = new PKMACBuilder(jcePkmacCalc);
        MacCalculator macCalculator = macbuilder.build("password".toCharArray());
        ProtectedPKIMessage message = pbuilder.build(macCalculator);

        HttpResponse<byte[]> resp=Unirest.post(urlCmp)
                .header("Content-Type", "application/pkixcmp")
                .body(encodeDER(message))
                .asBytes();        

        ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(resp.getBody()));

        PKIMessage pkiMessage = PKIMessage.getInstance(is.readObject());

        GeneralPKIMessage generalPKIMessage = new GeneralPKIMessage(pkiMessage.getEncoded());
        
        ProtectedPKIMessage message2=new ProtectedPKIMessage(generalPKIMessage);
        CertRepMessage certRep=(CertRepMessage)message2.getBody().getContent();
        byte[] certEncoded=certRep.getResponse()[0].getCertifiedKeyPair().getCertOrEncCert().getCertificate().getEncoded();

        return certEncoded;
    }
    
    private static byte[] encodeDER(ProtectedPKIMessage protectedMessage) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final DEROutputStream out = new DEROutputStream(outputStream);
        out.writeObject(protectedMessage.toASN1Structure());
        return outputStream.toByteArray();
    }
    
    public  static String derToPem(String derString)   {
        StringBuilder sb2=new StringBuilder("-----BEGIN CERTIFICATE-----"+System.lineSeparator());
        int i=0;
        while(i<derString.length())    {
            int start=i;
            int end=i+64;
            if(derString.length()<end)    {
                end=derString.length();
            }
            sb2.append(derString.substring(start,end)+System.lineSeparator());
            i+=64;
        }
        sb2.append("-----END CERTIFICATE-----");

        return sb2.toString();
    }
    
    public static int cmpRevoke(String dnIssuer,
            String dnUser, 
            String serialNumber, String urlCmp) throws Exception {
        X500Name issuerDN = new X500Name(dnIssuer);
        X500Name subjectDN =new X500Name(dnUser);
        BigInteger serNo=new BigInteger(serialNumber,16);
        CertTemplateBuilder certTemplateBuilder = new CertTemplateBuilder();
        certTemplateBuilder.setIssuer(issuerDN);
        certTemplateBuilder.setSubject(subjectDN);
        certTemplateBuilder.setSerialNumber(new ASN1Integer(serNo));

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(certTemplateBuilder.build());      

        //Revocation Reason (Set to Unspecified)
        ExtensionsGenerator extgen = new ExtensionsGenerator();            
        CRLReason crlReason = CRLReason.lookup(CRLReason.unspecified);
        extgen.addExtension(Extension.reasonCode, false, crlReason);
        Extensions exts = extgen.generate();
        v.add(exts);


        ASN1Sequence seq = new DERSequence(v);
        RevDetails revDetails = RevDetails.getInstance(seq);
        RevReqContent revReqContent = new RevReqContent(revDetails);

        final GeneralName recipient=new GeneralName(issuerDN);
        final GeneralName sender=new GeneralName(subjectDN);

        byte[] senderNonce = {1, 1, 1, 1, 1, 1};
        byte[] transactionId = new byte [1];
        PKIHeaderBuilder pkiHeaderBuilder = new PKIHeaderBuilder(PKIHeader.CMP_2000, new GeneralName(subjectDN), recipient);
        pkiHeaderBuilder.setMessageTime(new ASN1GeneralizedTime(new Date()));
        pkiHeaderBuilder.setSenderNonce(new DEROctetString(senderNonce));
        pkiHeaderBuilder.setTransactionID(new DEROctetString(transactionId));

        PKIBody pkiBody = new PKIBody(PKIBody.TYPE_REVOCATION_REQ, revReqContent);
        PKIMessage pkiMessage = new PKIMessage(pkiHeaderBuilder.build(), pkiBody);

        ProtectedPKIMessageBuilder pbuilder = new ProtectedPKIMessageBuilder(sender, recipient);
        pbuilder.setMessageTime(new Date());
        // senderNonce

        pbuilder.setSenderNonce(senderNonce);
        // TransactionId
        pbuilder.setTransactionID(transactionId);
        // Key Id used (required) by the recipient to do a lot of stuff
        pbuilder.setSenderKID("KeyId".getBytes());        
        pbuilder.setBody(pkiBody);
        JcePKMACValuesCalculator jcePkmacCalc = new JcePKMACValuesCalculator();

        final AlgorithmIdentifier digAlg = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1); // SHA1
        final AlgorithmIdentifier macAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1); // HMAC/SHA1
        jcePkmacCalc.setup(digAlg, macAlg);
        PKMACBuilder macbuilder = new PKMACBuilder(jcePkmacCalc);
        MacCalculator macCalculator = macbuilder.build("password".toCharArray());
        ProtectedPKIMessage message = pbuilder.build(macCalculator);

        HttpResponse<byte[]> resp=Unirest.post(urlCmp)
                .header("Content-Type", "application/pkixcmp")
                .body(encodeDER(message))
                .asBytes();
        System.out.println(resp.getStatus());

        ASN1InputStream is = new ASN1InputStream(new ByteArrayInputStream(resp.getBody()));

        PKIMessage pkiMessageResp = PKIMessage.getInstance(is.readObject());

        PKIBody bodyResp=pkiMessageResp.getBody();
        final RevRepContent revRepContent = (RevRepContent) bodyResp.getContent();
        final PKIStatusInfo pkiStatusInfo = revRepContent.getStatus()[0];

        return pkiStatusInfo.getStatus().intValue();
    }
    
    public  static String privateKeyPem(String derString)   {
        StringBuilder sb2=new StringBuilder("-----BEGIN PRIVATE KEY-----"+System.lineSeparator());
        int i=0;
        while(i<derString.length())    {
            int start=i;
            int end=i+64;
            if(derString.length()<end)    {
                end=derString.length();
            }
            sb2.append(derString.substring(start,end)+System.lineSeparator());
            i+=64;
        }
        sb2.append("-----END PRIVATE KEY-----");

        return sb2.toString();
    }



}
