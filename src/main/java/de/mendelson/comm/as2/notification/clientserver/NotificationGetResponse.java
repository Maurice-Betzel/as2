//$Header: /as2/de/mendelson/comm/as2/notification/clientserver/NotificationGetResponse.java 2     4/06/18 12:21p Heller $
package de.mendelson.comm.as2.notification.clientserver;

import de.mendelson.comm.as2.notification.NotificationData;
import de.mendelson.util.clientserver.messages.ClientServerResponse;
import java.io.Serializable;
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
 * @version $Revision: 2 $
 */
public class NotificationGetResponse extends ClientServerResponse implements Serializable {

    
    public static final long serialVersionUID = 1L;
    private NotificationData data = null;

    public NotificationGetResponse(NotificationGetRequest request) {
        super(request);
    }

    @Override
    public String toString() {
        return ("Get notification data");
    }

    /**
     * @return the data
     */
    public NotificationData getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(NotificationData data) {
        this.data = data;
    }

}
