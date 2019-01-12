//$Header: /mec_as2/de/mendelson/comm/as2/api/server/ServersideAPICommandProcessing.java 2     8.01.19 9:48 Heller $
package de.mendelson.comm.as2.api.server;

import de.mendelson.comm.as2.api.message.CommandRequest;
import de.mendelson.comm.as2.api.message.CommandResponse;
import de.mendelson.util.modulelock.LockClientInformation;
import de.mendelson.comm.as2.send.DirPollManager;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.security.cert.CertificateManager;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 *
 * @author S.Heller
 * @version $Revision: 2 $
 */
public class ServersideAPICommandProcessing {

    public ServersideAPICommandProcessing(Logger logger, Connection configConnection,
            Connection runtimeConnection,
            CertificateManager certManagerEncSign,
            CertificateManager certManagerSSL,
            DirPollManager dirPollManager, ClientServer clientserver) {
    }

    /**
     * Processes a command request on the server
     */
    public CommandResponse processRequest(CommandRequest request, Path requestFile, LockClientInformation clientInformation) {        
        throw new IllegalArgumentException("Not implemented in the community edition" );
    }
}
