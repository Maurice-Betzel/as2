//$Header: /as2/de/mendelson/util/security/cert/gui/ResourceBundleExportKeyPKCS12.java 2     4/06/18 1:35p Heller $ 
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
public class ResourceBundleExportKeyPKCS12 extends MecResourceBundle {

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
        {"button.cancel", "Cancel"},
        {"button.browse", "Browse"},
        {"keystore.contains.nokeys", "This keystore does not contain private keys."},
        {"label.exportkey", "Export key store (PKCS#12):"},
        {"label.keypass", "Key password for exported keystore:"},
        {"title", "Export key to keystore(PKCS#12 format)"},
        {"filechooser.key.export", "Please select the PKCS#12 keystore file for the export"},
        {"key.export.success.message", "The key has been exported successfully."},
        {"key.export.success.title", "Success"},
        {"key.export.error.message", "There occured an error during the export process.\n{0}"},
        {"key.export.error.title", "Error"},
        {"label.alias", "Key to export:"},
        {"key.exported.to.file", "The key \"{0}\" has been written to the keystore \"{1}\"."},};

}
