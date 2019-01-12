//$Header: /as2/de/mendelson/comm/as2/partner/clientserver/PartnerSystemRequest.java 2     4/06/18 12:21p Heller $
package de.mendelson.comm.as2.partner.clientserver;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.clientserver.messages.ClientServerMessage;
import java.io.Serializable;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Msg for the client server protocol
 *
 * @author S.Heller
 * @version $Revision: 2 $
 */
public class PartnerSystemRequest extends ClientServerMessage implements Serializable {

    public static final long serialVersionUID = 1L;
    private Partner partner;

    public PartnerSystemRequest(Partner partner) {
        this.partner = partner;
    }

    @Override
    public String toString() {
        return ("Request partner system");
    }

    /**
     * @return the partner
     */
    public Partner getPartner() {
        return partner;
    }
}
