//$Header: /as2/de/mendelson/comm/as2/sendorder/ResourceBundleSendOrderReceiver.java 6     6.12.18 16:26 Heller $
package de.mendelson.comm.as2.sendorder;
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
 * @version $Revision: 6 $
 */
public class ResourceBundleSendOrderReceiver extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"async.mdn.wait", "Will wait for async MDN until {0}." },  
        {"max.retry.reached", "The max retry has been reached, transmission canceled." },
        {"retry", "Will retry to send transmission after {0}s, retry {1}/{2}." },
        {"as2.send.disabled", "** The system will not send any AS2 message/MDN because the number of parallel outbound connections is set to 0. Please modify these settings in the server settings dialog to enable sending again **" },
        {"outbound.connection.prepare.mdn", "Preparing outbound MDN connection to \"{0}\", active connections: {1}/{2}." },
        {"outbound.connection.prepare.message", "Preparing outbound AS2 message connection to \"{0}\", active connections: {1}/{2}." },
        {"as2.send.newmaxconnections", "The number of parallel outbound connections has been set to {0}."},
        {"send.connectionsstillopen", "You have reduced the number of outbound connections to {0} but currently there are still {1} outbound connections." },
        {"warning.nomore.outbound.connections.available", "The maximum number of outbound connections ({0}) has been reached. It is no longer possible to create additional outbound connections. Please change this value in the server configuration if you still want to do so." },
    };
    
}