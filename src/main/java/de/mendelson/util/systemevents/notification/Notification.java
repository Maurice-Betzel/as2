//$Header: /as2/de/mendelson/util/systemevents/notification/Notification.java 19    13.12.18 15:42 Heller $
package de.mendelson.util.systemevents.notification;

import de.mendelson.util.systemevents.SystemEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


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
 * @version $Revision: 19 $
 */
public abstract class Notification {

    /**
     * Stores the connection data and notification eMail
     */
    private NotificationData notificationData;
    private Logger logger = Logger.getAnonymousLogger();
    private NotificationAccessDB notificationAccess;

    /**
     * Will not perform a lookup in the db but take the passed notification data
     * object
     */
    public Notification(NotificationData notificationData, NotificationAccessDB notificationAccess) {
        this.notificationData = notificationData;
        this.notificationAccess = notificationAccess;
    }

    /**
     * Constructor without notification data, will perform a lookup in the db
     */
    public Notification(NotificationAccessDB notificationAccess) {
        this.notificationAccess = notificationAccess;
        this.notificationData = this.notificationAccess.getNotificationData();
        if (notificationData == null) {
            throw new RuntimeException("Unable to read the notification settings");
        }
    }

    /**
     * Sends a test notification
     *
     */
    public abstract void sendTest(String userName, String processOriginHost) throws Exception;

    /**
     * Generates a subject line addition that has the format "[<product>@<hostname]"
     */
    public abstract String getNotificationSubjectServerIdentification();

    protected NotificationData getNotificationData(boolean reload) {
        if (reload) {
            synchronized (this) {
                this.notificationData = this.notificationAccess.getNotificationData();
            }
        }
        return (this.notificationData);
    }

    //should mainly be implemented by the code rb.getResourceString("test.message.debug")
    public abstract String getTestMessageDebugStr();

    /**
     * Returns the footer that should be added to the notification mail
     */
    public abstract String getNotificationFooter();

    /**
     * Returns the default session for the mail send process
     */
    private Session getDefaultSession() {
        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", this.notificationData.getMailServer());
        properties.setProperty("mail.smtp.port", String.valueOf(this.notificationData.getMailServerPort()));
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.connectiontimeout", String.valueOf(TimeUnit.SECONDS.toMillis(10)));
        properties.setProperty("mail.smtp.timeout", String.valueOf(TimeUnit.SECONDS.toMillis(10)));
        if (this.notificationData.getConnectionSecurity() == NotificationData.SECURITY_START_SSL) {
            properties.setProperty("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.ssl.protocols", "SSLv3 TLSv1 TLSv1.1 TLSv1.2 TLSv1.3");
        } else if (this.notificationData.getConnectionSecurity() == NotificationData.SECURITY_SSL) {
            properties.setProperty("mail.smtp.ssl.protocols", "SSLv3 TLSv1 TLSv1.1 TLSv1.2 TLSv1.3");
            properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(this.notificationData.getMailServerPort()));
        }
        Session session = null;
        if (this.notificationData.usesSMTHAuth()) {
            properties.put("mail.smtp.auth", "true");
            session = Session.getInstance(properties,
                    new SendMailAuthenticator(this.notificationData.getSMTPUser(),
                            String.valueOf(this.notificationData.getSMTPPass())));
        } else {
            session = Session.getInstance(properties, null);
        }
        return (session);
    }

    /**
     * Sends out the notification to the user
     */
    public abstract void sendNotification(List<SystemEvent> systemEventsToNotifyUserOf);

    @SuppressWarnings("static-access")
    protected void sendMail(String productName, SystemEvent event) throws Exception {
        boolean debug = false;

        Session session = this.getDefaultSession();
        ByteArrayOutputStream debugOut = new ByteArrayOutputStream();
        PrintStream debugPrintStream = new PrintStream(debugOut);
        if (debug) {
            session.setDebug(true);
            session.setDebugOut(debugPrintStream);
        }

        // construct the message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(this.notificationData.getReplyTo()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.notificationData.getNotificationMail(), false));
        String subject = event.getSubject();
        if (subject == null) {
            subject = "";
        }
        //add the server identification to the subject before performing the notification
        if (!subject.startsWith(this.getNotificationSubjectServerIdentification())) {
            subject = this.getNotificationSubjectServerIdentification() + " " + subject;
        }
        msg.setSubject(subject);
        String bodyText = event.getBody();
        String footer = this.getNotificationFooter();
        if (footer != null && !footer.trim().isEmpty()) {
            bodyText = bodyText
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "--"
                    + System.lineSeparator()
                    + footer;
        }
        msg.setText(bodyText);
        msg.setSentDate(new Date());
        msg.setHeader("X-Mailer", productName);
        // send the message
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.send(msg);
        } catch (Exception e) {
            if (e instanceof SendFailedException) {
                SendFailedException sendFailedException = (SendFailedException) e;
                Address failedAddresses[] = sendFailedException.getInvalidAddresses();
                StringBuilder errorMessage = new StringBuilder();
                if (failedAddresses != null) {
                    errorMessage.append("The following mail addresses are invalid:").append("\n");
                    for (Address address : failedAddresses) {
                        errorMessage.append(address.toString()).append("\n");
                    }
                }
                Address validUnsentAddresses[] = sendFailedException.getValidUnsentAddresses();
                if (validUnsentAddresses != null) {
                    errorMessage.append("No mail has been sent to the following valid addresses:").append("\n");
                    for (Address address : validUnsentAddresses) {
                        errorMessage.append(address.toString()).append("\n");
                    }
                }
                StringBuilder errorLog = new StringBuilder();
                errorLog.append("[");
                errorLog.append(sendFailedException.getClass().getSimpleName());
                errorLog.append("] ");
                errorLog.append(sendFailedException.getMessage()).append("\n");
                errorLog.append(errorMessage.toString());
                Exception detailledException = new Exception(errorLog.toString(), e);
                throw (detailledException);
            } else {
                StringBuilder errorLog = new StringBuilder();
                errorLog.append(this.getTestMessageDebugStr());
                errorLog.append(debugOut.toString());
                errorLog.append("\n[");
                errorLog.append(e.getClass().getSimpleName());
                errorLog.append("] ");
                errorLog.append(e.getMessage());
                if (e.getCause() != null) {
                    errorLog.append(" - caused by [" + e.getCause().getClass().getName() + "] ");
                    errorLog.append(e.getCause().getMessage());
                    if (e.getCause() instanceof SocketTimeoutException) {
                        errorLog.append("\nThere listens a server on the SMTP host \"" + this.notificationData.getMailServer()
                                + ":" + this.notificationData.getMailServerPort() + "\" but this seems either not to be a mail server "
                                + "or it does not answer to any request.");
                    }
                    errorLog.append("\nKey data for the connection:\n");
                    errorLog.append("mail.smtp.host: " + session.getProperty("mail.smtp.host")).append("\n");
                    errorLog.append("mail.smtp.port: " + session.getProperty("mail.smtp.port")).append("\n");
                    errorLog.append("mail.smtp.auth: " + session.getProperty("mail.smtp.auth")).append("\n");
                    if (session.getProperty("mail.smtp.auth").equalsIgnoreCase("true")) {
                        errorLog.append("SMTP user: " + this.notificationData.getSMTPUser()).append("\n");
                    }
                }
                Exception detailledException = new Exception(errorLog.toString(), e);
                throw (detailledException);
            }
        } finally {
            transport.close();
            if (debugOut != null) {
                debugOut.close();
            }
        }
    }

    /**
     * @return the configConnection
     */
    protected Connection getConfigConnection() {
        return (this.notificationAccess.getConfigConnection());
    }

    /**
     * @return the runtimeConnection
     */
    protected Connection getRuntimeConnection() {
        return (this.notificationAccess.getRuntimeConnection());
    }

    /**
     * Used for the SMTP authentication, this is required by some mail servers
     */
    private static class SendMailAuthenticator extends Authenticator {

        private String user;
        private String password;

        public SendMailAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                    this.user, this.password);
        }
    }
}
