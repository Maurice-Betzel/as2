//$Header: /as2/de/mendelson/util/clientserver/connectiontest/gui/ResourceBundleDialogConnectionTestResult_de.java 9     10.12.18 12:46 Helle $
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
 * @version $Revision: 9 $
 */
public class ResourceBundleDialogConnectionTestResult_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"title", "Ergebnis des Verbindungstests nach {0}"},
        {"description." + JDialogConnectionTestResult.CONNECTION_TEST_OFTP2, "<HTML>Das System hat einen Verbindungstest zur Addresse <strong>{0}</strong>, Port <strong>{1}</strong> durchgeführt. Das folgende Ergebnis zeigt, ob der Verbindungsaufbau erfolgreich war und ob an dieser Addresse ein OFTP2 Server läuft. Wenn eine TLS Verbindung verwendet werden sollte und dies erfolgreich möglich war, können Sie die Zertifikate Ihres Partners herunterladen und in Ihren Keystore importieren. Wenn dieser Verbindungstest erfolgreich war und es sich um eine SSL/TLS Verbindung handelte, ist nicht sichergestellt, dass diese auch mit Ihrer aktuellen Konfiguration funktioniert, da in diesem Test jedem Zertifikat eines entfernten Servers getraut wird - Es wird nicht geprüft, ob das entfernte Zertifikat gültig ist und ob es sich im lokalen SSL/TLS Keystore befindet.</HTML>"},
        {"description." + JDialogConnectionTestResult.CONNECTION_TEST_AS2, "<HTML>Das System hat einen Verbindungstest zur Addresse <strong>{0}</strong>, Port <strong>{1}</strong> durchgeführt. Das folgende Ergebnis zeigt, ob der Verbindungsaufbau erfolgreich war und ob an dieser Addresse ein HTTP Server läuft. Auch wenn der Test erfolgreich ist, ist nicht sichergestellt, ob dies ein normaler HTTP Server oder ein AS2 Server ist. Wenn eine TLS Verbindung verwendet werden sollte (HTTPS) und dies erfolgreich möglich war, können Sie die Zertifikate Ihres Partners herunterladen und in Ihren Keystore importieren. Wenn dieser Verbindungstest erfolgreich war und es sich um eine SSL/TLS Verbindung handelte, ist nicht sichergestellt, dass diese auch mit Ihrer aktuellen Konfiguration funktioniert, da in diesem Test jedem Zertifikat eines entfernten Servers getraut wird - Es wird nicht geprüft, ob das entfernte Zertifikat gültig ist und ob es sich im lokalen SSL/TLS Keystore befindet.</HTML>"},
        {"OK", "OK"},
        {"FAILED", "FEHLER"},
        {"state.ssl", "Ergebnis des Verbindungstests (TLS):"},
        {"state.plain", "Ergebnis des Verbindungstests (PLAIN):"},
        {"no.certificate.plain", "Nicht verfügbar (Ungesicherte Verbindung)"},
        {"button.viewcert", "Zertifikat(e) ansehen"},
        {"button.close", "Schliessen"},
        {"label.connection.established", "Die Verbindung wurde hergestellt"},
        {"label.certificates.downloaded", "Die Zertifikate wurden heruntergeladen"},
        {"label.running.oftpservice", "Es wurde ein laufender OFTP Service gefunden"},
        {"used.cipher", "Für den Test wurde der folgende Verschlüsselungsalgorithmus verwendet: \"{0}\"" },          
    };

}
