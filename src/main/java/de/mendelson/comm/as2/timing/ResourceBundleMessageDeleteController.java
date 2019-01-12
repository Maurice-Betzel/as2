//$Header: /as2/de/mendelson/comm/as2/timing/ResourceBundleMessageDeleteController.java 5     24.10.18 10:13 Heller $
package de.mendelson.comm.as2.timing;
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
 * @version $Revision: 5 $
 */
public class ResourceBundleMessageDeleteController extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"autodelete", "{0}: This message is older than {1} {2} and has been deleted by the system maintenance process." },    
        {"transaction.deleted.user", "Transaction(s) deleted by user interaction" },
        {"transaction.deleted.system", "Transaction(s) deleted by system maintenance process" },
        {"transaction.deleted.transactiondate", "Transaction date: {0}" },
    };
    
}