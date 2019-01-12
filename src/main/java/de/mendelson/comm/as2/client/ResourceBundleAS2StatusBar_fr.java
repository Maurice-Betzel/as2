//$Header: /as2/de/mendelson/comm/as2/client/ResourceBundleAS2StatusBar_fr.java 4     4/06/18 1:35p Heller $ 
package de.mendelson.comm.as2.client;

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
 * @version $Revision: 4 $
 */
public class ResourceBundleAS2StatusBar_fr extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"count.ok", "Transactions sans fautes"},
        {"count.all", "Nombre des transactions"},
        {"count.pending", "Transactions attendant"},
        {"count.failure", "Transactions defectueuses"},
        {"count.selected", "Transactions choisies"},
        {"configuration.issue.single", "{0} problème de configuration"},
        {"configuration.issue.multiple", "{0} problèmes de configuration"},
        {"no.configuration.issues", "Pas des problèmes de configuration"},};

}
