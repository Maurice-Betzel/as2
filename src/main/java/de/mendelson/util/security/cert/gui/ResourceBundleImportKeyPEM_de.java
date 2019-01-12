//$Header: /as2/de/mendelson/util/security/cert/gui/ResourceBundleImportKeyPEM_de.java 2     4/06/18 1:35p Heller $ 
package de.mendelson.util.security.cert.gui;

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
 *
 * @author S.Heller
 * @version $Revision: 2 $
 */
public class ResourceBundleImportKeyPEM_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"button.ok", "Ok"},
        {"button.cancel", "Abbrechen"},
        {"button.browse", "Durchsuchen"},
        {"label.importkey", "Schlüsseldatei (PEM):"},
        {"label.importcert", "Zertifikatdatei:"},
        {"label.alias", "Zu benutzener Alias:"},
        {"label.keypass", "Schlüsselpasswort:"},
        {"title", "Schlüssel importieren (PEM Format)"},
        {"filechooser.cert.import", "Bitte wählen Sie das zu importierende Zertifikat"},
        {"filechooser.key.import", "Bitte wählen Sie die zu importierende Schlüsseldatei (PEM Format)"},
        {"key.import.success.message", "Der Schlüssel wurde erfolgreich importiert."},
        {"key.import.success.title", "Erfolg"},
        {"key.import.error.message", "Es gab einen Fehler beim Import des Schlüssels.\n{0}"},
        {"key.import.error.title", "Fehler"},};

}
