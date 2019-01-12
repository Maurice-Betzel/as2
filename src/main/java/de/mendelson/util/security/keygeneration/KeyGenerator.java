//$Header: /oftp2/de/mendelson/util/security/keygeneration/KeyGenerator.java 7     6/01/17 11:59a Heller $
package de.mendelson.util.security.keygeneration;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * This class allows to generate a private key
 *
 * @author S.Heller
 * @version $Revision: 7 $
 */
public class KeyGenerator {

    public static final String KEYTYPE_DSA = "DSA";
    public static final String KEYTYPE_RSA = "RSA";
    public static final String KEYTYPE_ECDSA = "ECDSA";
    public static final String SIGNATUREALGORITHM_MD5_WITH_RSA_OID = PKCSObjectIdentifiers.md5WithRSAEncryption.getId();
    public final static String SIGNATUREALGORITHM_SHA1_WITH_RSA_OID = PKCSObjectIdentifiers.sha1WithRSAEncryption.getId();
    public final static String SIGNATUREALGORITHM_SHA256_WITH_RSA_OID = PKCSObjectIdentifiers.sha256WithRSAEncryption.getId();
    public final static String SIGNATUREALGORITHM_SHA256_WITH_ECDSA_OID = "1.2.840.10045.4.3.2";

    /**
     * Creates a new instance of KeyGenerator
     */
    public KeyGenerator() {
    }

    /**
     * Generate a key pair.
     *
     * @param keyType The type of the key alg as defined in this class
     * @param keySize The length of the key
     */
    public KeyGenerationResult generateKeyPair(KeyGenerationValues generationValues) throws Exception {
        //generation keypair
        KeyPairGenerator keyPairGen = null;
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        keyPairGen = KeyPairGenerator.getInstance(generationValues.getKeyType());
        keyPairGen.initialize(generationValues.getKeySize(), rand);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        X509Certificate certificate = this.generateCertificate(keyPair.getPublic(),
                keyPair.getPrivate(), generationValues);
        KeyGenerationResult result = new KeyGenerationResult(keyPair, certificate);
        return (result);
    }

    /**
     * Generates a self-signed X509 Version 3 certificate
     *
     */
    private X509Certificate generateCertificate(PublicKey publicKey, PrivateKey privateKey,
            KeyGenerationValues generationValues) throws Exception {
        //Stores certificate attributes
        Hashtable<ASN1ObjectIdentifier, String> attributes = new Hashtable<ASN1ObjectIdentifier, String>();
        Vector<ASN1ObjectIdentifier> order = new Vector<ASN1ObjectIdentifier>();
        attributes.put(X509Name.CN, generationValues.getCommonName());
        order.add(0, X509Name.CN);
        attributes.put(X509Name.OU, generationValues.getOrganisationUnit());
        order.add(0, X509Name.OU);
        attributes.put(X509Name.O, generationValues.getOrganisationName());
        order.add(0, X509Name.O);
        attributes.put(X509Name.L, generationValues.getLocalityName());
        order.add(0, X509Name.L);
        attributes.put(X509Name.ST, generationValues.getStateName());
        order.add(0, X509Name.ST);
        attributes.put(X509Name.C, generationValues.getCountryCode());
        order.add(0, X509Name.C);
        attributes.put(X509Name.E, generationValues.getEmailAddress());
        order.add(0, X509Name.E);
        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        // Set the issuer distinguished name
        certificateGenerator.setIssuerDN(new X509Principal(order, attributes));
        //add a key extension if this is requested
        if (generationValues.getKeyExtension() != null) {
            certificateGenerator.addExtension(X509Extensions.KeyUsage, true, generationValues.getKeyExtension());
        }
        //add a extended key extension if this is requested
        if (generationValues.getExtendedKeyExtension() != null) {
            certificateGenerator.addExtension(X509Extensions.ExtendedKeyUsage, false,
                    generationValues.getExtendedKeyExtension());
        }
        //add a subjectAlternativeName if requested
        if (!generationValues.getSubjectAlternativeNames().isEmpty()) {
            GeneralName[] generalNamesArray = new GeneralName[generationValues.getSubjectAlternativeNames().size()];
            generationValues.getSubjectAlternativeNames().toArray(generalNamesArray);
            certificateGenerator.addExtension(X509Extensions.SubjectAlternativeName, false,
                    new GeneralNames(generalNamesArray));
        }

        // Valid before and after dates now to iValidity days in the future
        Date startDate = new Date(System.currentTimeMillis());
        long duration = TimeUnit.DAYS.toMillis(generationValues.getKeyValidInDays());
        Date endDate = new Date(startDate.getTime() + duration);
        certificateGenerator.setNotBefore(startDate);
        certificateGenerator.setNotAfter(endDate);
        certificateGenerator.setSubjectDN(new X509Principal(order, attributes));
        certificateGenerator.setPublicKey(publicKey);
        certificateGenerator.setSignatureAlgorithm(generationValues.getSignatureAlgorithm());
        BigInteger serialNumber = new BigInteger(Long.toString(System.currentTimeMillis() / 1000));
        certificateGenerator.setSerialNumber(serialNumber);
        // Generate an X.509 certificate, based on the current issuer and subject
        X509Certificate certificate = certificateGenerator.generate(privateKey, "BC");
        // Return the certificate
        return certificate;
    }

}
