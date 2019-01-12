//$Header: /as2/de/mendelson/comm/as2/partner/PartnerSystemAccessDB.java 11    7.11.18 17:14 Heller $
package de.mendelson.comm.as2.partner;

import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Database access wrapper for partner system information. This is the
 * information that is collected if the AS2 system connects to an other AS2
 * system, it will be displayed in the partner panel
 *
 * @author S.Heller
 * @version $Revision: 11 $
 */
public class PartnerSystemAccessDB {

    /**
     * Logger to log information to
     */
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**
     * Connection to the database
     */
    private Connection configConnection;
    private Connection runtimeConnection;

    public PartnerSystemAccessDB(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    public PartnerSystem getPartnerSystem(Partner partner) {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM partnersystem WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            result = statement.executeQuery();
            if (result.next()) {
                PartnerSystem system = new PartnerSystem();
                system.setPartner(partner);
                system.setAs2Version(result.getString("as2version"));
                system.setProductName(result.getString("productname"));
                system.setCEM(result.getInt("cem") == 1);
                system.setCompression(result.getInt("msgcompression") == 1);
                system.setMa(result.getInt("ma") == 1);
                return (system);
            }
        } catch (SQLException e) {
            this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
            return (null);
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
            return (null);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerSystemAccessDB.getPartnerSystem: " + e.getMessage());
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
        return (null);
    }

    /**
     * Updates a single partnersystem in the db
     */
    private void updatePartnerSystem(PartnerSystem system) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "UPDATE partnersystem SET as2version=?,productname=?,msgcompression=?,ma=?,cem=? WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setString(1, system.getAs2Version());
            statement.setString(2, system.getProductName());
            statement.setInt(3, system.supportsCompression() ? 1 : 0);
            statement.setInt(4, system.supportsMA() ? 1 : 0);
            statement.setInt(5, system.supportsCEM() ? 1 : 0);
            statement.setInt(6, system.getPartner().getDBId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("PartnerSystemAccessDB.updatePartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.updatePartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**
     * Deletes a single partnersystem from the database
     */
    public void deletePartnerSystem(Partner partner) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("DELETE FROM partnersystem WHERE partnerid=?");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partner.getDBId());
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("PartnerSystemAccessDB.deletePartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.deletePartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    this.logger.severe("PartnerSystemAccessDB.deletePartnerSystem: " + e.getMessage());
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
    }

    /**
     * Inserts a new entry into the database or updates an existing one
     */
    public synchronized void insertOrUpdatePartnerSystem(PartnerSystem partnerSystem) {
        PartnerSystem system = this.getPartnerSystem(partnerSystem.getPartner());
        if (system == null) {
            this.insertPartnerSystem(partnerSystem);
        } else {
            this.updatePartnerSystem(partnerSystem);
        }
    }

    /**
     * Inserts a new partner system into the database
     */
    private void insertPartnerSystem(PartnerSystem partnerSystem) {
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "INSERT INTO partnersystem(partnerid,as2version,productname,msgcompression,ma,cem)VALUES(?,?,?,?,?,?)");
            statement.setEscapeProcessing(true);
            statement.setInt(1, partnerSystem.getPartner().getDBId());
            statement.setString(2, partnerSystem.getAs2Version());
            statement.setString(3, partnerSystem.getProductName());
            statement.setInt(4, partnerSystem.supportsCompression() ? 1 : 0);
            statement.setInt(5, partnerSystem.supportsMA() ? 1 : 0);
            statement.setInt(6, partnerSystem.supportsCEM() ? 1 : 0);
            statement.execute();
        } catch (SQLException e) {
            this.logger.severe("PartnerSystemAccessDB.insertPartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
        } catch (Exception e) {
            this.logger.severe("PartnerSystemAccessDB.insertPartnerSystem: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }
}
