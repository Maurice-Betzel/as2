//$Header: /as2/de/mendelson/util/clientserver/connectiontest/gui/ResourceBundleDialogConnectionTestResult.java 8     10.12.18 12:46 Heller $
package de.mendelson.util.clientserver.connectiontest.gui;

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
 * @version $Revision: 8 $
 */
public class ResourceBundleDialogConnectionTestResult extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"title", "Connection test result to {0}"},
        {"description." + JDialogConnectionTestResult.CONNECTION_TEST_OFTP2, "<HTML>The system performed an IP connection to the ip address <strong>{0}</strong>, port <strong>{1}</strong>. The following result shows if this connection was successful and if an OFTP2 server listens to this address. If a TLS connection has been requested and was successful it is possible to download the certificate(s), they will be stored in your TLS keystore. If this connection test is successful and it is a TLS/SSL connection it is not ensured that the SSL handshake will work with your current configuration as this test trusts all remote servers without checking their trust chain or check if the partner certificate is available in the local SSL/TLS keystore.</HTML>"},
        {"description." + JDialogConnectionTestResult.CONNECTION_TEST_AS2, "<HTML>The system performed an IP connection to the ip address <strong>{0}</strong>, port <strong>{1}</strong>. The following result shows if this connection was successful and if a HTTP server listens to this address. Even if the test is successful it is not ensured that there listens a AS2 server - it could be a normal HTTP server. If a TLS connection has been requested (HTTPS) and was successful it is possible to download the certificate(s), they will be stored in your TLS keystore. If this connection test is successful and it is a TLS/SSL connection it is not ensured that the SSL handshake will work with your current configuration as this test trusts all remote servers without checking their trust chain or check if the partner certificate is available in the local SSL/TLS keystore.</HTML>"},
        {"OK", "OK"},
        {"FAILED", "FAILED"},
        {"state.ssl", "Result of the connection test (TLS):"},
        {"state.plain", "Result of the connection test (PLAIN):"},
        {"no.certificate.plain", "Not available (Plain connection test)"},
        {"button.viewcert", "View Certificate(s)"},
        {"button.close", "Close"},
        {"label.connection.established", "The connection has been established"},
        {"label.certificates.downloaded", "The certificates have been downloaded"},
        {"label.running.oftpservice", "A running OFTP service has been found"},
        {"used.cipher", "The used cipher for this test is \"{0}\"" },           
    };

}
