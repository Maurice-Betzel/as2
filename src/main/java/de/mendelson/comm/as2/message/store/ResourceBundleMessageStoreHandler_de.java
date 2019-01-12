//$Header: /as2/de/mendelson/comm/as2/message/store/ResourceBundleMessageStoreHandler_de.java 9     7.12.18 9:45 Heller $
package de.mendelson.comm.as2.message.store;
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
 * @version $Revision: 9 $
 */
public class ResourceBundleMessageStoreHandler_de extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"message.error.stored", "Eingebettete Nachricht wurde unter \"{0}\" gespeichert." },
        {"message.error.raw.stored", "Die Übertragungsdaten wurden unter \"{0}\" gespeichert." },        
        {"dir.createerror", "Das Verzeichnis \"{0}\" konnte nicht erstellt werden." },        
        {"comm.success", "Die AS2 Kommunikation war erfolgreich, die Nutzdaten {0} wurden nach \"{1}\" verschoben." },
        {"outboundstatus.written", "Die Statusdatei für Ausgangstransaktion wurde geschrieben nach \"{0}\"."},
    };
    
}