//$Header: /as2/de/mendelson/comm/as2/message/ResourceBundleExecuteShellCommand.java 4     7.12.18 9:45 Heller $
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
 * @version $Revision: 4 $
 */
public class ResourceBundleExecuteShellCommand extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"executing.receipt", "Executing shell command after receipt for payload." },
        {"executing.send", "Executing shell command after send for payload." },
        {"executing.command", "Shell command: \"{0}\"." },
        {"executed.command", "Shell command executed, returncode={0}." },
    };
    
}