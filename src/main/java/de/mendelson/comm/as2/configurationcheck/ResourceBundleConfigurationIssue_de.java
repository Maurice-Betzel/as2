//$Header: /as2/de/mendelson/comm/as2/configurationcheck/ResourceBundleConfigurationIssue_de.java 8     5/08/18 12:42p Heller $
package de.mendelson.comm.as2.configurationcheck;

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
 *
 * @author S.Heller
 * @version $Revision: 8 $
 */
public class ResourceBundleConfigurationIssue_de extends MecResourceBundle {

    public static final long serialVersionUID = 1L;
    
    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }
    /**
     * List of messages in the specific language
     */
    static final Object[][] CONTENTS = {
        //preferences localized
        {String.valueOf(ConfigurationIssue.CERTIFICATE_EXPIRED_ENC_SIGN), "Zertifikat ist abgelaufen (enc/sign)"},
        {String.valueOf(ConfigurationIssue.CERTIFICATE_EXPIRED_SSL), "Zertifikat ist abgelaufen (SSL)"},
        {String.valueOf(ConfigurationIssue.MULTIPLE_KEYS_IN_SSL_KEYSTORE), "Mehrere Schlüssel im SSL Keystore gefunden - darf nur einer sein"},
        {String.valueOf(ConfigurationIssue.NO_KEY_IN_SSL_KEYSTORE), "Kein Schlüssel im SSL Keystore gefunden"},
        {String.valueOf(ConfigurationIssue.HUGE_AMOUNT_OF_TRANSACTIONS_NO_AUTO_DELETE), "Aktivieren Sie automatisches Löschen - Im System ist eine grosse Menge von Transaktionen"},
        {String.valueOf(ConfigurationIssue.FEW_CPU_CORES), "Weisen Sie dem System mindestens 4 Prozessorkerne zu"},
        {String.valueOf(ConfigurationIssue.LOW_MAX_HEAP_MEMORY), "Reservieren Sie mindestens 1GB Hauptspeicher für den Serverprozess"},
        {String.valueOf(ConfigurationIssue.NO_OUTBOUND_CONNECTIONS_ALLOWED), "Menge ausgehender Verbindungen ist auf 0 gesetzt - das System wird NICHT senden"},
        {String.valueOf(ConfigurationIssue.CERTIFICATE_MISSING_ENC_REMOTE_PARTNER), "Fehlendes Verschlüsselungszertifikat eines entfernten Partners"},
        {String.valueOf(ConfigurationIssue.CERTIFICATE_MISSING_SIGN_REMOTE_PARTNER), "Fehlendes Signaturzertifikat eines entfernten Partners"},        
        {String.valueOf(ConfigurationIssue.KEY_MISSING_ENC_LOCAL_STATION), "Fehlender Verschlüsselungsschlüssel einer lokalen Station"},
        {String.valueOf(ConfigurationIssue.KEY_MISSING_SIGN_LOCAL_STATION), "Fehlender Signaturschlüssel einer lokalen Station"},
        {String.valueOf(ConfigurationIssue.USE_OF_TEST_KEYS_IN_SSL), "Verwendung eines öffentlich verfügbaren Testschlüssels als SSL Schlüssel"},
        {String.valueOf(ConfigurationIssue.JVM_32_BIT), "Die Verwendung einer 32 Bit Java VM wird nicht für den produktiven Einsatz empfohlen, da dann der maximale Heap-Speicher auf 1,3GB begrenzt ist."},
    };
}
