//$Header: /as2/de/mendelson/util/systemevents/SystemEventManagerImplAS2.java 8     7.11.18 17:14 Heller $
package de.mendelson.util.systemevents;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.log.LogEntry;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.partner.PartnerCertificateInformation;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.comm.as2.statistic.QuotaAccessDB;
import de.mendelson.comm.as2.statistic.StatisticOverviewEntry;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.database.DebuggablePreparedStatement;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.util.systemevents.notification.ResourceBundleNotification;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Performs the notification for an event
 *
 * @author S.Heller
 * @version $Revision: 8 $
 */
public class SystemEventManagerImplAS2 extends SystemEventManager {

    private PreferencesAS2 preferences = new PreferencesAS2();
    /**
     * localize your output
     */
    private MecResourceBundle rb = null;


    /**
     * Constructor without notification data, will perform a lookup in the db
     */
    public SystemEventManagerImplAS2() {
        super();
        //Load resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleNotification.class.getName());
        } //load up  resourcebundle        
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    public void newEventResendDetected(AS2MessageInfo newMessageInfo, AS2MessageInfo alreadyExistingMessageInfo,
            Partner sender, Partner receiver) throws Exception {
        String template = "template_notification_resend_detected";
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", this.getHostname());
        replacement.setProperty("${EXISTING_MESSAGE_INIT_TIME}", alreadyExistingMessageInfo.getInitDate().toString());
        replacement.setProperty("${MESSAGEID}", newMessageInfo.getMessageId());
        replacement.setProperty("${SENDER}", sender.getName());
        replacement.setProperty("${RECEIVER}", receiver.getName());
        SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_WARNING,
                SystemEvent.ORIGIN_TRANSACTION,
                SystemEvent.TYPE_TRANSACTION_REJECTED_RESEND);
        event.readFromNotificationTemplate(template, replacement);
        this.storeEventToFile(event);
    }

    /**
     * The system has imported a new certificate into the SSL keystore. The SSL
     * connector has to be restarted before these changes are taken
     */
    public void newEventSSLCertificateAddedByCEM(Partner partner, KeystoreCertificate cert) throws Exception {
        String template = "template_notification_cem_ssl_cert_added";
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", this.getHostname());
        replacement.setProperty("${PARTNER}", partner.getName());
        StringBuilder techInfo = new StringBuilder();
        String alias = cert.getAlias();
        techInfo.append(alias).append(":\n");
        for (int i = 0; i < alias.length() + 1; i++) {
            techInfo.append("-");
        }
        techInfo.append("\n");
        techInfo.append(cert.getInfo());
        techInfo.append("\n");
        replacement.setProperty("${CERTIFICATETECHDETAILS}", techInfo.toString());
        SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_INFO, SystemEvent.ORIGIN_TRANSACTION, SystemEvent.TYPE_CERTIFICATE_EXCHANGE_ANY);
        event.readFromNotificationTemplate(template, replacement);
        this.storeEventToFile(event);
    }

    /**
     * The system has imported a new certificate into the SSL keystore. The SSL
     * connector has to be restarted before these changes are taken
     */
    public void newEventEncSignCertificateAddedByCEM(Partner partner, KeystoreCertificate cert) throws Exception {
        String template = "template_notification_cem_enc_sign_cert_added";
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", this.getHostname());
        replacement.setProperty("${PARTNER}", partner.getName());
        StringBuilder techInfo = new StringBuilder();
        String alias = cert.getAlias();
        techInfo.append(alias).append(":\n");
        for (int i = 0; i < alias.length() + 1; i++) {
            techInfo.append("-");
        }
        techInfo.append("\n");
        techInfo.append(cert.getInfo());
        techInfo.append("\n");
        replacement.setProperty("${CERTIFICATETECHDETAILS}", techInfo.toString());
        SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_INFO, SystemEvent.ORIGIN_TRANSACTION, SystemEvent.TYPE_CERTIFICATE_EXCHANGE_ANY);
        event.readFromNotificationTemplate(template, replacement);
        this.storeEventToFile(event);
    }

    /**
     * This is just a check routine - if the send quota has been exceeded
     *
     * @param partner
     */
    public void newEventPartnerSendQuotaExceededCheck(Partner localStation, Partner partner,
            Connection configConnection, Connection runtimeConnection) {
        StatisticOverviewEntry entry = null;
        QuotaAccessDB access = new QuotaAccessDB(configConnection, runtimeConnection);
        entry = access.getStatisticOverview(
                localStation.getAS2Identification(), partner.getAS2Identification());
        if (partner.isNotifySendEnabled() && partner.getNotifySend() == entry.getSendMessageCount()) {
            String template = "template_notification_sendquota_exceeded";
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", this.getHostname());
            replacement.setProperty("${PARTNER}", partner.getName());
            replacement.setProperty("${QUOTA}", String.valueOf(partner.getNotifySend()));
            SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_WARNING, SystemEvent.ORIGIN_SYSTEM,
                    SystemEvent.TYPE_QUOTA_SEND_EXCEEDED);
            try {
                event.readFromNotificationTemplate(template, replacement);
                this.storeEventToFile(event);
            } catch (Exception e) {
                return;
            }
        }
    }

    /**
     * Sends a notification if the receive quota has been exceeded
     *
     * @param partner
     */
    public void newEventPartnerReceiveQuotaExceededCheck(Partner localStation, Partner partner,
            Connection configConnection, Connection runtimeConnection) {
        StatisticOverviewEntry entry = null;
        QuotaAccessDB access = new QuotaAccessDB(configConnection, runtimeConnection);
        entry = access.getStatisticOverview(
                localStation.getAS2Identification(), partner.getAS2Identification());
        if (partner.isNotifyReceiveEnabled() && partner.getNotifyReceive() == entry.getReceivedMessageCount()) {
            String template = "template_notification_receivequota_exceeded";
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", this.getHostname());
            replacement.setProperty("${PARTNER}", partner.getName());
            replacement.setProperty("${QUOTA}", String.valueOf(partner.getNotifyReceive()));
            SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_WARNING, SystemEvent.ORIGIN_SYSTEM,
                    SystemEvent.TYPE_QUOTA_RECEIVE_EXCEEDED);
            try {
                event.readFromNotificationTemplate(template, replacement);
                this.storeEventToFile(event);
            } catch (Exception e) {
                return;
            }
        }
    }
    
    /**
     * There are situations where no database may be up and no notification will
     * work - like system shutdown or system startup Then use this method - it
     * will just create a new system event entry, nothing more
     */
    public static void newEvent(int severity, int origin, int type, String subject, String body) {
        SystemEvent event = new SystemEvent(severity, origin, type);
        event.setBody(body);
        event.setSubject(subject);
        newEvent(event);
    }

    /**
     * There are situations where no database may be up and no notification will
     * work - like system shutdown or system startup Then use this method - it
     * will just create a new system event entry, nothing more
     */
    public static void newEvent(SystemEvent event) {
        SystemEventManagerImplAS2 eventManager = new SystemEventManagerImplAS2();
        try {
            eventManager.storeEventToFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method to send a notification to the administrator that
     * something unexpected happened to the system. This is for a SQL related
     * problem and will display additional information about SQL parameters of
     * the query of a statement
     */
    public static void systemFailure(Throwable exception, int eventType, PreparedStatement statement) {
        if (AS2Server.inShutdownProcess) {
            return;
        }
        try {
            String template = "template_notification_systemproblem";
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", getHostname());
            String exceptionCategory = exception.getClass().getName().substring(exception.getClass().getName().lastIndexOf(".") + 1);
            replacement.setProperty("${CATEGORY}", exceptionCategory);
            replacement.setProperty("${MESSAGE}", exception.getMessage());
            StackTraceElement[] trace = exception.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : trace) {
                builder.append(element.toString());
                builder.append("\n");
            }
            if (exception instanceof SQLException && statement != null && statement instanceof DebuggablePreparedStatement) {
                builder.append("\n");
                DebuggablePreparedStatement debug = (DebuggablePreparedStatement) statement;
                builder.append(debug.getQueryWithParameter());
                builder.append("\n");
            }
            replacement.setProperty("${DETAILS}", builder.toString());
            SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_ERROR, SystemEvent.ORIGIN_SYSTEM, eventType);
            event.readFromNotificationTemplate(template, replacement);
            SystemEventManagerImplAS2 eventManager = new SystemEventManagerImplAS2();
            eventManager.storeEventToFile(event);
        } catch (Throwable e) {
            return;
        }
    }

    /**
     * Convenience method to send a notification to the administrator that
     * something unexpected happened to the system
     */
    public static void systemFailure(Throwable exception, int eventType) {
        if (AS2Server.inShutdownProcess) {
            return;
        }
        systemFailure(exception, eventType);
    }

    /**
     * Convenience method to send a notification to the administrator that
     * something unexpected happened to the system
     */
    public static void systemFailure(Throwable exception) {
        if (AS2Server.inShutdownProcess) {
            return;
        }
        systemFailure(exception, SystemEvent.TYPE_OTHER);
    }
    
    /**
     * Sends a notification if the receive quota has been exceeded
     *
     * @param partner
     */
    public void newEventPartnerSendReceiveQuotaExceededCheck(Partner localStation, Partner partner,
            Connection configConnection, Connection runtimeConnection) {
        StatisticOverviewEntry entry = null;
        QuotaAccessDB access = new QuotaAccessDB(configConnection, runtimeConnection);
        entry = access.getStatisticOverview(
                localStation.getAS2Identification(), partner.getAS2Identification());
        if (partner.isNotifySendReceiveEnabled() && partner.getNotifySendReceive() == entry.getSendMessageCount() + entry.getReceivedMessageCount()) {
            String template = "template_notification_sendreceivequota_exceeded";
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", this.getHostname());
            replacement.setProperty("${PARTNER}", partner.getName());
            replacement.setProperty("${QUOTA}", String.valueOf(partner.getNotifyReceive()));
            SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_WARNING, SystemEvent.ORIGIN_SYSTEM,
                    SystemEvent.TYPE_QUOTA_SEND_RECEIVE_EXCEEDED);
            try {
                event.readFromNotificationTemplate(template, replacement);
                this.storeEventToFile(event);
            } catch (Exception e) {
                return;
            }
        }
    }

    /**
     * Sends an email that a certification will expire
     */
    public void newEventCertificateWillExpire(KeystoreCertificate certificate, int expireDuration) throws Exception {
        String templateName = "template_notification_cert_expire";
        if (expireDuration <= 0) {
            templateName = templateName + "d";
        }
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", this.getHostname());
        if (expireDuration >= 0) {
            replacement.setProperty("${DURATION}", String.valueOf(expireDuration));
        }
        replacement.setProperty("${ALIAS}", certificate.getAlias());
        SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_INFO, SystemEvent.ORIGIN_SYSTEM, SystemEvent.TYPE_CERTIFICATE_EXPIRE);
        event.readFromNotificationTemplate(templateName, replacement);
        this.storeEventToFile(event);
    }

    /**
     * Sends an email that a CEM has been received
     */
    public void newEventCEMRequestReceived(Partner initiator, String requestId) throws Exception {
        String templateName = "template_notification_cem_request_received";
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", this.getHostname());
        replacement.setProperty("${PARTNER}", initiator.getName());
        replacement.setProperty("${REQUEST_ID}", requestId);
        SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_INFO, 
                SystemEvent.ORIGIN_TRANSACTION, 
                SystemEvent.TYPE_CERTIFICATE_EXCHANGE_REQUEST_RECEIVED);
        event.readFromNotificationTemplate(templateName, replacement);
        this.storeEventToFile(event);
    }

    /**
     * Sends an email that a the date of a certificate change event happens
     */
    public void newEventCertificateChangeShouldHappenNowByCEM(CertificateManager manager, Partner partner, int category) throws Exception {
        String templateName = "template_notification_cem_cert_changed";
        Properties replacement = new Properties();
        replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
        replacement.setProperty("${HOST}", this.getHostname());
        replacement.setProperty("${PARTNER}", partner.getName());
        String description = partner.getPartnerCertificateInformationList().getCertificatePurposeDescription(
                manager, partner, category);
        replacement.setProperty("${CERTIFICATEDESCRIPTION}", description);
        StringBuilder techInfo = new StringBuilder();
        PartnerCertificateInformation info = partner.getCertificateInformation(category);
        if (info != null) {
            KeystoreCertificate certificate = manager.getKeystoreCertificateByFingerprintSHA1(info.getFingerprintSHA1());
            if (certificate != null) {
                String alias = certificate.getAlias();
                techInfo.append(alias).append(":\n");
                for (int i = 0; i < alias.length() + 1; i++) {
                    techInfo.append("-");
                }
                techInfo.append("\n");
                techInfo.append(certificate.getInfo());
                techInfo.append("\n");
            }
        }
        replacement.setProperty("${CERTIFICATETECHDETAILS}", techInfo.toString());
        SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_INFO, 
                SystemEvent.ORIGIN_SYSTEM, 
                SystemEvent.TYPE_CERTIFICATE_EXCHANGE_ANY);
        event.readFromNotificationTemplate(templateName, replacement);
        this.storeEventToFile(event);
    }

    /**
     * Returns the message info object a a passed message id from the database
     */
    private AS2MessageInfo lookupMessageInfo(String messageId, Connection configConnection, Connection runtimeConnection) throws Exception {
        MessageAccessDB messageAccess = new MessageAccessDB(configConnection, runtimeConnection);
        AS2MessageInfo info = messageAccess.getLastMessageEntry(messageId);
        if (info == null) {
            throw new Exception("No message entry found for " + messageId);
        }
        return (info);
    }

    /**
     * Sends an email that an error occurred in a transaction
     */
    public void newEventTransactionError(String messageId, Connection configConnection, Connection runtimeConnection) {
        AS2MessageInfo info = null;
        try {
            //get additional properties for the notification eMail
            String senderName = "Unknown";
            String receiverName = "Unknown";
            info = this.lookupMessageInfo(messageId, configConnection, runtimeConnection);
            //lookup partner            
            PartnerAccessDB partnerAccess = new PartnerAccessDB(configConnection, runtimeConnection);
            if (info.getSenderId() != null) {
                Partner sender = partnerAccess.getPartner(info.getSenderId());
                if (sender != null) {
                    senderName = sender.getName();
                }
            }
            if (info.getReceiverId() != null) {
                Partner receiver = partnerAccess.getPartner(info.getReceiverId());
                if (receiver != null) {
                    receiverName = receiver.getName();
                }
            }
            LogAccessDB logAccess = new LogAccessDB(configConnection, runtimeConnection);
            StringBuilder log = new StringBuilder();
            List<LogEntry> entries = logAccess.getLog(messageId);
            DateFormat format = DateFormat.getDateTimeInstance();
            for (LogEntry entry : entries) {
                if (log.length() > 0) {
                    log.append("\n");
                }
                log.append("[").append(format.format(new Date(entry.getMillis()))).append("] ");
                log.append(entry.getMessage());
            }
            String templateName = "template_notification_transaction_error";
            Properties replacement = new Properties();
            replacement.setProperty("${PRODUCTNAME}", AS2ServerVersion.getProductName());
            replacement.setProperty("${HOST}", this.getHostname());
            replacement.setProperty("${MESSAGEID}", messageId);
            replacement.setProperty("${SENDER}", senderName);
            replacement.setProperty("${RECEIVER}", receiverName);
            replacement.setProperty("${LOG}", log.toString());
            if (info.getSubject() != null) {
                replacement.setProperty("${SUBJECT}", info.getSubject());
            } else {
                replacement.setProperty("${SUBJECT}", "");
            }
            SystemEvent event = new SystemEvent(SystemEvent.SEVERITY_ERROR, SystemEvent.ORIGIN_TRANSACTION, SystemEvent.TYPE_TRANSACTION_ERROR);
            event.readFromNotificationTemplate(templateName, replacement);
            this.storeEventToFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Path getStorageMainDir() {
        Path storageDir = Paths.get(this.preferences.get(PreferencesAS2.DIR_LOG));
        return (storageDir);
    }

}
