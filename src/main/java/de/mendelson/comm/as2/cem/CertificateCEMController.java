//$Header: /as2/de/mendelson/comm/as2/cem/CertificateCEMController.java 8     7.11.18 17:14 Heller $
package de.mendelson.comm.as2.cem;

import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.comm.as2.clientserver.message.RefreshClientCEMDisplay;
import de.mendelson.comm.as2.message.AS2Message;
import de.mendelson.comm.as2.message.AS2MessageInfo;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.clientserver.ClientServer;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.util.systemevents.SystemEventManager;
import de.mendelson.util.systemevents.SystemEventManagerImplAS2;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Checks the database and switches certificates if there are two available for a partner
 * @author S.Heller
 * @version $Revision: 8 $
 */
public class CertificateCEMController implements Runnable {

    /**Wait time, this is how long this thread waits*/
    long waitTime = TimeUnit.MINUTES.toMillis(1);
    /**Logger to log information to*/
    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    /**Connection to the database*/
    private Connection configConnection = null;
    private Connection runtimeConnection = null;
    /**Stores the certificates*/
    private CertificateManager certificateManager;
    private ClientServer clientserver;

    /** Creates new message I/O log and connects to localhost
     *@param host host to connect to
     */
    public CertificateCEMController(ClientServer clientserver, Connection configConnection, Connection runtimeConnection, CertificateManager certificateManager) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.certificateManager = certificateManager;
        this.clientserver = clientserver;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("CEM Certificate Controller");
        while (true) {
            try {
                try {
                    Thread.sleep(this.waitTime);
                } catch (InterruptedException e) {
                    //nop
                }
                this.handleRequestProcessingErrors();
                this.handleCertificateConfigChangeShouldHappenNow();
            } catch (Throwable e) {
                e.printStackTrace();
                this.logger.severe("CertificateCEMController: " + e.getMessage());
                SystemEventManagerImplAS2.systemFailure(e);
            }
        }
    }

    /**In CEM the certifiates for signatures and ssl may contain a respond
     * bydate to enable them. If the bydate is not give in the request nothing will happen until a response
     * comes.
     */
    private void handleCertificateConfigChangeShouldHappenNow() {
        PartnerAccessDB partnerAccess 
                = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        List<CEMEntry> certificateChangeList = cemAccess.getCertificatesToChange();
        SystemEventManagerImplAS2 eventManager 
                = new SystemEventManagerImplAS2();
        for (CEMEntry entry : certificateChangeList) {
            Partner partner = partnerAccess.getPartner(entry.getInitiatorAS2Id());
            KeystoreCertificate referencedCert = this.certificateManager.getKeystoreCertificateByIssuerDNAndSerial(
                    entry.getIssuername(), entry.getSerialId());
            if (referencedCert == null) {
                throw new RuntimeException("The CEM entry references a certificate with issuer " + entry.getIssuername()
                        + " and serial " + entry.getSerialId() + " that is not found in the system");
            }
            cemAccess.markAsProcessed(entry.getRequestId(), entry.getCategory());
            //a state has changed: inform the user
            this.logger.fine(partner.getPartnerCertificateInformationList().getCertificatePurposeDescription(this.certificateManager, partner, entry.getCategory()));
            try {
                eventManager.newEventCertificateChangeShouldHappenNowByCEM(certificateManager, partner, entry.getCategory());
            } catch (Exception e) {
                this.logger.warning("CertificateCEMController: Notification@handleCertificateChanges " + e.getMessage());
            }
        }
        if (this.clientserver != null) {
            this.clientserver.broadcastToClients(new RefreshClientCEMDisplay());
        }
    }

    /**Checks if a CEM request came back with a failure MDN. In this case the whole request should be set
     * to processing error*/
    private void handleRequestProcessingErrors() {
        CEMAccessDB cemAccess = new CEMAccessDB(this.configConnection, this.runtimeConnection);
        MessageAccessDB messageAccess 
                = new MessageAccessDB(this.configConnection,
                        this.runtimeConnection);
        List<CEMEntry> pendingCEM = cemAccess.getCEMEntriesPending();
        for (CEMEntry entry : pendingCEM) {
            if (entry.getRequestMessageid() != null) {
                AS2MessageInfo messageInfo = messageAccess.getLastMessageEntry(entry.getRequestMessageid());
                //it could happen that the pending request message no longer exists
                if (messageInfo != null && messageInfo.getState() == AS2Message.STATE_STOPPED) {
                        cemAccess.setPendingRequestsToState(entry.getInitiatorAS2Id(),
                                entry.getReceiverAS2Id(), CEMEntry.CATEGORY_CRYPT,
                                entry.getRequestId(), CEMEntry.STATUS_PROCESSING_ERROR_INT);
                        cemAccess.setPendingRequestsToState(entry.getInitiatorAS2Id(),
                                entry.getReceiverAS2Id(), CEMEntry.CATEGORY_SIGN,
                                entry.getRequestId(), CEMEntry.STATUS_PROCESSING_ERROR_INT);
                        cemAccess.setPendingRequestsToState(entry.getInitiatorAS2Id(),
                                entry.getReceiverAS2Id(), CEMEntry.CATEGORY_SSL,
                                entry.getRequestId(), CEMEntry.STATUS_PROCESSING_ERROR_INT);
                }
            }
        }
        if (this.clientserver != null) {
            this.clientserver.broadcastToClients(new RefreshClientCEMDisplay());
        }
    }
}
