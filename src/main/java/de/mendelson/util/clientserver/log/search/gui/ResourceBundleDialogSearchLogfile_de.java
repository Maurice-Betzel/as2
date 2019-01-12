//$Header: /as2/de/mendelson/util/clientserver/log/search/gui/ResourceBundleDialogSearchLogfile_de.java 1     20.12.18 15:05 Heller $
package de.mendelson.util.clientserver.log.search.gui;

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
 * @version $Revision: 1 $
 */
public class ResourceBundleDialogSearchLogfile_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"title", "Logdateien auf dem Server durchsuchen"},
        {"no.data.messageid", "**Es gibt keine Logdaten für die AS2 Nachrichtennummer \"{0}\" in dem gewählten Zeitraum. Bitte verwenden Sie als Suchzeichenkette die vollständige Nachrichtennummer." },        
        {"no.data.mdnid", "**Es gibt keine Logdaten für die MDN Nummer \"{0}\" in dem gewählten Zeitraum. Bitte verwenden Sie als Suchzeichenkette die vollständige MDN Nummer, die Sie dem Log einer Übertragung entnehmen können." },        
        {"no.data.uid", "**Es gibt keine Logdaten für die benutzerdefinierte Nummer \"{0}\" in dem gewählten Zeitraum. Bitte wählen Sie als Suchzeichenkette die vollständige benutzerdefinierte Nummer, die Sie der Übertragung mitgegeben haben." },        
        {"label.startdate", "Start: " },
        {"label.enddate", "Ende: " },
        {"button.close", "Schliessen" },
        {"label.search", "Log durchsuchen" },
        {"label.info", "<html>Bitte definieren Sie einen Zeitraum, geben eine vollständige AS2 Nachrichtennummer oder die vollständige Nummer einer MDN ein, um alle Logeinträge dafür auf dem Server zu finden - dann drücken Sie bitte den Knopf \"Log durchsuchen\". Die benutzerdefinierte Nummer können Sie für jede Transaktion definieren, wenn Sie die Daten über die Kommandozeile an den laufenden Server schicken.</html>" },
        {"textfield.preset", "mendelsonAS2@partnerAS2" },
        {"label.messageid", "Nachrichtennummer" },
        {"label.mdnid", "MDN Nummer" },
        {"label.uid", "Benutzerdefinierte Nummer" },
        {"problem.serverside", "Es gab ein serverseitiges Problem beim Durchsuchen der Logdateien: [{0}] {1}" },
    };
}
