//$Header: /as2/de/mendelson/util/security/cert/CertificateInUseInfo.java 1     3.06.11 15:25 Heller $
package de.mendelson.util.security.cert;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Contains information about the use of a certificate
 * @author S.Heller
 * @version $Revision: 1 $
 */
public class CertificateInUseInfo {

    private String message;
    
    public CertificateInUseInfo( String message ){
        this.message = message;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
}
