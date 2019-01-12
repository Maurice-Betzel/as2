//$Header: /as4/de/mendelson/util/httpconfig/gui/ResourceBundleDisplayHTTPConfiguration.java 9     9.10.18 12:29 Heller $ 
package de.mendelson.util.httpconfig.gui;

import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * ResourceBundle to localize gui entries
 * @author S.Heller
 * @version $Revision: 9 $
 */
public class ResourceBundleDisplayHTTPConfiguration extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"title", "Server side HTTP configuration"},
        {"reading.configuration", "Reading HTTP configuration..."},
        {"button.ok", "Close" },
        {"label.info.configfile", "This dialog contains the server side HTTP/S configuration. The bundled HTTP server has the version <strong>jetty {1}</strong>. You could set up the ports, protocols and ciphers in the file \"{0}\" on the server. Please restart the server for the changes to be applied." },
        {"tab.misc", "Misc"},
        {"tab.cipher", "SSL/TLS cipher"},
        {"tab.protocols", "SSL/TLS protocols"},
        {"no.ssl.enabled", "The TLS/SSL support has not been enabled in the underlaying HTTP server.\nPlease modify the configuration file {0}\naccording to the documentation and restart the server." },        
        {"no.embedded.httpserver", "You did not start the embedded HTTP server.\nThere is no information available." },               
    };
}