//$Header: /as2/de/mendelson/comm/as2/message/ByteStorageImplFile.java 6     6.11.18 16:59 Heller $
package de.mendelson.comm.as2.message;

import de.mendelson.util.AS2Tools;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Container that stores byte arrays in a temp file
 *
 * @author S.Heller
 * @version $Revision: 6 $
 */
public class ByteStorageImplFile implements IByteStorage {

    /**
     * IByteStorage extends Serializable
     */
    public static final long serialVersionUID = 1L;
    private Path file = null;

    public ByteStorageImplFile() {
    }

    @Override
    /**
     * Returns the actual stored data size
     */
    public int getSize() {
        if (this.file == null) {
            return (0);
        }
        try {
            return ((int) Files.size(this.file));
        } catch (IOException e) {
            return (0);
        }
    }

    @Override
    /**
     * store a byte array
     */
    public void put(byte[] data) throws Exception {
        //create the file storage
        Path tempFile = AS2Tools.createTempFile("AS2ByteStorage", ".bin");
        this.file = tempFile;
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        OutputStream outStream = null;
        try {
            outStream = Files.newOutputStream(this.file);
            this.copyStreams(inStream, outStream);
        } finally {
            if (outStream != null) {
                outStream.flush();
                outStream.close();
            }
            inStream.close();
        }
    }

    @Override
    public byte[] get() throws Exception {
        if (this.file == null) {
            return (new byte[0]);
        }
        InputStream inStream = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            inStream = Files.newInputStream(this.file);
            this.copyStreams(inStream, outStream);
        } finally {
            outStream.flush();
            if (inStream != null) {
                inStream.close();
            }
            outStream.close();
        }
        return (outStream.toByteArray());
    }

    @Override
    /**
     * Returns an input stream to read directly from the underlaying buffer
     */
    public InputStream getInputStream() throws Exception {
        return (Files.newInputStream(this.file));
    }

    @Override
    public void release() {
        try {
            Files.delete(this.file);
        } catch (IOException e) {
            //nop
        }
    }

    /**
     * Copies all data from one stream to another
     */
    private void copyStreams(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[2048];
        int read = 0;
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
}
