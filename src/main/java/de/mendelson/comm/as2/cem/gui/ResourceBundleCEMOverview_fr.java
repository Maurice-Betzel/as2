//$Header: /as2/de/mendelson/comm/as2/cem/gui/ResourceBundleCEMOverview_fr.java 6     6/21/18 12:48p Heller $
package de.mendelson.comm.as2.cem.gui;
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
 * @version $Revision: 6 $
 */
public class ResourceBundleCEMOverview_fr extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"title", "Certificat d'�change pr�sentation" },
        {"button.sendcem", "Noveau Exchange" },
        {"button.requestdetails", "D�tails de la demande" },
        {"button.responsedetails", "D�tails de la r�ponse" },
        {"button.exit", "Fermer" },
        {"button.cancel", "Annuler" },
        {"button.refresh", "Actualisation" },
        {"button.remove", "Supprimer" },
        {"header.state", "R�ponse" },
        {"header.category", "Utilis� pour" },
        {"header.requestdate", "Demand� au" },
        {"header.initiator", "De" },
        {"header.receiver", "�" },
        {"label.certificate", "Certificat:"},
        {"header.alias", "Certificat"},
        {"header.activity", "Activit� du syst�me" },
        {"activity.waitingforprocessing", "En attente de traitement" },
        {"activity.waitingforanswer", "En attente de r�ponse" },
        {"activity.waitingfordate", "En attente de la date d''activation ({0})" },
        {"activity.activated", "Aucun - Activ� au {0}" },
        {"activity.none", "Aucun" },
        {"tab.certificate", "Certificat" },
        {"tab.reasonforrejection", "Raison du rejet" },
    };
    
}