//$Header: /as2/de/mendelson/util/systemevents/notification/SystemEventNotificationControllerImplAS2.java 4     16.10.18 10:31 Heller $
package de.mendelson.util.systemevents.notification;

import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.systemevents.SystemEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Checks the database and switches certificates if there are two available for
 * a partner
 *
 * @author S.Heller
 * @version $Revision: 4 $
 */
public class SystemEventNotificationControllerImplAS2 extends SystemEventNotificationController {

    private PreferencesAS2 preferences = new PreferencesAS2();
    private Notification notification;

    /**
     * Controller that checks notifications and sends them out if required
     *
     * @param host host to connect to
     */
    public SystemEventNotificationControllerImplAS2(Logger logger, PreferencesAS2 preferences,
            ClientServer clientserver,
            Connection configConnection, Connection runtimeConnection) {
        super(logger, clientserver, configConnection, runtimeConnection);
        this.notification = new NotificationImplAS2(configConnection, runtimeConnection);
    }

    @Override
    public String getStorageDir() {
        return (this.preferences.get(PreferencesAS2.DIR_LOG));
    }

    @Override
    public List<SystemEvent> filterEventsForNotification(List<SystemEvent> foundSystemEvents) {
        List<SystemEvent> filteredEventsForNotification = new ArrayList<SystemEvent>();
        if (!AS2Server.inShutdownProcess) {
            NotificationDataImplAS2 notificationData = (NotificationDataImplAS2)this.notification.getNotificationData(true);            
            for (SystemEvent event : foundSystemEvents) {
                if (event.getOrigin() == SystemEvent.ORIGIN_TRANSACTION && event.getType() == SystemEvent.TYPE_TRANSACTION_ERROR) {
                    //Transaction failures
                    if (notificationData.notifyTransactionError()) {
                        filteredEventsForNotification.add(event);
                    }
                } else if (event.getOrigin() == SystemEvent.ORIGIN_SYSTEM && event.getType() == SystemEvent.TYPE_CERTIFICATE_EXPIRE) {
                    //certificate expire
                    if (notificationData.notifyCertExpire()) {
                        filteredEventsForNotification.add(event);
                    }
                } else if (event.getType() == SystemEvent.TYPE_CERTIFICATE_EXCHANGE_ANY
                        || event.getType() == SystemEvent.TYPE_CERTIFICATE_EXCHANGE_REQUEST_RECEIVED) {
                    //certificate exchange event
                    if (notificationData.notifyCEM()) {
                        filteredEventsForNotification.add(event);
                    }
                } else if (event.getType() == SystemEvent.TYPE_TRANSACTION_REJECTED_RESEND) {
                    //rejected resend
                    if (notificationData.notifySystemFailure()) {
                        filteredEventsForNotification.add(event);
                    }
                } else if (event.getSeverity() == SystemEvent.SEVERITY_ERROR && event.getOrigin() == SystemEvent.ORIGIN_SYSTEM) {
                    //system error
                    if (notificationData.notifySystemFailure()) {
                        filteredEventsForNotification.add(event);
                    }
                }
            }
        }
        return (filteredEventsForNotification);
    }

    /**Finally inform the user..*/
    @Override
    public void sendNotification( List<SystemEvent> systemEventsToNotifyUserOf){
        this.notification.sendNotification(systemEventsToNotifyUserOf);        
    }
    
}
