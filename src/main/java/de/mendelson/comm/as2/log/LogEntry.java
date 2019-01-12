//$Header: /as2/de/mendelson/comm/as2/log/LogEntry.java 10    4/06/18 12:21p Heller $
package de.mendelson.comm.as2.log;

import java.io.Serializable;
import java.util.logging.Level;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Enwrapps a single db log entry in an object
 *
 * @author S.Heller
 * @version $Revision: 10 $
 */
public class LogEntry implements Serializable {

    public static final long serialVersionUID = 1L;
    
    private Level level;
    private String message;
    private long millis;
    private String messageId;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Serializes this partner to XML
     *
     * @param level level in the XML hierarchie for the xml beautifying
     */
    public String toXML(int level) {
        String offset = "";
        for (int i = 0; i < level; i++) {
            offset += "\t";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(offset).append("<logentry level=\"").append(String.valueOf(this.level.intValue())).append("\"");
        builder.append(" time=\"").append(this.getMillis()).append("\">");
        if (this.message != null) {
            builder.append(this.toCDATA(this.message));
        }
        builder.append(offset).append("</logentry>\n");
        return (builder.toString());
    }

    /**
     * Adds a cdata indicator to xml data
     */
    private String toCDATA(String data) {
        return ("<![CDATA[" + data + "]]>");
    }
}
