//$Header: /as2/de/mendelson/util/security/cert/gui/keygeneration/ResourceBundleGenerateKey_de.java 9     4/06/18 1:35p Heller $
package de.mendelson.util.security.cert.gui.keygeneration;
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
public class ResourceBundleGenerateKey_de extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {

        {"title", "Schlüsselerstellung" },
        {"button.ok", "Ok" },
        {"button.cancel", "Abbruch" },
        {"label.keytype", "Schlüsseltyp:" },
        {"label.signature", "Signatur:" },
        {"label.size", "Schlüssellänge:" },
        {"label.commonname", "Common Name:" },
        {"label.commonname.hint", "(Domain Name des Servers)" },
        {"label.organisationunit", "Organisation (Unit):" },
        {"label.organisationname", "Organisation (Name):" },
        {"label.locality", "Ort:" },
        {"label.locality.hint", "(Stadt)" },
        {"label.state", "Land:" },
        {"label.countrycode", "Ländercode:" },
        {"label.countrycode.hint", "(2 Zeichen, ISO 3166)" },
        {"label.mailaddress", "Mail Adresse:" },
        {"label.validity", "Gültigkeit in Tagen:" },
        {"label.purpose", "Verwendungszweck / Schlüsselerweiterungen:" },
        {"label.purpose.encsign", "Verschlüsselung und digitale Signatur" },
        {"label.purpose.ssl", "TSL/SSL" },
        {"label.subjectalternativenames", "Alternative Antragstellernamen:" },        
        {"warning.mail.in.domain", "Die Mailadresse ist nicht Teil der Domain \"{0}\" (z.B. meinname@{0}).\nDies kann ein Problem sein, wenn der Schlüssel später beglaubigt werden soll."},
        {"warning.nonexisting.domain", "Die Domain \"{0}\" existiert nicht." },
        {"warning.invalid.mail", "Die Mail Adresse \"{0}\" ist ungültig." },
        {"button.reedit", "Überarbeiten" },
        {"button.ignore", "Warnungen ignorieren" },
        {"warning.title", "Mögliches Problem der Schlüsselparameter" },
        {"view.expert", "Experten Ansicht" },
        {"view.basic", "Standard Ansicht" },
    };
    
}