//$Header: /as2/de/mendelson/comm/as2/importexport/ConfigurationExport.java 22    2.11.18 15:58 Heller $
package de.mendelson.comm.as2.importexport;

import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.util.systemevents.notification.NotificationAccessDB;
import de.mendelson.util.systemevents.notification.NotificationData;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreStorage;
import de.mendelson.util.security.cert.KeystoreStorageImplFile;
import de.mendelson.util.systemevents.notification.NotificationAccessDBImplAS2;
import de.mendelson.util.systemevents.notification.NotificationDataImplAS2;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Exports configuration data to a file
 * @author S.Heller
 * @version $Revision: 22 $
 */
public class ConfigurationExport {

    private Connection configConnection;
    private Connection runtimeConnection;
    private CertificateManager certificateManager;
    private PreferencesAS2 preferences = new PreferencesAS2();
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);

    /**@param connection database connection to use to read the config*/
    public ConfigurationExport(Connection configConnection, Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.certificateManager = new CertificateManager(this.logger);
        try {
            KeystoreStorage storage = new KeystoreStorageImplFile("certificates.p12",
                    this.preferences.get(PreferencesAS2.KEYSTORE_PASS).toCharArray(),
                    KeystoreStorageImplFile.KEYSTORE_USAGE_ENC_SIGN,
                    KeystoreStorageImplFile.KEYSTORE_STORAGE_TYPE_PKCS12                    
            );
            this.certificateManager.loadKeystoreCertificates(storage);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    /**Writes the configuration to a file*/
    public void export(OutputStream outStream) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<configuration>\n");
        writer.write("\t<product>");
        writer.write(AS2ServerVersion.getProductName() + " " + AS2ServerVersion.getVersion() + " " + AS2ServerVersion.getBuild());
        writer.write("</product>\n");
        this.exportConfiguration(writer);
        PartnerAccessDB partnerAccess 
                = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        List<Partner> allPartner = partnerAccess.getPartner();
        for (Partner partner : allPartner) {
            writer.write(partner.toXML(this.certificateManager, 1));
        }
        NotificationAccessDB access = new NotificationAccessDBImplAS2(this.configConnection, this.runtimeConnection);
        NotificationData notificationData = access.getNotificationData();
        writer.write(((NotificationDataImplAS2)notificationData).toXML(1));
        writer.write("</configuration>\n");
        writer.flush();
    }

    /**Writes the main configuration data to the config export*/
    private void exportConfiguration(Writer writer) throws Exception {
        String[] exportPrefs = new String[]{
            PreferencesAS2.ASYNC_MDN_TIMEOUT,
            PreferencesAS2.AUTH_PROXY_PASS,
            PreferencesAS2.AUTH_PROXY_USE,
            PreferencesAS2.AUTH_PROXY_USER,
            PreferencesAS2.AUTO_MSG_DELETE,
            PreferencesAS2.AUTO_MSG_DELETE_LOG,
            PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN,
            PreferencesAS2.DIR_MSG,
            PreferencesAS2.HTTP_SEND_TIMEOUT,
            PreferencesAS2.KEYSTORE_HTTPS_SEND,
            PreferencesAS2.KEYSTORE_HTTPS_SEND_PASS,
            PreferencesAS2.KEYSTORE_PASS,
            PreferencesAS2.LANGUAGE,
            PreferencesAS2.PROXY_HOST,
            PreferencesAS2.PROXY_PORT,
            PreferencesAS2.PROXY_USE,
            PreferencesAS2.RECEIPT_PARTNER_SUBDIR,
            PreferencesAS2.WRITE_OUTBOUND_STATUS_FILE
        };
        for (String pref : exportPrefs) {
            writer.write("\t<" + pref + ">");
            writer.write(this.toCDATA(preferences.get(pref)));
            writer.write("</" + pref + ">\n");
        }
    }

    /**Adds a cdata indicator to xml data*/
    private String toCDATA(String data) {
        return ("<![CDATA[" + data + "]]>");
    }
}
