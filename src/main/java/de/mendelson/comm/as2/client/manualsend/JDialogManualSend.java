//$Header: /mec_as2/de/mendelson/comm/as2/client/manualsend/JDialogManualSend.java 24    8.01.19 11:58 Heller $
package de.mendelson.comm.as2.client.manualsend;

import de.mendelson.comm.as2.client.AS2StatusBar;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.clientserver.PartnerListRequest;
import de.mendelson.comm.as2.partner.clientserver.PartnerListResponse;
import de.mendelson.comm.as2.partner.gui.ListCellRendererPartner;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.util.LockingGlassPane;
import de.mendelson.util.MecFileChooser;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.datatransfer.TransferClientWithProgress;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreStorage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to send a file to a single partner
 *
 * @author S.Heller
 * @version $Revision: 24 $
 */
public class JDialogManualSend extends JDialog {

    private final static boolean MULTIPLE_FILES = false;

    /**
     * ResourceBundle to localize the GUI
     */
    private MecResourceBundle rb = null;
    private Logger logger = Logger.getLogger("de.mendelson.as2.client");
    private List<Partner> localStations = null;
    private CertificateManager certificateManagerEncSign = null;
    //DB connection for the partner access
    private BaseClient baseClient;
    private AS2StatusBar statusbar;
    /**
     * String that is displayed while the client uploads data to the server to
     * send
     */
    private String uploadDisplay;

    /**
     * Creates new form JDialogPartnerConfig
     *
     * @param uploadDisplay String that is displayed while the client uploads
     * data to the server to send
     */
    public JDialogManualSend(JFrame parent, BaseClient baseClient,
            AS2StatusBar statusbar, String uploadDisplay,
            CertificateManager certificateManagerEncSign) {
        super(parent, true);
        this.statusbar = statusbar;
        this.uploadDisplay = uploadDisplay;
        this.certificateManagerEncSign = certificateManagerEncSign;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleManualSend.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.baseClient = baseClient;
        this.setTitle(this.rb.getResourceString("title"));
        initComponents();
        this.jButtonBrowse2.setVisible(MULTIPLE_FILES);
        this.jTextFieldFilename2.setVisible(MULTIPLE_FILES);
        this.jLabelFilename2.setVisible(MULTIPLE_FILES);

        this.getRootPane().setDefaultButton(this.jButtonOk);
        //fill in data
        try {
            PartnerListResponse response = (PartnerListResponse) baseClient.sendSync(new PartnerListRequest(PartnerListRequest.LIST_NON_LOCALSTATIONS));
            List<Partner> nonlocalStations = response.getList();
            for (Partner partner : nonlocalStations) {
                this.jComboBoxPartner.addItem(partner);
            }
            response = (PartnerListResponse) baseClient.sendSync(new PartnerListRequest(PartnerListRequest.LIST_LOCALSTATION));
            this.localStations = response.getList();            
        } catch (Exception e) {
            this.logger.severe("JDialogManualSend: " + e.getMessage());
        }
        //single local station? No need to select the sender
        if (this.localStations.size() == 1) {
            this.jLabelSender.setVisible(false);
            this.jComboBoxSender.setVisible(false);
        } else {
            //add all local stations to the selection box
            this.jComboBoxSender.removeAllItems();
            for (Partner localStation : this.localStations) {
                this.jComboBoxSender.addItem(localStation);
            }
            this.jComboBoxSender.setSelectedItem(0);
        }
        this.jComboBoxPartner.setRenderer(new ListCellRendererPartner());
        this.jComboBoxSender.setRenderer(new ListCellRendererPartner());
        this.setButtonState();
    }

    /**
     * Lock the component: Add a glasspane that prevents any action on the UI
     */
    private void lock() {
        //init glasspane for first use
        if (!(this.getGlassPane() instanceof LockingGlassPane)) {
            this.setGlassPane(new LockingGlassPane());
        }
        this.getGlassPane().setVisible(true);
        this.getGlassPane().requestFocusInWindow();
    }

    /**
     * Unlock the component: remove the glasspane that prevents any action on
     * the UI
     */
    private void unlock() {
        getGlassPane().setVisible(false);
    }

    /**
     * Fills in some preselections for the file send dialog
     */
    public void initialize(Partner sender, Partner receiver, String filename) {
        this.jComboBoxPartner.setSelectedItem(receiver);
        this.jComboBoxSender.setSelectedItem(sender);
        this.jTextFieldFilename1.setText(filename);
        this.setButtonState();
    }

    /**
     * Sets the ok and cancel buttons of this GUI
     */
    private void setButtonState() {
        this.jButtonOk.setEnabled(
                this.jTextFieldFilename1.getText().length() > 0);
    }

    public ManualSendResponse performSend(String resendMessageId) throws Throwable {
        Partner receiver = (Partner) this.jComboBoxPartner.getSelectedItem();
        Partner sender = null;
        if (this.localStations.size() == 1) {
            sender = this.localStations.get(0);
        } else {
            sender = (Partner) this.jComboBoxSender.getSelectedItem();
        }
        ManualSendRequest request = new ManualSendRequest();
        request.setResendMessageId(resendMessageId);
        request.setReceiver(receiver);
        request.setSender(sender);
        ManualSendResponse response = null;
        List<String> uploadHashs = new ArrayList<String>();
        List<Path> files = new ArrayList<Path>();
        files.add(Paths.get(this.jTextFieldFilename1.getText()));
        if (this.jTextFieldFilename2.isVisible() && this.jTextFieldFilename2.getText().trim().length() > 0) {
            files.add(Paths.get(this.jTextFieldFilename2.getText()));
        }
        for (Path uploadFile : files) {
            InputStream inStream = null;
            try {
                //perform the upload to the server, chunked
                TransferClientWithProgress transferClient = new TransferClientWithProgress(
                        this.baseClient,
                        this.statusbar.getProgressPanel());
                inStream = Files.newInputStream(uploadFile);
                String uploadHash = transferClient.uploadChunkedWithProgress(inStream, this.uploadDisplay,
                        (int) Files.size(uploadFile));
                uploadHashs.add(uploadHash);
                request.addFilename(uploadFile.getFileName().toString());
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }
        }
        request.setUploadHashs(uploadHashs);
        response = (ManualSendResponse) baseClient.sendSyncWaitInfinite(request);
        if (response.getException() != null) {
            throw (response.getException());
        }
        return (response);
    }

    /**
     * Allows to resend an existing transaction by keeping the original filename
     *
     * @param resendMessageId
     * @param sender
     * @param receiver
     * @param dataFile
     * @param originalFilename
     * @return
     * @throws Throwable
     */
    public ManualSendResponse performResend(String resendMessageId, Partner sender, Partner receiver,
            Path dataFile, String originalFilename) throws Throwable {
        InputStream inStream = null;
        ManualSendResponse response = null;
        try {
            if( dataFile == null ){
                throw new FileNotFoundException();
            }
            TransferClientWithProgress transferClient = new TransferClientWithProgress(
                    this.baseClient,
                    this.statusbar.getProgressPanel());
            inStream = Files.newInputStream(dataFile);
            //perform the upload to the server, chunked
            String uploadHash = transferClient.uploadChunkedWithProgress(inStream, this.uploadDisplay,
                    (int) Files.size(dataFile));
            ManualSendRequest request = new ManualSendRequest();
            request.setResendMessageId(resendMessageId);
            request.setUploadHash(uploadHash);
            request.addFilename(originalFilename);
            request.setReceiver(receiver);
            request.setSender(sender);
            response = (ManualSendResponse) transferClient.uploadWaitInfinite(request);
            if (response.getException() != null) {
                throw (response.getException());
            }
        } finally {
            if (inStream != null) {
                inStream.close();

            }
        }
        return (response);
    }

    private void okButtonPressed() {
        this.jButtonOk.setEnabled(false);
        this.jButtonCancel.setEnabled(false);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JDialogManualSend.this.lock();
                try {
                    //perform send has an own progress bar, no need to set one here. This is never a resend of an existing
                    //transaction, set the resendMessageId to null
                    JDialogManualSend.this.performSend(null);
                    //display success dialog
                    JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, JDialogManualSend.this);
                    JDialogManualSend.this.unlock();
                    JDialogManualSend.this.setVisible(false);
                    JOptionPane.showMessageDialog(parent,
                            JDialogManualSend.this.rb.getResourceString("send.success"));
                } catch (Throwable e) {
                    JDialogManualSend.this.logger.warning("Manual send: " + e.getMessage());
                } finally {
                    JDialogManualSend.this.unlock();
                    JDialogManualSend.this.setVisible(false);
                    JDialogManualSend.this.dispose();
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelEdit = new javax.swing.JPanel();
        jLabelIcon = new javax.swing.JLabel();
        jLabelFilename1 = new javax.swing.JLabel();
        jTextFieldFilename1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabelPartner = new javax.swing.JLabel();
        jComboBoxPartner = new javax.swing.JComboBox();
        jButtonBrowse1 = new javax.swing.JButton();
        jComboBoxSender = new javax.swing.JComboBox();
        jLabelSender = new javax.swing.JLabel();
        jLabelFilename2 = new javax.swing.JLabel();
        jTextFieldFilename2 = new javax.swing.JTextField();
        jButtonBrowse2 = new javax.swing.JButton();
        jPanelSpace = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelEdit.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelEdit.setLayout(new java.awt.GridBagLayout());

        jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/manualsend/send_32x32.gif"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelEdit.add(jLabelIcon, gridBagConstraints);

        jLabelFilename1.setText(this.rb.getResourceString( "label.filename"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelFilename1, gridBagConstraints);

        jTextFieldFilename1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFilename1KeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jTextFieldFilename1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanelEdit.add(jPanel3, gridBagConstraints);

        jLabelPartner.setText(this.rb.getResourceString( "label.partner"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelPartner, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jComboBoxPartner, gridBagConstraints);

        jButtonBrowse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/manualsend/folder.gif"))); // NOI18N
        jButtonBrowse1.setToolTipText(this.rb.getResourceString( "button.browse"));
        jButtonBrowse1.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonBrowse1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowse1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEdit.add(jButtonBrowse1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jComboBoxSender, gridBagConstraints);

        jLabelSender.setText(this.rb.getResourceString( "label.localstation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelSender, gridBagConstraints);

        jLabelFilename2.setText(this.rb.getResourceString( "label.filename"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jLabelFilename2, gridBagConstraints);

        jTextFieldFilename2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldFilename2KeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEdit.add(jTextFieldFilename2, gridBagConstraints);

        jButtonBrowse2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/client/manualsend/folder.gif"))); // NOI18N
        jButtonBrowse2.setToolTipText(this.rb.getResourceString( "button.browse"));
        jButtonBrowse2.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonBrowse2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowse2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        jPanelEdit.add(jButtonBrowse2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelEdit.add(jPanelSpace, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanelEdit, gridBagConstraints);

        jPanelButtons.setLayout(new java.awt.GridBagLayout());

        jButtonOk.setText(this.rb.getResourceString( "button.ok" ));
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButtons.add(jButtonOk, gridBagConstraints);

        jButtonCancel.setText(this.rb.getResourceString( "button.cancel" ));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelButtons.add(jButtonCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelButtons, gridBagConstraints);

        setSize(new java.awt.Dimension(460, 284));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonBrowse1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowse1ActionPerformed
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        MecFileChooser chooser = new MecFileChooser(parent,
                this.rb.getResourceString("label.selectfile"));
        chooser.browseFilename(this.jTextFieldFilename1);
        this.setButtonState();
    }//GEN-LAST:event_jButtonBrowse1ActionPerformed

    private void jTextFieldFilename1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFilename1KeyReleased
        this.setButtonState();
    }//GEN-LAST:event_jTextFieldFilename1KeyReleased

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        this.okButtonPressed();
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jTextFieldFilename2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFilename2KeyReleased
        this.setButtonState();
    }//GEN-LAST:event_jTextFieldFilename2KeyReleased

    private void jButtonBrowse2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowse2ActionPerformed
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        MecFileChooser chooser = new MecFileChooser(parent,
                this.rb.getResourceString("label.selectfile"));
        chooser.browseFilename(this.jTextFieldFilename2);
        this.setButtonState();
    }//GEN-LAST:event_jButtonBrowse2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse1;
    private javax.swing.JButton jButtonBrowse2;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox jComboBoxPartner;
    private javax.swing.JComboBox jComboBoxSender;
    private javax.swing.JLabel jLabelFilename1;
    private javax.swing.JLabel jLabelFilename2;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JLabel jLabelPartner;
    private javax.swing.JLabel jLabelSender;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelEdit;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JTextField jTextFieldFilename1;
    private javax.swing.JTextField jTextFieldFilename2;
    // End of variables declaration//GEN-END:variables
}
