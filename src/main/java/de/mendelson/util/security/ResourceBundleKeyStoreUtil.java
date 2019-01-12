//$Header: /as2/de/mendelson/util/security/ResourceBundleKeyStoreUtil.java 4     4/06/18 1:35p Heller $
package de.mendelson.util.security;
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
 * @version $Revision: 4 $
 */
public class ResourceBundleKeyStoreUtil extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {                
        {"alias.exist", "An entry with the alias \"{0}\" does already exist in the underlaying keystore." },
        {"readerror.invalidcert", "This is not a valid certificate or contains unsupported encoding." },
        {"readerror.zipcert", "This is a zip archive and not a valid certificate." },
        {"privatekey.notfound", "The keystore does not contain the private key with the alias \"{0}\"." },        
    };
    
}