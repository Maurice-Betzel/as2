//$Header: /oftp2/de/mendelson/util/systemevents/gui/ResourceBundleDialogSystemEvent.java 6     23.10.18 11:00 Heller $
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
 * @version $Revision: 6 $
 */
public class ResourceBundleDialogSystemEvent extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        {"title", "System event viewer"},
        {"label.user", "Owner:"},
        {"label.host", "Host:"},
        {"label.id", "Event id:"},
        {"label.date", "Date:"},
        {"label.type", "Type:"},
        {"label.category", "Category:"},
        {"header.timestamp", "Timestamp"},
        {"header.type", "Type"},
        {"header.category", "Category"},
        {"user.server.process", "Server process" },
        {"no.data", "There is no system event that matches the current date/type selection." },        
        {"label.startdate", "Start: " },
        {"label.enddate", "End: " },
        {"label.freetext", "Free text: " },
        {"label.freetext.hint", "Could be a full event id or textual search in body and subject" },
        {"category.all", "-- All --" },
        {"label.close", "Close" },
        {"label.search", "Event search" },
    };
}
