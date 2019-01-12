//$Header: /as2/de/mendelson/comm/as2/timing/FileDeleteController.java 9     6.11.18 17:50 Heller $
package de.mendelson.comm.as2.timing;

import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.IOFileFilterCreationDate;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Controls the timed deletion of AS2 file entries from the file system
 *
 * @author S.Heller
 * @version $Revision: 9 $
 */
public class FileDeleteController {

    /**
     * Logger to log information to
     */
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private PreferencesAS2 preferences = new PreferencesAS2();
    private FileDeleteThread deleteThread;
    private ClientServer clientserver = null;
    private MecResourceBundle rb = null;
    private Connection configConnection;
    private Connection runtimeConnection;

    public FileDeleteController(ClientServer clientserver, Connection configConnection,
            Connection runtimeConnection) {
        this.clientserver = clientserver;
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleFileDeleteController.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**
     * Starts the embedded task that guards the files to delete
     */
    public void startAutoDeleteControl() {
        this.deleteThread = new FileDeleteThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.deleteThread);
    }

    public class FileDeleteThread implements Runnable {

        private boolean stopRequested = false;
        //wait this time between checks
        private final long WAIT_TIME = TimeUnit.HOURS.toMillis(6);
        //DB connection
        private Connection configConnection;
        private Connection runtimeConnection;

        public FileDeleteThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Contol auto AS2 file delete");
            while (!stopRequested) {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    //nop
                }
                if (preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE)) {
                    Path rawIncomingDir 
                            = Paths.get(Paths.get(preferences.get(PreferencesAS2.DIR_MSG)).toAbsolutePath().toString()
                                    + FileSystems.getDefault().getSeparator() + "_rawincoming");
                    //delete all files that are older than MDN wait time + delete log time
                    long maxAgeInS = (preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN)
                            * preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S))
                            + TimeUnit.MINUTES.toSeconds(preferences.getInt(PreferencesAS2.ASYNC_MDN_TIMEOUT));
                    long olderThanTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(maxAgeInS);
                    int eventSeverity = SystemEvent.SEVERITY_INFO;
                    IOFileFilterCreationDate fileFilter = new IOFileFilterCreationDate(IOFileFilterCreationDate.MODE_OLDER_THAN, olderThanTime);
                    StringBuilder deleteLog = new StringBuilder();
                    AtomicInteger foundEntries = new AtomicInteger(0);
                    try {
                        deleteLog.append(rb.getResourceString("delete.title._rawincoming"));
                        deleteLog.append(System.lineSeparator()).append("---").append(System.lineSeparator());
                        //delete _rawincoming entries
                        int severity = this.deleteFilesInDirectory(rawIncomingDir, fileFilter, deleteLog, foundEntries);
                        if (severity == SystemEvent.SEVERITY_WARNING) {
                            eventSeverity = SystemEvent.SEVERITY_WARNING;
                        }
                    } catch (Exception e) {
                        eventSeverity = SystemEvent.SEVERITY_WARNING;
                        deleteLog.append("[").append(e.getClass().getSimpleName()).append("]: ").append(e.getMessage());
                    }
                    try {
                        //delete temp dir entries and subdirectories
                        fileFilter.setIncludeDirecories(true);
                        deleteLog.append(System.lineSeparator());
                        deleteLog.append(System.lineSeparator());
                        deleteLog.append(rb.getResourceString("delete.title.tempfiles"));
                        deleteLog.append(System.lineSeparator()).append("---").append(System.lineSeparator());
                        int severity = this.deleteFilesInDirectory(Paths.get("temp"), fileFilter, deleteLog, foundEntries);
                        if (severity == SystemEvent.SEVERITY_WARNING) {
                            eventSeverity = SystemEvent.SEVERITY_WARNING;
                        }
                    } catch (Exception e) {
                        eventSeverity = SystemEvent.SEVERITY_WARNING;
                        deleteLog.append("[").append(e.getClass().getSimpleName()).append("]: ").append(e.getMessage());
                    }
                    if (foundEntries.intValue() > 0) {
                        SystemEvent event = new SystemEvent(
                                eventSeverity, SystemEvent.ORIGIN_SYSTEM, SystemEvent.TYPE_FILE_DELETE);
                        event.setSubject(rb.getResourceString("delete.title"));
                        event.setBody(deleteLog.toString());
                        SystemEventManagerImplAS2.newEvent(event);
                    }
                }
            }
        }

        /**
         * Deletes all files found in the passed path that matches the file
         * filter
         *
         * @return The severity of the operation
         */
        private int deleteFilesInDirectory(Path directory, IOFileFilterCreationDate fileFilter,
                StringBuilder deleteLog, AtomicInteger foundEntries) throws Exception {
            int eventSeverity = SystemEvent.SEVERITY_INFO;
            List<Path> fileList = this.listFilesNIO(directory, fileFilter);
            if (fileList.isEmpty()) {
                deleteLog.append(rb.getResourceString("no.entries", directory.toAbsolutePath().toString()));
                deleteLog.append(System.lineSeparator());
            }
            for (Path singlePath : fileList) {
                foundEntries.incrementAndGet();
                //if its a directory descent into it and delete it first
                if (Files.isDirectory(singlePath)) {
                    int severity = this.deleteFilesInDirectory(singlePath, fileFilter, deleteLog, foundEntries);
                    if (severity == SystemEvent.SEVERITY_WARNING) {
                        eventSeverity = SystemEvent.SEVERITY_WARNING;
                    }
                }
                try {
                    Files.delete(singlePath);
                    deleteLog.append(rb.getResourceString("success") + ": ");
                    deleteLog.append(singlePath.toAbsolutePath().toString());
                    deleteLog.append(System.lineSeparator());
                    logger.config(rb.getResourceString("autodelete", singlePath.toAbsolutePath().toString()));
                } catch (Exception delEx) {
                    deleteLog.append(rb.getResourceString("failure") + " [" + delEx.getClass().getSimpleName() + "]: ");
                    deleteLog.append(singlePath.toAbsolutePath().toString());
                    deleteLog.append(System.lineSeparator());
                    eventSeverity = SystemEvent.SEVERITY_WARNING;
                }
            }
            return (eventSeverity);
        }

        /**
         * Non blocking file directory list
         */
        private List<Path> listFilesNIO(Path dir, DirectoryStream.Filter fileFilter) throws Exception {
            List<Path> result = new ArrayList<Path>();
            DirectoryStream<Path> stream = null;
            try {
                stream = Files.newDirectoryStream(dir, fileFilter);
                for (Path entry : stream) {
                    result.add(entry);
                }
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return result;
        }

    }
}
