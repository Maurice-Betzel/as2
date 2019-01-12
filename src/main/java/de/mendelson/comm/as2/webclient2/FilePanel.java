//$Header: /as2/de/mendelson/comm/as2/webclient2/FilePanel.java 4     7.11.18 10:40 Heller $
package de.mendelson.comm.as2.webclient2;

import com.vaadin.ui.TextArea;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog that display a file content
 *
 * @author S.Heller
 * @version $Revision: 4 $
 */
public class FilePanel extends TextArea {

    private String displayedFilename = "";
    /**
     * Max filesize for the display of data in the panel, actual 100kB
     */
    private final static double MAX_FILESIZE = 100 * Math.pow(2, 10);

    public FilePanel() {
        this.setRows(15);
        this.setSizeFull();
    }

    /**
     * Copies all data from one stream to another
     */
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[1024];
        int read = 1024;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1) {
            read = inStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }
        }
        outStream.flush();
    }

    public void displayFile(Path file) {
        long filesize = 0;
        try {
            filesize = Files.size(file);
        } catch (IOException e) {
            //nop
        }
        boolean readOnlyStateOld = this.isReadOnly();
        //there will be displayed a new value to the panel
        this.setReadOnly(false);
        String filename = "";
        if (file == null) {
            this.setValue("No file");
        } else if (!Files.exists(file)) {
            this.setValue("File not found: " + file.toAbsolutePath().toString());
            filename = file.toAbsolutePath().toString();
        } else if (filesize > MAX_FILESIZE) {
            this.setValue("Filesize too large to display");
            filename = file.toAbsolutePath().toString();
        } else {
            try {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                InputStream inStream = null;
                try {
                    inStream = Files.newInputStream(file);
                    this.copyStreams(inStream, outStream);
                } finally {
                    if (inStream != null) {
                        inStream.close();
                    }
                }
                outStream.flush();
                this.setValue(new String(outStream.toByteArray()));
                outStream.close();
                filename = file.toAbsolutePath().toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.displayedFilename = filename;
        this.setReadOnly(readOnlyStateOld);
    }

    public String getDisplayedFilename() {
        return displayedFilename;
    }

}
