//$Header: /as2/de/mendelson/comm/as2/database/ResourceBundleDBServer_fr.java 9     2.10.18 13:14 Heller $
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
 * ResourceBundle to localize the mendelson business integration  - if you want to localize 
 * eagle to your language, please contact us: localize@mendelson.de
 * @author S.Heller
 * @author E.Pailleau
 * @version $Revision: 9 $
 */
public class ResourceBundleDBServer_fr extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"database." + DBDriverManager.DB_CONFIG, "base de configuration" },
        {"database." + DBDriverManager.DB_RUNTIME, "base de maturité" },
        {"dbserver.startup", "Démarrer le serveur de base de données.." },
        {"dbserver.running", "{0} données en cours d''exécution"},
        {"update.versioninfo", "Mise à jour automatique de BD: la version de BD trouvé est {0}"
            + ", la version de BD requise est {1}."},
        {"update.progress", "Mise à jour incrementale de base de données ..."},
        {"update.progress.version.start", "(Commencement) La {1} a été mise à jour vers la version {0}."},
        {"update.progress.version.end", "(Fin) La {1} a été mise à jour vers la version {0}."},
        {"update.error", "FATAL: impossible de mettre à jour la base de données "
            + " de la version {0} vers la version {1}.\n"
            + "Merci de supprimer entièrement la base de donnée par la suppression"
            + " de tous les fichiers  AS2_DB_*.* dans le répertoire d''installation.\n"
            + "Toute vos données personnelles seront détruite à l''issue de cette opération."},
        {"update.successfully", "La mise à jour de la BD vers la version requise a été réalisée avec succès."},
        {"update.notfound", "Pour la mise à jour, the fichier update{0}to{1}.sql et/ou "
            + "Update{0}to{1}.class doivent être présents dans le répertoire sqlscript."},
        {"upgrade.required", "Une mise à jour est nécessaire.\nS''il vous plaît exécuter as2upgrade.bat ou as2upgrade.sh avant de démarrer le serveur."},
    };
}
