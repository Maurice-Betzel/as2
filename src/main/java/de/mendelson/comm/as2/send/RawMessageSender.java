//$Header: /as2/de/mendelson/comm/as2/send/RawMessageSender.java 19    5/03/17 2:40p Heller $
package de.mendelson.comm.as2.send;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.clientserver.message.IncomingMessageRequest;
import de.mendelson.comm.as2.clientserver.message.IncomingMessageResponse;
import de.mendelson.comm.as2.database.DBDriverManager;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.clientserver.AnonymousTextClient;
import de.mendelson.util.security.BCCryptoHelper;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Raw data uploader, mainly for test purpose. Sends a already fully prepared
 * AS2 message to a specified sender
 *
 * @author S.Heller
 * @version $Revision: 19 $
 */
public class RawMessageSender {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);

    /**
     * Creates new raw message sender
     */
    public RawMessageSender() {
    }

    private IncomingMessageResponse send(File rawDataFile, File headerFile) throws Throwable {
        Properties header = new Properties();
        FileInputStream headerStream = new FileInputStream(headerFile);
        header.load(headerStream);
        headerStream.close();
        AnonymousTextClient client = null;
        client = new AnonymousTextClient();
        PreferencesAS2 preferences = new PreferencesAS2();
        client.connect("localhost", preferences.getInt(PreferencesAS2.CLIENTSERVER_COMM_PORT), 30000);
        IncomingMessageRequest messageRequest = new IncomingMessageRequest();
        messageRequest.setMessageDataFilename(rawDataFile.getAbsolutePath());
        messageRequest.setHeader(header);
        messageRequest.setContentType(header.getProperty("content-type"));
        messageRequest.setRemoteHost("localhost");
        IncomingMessageResponse response = (IncomingMessageResponse) client.sendSyncWaitInfinite(messageRequest);
        if (response.getException() != null) {
            throw (response.getException());
        }
        return (response);
    }

    /**
     * Displays a usage of how to use this class
     */
    public static void printUsage() {
        System.out.println("java " + RawMessageSender.class.getName() + " <options>");
        System.out.println("Start up a " + AS2ServerVersion.getProductNameShortcut() + " server ");
        System.out.println("Options are:");
        System.out.println("-datafile <String>: File that contains the AS2 message, fully packed");
        System.out.println("-headerfile <String>: File that contains the AS2 message header");
    }

    public static final void main(String[] args) {
        String file = null;
        String header = null;
        int optind;
        for (optind = 0; optind < args.length; optind++) {
            if (args[optind].toLowerCase().equals("-datafile")) {
                file = args[++optind];
            } else if (args[optind].toLowerCase().equals("-headerfile")) {
                header = args[++optind];
            } else if (args[optind].toLowerCase().equals("-?")) {
                RawMessageSender.printUsage();
                System.exit(1);
            } else if (args[optind].toLowerCase().equals("-h")) {
                RawMessageSender.printUsage();
                System.exit(1);
            } else if (args[optind].toLowerCase().equals("-help")) {
                RawMessageSender.printUsage();
                System.exit(1);
            }
        }
        if (file == null) {
            System.err.println("Parameter missing: " + "datafile");
            printUsage();
            System.exit(1);
        }
        if (header == null) {
            System.err.println("Parameter missing: " + "headerfile");
            printUsage();
            System.exit(1);
        }
        RawMessageSender sender = new RawMessageSender();
        try {
            //register the database drivers for the VM
            DBDriverManager.registerDriver();
            //initialize the security provider
            BCCryptoHelper helper = new BCCryptoHelper();
            helper.initialize();
            IncomingMessageResponse response = sender.send(new File(file), new File(header));
            if (response.getMDNData() != null) {
                Logger.getLogger(AS2Server.SERVER_LOGGER_NAME).info(new String(response.getMDNData()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
