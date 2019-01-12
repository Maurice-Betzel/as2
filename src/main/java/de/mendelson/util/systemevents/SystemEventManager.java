//$Header: /as2/de/mendelson/util/systemevents/SystemEventManager.java 11    6.11.18 16:59 Heller $
package de.mendelson.util.systemevents;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


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
 * @version $Revision: 11 $
 */
public abstract class SystemEventManager {

    private DateFormat eventFileDateFormat = new SimpleDateFormat("HH-mm-ss-SSS");
    private DateFormat dailySubDirFormat = new SimpleDateFormat("yyyyMMdd");


    protected SystemEventManager() {
    }


    public static String getHostname() {
        try {
            return (InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return ("Unknown");
        }
    }

    public abstract Path getStorageMainDir();
    
    /**
     * Stores the system event to a file - to be browsed later
     */
    protected void storeEventToFile(SystemEvent event) throws Exception {
        Path storageDir = Paths.get(this.getStorageMainDir().toString(),
                this.dailySubDirFormat.format(new Date())
                + FileSystems.getDefault().getSeparator() + "events");
        String storageFilePrefix = this.eventFileDateFormat.format(new Date())
                + "_" + event.severityToFilename()
                + "_" + event.originToFilename()
                + "_" + event.typeToFilename()
                + "_";
        String storageFileSuffix = ".event";
        event.store(storageDir, storageFilePrefix, storageFileSuffix);
    }

}
