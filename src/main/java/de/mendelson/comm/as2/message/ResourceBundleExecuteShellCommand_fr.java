//$Header: /as2/de/mendelson/comm/as2/message/ResourceBundleExecuteShellCommand_fr.java 5     7.12.18 9:45 Heller $
package de.mendelson.comm.as2.message;
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
 * @author E.Pailleau
 * @version $Revision: 5 $
 */
public class ResourceBundleExecuteShellCommand_fr extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"executing.receipt", "Exécution de la commande système sur réception d''un contenu." },
        {"executing.send", "Exécution de la commande système sur envoyer d''un contenu." },
        {"executing.command", "Commande système: \"{0}\"." },
        {"executed.command", "Commande système a exporté, returncode={0}." },
    };
    
}
