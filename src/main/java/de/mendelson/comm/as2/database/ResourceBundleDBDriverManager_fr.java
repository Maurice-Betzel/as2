//$Header: /as2/de/mendelson/comm/as2/database/ResourceBundleDBDriverManager_fr.java 1     21.09.18 9:25 Heller $
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
public class ResourceBundleDBDriverManager_fr extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"creating.database." + DBDriverManager.DB_RUNTIME, "Créer une base de données d'exécution"},
        {"creating.database." + DBDriverManager.DB_CONFIG, "Créer une base de données de configuration"},
        {"database.creation.success." + DBDriverManager.DB_RUNTIME, "La base de données d'exécution a été créée avec succès" },
        {"database.creation.success." + DBDriverManager.DB_CONFIG, "La base de données de configuration a été créée avec succès" },
        {"database.creation.failed." + DBDriverManager.DB_RUNTIME, "Une erreur est survenue lors de la création de la base de données d''exécution" },
        {"database.creation.failed." + DBDriverManager.DB_CONFIG, "Une erreur est survenue lors de la création de la base de données de configuration." },
    };

}
