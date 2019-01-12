//$Header: /as2/de/mendelson/util/systemevents/notification/ResourceBundleNotification_fr.java 9     22.10.18 15:29 Heller $
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
 *
 * @author S.Heller
 * @author E.Pailleau
 * @version $Revision: 9 $
 */
public class ResourceBundleNotification_fr extends MecResourceBundle {

    public static final long serialVersionUID = 1L;

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        //dialog
        {"test.message.send", "Un e-mail de test a été envoyé à {0}."},
        {"test.message.debug", "\nEnvoyer un processus envoi a échoué, voici quelques informations de débogage qui pourraient vous aider à:\n"},
        {"misc.message.send", "Un e-mail de notification a été envoyé à {0} ({1}-{2})."},
        {"misc.message.send.failed", "L''envoi d'un message de notification à {0} a échoué"},
        {"notification.about.event", "Cette notification se réfère à l'événement système de {0}.\nUrgence: {1}\nEnfin: {2}\nTyp: {3}\nId: {4}" },
        {"notification.summary", "Résumé des {0} événements système" },
        {"misc.message.summary.send", "Un courriel de notification sommaire a été envoyé à {0}" },
        {"misc.message.summary.failed", "L'envoi d'un message de notification sommaire à {0} a échoué" },
        {"do.not.reply", "Veuillez ne pas répondre à ce mail."},         
    };

}
