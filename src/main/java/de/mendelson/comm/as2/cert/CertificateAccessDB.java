//$Header: /as2/de/mendelson/comm/as2/cert/CertificateAccessDB.java 21    7.11.18 17:14 Heller $
package de.mendelson.comm.as2.cert;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerCertificateInformation;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Access the certificate lists in the database
 * @author S.Heller
 * @version $Revision: 21 $
 */
public class CertificateAccessDB {

    /**Logger to log information to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection configConnection;
    private Connection runtimeConnection;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public CertificateAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    /**Returns the list of certificates used by the passed partner*/
    public void loadPartnerCertificateInformation(Partner partner) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM certificates WHERE partnerid=?");
            statement.setInt(1, partner.getDBId());
            statement.setEscapeProcessing(true);
            result = statement.executeQuery();
            while (result.next()) {
                String fingerprint = result.getString("fingerprintsha1");
                PartnerCertificateInformation information = new PartnerCertificateInformation(
                        fingerprint, result.getInt("category"));
                partner.setCertificateInformation(information);
            }
        } catch (Exception e) {
            this.logger.severe("CertificateAccessDB.loadPartnerCertificateInformation: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    /**Stores the actual partner certificate list of a partner*/
    public void storePartnerCertificateInformationList(Partner partner) {
        this.deletePartnerCertificateInformationList(partner);
        Collection<PartnerCertificateInformation> list = partner.getPartnerCertificateInformationList().asList();
        for (PartnerCertificateInformation certInfo : list) {
            PreparedStatement statement = null;
            try {
                statement = this.configConnection.prepareStatement("INSERT INTO certificates(partnerid,fingerprintsha1,category)VALUES(?,?,?)");
                statement.setEscapeProcessing(true);
                statement.setInt(1, partner.getDBId());
                statement.setString(2, certInfo.getFingerprintSHA1());
                statement.setInt(3, certInfo.getCategory());
                statement.execute();
            } catch (SQLException e) {
                this.logger.severe("storePartnerCertificateInformationList: " + e.getMessage());
                SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (Exception e) {
                        SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                    }
                }
            }
        }
    }

    /**Deletes the actual partner certificate list of a partner*/
    public void deletePartnerCertificateInformationList(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("DELETE FROM certificates WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("CertificateAccessDB.deletePartnerCertificateInformationList: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("CertificateAccessDB.deletePartnerCertificateInformationList: " + e.getMessage());
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }
}
