//$Header: /as2/de/mendelson/comm/as2/timing/MessageDeleteController.java 31    7.11.18 17:14 Heller $
package de.mendelson.comm.as2.timing;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.log.LogAccessDB;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.preferences.ResourceBundlePreferences;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Controlles the timed deletion of AS2 entries from the log
 *
 * @author S.Heller
 * @version $Revision: 31 $
 */
public class MessageDeleteController {

    /**
     * Logger to log information to
     */
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private PreferencesAS2 preferences = new PreferencesAS2();
    private MessageDeleteThread deleteThread;
    private ClientServer clientserver = null;
    private MecResourceBundle rb = null;
    private MecResourceBundle rbTime = null;
    private Connection configConnection;
    private Connection runtimeConnection;

    public MessageDeleteController(ClientServer clientserver, Connection configConnection,
            Connection runtimeConnection) {
        this.clientserver = clientserver;
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleMessageDeleteController.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        try {
            this.rbTime = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePreferences.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**
     * Starts the embedded task that guards the log
     */
    public void startAutoDeleteControl() {
        this.deleteThread = new MessageDeleteThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.deleteThread);
    }

    /**
     * Deletes a message entry from the log. Clears all files
     */
    public void deleteMessageFromLog(AS2MessageInfo info, boolean broadcastRefresh) {
        LogAccessDB logAccess = new LogAccessDB(this.configConnection, this.runtimeConnection);
        logAccess.deleteMessageLog(info.getMessageId());
        MessageAccessDB messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        try {
            //delete all raw files from the disk
            List<String> rawfilenames = messageAccess.getRawFilenamesToDelete(info);
            if (rawfilenames != null) {
                for (String rawfilename : rawfilenames) {
                    try {
                        Files.delete(Paths.get(rawfilename));
                    } catch (Exception e) {
                        SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_FILE_OPERATION_ANY);
                        new File(rawfilename).deleteOnExit();
                    }
                }
            }
            messageAccess.deleteMessage(info);
        } catch (Exception e) {
            this.logger.severe("deleteMessageFromLog: " + e.getMessage());
            SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_PROCESSING_ANY);
        }
        if (broadcastRefresh && this.clientserver != null) {
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        }
    }

    public class MessageDeleteThread implements Runnable {

        private boolean stopRequested = false;
        //wait this time between checks
        private final long WAIT_TIME = TimeUnit.MINUTES.toMillis(1);
        private int minutesSinceLastDelete = 0;
        //DB connection
        private Connection configConnection;
        private Connection runtimeConnection;

        public MessageDeleteThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Contol auto as2 message delete");
            while (!stopRequested) {
                try {
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        //nop
                    }
                    if (preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE)) {
                        //if the user entered minutes into the delete settings the system might be under heavy load and
                        //it might be required to run this message delete attempt once a minute. But else once an hour should be enough
                        int timebase = preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S);
                        if (timebase == TimeUnit.MINUTES.toSeconds(1) || minutesSinceLastDelete > 59) {
                            minutesSinceLastDelete = 0;
                            MessageAccessDB messageAccess = null;
                            try {
                                messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
                                long olderThan = System.currentTimeMillis()
                                        - TimeUnit.SECONDS.toMillis(
                                                preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN)
                                                * preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S));
                                List<AS2MessageInfo> overviewList = messageAccess.getMessagesOlderThan(olderThan, -1);
                                if (overviewList != null) {
                                    List<AS2MessageInfo> deletedTransactionList = new ArrayList<AS2MessageInfo>();
                                    for (AS2MessageInfo messageInfo : overviewList) {
                                        if (messageInfo.getState() == AS2Message.STATE_FINISHED || messageInfo.getState() == AS2Message.STATE_STOPPED) {
                                            if (preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE_LOG)) {
                                                String timeUnit = "";
                                                if (preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S) == TimeUnit.DAYS.toSeconds(1)) {
                                                    timeUnit = rbTime.getResourceString("maintenancemultiplier.day");
                                                } else if (preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S) == TimeUnit.HOURS.toSeconds(1)) {
                                                    timeUnit = rbTime.getResourceString("maintenancemultiplier.hour");
                                                } else if (preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S) == TimeUnit.MINUTES.toSeconds(1)) {
                                                    timeUnit = rbTime.getResourceString("maintenancemultiplier.minute");
                                                }
                                                logger.fine(rb.getResourceString("autodelete",
                                                        new Object[]{
                                                            messageInfo.getMessageId(),
                                                            String.valueOf(preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN)),
                                                            timeUnit
                                                        }));
                                            }
                                            deleteMessageFromLog(messageInfo, true);
                                            deletedTransactionList.add(messageInfo);
                                        }
                                    }
                                    //fire system event
                                    if (!deletedTransactionList.isEmpty()) {
                                        this.fireSystemEventTransactionsDeletedByMaintenance(deletedTransactionList);
                                    }
                                }
                            } catch (Throwable e) {
                                logger.severe("MessageDeleteThread: " + e.getMessage());
                                SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_PROCESSING_ANY);
                            }
                        } else {
                            minutesSinceLastDelete++;
                        }
                    }
                } catch (Throwable e) {
                    //final try/catch - this thead must not stop!
                    logger.severe("MessageDeleteThread: " + e.getMessage());
                    SystemEventManagerImplAS2.systemFailure(e, SystemEvent.TYPE_PROCESSING_ANY);
                }
            }
        }

        /**
         * Fire a system event that the system maintenance process has deleted
         * transactions
         */
        private void fireSystemEventTransactionsDeletedByMaintenance(List<AS2MessageInfo> deletedTransactionList) {
            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
            SystemEvent event = new SystemEvent(
                    SystemEvent.SEVERITY_INFO,
                    SystemEvent.ORIGIN_SYSTEM,
                    SystemEvent.TYPE_TRANSACTION_DELETE);
            event.setSubject(rb.getResourceString("transaction.deleted.system"));
            StringBuilder builder = new StringBuilder();
            for (AS2MessageInfo info : deletedTransactionList) {
                builder.append("[");
                builder.append(rb.getResourceString("transaction.deleted.transactiondate",
                        dateFormat.format(info.getInitDate())));
                builder.append("] (");
                builder.append(info.getSenderId());
                builder.append(" --> ");
                builder.append(info.getReceiverId());
                builder.append(") ");
                builder.append(info.getMessageId());
                builder.append(System.lineSeparator());
            }
            event.setBody(builder.toString());
            SystemEventManagerImplAS2.newEvent(event);
        }

    }
}
