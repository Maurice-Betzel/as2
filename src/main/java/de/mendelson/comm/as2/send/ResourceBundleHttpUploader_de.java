//$Header: /as2/de/mendelson/comm/as2/send/ResourceBundleHttpUploader_de.java 23    7.12.18 10:25 Heller $
package de.mendelson.comm.as2.send;
import de.mendelson.util.MecResourceBundle;

/**
 * ResourceBundle to localize a mendelson product
 * @author S.Heller
 * @version $Revision: 23 $
 */
public class ResourceBundleHttpUploader_de extends MecResourceBundle{
    
    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    
    /**List of messages in the specific language*/
    static final Object[][] CONTENTS = {
        {"returncode.ok", "Nachricht erfolgreich versandt (HTTP {0}); {1} übertragen in {2} [{3} KB/s]." },
        {"returncode.accepted", "Nachricht erfolgreich versandt (HTTP {0}); {1} übertragen in {2} [{3} KB/s]." },
        {"sending.msg.sync", "Sende AS2 Nachricht an {0}, erwarte synchrone MDN zur Empfangsbestätigung." },
        {"sending.cem.sync", "Sende CEM Nachricht an {0}, erwarte synchrone MDN zur Empfangsbestätigung." },
        {"sending.msg.async", "Sende AS2 Nachricht an {0}, erwarte asynchrone MDN zur Empfangsbestätigung auf {1}." },
        {"sending.cem.async", "Sende CEM Nachricht an {0}, erwarte asynchrone MDN zur Empfangsbestätigung auf {1}." },
        {"sending.mdn.async", "Sende asynchrone Empfangsbestätigung (MDN) an {0}." },
        {"error.httpupload", "Übertragung fehlgeschlagen, entfernter AS2 Server meldet \"{0}\"." },
        {"error.noconnection", "Verbindungsproblem, es konnten keine Daten übertragen werden." },
        {"error.http502", "Verbindungsproblem, es konnten keine Daten übertragen werden. (HTTP 502 - BAD GATEWAY)" },
        {"error.http503", "Verbindungsproblem, es konnten keine Daten übertragen werden. (HTTP 503 - SERVICE UNAVAILABLE)" },
        {"error.http504", "Verbindungsproblem, es konnten keine Daten übertragen werden. (HTTP 504 - GATEWAY TIMEOUT)" },
        {"using.proxy", "Benutze Proxy {0}:{1}." },  
        {"answer.no.sync.mdn", "Die empfangene synchrone Empfangsbestätigung ist nicht im richtigen Format. Da MDN-Strukturprobleme ungewöhnlich sind, könnte es sein, dass dies keine Antwort des AS2-Systems ist, das Sie ansprechen wollten, sondern vielleicht die Antwort eines Proxies oder die Antwort einer Standard-Website? Die folgenden HTTP Header-Werte fehlen: [{0}].\nDie erhaltenen Daten fangen mit folgenden Strukturen an:\n{1}" },
        {"hint.SSLPeerUnverifiedException", "Hinweis:\nDieses Problem passierte während des SSL Handshake. Das System konnte somit keine sichere Verbindung zu Ihrem Partner aufbauen, das Problem hat nichts mit dem AS2 Protokoll zu tun.\nBitte prüfen Sie folgendes:\n*Haben Sie alle Zertifikate Ihres Partners in Ihren SSL Keystore importiert (für SSL, inkl Intermediate/Root Zertifikate)?\n*Hat Ihr Partner alle Zertifiakte von Ihnen importiert (für SSL, inkl Intermediate/Root Zertifikate)?" }, 
        {"hint.ConnectTimeoutException", "Hinweis:\nDies ist in der Regel ein Infrastrukturproblem, das nichts mit dem AS2 Protokoll zu tun hat. Es ist nicht möglich, eine ausgehende Verbindung zu Ihrem Partner aufzubauen.\nBitte prüfen Sie folgendes, um das Problem zu beheben:\n*Haben Sie eine aktive Internetverbindung?\n*Bitte prüfen sie, ob Sie in der Partnerverwaltung die richtige EmpfangsURL Ihres Partners eingegeben haben?\n*Bitte kontaktieren Sie Ihren Partner, eventuell ist sein AS2 System nicht verfügbar?" },
    };
    
}