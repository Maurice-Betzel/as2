//$Header: /as2/de/mendelson/comm/as2/clientserver/message/ServerInfoResponse.java 4     28-10-16 1:49p Heller $
package de.mendelson.comm.as2.clientserver.message;

import de.mendelson.util.clientserver.messages.ClientServerResponse;
import java.io.Serializable;
import java.util.Properties;
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
 * @version $Revision: 4 $
 */
public class ServerInfoResponse extends ClientServerResponse implements Serializable {

    public static final String SERVER_PRODUCT_NAME = ServerInfoRequest.SERVER_PRODUCT_NAME;
    public static final String SERVER_VERSION = ServerInfoRequest.SERVER_VERSION;
    public static final String SERVER_BUILD = ServerInfoRequest.SERVER_BUILD;
    public static final String SERVER_START_TIME = ServerInfoRequest.SERVER_START_TIME;

    /**
     * Properties to return
     */
    private Properties properties = new Properties();

    public ServerInfoResponse(ServerInfoRequest request) {
        super(request);
    }

    /**
     * Sets a single property
     */
    public void setProperty(String key, String value) {
        if (key != null && value != null) {
            this.properties.setProperty(key.toLowerCase(), value);
        }
    }

    /**
     * Returns the server properties
     */
    public Properties getProperties() {
        return (this.properties);
    }

    @Override
    public String toString() {
        return ("Server info response");
    }
}
