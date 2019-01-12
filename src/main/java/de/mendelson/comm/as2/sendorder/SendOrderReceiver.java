//$Header: /as2/de/mendelson/comm/as2/sendorder/SendOrderReceiver.java 18    6.12.18 16:26 Heller $
package de.mendelson.comm.as2.sendorder;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.message.AS2MDNInfo;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.ExecuteShellCommand;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.send.HttpConnectionParameter;
import de.mendelson.comm.as2.send.MessageHttpUploader;
import de.mendelson.comm.as2.send.NoConnectionException;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Receiver class that enqueues send orders
 *
 * @author S.Heller
 * @version $Revision: 18 $
 */
public class SendOrderReceiver implements Runnable {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private MecResourceBundle rb;
    private Connection configConnection;
    private Connection runtimeConnection;
    private SendOrderAccessDB sendOrderAccess;
    /**
     * Thread will stop if this is no longer set
     */
    private boolean runPermission = true;
    /**
     * Needed for refresh
     */
    private ClientServer clientserver = null;
    /**
     * Server preferences
     */
    private PreferencesAS2 preferences = new PreferencesAS2();
    /**
     * Handles messages storage
     */
    private MessageStoreHandler messageStoreHandler;
    private MessageAccessDB messageAccess;

    public SendOrderReceiver(Connection configConnection, Connection runtimeConnection,
            ClientServer clientserver) {
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleSendOrderReceiver.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.sendOrderAccess = new SendOrderAccessDB(this.configConnection, this.runtimeConnection);
        this.messageAccess = new MessageAccessDB(this.configConnection, this.runtimeConnection);
        this.messageStoreHandler = new MessageStoreHandler(this.configConnection, this.runtimeConnection);
        this.clientserver = clientserver;
    }

    /**
     * Stops the listener
     */
    public void stopReceiver() {
        this.runPermission = false;
    }

    @Override
    public void run() {
        int maxOutboundConnections = this.preferences.getInt(PreferencesAS2.MAX_OUTBOUND_CONNECTIONS);
        if (maxOutboundConnections == 0) {
            this.logger.config(this.rb.getResourceString("as2.send.disabled"));
        }
        //Max number of outbound connections. All other connection attempts are scheduled in a queue
        //Queue to store the (not active) threads
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        //This is a fixed thread executor - it will not use the queue
        //Parameter for pool executor: corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue        
        ThreadPoolExecutor threadExecutor
                = new ThreadPoolExecutor(Math.max(maxOutboundConnections, 1), Math.max(maxOutboundConnections, 1),
                        0L, TimeUnit.MILLISECONDS, queue);
        //listen until send stop is requested
        long lastConfigCheckTime = System.currentTimeMillis();
        //stores the time a warning ist last displayed that all outbound connections are used
        long lastWarningMaxOutboundConnectionsReachedTime = System.currentTimeMillis();
        while (this.runPermission) {
            //this try is necessary because this thread must never stop. If it stops no more messages
            //and MDN are send!
            try {
                //read new orders from the database if there are free possible outbound connections
                if (System.currentTimeMillis() - lastConfigCheckTime > TimeUnit.SECONDS.toMillis(10)) {
                    int activeConnections = threadExecutor.getActiveCount();
                    //check if the user has changed the outbound connection settings
                    int maxOutboundConnectionsNew = this.preferences.getInt(PreferencesAS2.MAX_OUTBOUND_CONNECTIONS);
                    if (maxOutboundConnectionsNew != maxOutboundConnections) {
                        try {
                            maxOutboundConnections = maxOutboundConnectionsNew;
                            if (maxOutboundConnections > 0) {
                                if (threadExecutor.getCorePoolSize() != maxOutboundConnections) {
                                    threadExecutor.setCorePoolSize(maxOutboundConnections);
                                    if (threadExecutor.getMaximumPoolSize() != maxOutboundConnections) {
                                        threadExecutor.setMaximumPoolSize(maxOutboundConnections);
                                    }
                                }
                                this.logger.config(this.rb.getResourceString("as2.send.newmaxconnections", String.valueOf(maxOutboundConnections)));
                            } else {
                                this.logger.config(this.rb.getResourceString("as2.send.disabled"));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    if (activeConnections > maxOutboundConnections) {
                        this.logger.config(this.rb.getResourceString("send.connectionsstillopen",
                                new Object[]{
                                    String.valueOf(maxOutboundConnections),
                                    String.valueOf(activeConnections)
                                }));
                    }
                    lastConfigCheckTime = System.currentTimeMillis();
                }
                //check if new outbound connection are currently possible
                int possibleNewConnections = maxOutboundConnections - threadExecutor.getActiveCount();
                if (possibleNewConnections > 0) {
                    //Get max number of outbound send orders and pass them to the thread executor
                    List<SendOrder> waitingOrders = this.sendOrderAccess.getNext(possibleNewConnections);
                    for (SendOrder order : waitingOrders) {
                        final SendOrder finalOrder = order;
                        final int finalMaxOutboundConnectionCount = maxOutboundConnections;
                        final ThreadPoolExecutor finalFixedThreadExecutor = threadExecutor;
                        Runnable connectionRunner = new Runnable() {
                            @Override
                            public void run() {
                                final int finalActiveConnectionCount = finalFixedThreadExecutor.getActiveCount();
                                processOrder(finalOrder, finalMaxOutboundConnectionCount, finalActiveConnectionCount);
                            }
                        };
                        threadExecutor.execute(connectionRunner);
                    }
                }else{
                    if (System.currentTimeMillis() - lastWarningMaxOutboundConnectionsReachedTime > TimeUnit.MINUTES.toMillis(1)) {
                        this.logger.warning(this.rb.getResourceString("warning.nomore.outbound.connections.available",
                                new Object[]{
                                    String.valueOf(maxOutboundConnections),                                    
                                }));
                        lastWarningMaxOutboundConnectionsReachedTime = System.currentTimeMillis();
                    }
                }
                Thread.sleep(TimeUnit.MILLISECONDS.toMillis(200));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes a single send oder
     */
    private void processOrder(SendOrder order,
            int maxOutboundConnectionsCount,
            int activeConnectionsCount) {
        try {
            boolean processingAllowed = true;
            //before performing the send there has to be checked if the send process is still valid. The orders
            //are queued, between scheduling and processing the orders the transmission time could expire
            //or the user could cancel it
            if (order.getMessage().isMDN()) {
                //if the MDN state is on failure then the related transmission is on failure state, too - 
                //checking this makes no sense here
                AS2MDNInfo mdnInfo = (AS2MDNInfo) order.getMessage().getAS2Info();
                AS2MessageInfo relatedMessageInfo = messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
                if (relatedMessageInfo == null) {
                    processingAllowed = false;
                }
            } else {
                AS2MessageInfo messageInfo = (AS2MessageInfo) order.getMessage().getAS2Info();
                if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_AS2) {
                    //update the message info from the database
                    messageInfo = this.messageAccess.getLastMessageEntry(messageInfo.getMessageId());
                    if (messageInfo == null || messageInfo.getState() == AS2Message.STATE_STOPPED) {
                        processingAllowed = false;
                    }
                } else if (messageInfo.getMessageType() == AS2Message.MESSAGETYPE_CEM) {
                    processingAllowed = true;
                }
            }
            if (processingAllowed) {
                //display some log information that the outbound connection is prepared
                if (order.getMessage().isMDN()) {
                    AS2MDNInfo mdnInfo = (AS2MDNInfo) order.getMessage().getAS2Info();
                    logger.log(Level.INFO, rb.getResourceString("outbound.connection.prepare.mdn",
                            new Object[]{
                                order.getReceiver().getMdnURL(),
                                String.valueOf(activeConnectionsCount),
                                String.valueOf(maxOutboundConnectionsCount),}), mdnInfo);
//                    logger.log(Level.INFO, mdnInfo.getMessageId() + ": Sleeping for 1 min...", mdnInfo);
//                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
//                    logger.log(Level.INFO, mdnInfo.getMessageId() + ": Woke up...", mdnInfo);
                } else {
                    //its a AS2 message that has been sent
                    AS2MessageInfo messageInfo = (AS2MessageInfo) order.getMessage().getAS2Info();
                    this.messageAccess.initializeOrUpdateMessage(messageInfo);
                    this.logger.log(Level.INFO, rb.getResourceString("outbound.connection.prepare.message",
                            new Object[]{
                                order.getReceiver().getURL(),
                                String.valueOf(activeConnectionsCount),
                                String.valueOf(maxOutboundConnectionsCount),}), messageInfo);
//                    logger.log(Level.INFO, messageInfo.getMessageId() + ": Sleeping for 1 min...", messageInfo);
//                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
//                    logger.log(Level.INFO, messageInfo.getMessageId() + ": Woke up...", messageInfo);
                }
                MessageHttpUploader messageUploader = new MessageHttpUploader();
                if (!this.preferences.getBoolean(PreferencesAS2.CEM)) {
                    messageUploader.setEDIINTFeatures("multiple-attachments");
                } else {
                    messageUploader.setEDIINTFeatures("multiple-attachments, CEM");
                }
                messageUploader.setLogger(this.logger);
                messageUploader.setAbstractServer(this.clientserver);
                messageUploader.setDBConnection(this.configConnection, this.runtimeConnection);
                //configure the connection parameters
                HttpConnectionParameter connectionParameter = new HttpConnectionParameter();
                connectionParameter.setConnectionTimeoutMillis(this.preferences.getInt(PreferencesAS2.HTTP_SEND_TIMEOUT));
                connectionParameter.setHttpProtocolVersion(order.getReceiver().getHttpProtocolVersion());
                connectionParameter.setProxy(messageUploader.createProxyObjectFromPreferences());
                connectionParameter.setUseExpectContinue(true);
                Properties requestHeader = messageUploader.upload(connectionParameter, order.getMessage(), order.getSender(), order.getReceiver());
                //set error or finish state, remember that this send order could be
                //also an MDN if async MDN is requested
                if (order.getMessage().isMDN()) {
                    AS2MDNInfo mdnInfo = (AS2MDNInfo) order.getMessage().getAS2Info();
                    if (mdnInfo.getState() == AS2Message.STATE_FINISHED) {
                        AS2MessageInfo relatedMessageInfo = messageAccess.getLastMessageEntry(mdnInfo.getRelatedMessageId());
                        this.messageStoreHandler.movePayloadToInbox(relatedMessageInfo.getMessageType(), mdnInfo.getRelatedMessageId(),
                                order.getSender(), order.getReceiver());
                        //execute a shell command after send SUCCESS
                        ExecuteShellCommand executeCommand = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                        //switch sender and receiver because its the MDN sender that is requested, not the message sender
                        executeCommand.executeShellCommandOnReceipt(order.getReceiver(), order.getSender(), relatedMessageInfo);
                    }
                    //set the transaction state to the MDN state
                    messageAccess.setMessageState(mdnInfo.getRelatedMessageId(), mdnInfo.getState());
                } else {
                    //its a AS2 message that has been sent
                    AS2MessageInfo messageInfo = (AS2MessageInfo) order.getMessage().getAS2Info();
                    messageAccess.setMessageSendDate(messageInfo);
                    messageAccess.updateFilenames(messageInfo);
                    if (!messageInfo.requestsSyncMDN()) {
                        long endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(preferences.getInt(PreferencesAS2.ASYNC_MDN_TIMEOUT));
                        DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,
                                DateFormat.MEDIUM);
                        logger.log(Level.INFO, rb.getResourceString("async.mdn.wait",
                                new Object[]{
                                   format.format(endTime)
                                }), messageInfo);
                    }
                }
            }
            //even if a processing was not possible: delete the sendorder
            this.sendOrderAccess.delete(order.getDbId());
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        } catch (NoConnectionException e) {
            int retryCount = order.incRetryCount();
            int maxRetryCount = this.preferences.getInt(PreferencesAS2.MAX_CONNECTION_RETRY_COUNT);
            //to many retries: cancel the transaction
            if (retryCount > maxRetryCount) {
                logger.log(Level.SEVERE, e.getMessage(), order.getMessage().getAS2Info());
                logger.log(Level.SEVERE, rb.getResourceString("max.retry.reached"), order.getMessage().getAS2Info());
                this.processUploadError(order);
            } else {
                logger.log(Level.WARNING, e.getMessage(), order.getMessage().getAS2Info());
                logger.log(Level.WARNING, rb.getResourceString("retry",
                        new Object[]{
                            String.valueOf(this.preferences.getInt(PreferencesAS2.CONNECTION_RETRY_WAIT_TIME_IN_S)),
                            String.valueOf(retryCount),
                            String.valueOf(maxRetryCount)
                        }), order.getMessage().getAS2Info());
                this.sendOrderToRetry(order);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), order.getMessage().getAS2Info());
            this.processUploadError(order);
        }
    }

    /**
     * Update the order in the queue - with a new nextexecution time
     */
    private void sendOrderToRetry(SendOrder order) {
        SendOrderSender sender = null;
        sender = new SendOrderSender(this.configConnection, this.runtimeConnection);
        sender.resend(order, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.preferences.getInt(PreferencesAS2.CONNECTION_RETRY_WAIT_TIME_IN_S)));
    }

    /**
     * The upload process of the data failed. Set the message state, execute the
     * command, ..
     */
    private void processUploadError(SendOrder order) {
        try {
            //stores
            this.messageStoreHandler.storeSentErrorMessage(
                    order.getMessage(), order.getSender(), order.getReceiver());
            if (!order.getMessage().isMDN()) {
                //message upload failure
                messageAccess.setMessageState(order.getMessage().getAS2Info().getMessageId(),
                        AS2Message.STATE_STOPPED);
                //its important to set the state in the message info, too. An event exec is not performed
                //for pending messages
                order.getMessage().getAS2Info().setState(AS2Message.STATE_STOPPED);
                messageAccess.updateFilenames((AS2MessageInfo) order.getMessage().getAS2Info());
                //execute a shell command after send ERROR if this is configured (sync MDN, async MDN)
                ExecuteShellCommand executor = new ExecuteShellCommand(this.configConnection, this.runtimeConnection);
                executor.executeShellCommandOnSend((AS2MessageInfo) order.getMessage().getAS2Info(), null);
                //write status file
                this.messageStoreHandler.writeOutboundStatusFile((AS2MessageInfo) order.getMessage().getAS2Info());
            } else {
                //MDN send failure, e.g. wrong URL for async MDN in message
                messageAccess.setMessageState(((AS2MDNInfo) order.getMessage().getAS2Info()).getRelatedMessageId(),
                        AS2Message.STATE_STOPPED);
            }
            this.sendOrderAccess.delete(order.getDbId());
            this.clientserver.broadcastToClients(new RefreshClientMessageOverviewList());
        } catch (Exception ee) {
            ee.printStackTrace();
            logger.log(Level.SEVERE, "SendOrderReceiver.processUploadError(): " + ee.getMessage(),
                    order.getMessage().getAS2Info());
            this.messageAccess.setMessageState(order.getMessage().getAS2Info().getMessageId(), AS2Message.STATE_STOPPED);
        }
    }

}
