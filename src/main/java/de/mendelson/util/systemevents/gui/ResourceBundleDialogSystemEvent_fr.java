//$Header: /oftp2/de/mendelson/util/systemevents/gui/ResourceBundleDialogSystemEvent_fr.java 5     23.10.18 11:00 Heller $
package de.mendelson.util.systemevents.gui;

import de.mendelson.util.MecResourceBundle;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * ResourceBundle to localize the mendelson products
 *
 * @author S.Heller
 * @version $Revision: 5 $
 */
public class ResourceBundleDialogSystemEvent_fr extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"title", "Visualiseur d'événements système"},
        {"label.user", "Propriétaire:"},
        {"label.host", "Hôte:"},
        {"label.id", "Id de l'événement:"},
        {"label.date", "Date:"},
        {"label.type", "Type:"},
        {"label.category", "Catégorie:"},
        {"header.timestamp", "Horodatage"},
        {"header.type", "Type"},
        {"header.category", "Catégorie"},
        {"user.server.process", "Processus serveur" },
        {"label.startdate", "Début: " },
        {"label.enddate", "Fin: " },
        {"no.data", "Aucun événement système ne correspond à la sélection de date/type en cours." },  
        {"label.freetext", "Rechercher du texte: " },
        {"label.freetext.hint", "Recherche par numéro d'événement ou par texte dans la partie texte des événements" },
        {"category.all", "-- Tous --" },      
        {"label.close", "Fermer" },
        {"label.search", "Recherche par événement" },
    };
}
