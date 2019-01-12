//$Header: /mendelson_business_integration/de/mendelson/util/systemevents/notification/NotificationData.java 7     19.10.18 14:32 Heller $
package de.mendelson.util.systemevents.notification;

import java.io.Serializable;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Stores the notification data for the mendelson products
 * @author S.Heller
 * @version $Revision: 7 $
 */
public abstract class NotificationData implements Serializable{

    public static final long serialVersionUID = 1L;
    
    public static final int SECURITY_PLAIN = 0;
    public static final int SECURITY_START_SSL = 1;
    public static final int SECURITY_SSL = 2;
    
    public abstract String getMailServer();
    public abstract int getMailServerPort();
    public abstract int getConnectionSecurity();
    public abstract boolean usesSMTHAuth();
    public abstract String getSMTPUser();
    public abstract char[] getSMTPPass();
    public abstract String getReplyTo();
    public abstract String getNotificationMail();
    public abstract int getMaxNotificationsPerMin();
    
}
