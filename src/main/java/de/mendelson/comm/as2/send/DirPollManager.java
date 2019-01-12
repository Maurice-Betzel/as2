//$Header: /as2/de/mendelson/comm/as2/send/DirPollManager.java 47    13.11.18 12:36 Heller $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */

/**
 * Manager that polls the outbox directories of the partners, creates messages
 * and sends them
 *
 * @author S.Heller
 * @version $Revision: 47 $
 */
public class DirPollManager {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private CertificateManager certificateManager;
    /**
     * Stores all poll threads key: partner DB id, value: pollThread
     */
    private final Map<String, DirPollThread> mapPollThread = Collections.synchronizedMap(new HashMap<String, DirPollThread>());
    /**
     * Localize the GUI
     */
    private MecResourceBundle rb = null;
    //DB connection
    private Connection configConnection;
    private Connection runtimeConnection;
    private ClientServer clientserver;

    public DirPollManager(CertificateManager certificateManager, Connection configConnection, Connection runtimeConnection,
            ClientServer clientserver) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.clientserver = clientserver;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleDirPollManager.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.certificateManager = certificateManager;
    }

    /**
     * Start all poll threads
     */
    public void start() {
        this.partnerConfigurationChanged();
        List<DirPollThread> threadList = this.getPollThreads();
        this.logger.info(this.rb.getResourceString("manager.status.modified", String.valueOf(threadList.size())));
    }

    /**
     * Returns the list of poll threads that are currently active
     */
    private List<DirPollThread> getPollThreads() {
        List<DirPollThread> pollThreadList = new ArrayList<DirPollThread>();
        synchronized (this.mapPollThread) {
            for (String key : this.mapPollThread.keySet()) {
                pollThreadList.add(this.mapPollThread.get(key));
            }
        }
        return (pollThreadList);
    }

    /**
     * Indicates that the partner configuration has been changed: This should
     * stop now unused tasks and start other
     */
    public void partnerConfigurationChanged() {
        List<String> pollStopLines = new ArrayList<String>();
        List<String> pollStartLines = new ArrayList<String>();

        PartnerAccessDB access = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        List<Partner> partner = access.getPartner();
        List<Partner> localStations = access.getLocalStations();
        if (partner == null) {
            this.logger.severe("partnerConfigurationChanged: Unable to load partner");
            return;
        }
        synchronized (this.mapPollThread) {
            for (Partner sender : localStations) {
                for (Partner receiver : partner) {
                    String id = sender.getDBId() + "_" + receiver.getDBId();
                    //add partner task if it does not exist so far and if the receiver is no local station and the dir poll is enabled
                    if (!this.mapPollThread.containsKey(id) && !receiver.isLocalStation() && receiver.isEnableDirPoll()) {
                        DirPollThread newPoll = this.addPartnerPollThread(sender, receiver);
                        pollStartLines.add(newPoll.getLogLine());
                    } else if (this.mapPollThread.containsKey(id)) {
                        DirPollThread thread = (DirPollThread) this.mapPollThread.get(id);
                        if (!receiver.isLocalStation()) {
                            if (thread.hasBeenModified(sender, receiver)) {
                                //restart a poll thread - it has been modified
                                thread.requestStop();
                                this.mapPollThread.remove(id);
                                //restart the poll thread with the new values
                                if (receiver.isEnableDirPoll()) {
                                    DirPollThread restartPoll = this.addPartnerPollThread(sender, receiver);
                                } else {
                                    //no restart - means it has been stopped/deleted
                                    pollStopLines.add(thread.getLogLine());
                                }
                            }
                        } else {
                            //its a local station now: stop the task and remove it
                            pollStopLines.add(thread.getLogLine());
                            thread.requestStop();
                            this.mapPollThread.remove(id);
                        }
                    }
                }
            }
            //still running task that is not in the configuration any more: stop and remove
            List<String> idList = new ArrayList<String>();
            Iterator iterator = this.mapPollThread.keySet().iterator();
            while (iterator.hasNext()) {
                idList.add((String) iterator.next());
            }
            for (String id : idList) {
                boolean idFound = false;
                for (Partner sender : localStations) {
                    for (Partner receiver : partner) {
                        String relationShipId = sender.getDBId() + "_" + receiver.getDBId();
                        if (id.equals(relationShipId)) {
                            idFound = true;
                        }
                    }
                }
                //old still running taks, has been deleted in the config: stop and remove
                if (!idFound) {
                    DirPollThread thread = this.mapPollThread.get(id);
                    pollStopLines.add(thread.getLogLine());
                    thread.requestStop();
                    this.mapPollThread.remove(id);
                }
            }
        }
        //all done - now fire a system event
        if (!pollStopLines.isEmpty() || !pollStartLines.isEmpty()) {
            SystemEvent event = new SystemEvent(
                    SystemEvent.SEVERITY_INFO,
                    SystemEvent.ORIGIN_SYSTEM,
                    SystemEvent.TYPE_DIRECTORY_MONITORING_STATE_CHANGED);
            List<DirPollThread> threadList = this.getPollThreads();
            event.setSubject(this.rb.getResourceString("manager.status.modified", String.valueOf(threadList.size())));

            StringBuilder bodyBuilder = new StringBuilder();
            //display stopped polls
            bodyBuilder.append(rb.getResourceString("title.list.polls.stopped")).append("\n");
            bodyBuilder.append("------").append("\n");
            Collections.sort(pollStopLines);
            for (String line : pollStopLines) {
                bodyBuilder.append(line).append("\n");
            }
            if (pollStopLines.isEmpty()) {
                bodyBuilder.append(rb.getResourceString("none")).append("\n");
            }
            bodyBuilder.append("\n\n");
            //display started polls
            bodyBuilder.append(rb.getResourceString("title.list.polls.started")).append("\n");
            bodyBuilder.append("------").append("\n");
            Collections.sort(pollStartLines);
            for (String line : pollStartLines) {
                bodyBuilder.append(line).append("\n");
            }
            if (pollStartLines.isEmpty()) {
                bodyBuilder.append(rb.getResourceString("none")).append("\n");
            }
            bodyBuilder.append("\n\n");
            //display all current polls
            bodyBuilder.append(rb.getResourceString("title.list.polls.running")).append("\n");
            bodyBuilder.append("------").append("\n");
            List<String> pollRunningLines = new ArrayList<String>();
            for (DirPollThread thread : threadList) {
                pollRunningLines.add(thread.getLogLine() + "\n");
            }
            Collections.sort(pollRunningLines);
            for (String line : pollRunningLines) {
                bodyBuilder.append(line);
            }
            if (pollRunningLines.isEmpty()) {
                bodyBuilder.append(rb.getResourceString("none")).append("\n");
            }
            event.setBody(bodyBuilder.toString());
            SystemEventManagerImplAS2.newEvent(event);
        }
    }

    /**
     * Adds a new partner to the poll thread list
     *
     */
    private DirPollThread addPartnerPollThread(Partner localStation, Partner partner) {
        DirPollThread thread = new DirPollThread(this.configConnection, this.runtimeConnection, this.clientserver, this.certificateManager,
                localStation, partner);
        synchronized (this.mapPollThread) {
            this.mapPollThread.put(localStation.getDBId() + "_" + partner.getDBId(), thread);
            Executors.newSingleThreadExecutor().submit(thread);
        }
        return (thread);
    }

}
