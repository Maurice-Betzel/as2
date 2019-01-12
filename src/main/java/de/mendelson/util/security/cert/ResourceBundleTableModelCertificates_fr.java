//$Header: /as2/de/mendelson/util/security/cert/ResourceBundleTableModelCertificates_fr.java 2     4/06/18 1:35p Heller $
package de.mendelson.util.security.cert;
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
 * @author E.Pailleau
 * @version $Revision: 2 $
 */
public class ResourceBundleTableModelCertificates_fr extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"header.alias", "Alias" },
        {"header.expire", "Date d''expiration" },
        {"header.length", "Longueur" },
        {"header.organization", "Organisation" },
        {"header.ca", "CA" },
    };
    
}