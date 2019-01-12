//$Header: /as2/de/mendelson/util/security/csr/ResourceBundleCSR_de.java 3     4/06/18 1:35p Heller $
package de.mendelson.util.security.csr;

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
 * @version $Revision: 3 $
 */
public class ResourceBundleCSR_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        
        {"label.selectcsrfile", "Bitte wählen Sie die Datei zum Speichern des CSR"},
        {"csr.title", "Zertificate beglaubigen: Certificate Sign Request" },
        {"csr.title.renew", "Zertificate erneuern: Certificate Sign Request" },
        {"csr.message.storequestion", "Möchten Sie den Schlüssel bei der mendelson CA beglaubigen lassen\noder die Anfrage in einer Datei speichern?" },
        {"csr.message.storequestion.renew", "Möchten Sie den Schlüssel bei der mendelson CA erneuern lassen\noder die Anfrage in einer Datei speichern?" },
        {"csr.option.1", "Beglaubigen bei der mendelson CA" },
        {"csr.option.1.renew", "Erneuern bei der mendelson CA" },
        {"csr.option.2", "In Datei speichern" },
        {"csr.generation.success.message", "Die generierte Anfrage zur Beglaubigung wurde in der Datei\n\"{0}\" gespeichert.\nBitte senden Sie diese Daten an Ihre CA.\nWir empfehlen Ihnen die mendelson CA (http://ca.mendelson-e-c.com)."},
        {"csr.generation.success.title", "CSR wurde erfolgreich erstellt"},
        {"csr.generation.failure.title", "Fehler bei der CSR Erstellung"},
        {"csr.generation.failure.message", "{0}"},
        {"label.selectcsrrepsonsefile", "Bitte wählen Sie die Antwortsdatei der CA"},
        {"csrresponse.import.success.message", "Der Schlüssel wurde erfolgreich mit der Antwort der CA gepatched."},
        {"csrresponse.import.success.title", "Erfolg"},
        {"csrresponse.import.failure.message", "{0}"},
        {"csrresponse.import.failure.title", "Problem beim Patchen des Schlüssels"},        
    };
}
