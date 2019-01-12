//$Header: /as2/de/mendelson/comm/as2/server/ResourceBundleAS2Server.java 16    9.10.18 12:53 Heller $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @version $Revision: 16 $
 */
public class ResourceBundleAS2Server extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"fatal.limited.strength", "Limited key strength has been detected in the JVM. Please install the \"Unlimited jurisdiction key strength policy files\" before running the " + AS2ServerVersion.getProductName() + " server." },
        {"server.willstart", "{0} is starting"},        
        {"server.start.details", "{0} parameter:\n\nStart integrated HTTP server: {1}\nAllow client-server connections from other hosts: {2}\nHeap memory: {3}\nJava version: {4}\nSystem user: {5}"},
        {"server.started", AS2ServerVersion.getFullProductName() + " startup in {0} ms."},
        {"server.already.running", "An instance of " + AS2ServerVersion.getProductName() + " seems to be already running.\nIt is also possible that the previous instance of the program did not exit correctly. If you are sure that no other instance is running\nplease delete the lock file \"{0}\" (start date {1}) and restart the server."},
        {"server.nohttp", "The integrated HTTP server has not been started." },
        {"server.startup.failed", "The server failed to startup" },
        {"server.shutdown", "{0} is shutting down." },
        {"bind.exception", "{0}\nYou defined a port that is currently used in your system by another process.\nThis might be the client-server port or the HTTP/S port you defined in the HTTP configuration.\nPlease modify your configuration or stop the other process before using the {1}."},
        {"httpserver.willstart", "Embedded HTTP server is starting" },
        {"httpserver.running", "Embedded HTTP server is running ({0})" },
         {"server.started.issues", "Warning: There has been found {0} configuration issues during server startup." },
        {"server.started.issue", "Warning: There has been found 1 configuration issue during server startup." },
    };
}