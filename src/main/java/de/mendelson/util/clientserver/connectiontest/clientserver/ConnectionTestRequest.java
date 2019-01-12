//$Header: /as2/de/mendelson/util/clientserver/connectiontest/clientserver/ConnectionTestRequest.java 3     23.10.18 9:34 Heller $
package de.mendelson.util.clientserver.connectiontest.clientserver;

import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Msg for the client server protocol
 *
 * @author S.Heller
 * @version $Revision: 3 $
 */
public class ConnectionTestRequest extends ClientServerMessage implements Serializable {
    
    public static final long serialVersionUID = 1L;
    private boolean ssl = false;
    private String host = null;
    private int port;
    private long timeout = TimeUnit.SECONDS.toMillis(2);
    /**Some additional information for the log etc*/
    private String partnerName = null;

    public ConnectionTestRequest(String host, int port, boolean ssl) {
        this.ssl = ssl;
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return ("Connection test request");
    }

    /**
     * @return the timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * @return the ssl
     */
    public boolean getSSL() {
        return ssl;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * @return the partnerName - may return null
     */
    public String getPartnerName() {
        return partnerName;
    }

    /**
     * @param partnerName the partnerName to set
     */
    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

}
