//$Header: /as4/de/mendelson/util/httpconfig/server/ResourceBundleHTTPServerConfigProcessor.java 7     9.10.18 12:29 Heller $
package de.mendelson.util.httpconfig.server;

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
 *
 * @author S.Heller
 * @version $Revision: 7 $
 */
public class ResourceBundleHTTPServerConfigProcessor extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"http.server.config.listener", "Port {0} ({1}) is bound to the adapter {2}"},
        {"http.server.config.keystorepath", "SSL/TLS keystore path: \"{0}\""},
        {"http.server.config.clientauthentication", "Server requires client authentication: {0}"},
        {"external.ip", "External ip: {0} / {1}"},
        {"external.ip.error", "External ip: -Unable to compute-"},
        {"http.receipturls", "Full receipt URLs that are possible in the current configuration:"},
        {"http.serverstateurl", "Check the server state:"},
        {"http.deployedwars", "Currently deployed war files in the HTTP server (Servlet functionality):"},
        {"webapp.as2.war", "mendelson AS2 receipt servlet"},
        {"webapp.as4.war", "mendelson AS4 receipt servlet"},
        {"webapp.webas2.war", "mendelson AS2 server web monitoring"},
        {"webapp.as2-sample.war", "mendelson AS2 API samples"},
        {"info.cipher", "The following ciphers are supported by the underlying HTTP server.\nWhich ones are supported depends on your used Java VM (it''s {1}).\nYou can disable single ciphers in the configuration file\n(\"{0}\")."},
        {"info.protocols", "The following protocols are supported by the underlying HTTP server.\nWhich ones are supported depends on your used Java VM (it''s {1}).\nYou can disable single protocols in the configuration file\n(\"{0}\")."},};

}
