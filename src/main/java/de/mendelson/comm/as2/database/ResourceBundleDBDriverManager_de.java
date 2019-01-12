//$Header: /as2/de/mendelson/comm/as2/database/ResourceBundleDBDriverManager_de.java 1     21.09.18 9:25 Heller $
package de.mendelson.comm.as2.database;

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
 *
 * @author S.Heller
 * @version $Revision: 1 $
 */
public class ResourceBundleDBDriverManager_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"creating.database." + DBDriverManager.DB_RUNTIME, "Erstelle Laufzeitdatenbank"},
        {"creating.database." + DBDriverManager.DB_CONFIG, "Erstelle Konfigurationsdatenbank"},
        {"database.creation.success." + DBDriverManager.DB_RUNTIME, "Die Laufzeitdatenbank wurde erfolgreich erstellt" },
        {"database.creation.success." + DBDriverManager.DB_CONFIG, "Die Konfigurationsdatenbank wurde erfolgreich erstellt" },
        {"database.creation.failed." + DBDriverManager.DB_RUNTIME, "Ein Fehler trat beim Erstellen der Laufzeitdatenbank auf" },
        {"database.creation.failed." + DBDriverManager.DB_CONFIG, "Ein Fehler trat beim Erstellen der Konfigurationsdatenbank auf" },
    };

}
