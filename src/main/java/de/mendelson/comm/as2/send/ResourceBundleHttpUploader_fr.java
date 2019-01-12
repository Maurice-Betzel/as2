//$Header: /as2/de/mendelson/comm/as2/send/ResourceBundleHttpUploader_fr.java 14    6.12.18 16:26 Heller $
package de.mendelson.comm.as2.send;
import de.mendelson.util.MecResourceBundle;

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @author E.Pailleau
 * @version $Revision: 14 $
 */
public class ResourceBundleHttpUploader_fr extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"returncode.ok", "Message envoyé avec succès (HTTP {0}); {1} transféré en {2} [{3} KB/s]." },
        {"returncode.accepted", "Message envoyé avec succès (HTTP {0}); {1} transféré en {2} [{3} KB/s]." },
        {"sending.msg.sync", "Envoi du AS2 message vers {0}, sync MDN demandé." },
        {"sending.cem.sync", "Envoi du CEM message vers {0}, sync MDN demandé." },
        {"sending.msg.async", "Envoi du AS2 message vers {0}, async MDN demandé vers {1}." },
        {"sending.cem.async", "Envoi du CEM message vers {0}, async MDN demandé vers {1}." },
        {"sending.mdn.async", "Envoi d''un MDN asynchrone vers {0}." },
        {"error.httpupload", "La transmission a echouée, le serveur AS2 distant signale \"{0}\"." },
        {"error.noconnection", "Problème de connexion, données non transmises." },
        {"error.http502", "Problème de connexion, données non transmises. (HTTP 502 - BAD GATEWAY)" },
        {"error.http503", "Problème de connexion, données non transmises. (HTTP 503 - SERVICE UNAVAILABLE)" },
        {"error.http504", "Problème de connexion, données non transmises. (HTTP 504 - GATEWAY TIMEOUT)" },
        {"using.proxy", "Utilisation d''un serveur mandataire (proxy) {0}:{1}." },
        {"answer.no.sync.mdn", "La synchronisation MDN reçue ne semble pas être au bon format. Comme les problèmes de structure MDN sont rares, il se peut que ce n'est pas une réponse du système AS2 que vous vouliez aborder, mais peut-être un proxy ou un site web standard? Les valeurs d'en-tête HTTP suivantes sont manquantes: [{0}].\nLes données reçues commencent par les structures suivantes: \n{1}" },
        {"hint.SSLPeerUnverifiedException", "Astuce:\nCette est un problème qui est survenue au cours de la négociation SSL. Le système a été incapable d''établir une connexion sécurisée avec votre partenaire, ce problème n''est pas lié protocole AS2.\nVeuillez vérifier les points suivants à corriger cette question:\n* Avez-vous importé toutes vos partenaires certificats SSL dans votre magasin de clés SSL (certificats root/intermédiaires)\n*Votre partenaire importé tous vos certificats SSL dans son magasin de clés (Les certificats root/intermédiaire)? "},
        {"hint.ConnectTimeoutException", "Hint:\nCeci est un problème essentiellement d''une infrastructure qui est pas le protocole AS2 connexes. Le système n'a pas pu établir une connexion sortante à votre système partenaires AS2.\nPlease check the following to fix this issue:\n*S'il vous plaît vérifier les points suivants pour résoudre ce problème?\n*S''il vous plaît Vérifier l''URL de la réception de votre partenaire, est-il une faute de frappe?\n*S''il vous plaît communiquer avec votre partenaire, peut-être son système de AS2 est en baisse?" },
    };
    
}
