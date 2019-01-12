//$Header: /as2/de/mendelson/comm/as2/partner/CertificateUsedByPartnerChecker.java 4     18.09.12 11:29 Heller $
package de.mendelson.comm.as2.partner;

import de.mendelson.comm.as2.partner.clientserver.PartnerListRequest;
import de.mendelson.comm.as2.partner.clientserver.PartnerListResponse;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.security.cert.CertificateInUseChecker;
import de.mendelson.util.security.cert.CertificateInUseInfo;
import de.mendelson.util.security.cert.KeystoreCertificate;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Checks if a certificate is in use by a partner
 *
 * @author S.Heller
 * @version $Revision: 4 $
 */
public class CertificateUsedByPartnerChecker implements CertificateInUseChecker {

    private MecResourceBundle rb;
    private BaseClient baseClient;

    public CertificateUsedByPartnerChecker(BaseClient baseClient) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCertificateUsedByPartnerChecker.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.baseClient = baseClient;
    }

    @Override
    public List<CertificateInUseInfo> checkUsed(KeystoreCertificate cert) {
        List<CertificateInUseInfo> list = new ArrayList<CertificateInUseInfo>();
        PartnerListResponse response = (PartnerListResponse) this.baseClient.sendSync(new PartnerListRequest(PartnerListRequest.LIST_ALL));
        List<Partner> partnerList = response.getList();
        for (Partner singlePartner : partnerList) {
            String cryptFingerprint = singlePartner.getCryptFingerprintSHA1();
            String signFingerprint = singlePartner.getSignFingerprintSHA1();
            if (cert.getFingerPrintSHA1().equals(cryptFingerprint)) {
                CertificateInUseInfo info = new CertificateInUseInfo(
                        this.rb.getResourceString("used.crypt", singlePartner.getName()));
                list.add(info);
            }
            if (cert.getFingerPrintSHA1().equals(signFingerprint)) {
                CertificateInUseInfo info = new CertificateInUseInfo(
                        this.rb.getResourceString("used.sign", singlePartner.getName()));
                list.add(info);
            }
        }
        return (list);
    }
}
