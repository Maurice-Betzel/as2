//$Header: /as2/de/mendelson/util/clientserver/connectiontest/ResourceBundleConnectionTest_de.java 8     10.12.18 12:46 Heller $
package de.mendelson.util.clientserver.connectiontest;

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
public class ResourceBundleConnectionTest_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"tag", "Verbindungstest zu {0}"},
        {"timeout.set", "Setze Timeout auf {0}ms"},
        {"test.start.ssl", "Starte Verbindungsprüfung zu {0}, SSL/TLS. Bitte beachten Sie, dass dieser Test jedem Serverzertifikat vertraut - auch wenn dieser Test erfolgreich verläuft, bedeutet dies also nicht, dass Ihr SSL/TLS Keystore korrekt konfiguriert ist."},
        {"test.start.plain", "Starte Verbindungsprüfung zu {0}, PLAIN..."},
        {"connection.problem", "{0} kann nicht erreicht werden - dies kann ein Infrastrukturproblem sein oder es wurden falsche Daten eingegeben"},
        {"connection.success", "Die Verbindung zu {0} wurde erfolgreich hergestellt"},
        {"exception.occured", "Es kam zu einem Problem während des Verbindungstests: [{0}] {1}"},
        {"exception.occured.oftpservice", "Es konnte an der gegebenen Addresse und dem gegebenen Port kein laufender OFTP2 Server identifiziert werden. Dies kann ein temporäres Problem sein, zum Beispiel dass der entfernte OFTP2 Server gerade nicht läuft, die Addressdaten jedoch korrekt sind. Es kam zu folgendem Problem: [{0}] {1}"},
        {"remote.service.identification", "Service Identifikation des entfernten Servers: \"{0}\""},
        {"service.found.success", "Erfolg: Es wurde ein laufender OFTP Service gefunden an {0}"},
        {"service.found.failure", "Fehler: Es wurde kein laufender OFTP Service gefunden an {0}"},
        {"wrong.protocol", "Das gefundene Protokol ist \"{0}\", dies ist keine gesicherte Verbindung. Sie haben versucht, sich per SSL/TLS zu diesem Partner zu verbinden. Auf dem gegebenen Port und der gegebenen Adresse bietet Ihr Partner jedoch keine Leitungssicherung an."},
        {"wrong.protocol.hint", "Entweder Ihr Partner erwartet eine ungesicherte Verbindung, es liegt ein Protokollproblem vor oder er benötigt eine Client Authentication"},
        {"protocol.information", "Das verwendete Protokoll wurde als \"{0}\" identifiziert"},
        {"requesting.certificates", "Es werden die Zertifikate des entfernten Servers heruntergeladen"},
        {"certificates.found", "{0} Zertifikate wurden gefunden und heruntergeladen"},
        {"certificates.found.details", "Zertifikat [{0}/{1}]: {2}" },
        {"check.for.service.oftp2", "Prüfe auf laufenden OFTP2 Service..."},
        {"certificate.ca", "CA Zertifikat" },        
        {"certificate.enduser", "Endbenutzer Zertifikat" },   
        {"certificate.selfsigned", "Self Signed" },        
        {"certificate.does.not.exist.local", "Dieses Zertifikat existiert noch nicht in Ihrem lokalen SSL/TLS Keystore - bitte importieren Sie es" },
        {"certificate.does.exist.local", "Dieses Zertifikat exisitert bereits in Ihrem lokalen SSL/TLS Keystore, der Alias ist \"{0}\"" },
        {"test.connection.direct", "Eine direkte IP Verbindung wird verwendet" },
        {"test.connection.proxy.auth", "Die Verbindung verwendet den Proxy {0} mit Authentifikation (Benutzer \"{1}\")" },
        {"test.connection.proxy.noauth", "Die Verbindung verwendet den Proxy {0} ohne Authentifikation" },
        {"result.exception", "Der folgende Fehler trat beim Test auf: {0}." },        
    };

}
