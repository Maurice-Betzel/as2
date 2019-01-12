//$Header: /as2/de/mendelson/comm/as2/server/ModuleLockReleaseController.java 2     4/03/18 12:27p Heller $
package de.mendelson.comm.as2.server;

import de.mendelson.util.modulelock.ModuleLock;
import java.sql.Connection;
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
 * Heartbeat control for the exclusive modules: If a client does not refresh its lock all n seconds and this
 * is detected by this class the exclusive lock is deleted in the server. This might be necessary if a client has been shut down
 * without deleteting it's exclusive lock on a module (or a connection has been cut or something else)
 * @author S.Heller
 * @version $Revision: 2 $
 */
public class ModuleLockReleaseController {

    /**Logger to log inforamtion to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private LockReleaseThread releaseThread;
    private Connection runtimeConnection;
    private Connection configConnection;

    public ModuleLockReleaseController(Connection configConnection, Connection runtimeConnection) {
        this.runtimeConnection = runtimeConnection;
        this.configConnection = configConnection;        
    }

    /**Starts the embedded task that guards the log
     */
    public void startLockReleaseControl() {
        this.releaseThread = new LockReleaseThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.releaseThread);
    }

   /**Starts the embedded task that guards the log
     */
    public void stopLockReleaseControl() {
        this.releaseThread.requestStop();
    }

    public class LockReleaseThread implements Runnable {

        private boolean stopRequested = false;
        //wait this time between checks
        private final long WAIT_TIME = TimeUnit.MINUTES.toMillis(1);
        //DB connection
        private Connection runtimeConnection;
        private Connection configConnection;

        public LockReleaseThread(Connection configConnection, Connection runtimeConnection) {
            this.runtimeConnection = runtimeConnection;
            this.configConnection = configConnection;
        }

        public void requestStop(){
            this.stopRequested = true;
        }
        
        @Override
        public void run() {
            Thread.currentThread().setName("Module lock heartbeat control");
            while (!stopRequested) {                
                ModuleLock.releaseAllLocksOlderThan(this.runtimeConnection, TimeUnit.MINUTES.toMillis(2));
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    //nop
                }
            }
        }
    }
}
