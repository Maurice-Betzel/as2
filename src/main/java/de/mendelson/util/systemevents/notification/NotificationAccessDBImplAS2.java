//$Header: /as2/de/mendelson/util/systemevents/notification/NotificationAccessDBImplAS2.java 4     7.11.18 17:14 Heller $
package de.mendelson.util.systemevents.notification;

import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Stores the notification data for the AS2
 *
 * @author S.Heller
 * @version $Revision: 4 $
 */
public class NotificationAccessDBImplAS2 implements NotificationAccessDB {

    /**
     * Connection to the database
     */
    private Connection configConnection = null;
    private Connection runtimeConnection = null;

    public NotificationAccessDBImplAS2(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
    }

    /**
     * Reads the notification data from the db, there is only one available
     */
    @Override
    public NotificationData getNotificationData() {
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement("SELECT * FROM notification");
            result = statement.executeQuery();
            if (result.next()) {
                NotificationDataImplAS2 data = new NotificationDataImplAS2();
                data.setMailServer(result.getString("mailhost"));
                data.setMailServerPort(result.getInt("mailhostport"));
                data.setNotificationMail(result.getString("notificationemailaddress"));
                data.setNotifyCertExpire(result.getInt("notifycertexpire") == 1 ? true : false);
                data.setNotifyTransactionError(result.getInt("notifytransactionerror") == 1 ? true : false);
                data.setNotifyCEM(result.getInt("notifycem") == 1 ? true : false);
                data.setNotifySystemFailure(result.getInt("notifysystemfailure") == 1 ? true : false);
                data.setNotifyResendDetected(result.getInt("notifyresend") == 1 ? true : false);
                data.setReplyTo(result.getString("replyto"));
                data.setUsesSMTHAuth(result.getInt("usesmtpauth") == 1 ? true : false);
                data.setSMTPUser(result.getString("smtpauthuser"));
                String smtpPass = result.getString("smtpauthpass");
                if (!result.wasNull()) {
                    data.setSMTPPass(smtpPass.toCharArray());
                }
                data.setConnectionSecurity(result.getInt("security"));
                data.setMaxNotificationsPerMin(result.getInt("maxnotificationspermin"));
                return (data);
            }
        } catch (Exception e) {
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY, statement);
            e.printStackTrace();
            return (null);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_DATABASE_ANY);
                }
            }
        }
        return (null);
    }

    /**
     * Inserts a new message entry into the database
     */
    @Override
    public void updateNotification(NotificationData notificationData) {
        NotificationDataImplAS2 data = (NotificationDataImplAS2)notificationData;
        PreparedStatement statement = null;
        try {
            statement = this.configConnection.prepareStatement(
                    "UPDATE notification SET mailhost=?,mailhostport=?,notificationemailaddress=?,"
                    + "notifycertexpire=?,notifytransactionerror=?,notifycem=?,notifysystemfailure=?,replyto=?,usesmtpauth=?,"
                    + "smtpauthuser=?,smtpauthpass=?,notifyresend=?,security=?,maxnotificationspermin=?");
            statement.setEscapeProcessing(true);
            statement.setString(1, data.getMailServer());
            statement.setInt(2, data.getMailServerPort());
            statement.setString(3, data.getNotificationMail());
            statement.setInt(4, data.notifyCertExpire() ? 1 : 0);
            statement.setInt(5, data.notifyTransactionError() ? 1 : 0);
            statement.setInt(6, data.notifyCEM() ? 1 : 0);
            statement.setInt(7, data.notifySystemFailure() ? 1 : 0);
            statement.setString(8, data.getReplyTo());
            statement.setInt(9, data.usesSMTHAuth() ? 1 : 0);
            if (data.getSMTPUser() != null) {
                statement.setString(10, data.getSMTPUser());
            } else {
                statement.setNull(10, Types.VARCHAR);
            }
            if (data.getSMTPPass() != null) {
                statement.setString(11, String.valueOf(data.getSMTPPass()));
            } else {
                statement.setNull(11, Types.VARCHAR);
            }
            statement.setInt(12, data.notifyResendDetected() ? 1 : 0);
            statement.setInt(13, data.getConnectionSecurity());
            statement.setInt(14, data.getMaxNotificationsPerMin());
            statement.execute();
        } catch (Exception e) {
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

    @Override
    public Connection getConfigConnection() {
        return (this.configConnection);
    }

    @Override
    public Connection getRuntimeConnection() {
        return (this.runtimeConnection);
    }
}
