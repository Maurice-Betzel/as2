//$Header: /as2/de/mendelson/comm/as2/client/manualsend/ManualSendRequest.java 8     6/13/18 2:03p Heller $
package de.mendelson.comm.as2.client.manualsend;

import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.util.clientserver.clients.datatransfer.UploadRequestFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Message for the client server protocol
 *
 * @author S.Heller
 * @version $Revision: 8 $
 */
public class ManualSendRequest extends UploadRequestFile implements Serializable {

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public static final long serialVersionUID = 1L;
    private Partner sender;
    private Partner receiver;
    private List<String> filenames = new ArrayList<String>();
    private String resendMessageId = null;
    private String userdefinedId = null;
    private List<String> uploadHashs = new ArrayList<String>();
    private String subject = null;

    @Override
    public String toString() {
        return ("Manual send request");
    }

    /**
     * @return the sender
     */
    public Partner getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(Partner sender) {
        this.sender = sender;
    }

    /**
     * @return the receiver
     */
    public Partner getReceiver() {
        return receiver;
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(Partner receiver) {
        this.receiver = receiver;
    }

    /**
     * @return the filename
     */
    public List<String> getFilenames() {
        return (this.filenames);
    }

    /**
     * @param filename the filename to set
     */
    public void addFilename(String filename) {
        this.filenames.add(filename);
    }

    /**
     * @return the resendMessageId
     */
    public String getResendMessageId() {
        return (this.resendMessageId);
    }

    /**
     * Set this message id if this is a resend of an existing message
     *
     * @param resendMessageId the resendMessageId to set
     */
    public void setResendMessageId(String resendMessageId) {
        this.resendMessageId = resendMessageId;
    }

    /**
     * @return the userdefinedId
     */
    public String getUserdefinedId() {
        return userdefinedId;
    }

    /**
     * Sets a user defined id to this transaction. If this is set the user
     * defined id could be used later to track the progress of this send
     * transmission.
     *
     * @param userdefinedId the userdefinedId to set
     */
    public void setUserdefinedId(String userdefinedId) {
        this.userdefinedId = userdefinedId;
    }

    /**
     * @return the uploadHashs
     */
    public List<String> getUploadHashs() {
        return (this.uploadHashs);
    }

    /**
     * @param uploadHashs the uploadHashs to set
     */
    public void setUploadHashs(List<String> uploadHashs) {
        this.uploadHashs.addAll(uploadHashs);
    }

    @Override
    public void setUploadHash(String singleUploadHash) {
        this.uploadHashs.add(singleUploadHash);
    }

    @Override
    public String getUploadHash() {
        throw new IllegalArgumentException("ManualSendRequest: Use the method getUploadHashs() to get the uploaded file hashs");
    }

}
