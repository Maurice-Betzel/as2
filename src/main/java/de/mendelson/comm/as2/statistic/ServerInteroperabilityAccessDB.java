//$Header: /mec_as2/de/mendelson/comm/as2/statistic/ServerInteroperabilityAccessDB.java 3     8.01.19 9:48 Heller $
package de.mendelson.comm.as2.statistic;

import java.sql.Connection;
import java.util.List;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * @author S.Heller
 * @version $Revision: 3 $
 */
public class ServerInteroperabilityAccessDB {

    public ServerInteroperabilityAccessDB(Connection configConnection, Connection runtimeConnection) {
    }

    public void addEntry(String messageId) {
    }

    public List<ServerInteroperabilityContainer> getServer() {
        throw new IllegalArgumentException("Not implemented in the community edition");
    }

    public ServerInteroperabilityContainer getServer(String serverId) {
        throw new IllegalArgumentException("Not implemented in the community edition");
    }
}
