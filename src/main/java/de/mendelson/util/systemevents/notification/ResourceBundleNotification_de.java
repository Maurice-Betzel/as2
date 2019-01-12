//$Header: /as2/de/mendelson/util/systemevents/notification/ResourceBundleNotification_de.java 8     22.10.18 15:29 Heller $
package de.mendelson.util.systemevents.notification;

import de.mendelson.util.MecResourceBundle;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/** 
 * ResourceBundle to localize the mendelson products - if you want to localize 
 * eagle to your language, please contact us: localize@mendelson.de
 * @author  S.Heller
 * @version $Revision: 8 $
 */
public class ResourceBundleNotification_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        //dialog
        {"test.message.send", "Eine Testnachricht wurde geschickt an {0}."},
        {"test.message.debug", "\nDer Mailversandprozess schlug fehlt, die folgenden Zusatzinformationen könnten Ihnen helfen, das Problem einzugrenzen:\n" },        
        {"misc.message.send", "Eine Benachrichtigungsmail wurde an {0} geschickt ({1}-{2})."},
        {"misc.message.send.failed", "Das Senden einer Benachrichtigungsmail an {0} schlug fehl"},
        {"notification.about.event", "Diese Benachrichtigung bezieht sich auf das Systemereignis von {0}.\nDringlichkeit: {1}\nQuelle: {2}\nTyp: {3}\nId: {4}" },
        {"notification.summary", "Zusammenfassung von {0} Systemereignissen" },
        {"misc.message.summary.send", "Eine zusammenfassende Benachrichtigungsmail wurde an {0} geschickt" },
        {"misc.message.summary.failed", "Das Senden einer zusammenfassenden Benachrichtigungsmail an {0} schlug fehl" },
        {"do.not.reply", "Bitte antworten Sie nicht auf diese Nachricht."},      
    };
}
