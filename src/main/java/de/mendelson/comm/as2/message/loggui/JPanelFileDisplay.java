//$Header: /as2/de/mendelson/comm/as2/message/loggui/JPanelFileDisplay.java 20    4/05/18 2:07p Heller $
package de.mendelson.comm.as2.message.loggui;

import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadRequestFileLimited;
import de.mendelson.util.clientserver.clients.datatransfer.DownloadResponseFileLimited;
import de.mendelson.util.clientserver.clients.datatransfer.TransferClient;
import de.mendelson.util.xmleditorkit.XMLEditorKit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Panel to display the content of a file
 *
 * @author S.Heller
 * @version $Revision: 20 $
 */
public class JPanelFileDisplay extends JPanel {

    public static final int EDITOR_TYPE_XML = 1;
    public static final int EDITOR_TYPE_RAW = 0;

    /**
     * Max filesize for the display of data in the panel, actual 350kB
     */
    private final static long MAX_FILESIZE = (long) (500 * Math.pow(2, 10));
    /**
     * Resourcebundle to localize the GUI
     */
    private MecResourceBundle rb = null;
    private BaseClient baseClient;

    /**
     * Creates new form JPanelFunctionGraph
     */
    public JPanelFileDisplay(BaseClient baseClient) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleFileDisplay.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.baseClient = baseClient;
        this.initComponents();
        //this is just displayed if it is an image
        this.jPanelImage.setVisible(false);
        this.jEditorPaneXML.setEditorKit(new XMLEditorKit());
        this.jSplitPaneTextAndXML.setVisible( false );
    }

    /**
     * Loads a file to the editor and displays it
     */
    public void displayFile(String filename) {
        this.jLabelImage.setIcon(null);
        this.jPanelImage.setVisible(false);
        this.jScrollPaneTextEditor.setVisible(true);
        if (filename == null) {
            this.jTextFieldFilename.setText("");
            this.jEditorPaneRawText.setText(this.rb.getResourceString("no.file"));            
            return;
        }
        TransferClient transferClient = new TransferClient(this.baseClient);
        InputStream inStream = null;
        try {
            DownloadRequestFileLimited request = new DownloadRequestFileLimited();
            request.setMaxSize(MAX_FILESIZE);
            request.setFilename(filename);
            DownloadResponseFileLimited response = (DownloadResponseFileLimited) transferClient.download(request);
            this.jTextFieldFilename.setText(response.getFullFilename());
            if (response.isSizeExceeded()) {
                this.jEditorPaneRawText.setText(this.rb.getResourceString("file.tolarge",
                        new Object[]{filename}));
            } else {
                inStream = response.getDataStream();
                ByteArrayOutputStream memOut = new ByteArrayOutputStream();
                this.copyStreams(inStream, memOut);
                memOut.close();
                byte[] data = memOut.toByteArray();
                if (this.isImage(new ByteArrayInputStream(data))) {
                    ImageIcon icon = new ImageIcon(ImageIO.read(new ByteArrayInputStream(data)));
                    this.jLabelImage.setIcon(icon);
                    this.getToolkit().sync();
                    this.jScrollPaneTextEditor.setVisible(false);
                    this.jPanelImage.setVisible(true);
                } else {
                    inStream = new ByteArrayInputStream(data);
                    this.jEditorPaneRawText.read(inStream, null);
                    try{
                        this.jEditorPaneXML.read(new ByteArrayInputStream(data), data);
                        //the XML data is parsable and could be displayed: move the raw text editor to the split pane
                        this.jScrollPaneTextEditor.getParent().remove(this.jScrollPaneTextEditor);
                        this.jSplitPaneTextAndXML.setTopComponent(this.jScrollPaneTextEditor);
                        this.jSplitPaneTextAndXML.setVisible(true);
                    }
                    catch( Throwable e ){
                        //its no parsable XML data: no action required
                    }
                }
            }
        } catch (Throwable e) {
            if (e instanceof FileNotFoundException) {
                this.jEditorPaneRawText.setText(this.rb.getResourceString("file.notfound",
                        filename));
            } else {
                this.jEditorPaneRawText.setText(e.getMessage());
            }
            return;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    //nop
                }
            }
        }
    }

    /**
     * Checks if the passed stream is an image
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    private boolean isImage(InputStream inStream) throws Exception {
        if (ImageIO.read(inStream) == null) {
            return (false);
        } else {
            return (true);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelImage = new javax.swing.JPanel();
        jScrollPaneImage = new javax.swing.JScrollPane();
        jLabelImage = new javax.swing.JLabel();
        jScrollPaneTextEditor = new javax.swing.JScrollPane();
        jEditorPaneRawText = new javax.swing.JEditorPane();
        jSplitPaneTextAndXML = new javax.swing.JSplitPane();
        jPanelXMLStructure = new javax.swing.JPanel();
        jScrollPaneXML = new javax.swing.JScrollPane();
        jEditorPaneXML = new javax.swing.JEditorPane();
        jTextFieldFilename = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jPanelImage.setLayout(new java.awt.GridBagLayout());

        jScrollPaneImage.setViewportView(jLabelImage);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelImage.add(jScrollPaneImage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelImage, gridBagConstraints);

        jEditorPaneRawText.setEditable(false);
        jScrollPaneTextEditor.setViewportView(jEditorPaneRawText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPaneTextEditor, gridBagConstraints);

        jSplitPaneTextAndXML.setDividerLocation(200);
        jSplitPaneTextAndXML.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanelXMLStructure.setLayout(new java.awt.GridBagLayout());

        jScrollPaneXML.setViewportView(jEditorPaneXML);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelXMLStructure.add(jScrollPaneXML, gridBagConstraints);

        jSplitPaneTextAndXML.setBottomComponent(jPanelXMLStructure);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPaneTextAndXML, gridBagConstraints);

        jTextFieldFilename.setEditable(false);
        jTextFieldFilename.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldFilename, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPaneRawText;
    private javax.swing.JEditorPane jEditorPaneXML;
    private javax.swing.JLabel jLabelImage;
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelXMLStructure;
    private javax.swing.JScrollPane jScrollPaneImage;
    private javax.swing.JScrollPane jScrollPaneTextEditor;
    private javax.swing.JScrollPane jScrollPaneXML;
    private javax.swing.JSplitPane jSplitPaneTextAndXML;
    private javax.swing.JTextField jTextFieldFilename;
    // End of variables declaration//GEN-END:variables
}
