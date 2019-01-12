//$Header: /as2/de/mendelson/util/security/cert/gui/keygeneration/ResourceBundleGenerateKey.java 9     4/06/18 1:35p Heller $
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
public class ResourceBundleGenerateKey extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {

        {"title", "Generate key" },
        {"button.ok", "Ok" },
        {"button.cancel", "Cancel" },
        {"label.keytype", "Key type:" },
        {"label.signature", "Signature:" },
        {"label.size", "Size:" },
        {"label.commonname", "Common name:" },
        {"label.commonname.hint", "(Domain name of the server)" },
        {"label.organisationunit", "Organisation unit:" },
        {"label.organisationname", "Organisation name:" },
        {"label.locality", "Locality:" },
        {"label.locality.hint", "(City)" },
        {"label.state", "State:" },
        {"label.countrycode", "Country code:" },
        {"label.countrycode.hint", "(2 digits, ISO 3166)" },
        {"label.mailaddress", "Mail address:" },
        {"label.validity", "Validity in days:" },
        {"label.purpose", "Key purpose / Additional key usage:" },
        {"label.purpose.encsign", "Encryption and signature" },
        {"label.purpose.ssl", "TSL/SSL" },        
        {"label.subjectalternativenames", "Subject alternative names:" },        
        {"warning.mail.in.domain", "The mail address is not part of the domain \"{0}\" (e.g. myname@{0}).\nThis might be a problem if you would like to trust the key later."},
        {"warning.nonexisting.domain", "The domain \"{0}\" seems not to exist." },
        {"warning.invalid.mail", "The mail address \"{0}\" is invalid." },
        {"button.reedit", "Edit settings" },
        {"button.ignore", "Ignore warnings" },
        {"warning.title", "Possible key settings problem" },
        {"view.expert", "Expert view" },
        {"view.basic", "Basic view" },
    };
    
}