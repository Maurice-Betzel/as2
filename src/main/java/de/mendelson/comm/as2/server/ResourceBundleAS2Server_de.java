//$Header: /as2/de/mendelson/comm/as2/server/ResourceBundleAS2Server_de.java 16    9.10.18 12:53 Heller $
package de.mendelson.comm.as2.server;

import de.mendelson.comm.as2.AS2ServerVersion;
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
 * @version $Revision: 16 $
 */
public class ResourceBundleAS2Server_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"fatal.limited.strength", "Diese Java VM unterstützt nicht die notwendige Schlüssellänge. Bitte installieren Sie die \"Unlimited jurisdiction key strength policy\" Dateien, bevor Sie den " + AS2ServerVersion.getProductName() + " Server starten." },
        {"server.willstart", "{0} startet"},
        {"server.start.details", "{0} Parameter:\n\nStarte den integrierten HTTP Server: {1}\nErlaube Client-Server Verbindungen von anderen Hosts: {2}\nHeap Speicher: {3}\nJava Version: {4}\nSystembenutzer: {5}"},
        {"server.started", AS2ServerVersion.getFullProductName() + " gestartet in {0} ms."},
        {"server.already.running", "Eine " + AS2ServerVersion.getProductName() + " Instanz scheint bereits zu laufen.\nEs könnte jedoch auch sein, dass eine vorherige Instanz nicht korrekt beendet wurde." + " Wenn Sie sicher sind, dass keine andere Instanz läuft,\nlöschen Sie bitte die Lock Datei \"{0}\"\n(Start Datum {1}) und starten den Server erneut."},
        {"server.nohttp", "Der integrierte HTTP Server wurde nicht gestartet." }, 
        {"server.startup.failed", "Es gab ein Problem beim Starten des Servers - der Start wurde abgebrochen" },
        {"server.shutdown", "{0} fährt herunter." },
        {"bind.exception", "{0}\nSie haben einen Port definiert, der derzeit von einem anderen Prozess in Ihrem System verwendet wird.\nDies kann der Client-Server-Port oder der HTTP/S-Port sein, den Sie in der HTTP-Konfiguration definiert haben.\nBitte ändern Sie Ihre Konfiguration oder stoppen Sie den anderen Prozess, bevor Sie den {1} verwenden."},
        {"httpserver.willstart", "Integrierter HTTP Server startet" },
        {"httpserver.running", "Integrierter HTTP Server läuft ({0})" },
        {"server.started.issues", "Warnung: Es wurden {0} Konfigurationsprobleme beim Starten des Servers erkannt." },
        {"server.started.issue", "Warnung: Es wurde 1 Konfigurationsproblem beim Starten des Servers erkannt." },
    };
}