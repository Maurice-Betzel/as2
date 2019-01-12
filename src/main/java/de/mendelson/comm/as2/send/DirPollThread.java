//$Header: /as2/de/mendelson/comm/as2/send/DirPollThread.java 21    7.12.18 9:51 Heller $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.clientserver.message.RefreshClientMessageOverviewList;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.store.MessageStoreHandler;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.sendorder.SendOrderSender;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.IOFileFilterRegexpMatch;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.systemevents.SystemEvent;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Thread that polls a directory
 *
 * @author S.Heller
 * @version $Revision: 21 $
 */
public class DirPollThread implements Runnable {

    /**
     * Polls all 10s by default
     */
    private long pollInterval = TimeUnit.SECONDS.toMillis(10);
    private boolean stopRequested = false;
    private Partner receiver = null;
    private Partner sender = null;
    private Connection configConnection;
    private Connection runtimeConnection;
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    //Localize the GUI
    private MecResourceBundle rb = null;
    private ClientServer clientserver;
    private CertificateManager certificateManagerEncSign;
    /**
     * GUI preferences
     */
    private PreferencesAS2 preferences;
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public DirPollThread(Connection configConnection, Connection runtimeConnection, ClientServer clientserver,
            CertificateManager certificateManagerEncSign, Partner sender, Partner receiver) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.clientserver = clientserver;
        this.certificateManagerEncSign = certificateManagerEncSign;
        this.preferences = new PreferencesAS2();
        this.receiver = receiver;
        this.sender = sender;
        //set the poll interval to a min value of 1s - even if the user requested 0. But in this case the CPU activity will go up to 100%
        this.pollInterval = Math.max(TimeUnit.SECONDS.toMillis(receiver.getPollInterval()), TimeUnit.SECONDS.toMillis(1));
        //Load default resourcebundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleDirPollManager.class.getName());
        } //load up resourcebundle
        catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
    }

    /**
     * Returns a line that describes this thread for the log
     */
    public String getLogLine() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(this.sender.getName());
        builder.append(" -> ");
        builder.append(this.receiver.getName());
        builder.append("] ");        
        builder.append( Paths.get(this.getMonitoredDirectory()).toAbsolutePath().toString() );
        builder.append( " (");
        builder.append( String.valueOf(TimeUnit.MILLISECONDS.toSeconds(this.pollInterval)));
        builder.append( "s)");
        return (builder.toString());
    }

    /**
     * Checks if the passed data is still the data that is stored in this thread
     */
    public boolean hasBeenModified(Partner newSender, Partner newReceiver) {
        //check for name changes
        //partner renamed, this results in a new poll directory
        if ((this.receiver != null && this.sender != null)) {
            if (!Partner.hasSameContent(this.sender, newSender, this.certificateManagerEncSign)) {
                this.logger.info(rb.getResourceString("poll.modified",
                        new Object[]{this.sender.getName(), this.receiver.getName()}));
                return (true);
            }
            if (!Partner.hasSameContent(this.receiver, newReceiver, this.certificateManagerEncSign)) {
                this.logger.info(rb.getResourceString("poll.modified",
                        new Object[]{this.sender.getName(), this.receiver.getName()}));
                return (true);
            }
        }
        return (false);
    }

    /**
     * Asks the thread to stop
     */
    public void requestStop() {
        this.logger.info(rb.getResourceString("poll.stopped",
                new Object[]{
                    this.sender.getName(),
                    this.receiver.getName()
                }));
        this.stopRequested = true;
    }

    /**Builds up the directory that is monitored by this process*/
    private String getMonitoredDirectory() {
        StringBuilder outboxDirName = new StringBuilder();
        outboxDirName.append(Paths.get(this.preferences.get(PreferencesAS2.DIR_MSG)).toAbsolutePath().toString());
        outboxDirName.append(FileSystems.getDefault().getSeparator());
        outboxDirName.append(MessageStoreHandler.convertToValidFilename(this.receiver.getName()));
        outboxDirName.append(FileSystems.getDefault().getSeparator());
        outboxDirName.append("outbox");
        outboxDirName.append(FileSystems.getDefault().getSeparator());
        outboxDirName.append(MessageStoreHandler.convertToValidFilename(this.sender.getName()));
        outboxDirName.append(FileSystems.getDefault().getSeparator());
        return( outboxDirName.toString());
    }

    /**
     * Runs this thread
     */
    @Override
    public void run() {
        String pollIgnoreList = this.receiver.getPollIgnoreListAsString();
        if (pollIgnoreList == null) {
            pollIgnoreList = "--";
        }
        this.logger.info(rb.getResourceString("poll.started",
                new Object[]{
                    this.sender.getName(),
                    this.receiver.getName(),
                    pollIgnoreList,
                    this.receiver.getPollInterval()
                }
        ));
        //allow to process 3 files threaded
        ExecutorService fixedTheadExecutor = Executors.newFixedThreadPool(3);
        while (!stopRequested) {
            if (this.preferences.getBoolean(PreferencesAS2.LOG_POLL_PROCESS)) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, (int) TimeUnit.MILLISECONDS.toSeconds(this.pollInterval));
                this.logger.log(Level.FINER, this.rb.getResourceString("poll.log.wait",
                        new Object[]{
                            this.sender.getName(), this.receiver.getName(),
                            String.valueOf(TimeUnit.MILLISECONDS.toSeconds(this.pollInterval)),
                            this.format.format(calendar.getTime())
                        })
                );
            }
            try {
                Thread.sleep(this.pollInterval);
            } catch (InterruptedException e) {
                //nop
            }
            if (!stopRequested) {
                Path outboxDir = Paths.get(this.getMonitoredDirectory());
                if (Files.notExists(outboxDir)) {
                    try {
                        Files.createDirectories(outboxDir);
                    } catch (Exception e) {
                        //nop
                    }
                }
                IOFileFilterRegexpMatch fileFilter = new IOFileFilterRegexpMatch();
                if (this.receiver.getPollIgnoreList() != null) {
                    for (String ignoreEntry : this.receiver.getPollIgnoreList()) {
                        fileFilter.addNonMatchingPattern(ignoreEntry);
                    }
                }
                if (this.preferences.getBoolean(PreferencesAS2.LOG_POLL_PROCESS)) {
                    this.logger.log(Level.FINER, this.rb.getResourceString("poll.log.polling",
                            new Object[]{
                                this.sender.getName(), this.receiver.getName(),
                                outboxDir.toAbsolutePath().toString()
                            }));
                }
                try {
                    List<Path> files = this.listFilesNIO(outboxDir, fileFilter);
                    Collections.sort(files, new ComparatorFiledateOldestFirst());
                    List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
                    int fileCounter = 0;
                    for (Path file : files) {
                        //take a defined max number of files per poll process only
                        if (fileCounter == this.receiver.getMaxPollFiles()) {
                            break;
                        }
                        //ignore directories
                        if (Files.isDirectory(file)) {
                            continue;
                        }
                        if (!Files.isReadable(file)) {
                            logger.warning(rb.getResourceString("warning.noread", file.toString()));
                            continue;
                        }
                        if (!Files.isWritable(file)) {
                            logger.warning(rb.getResourceString("warning.ro", file.toString()));
                            continue;
                        }
                        //it is not sure that this triggers as the behavior depends on the OS file locking mechanism
                        if (!this.renameIsPossible(file.toFile())) {
                            logger.warning(rb.getResourceString("warning.notcomplete", file.toString()));
                            continue;
                        }
                        final Path finalFile = file;
                        Callable<Boolean> singleTask = new Callable<Boolean>() {
                            @Override
                            public Boolean call() {
                                processFile(finalFile);
                                return (Boolean.TRUE);
                            }
                        };
                        tasks.add(singleTask);
                        fileCounter++;
                    }
                    //wait for all threads to be finished
                    try {
                        fixedTheadExecutor.invokeAll(tasks);
                    } catch (InterruptedException e) {
                        //nop
                    }
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**
     * Non blocking file directory polling
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

    /**
     * Checks if the passed file could be renamed. If this is not possible, the
     * file is still used as stream target and should not be touched (works
     * actually only on windows but does not lead to problems for other OS)
     *
     * @param file
     * @return
     */
    private boolean renameIsPossible(File file) {
        File newFile = new File(file.getAbsolutePath() + "x");
        boolean renamePossible = file.renameTo(newFile);
        boolean renameBackPossible = newFile.renameTo(file);
        return (renamePossible && renameBackPossible);
    }

    /**
     * Processes a single, found file
     */
    private void processFile(Path file) {
        try {
            logger.fine(rb.getResourceString("processing.file",
                    new Object[]{
                        file.getFileName().toString(),
                        this.sender.getName(),
                        this.receiver.getName()
                    }));
            SendOrderSender orderSender = new SendOrderSender(this.configConnection, this.runtimeConnection);
            AS2Message message = orderSender.send(this.certificateManagerEncSign, this.sender, this.receiver, file.toFile(), null,
                    this.receiver.getSubject());
            clientserver.broadcastToClients(new RefreshClientMessageOverviewList());

            try {
                Files.delete(file);
                logger.log(Level.INFO,
                        rb.getResourceString("messagefile.deleted",
                                new Object[]{
                                    file.getFileName().toString()}),
                        message.getAS2Info());
            } catch (IOException e) {
                SystemEvent event = new SystemEvent(
                        SystemEvent.SEVERITY_WARNING,
                        SystemEvent.ORIGIN_SYSTEM,
                        SystemEvent.TYPE_FILE_DELETE);
                event.setSubject(event.typeToTextLocalized());
                event.setBody(
                        rb.getResourceString("processing.file",
                                new Object[]{
                                    file.getFileName().toString(),
                                    this.sender.getName(),
                                    this.receiver.getName()
                                }) + "\n\n[" + e.getClass().getSimpleName() + "]: " + e.getMessage());
                SystemEventManagerImplAS2.newEvent(event);
            }
        } catch (Throwable e) {
            String message = rb.getResourceString("processing.file.error",
                    new Object[]{
                        file.getFileName().toString(),
                        this.sender,
                        this.receiver,
                        e.getMessage()});
            logger.severe(message);
            Exception exception = new Exception(message, e);
            SystemEventManagerImplAS2.systemFailure(exception, SystemEvent.TYPE_PROCESSING_ANY);
        }
    }
}
