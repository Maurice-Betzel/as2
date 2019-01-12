//$Header: /as2/de/mendelson/comm/as2/partner/gui/ResourceBundlePartnerPanel_fr.java 32    22.08.18 11:59 Heller $
package de.mendelson.comm.as2.partner.gui;

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
 * @author E.Pailleau
 * @version $Revision: 32 $
 */
public class ResourceBundlePartnerPanel_fr extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"title", "Configuration des partenaires"},
        {"label.name", "Nom:"},
        {"label.id", "AS2 id:"},
        {"label.partnercomment", "Commentaire:" },
        {"label.url", "URL de réception:"},
        {"label.mdnurl", "URL des MDN:"},
        {"label.signalias.key", "Clef privée (Création de signature):"},
        {"label.cryptalias.key", "Clef privée (Décryptage):"},
        {"label.signalias.cert", "Certificat du partenaire (Vérification de la signature):"},
        {"label.cryptalias.cert", "Certificat du partenaire (Cryptage):"},
        {"label.signtype", "Algorithme de signature numérique:"},
        {"label.encryptiontype", "Algorithme de chiffrement des messages:"},
        {"label.email", "Adresse E-mail:"},
        {"label.localstation", "Station locale"},
        {"label.compression", "Compresser les messages sortants (nécessite une solution AS2 1.1 en face)"},
        {"label.usecommandonreceipt", "Commande système sur réception de message:"},
        {"label.usecommandonsenderror", "Commande système sur envoi échoué de message:"},
        {"label.usecommandonsendsuccess", "Commande système sur envoi réussi de message:"},
        {"label.keepfilenameonreceipt", "Garder le nom de fichier original sur réception (si l''émetteur a ajouté cette information)"},
        {"label.address", "Adresse:" },
        {"label.contact", "Contact:" },        
        {"tab.misc", "Divers"},
        {"tab.security", "Sécurité"},
        {"tab.send", "Envoi"},
        {"tab.mdn", "MDN"},
        {"tab.dirpoll", "Scrutation de répertoire"},
        {"tab.receipt", "Réception"},
        {"tab.httpauth", "Authentication HTTP"},
        {"tab.httpheader", "En-tête de HTTP"},
        {"tab.notification", "Notification" },
        {"tab.events", "Evénements" },
        {"tab.partnersystem", "Info" },
        {"label.subject", "Sujet du contenu:"},
        {"label.contenttype", "Type de contenu:"},
        {"label.syncmdn", "Utilise des MDN synchrone"},
        {"label.asyncmdn", "Utilise des MDN asynchrone"},
        {"label.signedmdn", "Utilise des MDN signés"},
        {"label.polldir", "Répertoire de scrutation:"},
        {"label.pollinterval", "Intervalle de scrutation:"},
        {"label.pollignore", "Ignorer les fichiers:"},
        {"label.maxpollfiles", "Maximale des fichiers par sondage:"},
        {"label.usehttpauth", "Utiliser l''authentication HTTP pour envoyer les messages AS2"},
        {"label.usehttpauth.user", "Utilisateur:"},
        {"label.usehttpauth.pass", "Mot de passe:"},
        {"label.usehttpauth.asyncmdn", "Utiliser l''authentication HTTP pour envoyer les MDN asynchrones"},
        {"label.usehttpauth.asyncmdn.user", "Utilisateur:"},
        {"label.usehttpauth.asyncmdn.pass", "Mot de passe:"},
        {"hint.filenamereplacement.receipt1", "Remplacement: $'{'filename}, $'{'subject},"},
        {"hint.filenamereplacement.receipt2", "$'{'sender}, $'{'receiver}, $'{'messageid}"},
        {"hint.replacement.send1", "Remplacement: $'{'filename}, $'{'fullstoragefilename}, $'{'log}, $'{'subject},"},        
        {"hint.replacement.send2", "$'{'sender}, $'{'receiver}, $'{'messageid}, $'{'mdntext}, $'{'userdefinedid}."},
        {"hint.subject.replacement", "L''expression $'{'filename} est remplacée par le nom de fichier d''envoi."},
        {"hint.keepfilenameonreceipt", "Merci de vous assurer que votre partenaire envoi des nom de fichiers uniques avant d''activer cette option!"},
        {"label.notify.send", "Notifier lors d''un dépassement de quota sur message envoyé" },
        {"label.notify.receive", "Notifier lors d''un dépassement de quota sur message reçu" },
        {"label.notify.sendreceive", "Notifier lors d''un dépassement de quota sur message envoyé ou reçu" },
        {"header.httpheaderkey", "Nom" },
        {"header.httpheadervalue", "Valeur" },
        {"httpheader.add", "Ajouter " },
        {"httpheader.delete", "Éliminer" },
        {"label.as2version", "Version AS2:" },
        {"label.productname", "Nom du produit:" },
        {"label.features", "Fonctionnalités:" },
        {"label.features.cem", "Certificat d'échange via CEM" },
        {"label.features.ma", "Plusieurs pièces jointes" },
        {"label.features.compression", "Compression" },
        {"partnerinfo", "Votre partenaire transmet avec chaque message AS2 quelques informations à propos de ses capacités de système AS2. Il s'agit d'une liste de fonctions qui a été transmise par votre partenaire." },
        {"partnersystem.noinfo", "Aucune information n''est disponible, qu''il y avait déjà une transaction?" },
        {"label.httpversion", "Version du protocole HTTP:" },
        {"label.test.connection", "Connexion de test" },
        {"label.url.hint", "<HTML>Merci de faire démarrer cette URL avec le protocole \"http://\" ou \"https://\".</HTML>"},
        {"label.url.hint.mdn", "<HTML>Merci de faire démarrer cette URL avec le protocole \"http://\" ou \"https://\". Il s''agit de l''URL que votre partenaire utilisera pour le MDN asynchrone entrant vers cette station locale.</HTML>"},
        {"label.mdn.description", "<HTML>Le MDN (Message Delivery Notification) est la confirmation du message AS2. Cette section définit le comportement de votre partenaire pour vos messages AS2 sortants.</HTML>" },
        {"label.mdn.sync.description", "<HTML>Le partenaire envoie la confirmation (MDN) sur le canal de retour de votre connexion sortante.</HTML>" },
        {"label.mdn.async.description", "<HTML>Le partenaire établit une nouvelle connexion à votre système pour envoyer une confirmation pour votre message sortant.</HTML>" },
        {"label.mdn.sign.description", "<HTML>Le protocole AS2 ne définit pas comment gérer un MDN si la signature ne correspond pas - mendelson AS2 affiche un avertissement dans ce cas.</HTML>" },
        {"label.algorithmidentifierprotection", "<HTML>Utiliser l''attribut de protection de l''identificateur d''algorithme dans la signature (recommandé), voir RFC 6211</HTML>" },
        {"label.enabledirpoll", "Activer le sondage d''annuaire pour ce partenaire" }
    };
}
