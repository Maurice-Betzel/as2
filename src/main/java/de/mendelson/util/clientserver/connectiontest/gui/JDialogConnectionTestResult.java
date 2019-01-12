package de.mendelson.util.clientserver.connectiontest.gui;

import de.mendelson.util.clientserver.connectiontest.ConnectionTestResult;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.connectiontest.ConnectionTest;
import de.mendelson.util.log.JTextPaneLoggingHandler;
import de.mendelson.util.log.LogFormatter;
import de.mendelson.util.log.LoggingHandlerLogEntryArray;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_BLACK;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_BLUE;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_BROWN;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_DARK_GREEN;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_LIGHT_GRAY;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_OLIVE;
import static de.mendelson.util.log.panel.LogConsolePanel.COLOR_RED;
import de.mendelson.util.security.KeyStoreUtil;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.gui.JDialogInfoOnExternalCertificate;
import de.mendelson.util.security.cert.gui.ResourceBundleCertificates;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to display the test result of a connection test
 *
 * @author S.Heller
 * @version $Revision: 14 $
 */
public class JDialogConnectionTestResult extends JDialog {

    public static final int CONNECTION_TEST_OFTP2 = ConnectionTest.CONNECTION_TEST_OFTP2;
    public static final int CONNECTION_TEST_AS2 = ConnectionTest.CONNECTION_TEST_AS2;

    private ConnectionTestResult result;
    private MecResourceBundle rb = null;
    private CertificateManager certManagerSSL;
    private MecResourceBundle rbCerts;

    /**
     * Creates new form JDialogTestResult
     */
    public JDialogConnectionTestResult(JFrame parent,
            final int CONNECTION_TYPE_TEST,
            List<LoggingHandlerLogEntryArray.LogEntry> logEntries,
            ConnectionTestResult result,
            CertificateManager certManagerEncSign,
            CertificateManager certManagerSSL) {
        super(parent, true);
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleDialogConnectionTestResult.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        try {
            this.rbCerts = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCertificates.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        initComponents();
        this.jLabelOFTPService.setVisible(false);
        this.jLabelOFTPServiceState.setVisible(false);
        if (CONNECTION_TYPE_TEST == CONNECTION_TEST_OFTP2) {
            this.jLabelOFTPService.setVisible(true);
            this.jLabelOFTPServiceState.setVisible(true);
        }
        this.certManagerSSL = certManagerSSL;
        this.result = result;
        this.setTitle(this.rb.getResourceString("title", result.getTestedRemoteAddress()));
        if (result.wasSSLTest()) {
            this.jLabelHeader.setText(this.rb.getResourceString("state.ssl"));
        } else {
            this.jLabelHeader.setText(this.rb.getResourceString("state.plain"));
        }
        if (result.isConnectionIsPossible()) {
            this.jLabelConnectionState.setForeground(Color.green.darker().darker());
            this.jLabelConnectionState.setText(this.rb.getResourceString("OK"));
        } else {
            this.jLabelConnectionState.setForeground(Color.red.darker());
            this.jLabelConnectionState.setText(this.rb.getResourceString("FAILED"));
        }
        if (!result.wasSSLTest()) {
            this.jButtonViewCertificates.setEnabled(false);
            this.jLabelCertificate.setEnabled(false);
            this.jLabelCertificateState.setEnabled(false);
            this.jLabelCertificateState.setText(this.rb.getResourceString("no.certificate.plain"));
        } else {
            if (result.getFoundCertificates() != null && result.getFoundCertificates().length > 0) {
                this.jLabelCertificateState.setForeground(Color.green.darker().darker());
                this.jLabelCertificateState.setText(this.rb.getResourceString("OK"));
            } else {
                this.jLabelCertificateState.setForeground(Color.red.darker());
                this.jLabelCertificateState.setText(this.rb.getResourceString("FAILED"));
                this.jButtonViewCertificates.setEnabled(false);
            }
        }
        if (result.isOftpServiceFound()) {
            this.jLabelOFTPServiceState.setForeground(Color.green.darker().darker());
            this.jLabelOFTPServiceState.setText(this.rb.getResourceString("OK"));
        } else {
            this.jLabelOFTPServiceState.setForeground(Color.red.darker());
            this.jLabelOFTPServiceState.setText(this.rb.getResourceString("FAILED"));
        }
        this.jLabelDescription.setText(this.rb.getResourceString("description." + CONNECTION_TYPE_TEST,
                new Object[]{result.getTestedRemoteAddress().getHostString(),
                    String.valueOf(result.getTestedRemoteAddress().getPort())}));
        //display the log if there is any
        Logger testLogger = Logger.getAnonymousLogger();
        testLogger.setUseParentHandlers(false);
        JTextPaneLoggingHandler handler = new JTextPaneLoggingHandler(this.jTextPaneLog,
                new LogFormatter(LogFormatter.FORMAT_CONSOLE));
        this.setDefaultLogColors(handler);
        testLogger.setLevel(Level.ALL);
        testLogger.addHandler(handler);
        for (LoggingHandlerLogEntryArray.LogEntry logEntry : logEntries) {
            testLogger.log(logEntry.getLevel(), logEntry.getMessage());
        }
        //log some technical information - the ciphers
        if (result.wasSSLTest()) {
            String cipher = result.getUsedCipherSuite();
            //SSL_NULL_WITH_NULL_NULL is the inital cipher - if it is still the selected then a successful handshake did not happen
            if (cipher != null && !cipher.equals("SSL_NULL_WITH_NULL_NULL")) {
                testLogger.log(Level.FINEST, this.rb.getResourceString("used.cipher", cipher));
            }
        }
        //hide dialog on esc
        ActionListener actionListenerESC = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonClose.doClick();
            }
        };
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        this.getRootPane().registerKeyboardAction(actionListenerESC, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Sets some default colors for the logger levels. Overwrite these colors
     * using the setColor method
     *
     */
    public void setDefaultLogColors(JTextPaneLoggingHandler handler) {
        handler.setColor(Level.SEVERE, COLOR_RED);
        handler.setColor(Level.WARNING, COLOR_BROWN);
        handler.setColor(Level.INFO, COLOR_BLACK);
        handler.setColor(Level.CONFIG, COLOR_DARK_GREEN);
        handler.setColor(Level.FINE, COLOR_BLUE);
        handler.setColor(Level.FINER, COLOR_OLIVE);
        handler.setColor(Level.FINEST, COLOR_LIGHT_GRAY);
    }

    private void viewCertificates() {
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.addAll(Arrays.asList(this.result.getFoundCertificates()));
        JDialogInfoOnExternalCertificate infoDialog = new JDialogInfoOnExternalCertificate(parent, certList, this.certManagerSSL);
        infoDialog.setVisible(true);
        //user pressed cancel: bail out
        while (infoDialog.importPressed()) {
            int selectedCertificateIndex = infoDialog.getCertificateIndex();
            KeyStoreUtil util = new KeyStoreUtil();
            Provider provBC = new BouncyCastleProvider();
            X509Certificate importCertificate = certList.get(selectedCertificateIndex);
            try {
                String alias = util.importX509Certificate(this.certManagerSSL.getKeystore(), importCertificate, provBC);
                this.certManagerSSL.saveKeystore();
                this.certManagerSSL.rereadKeystoreCertificates();
                JOptionPane.showMessageDialog(this,
                        this.rbCerts.getResourceString("certificate.import.success.message", alias),
                        this.rbCerts.getResourceString("certificate.import.success.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Throwable e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        this.rbCerts.getResourceString("certificate.import.error.message", e.getMessage()),
                        this.rbCerts.getResourceString("certificate.import.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
            if (certList.size() > 1) {
                infoDialog.setVisible(true);
            } else {
                break;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane = new javax.swing.JSplitPane();
        jPanelOverview = new javax.swing.JPanel();
        jLabelHeader = new javax.swing.JLabel();
        jLabelConnection = new javax.swing.JLabel();
        jLabelCertificate = new javax.swing.JLabel();
        jLabelOFTPService = new javax.swing.JLabel();
        jLabelConnectionState = new javax.swing.JLabel();
        jLabelCertificateState = new javax.swing.JLabel();
        jLabelOFTPServiceState = new javax.swing.JLabel();
        jLabelDescription = new javax.swing.JLabel();
        jPanelSpace = new javax.swing.JPanel();
        jPanelLog = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        jTextPaneLog = new javax.swing.JTextPane();
        jPanelButton = new javax.swing.JPanel();
        jButtonViewCertificates = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(220);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanelOverview.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelOverview.setLayout(new java.awt.GridBagLayout());

        jLabelHeader.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelHeader.setText("<STATE>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 15, 5);
        jPanelOverview.add(jLabelHeader, gridBagConstraints);

        jLabelConnection.setText(this.rb.getResourceString("label.connection.established"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanelOverview.add(jLabelConnection, gridBagConstraints);

        jLabelCertificate.setText(this.rb.getResourceString("label.certificates.downloaded"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanelOverview.add(jLabelCertificate, gridBagConstraints);

        jLabelOFTPService.setText(this.rb.getResourceString("label.running.oftpservice"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 15, 5);
        jPanelOverview.add(jLabelOFTPService, gridBagConstraints);

        jLabelConnectionState.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelConnectionState.setText("<STATE>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanelOverview.add(jLabelConnectionState, gridBagConstraints);

        jLabelCertificateState.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelCertificateState.setText("<STATE>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanelOverview.add(jLabelCertificateState, gridBagConstraints);

        jLabelOFTPServiceState.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelOFTPServiceState.setText("<STATE>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 15, 5);
        jPanelOverview.add(jLabelOFTPServiceState, gridBagConstraints);

        jLabelDescription.setText("<HTML>The system performed a ip connection to the ip address ..., port ... The following result shows if this connection was successful and if a ..product type... server listens to this address. If a TLS connection has been requested and was successful it is possible to download the certificate(s), they will be stored in your TLS keystore.</HTML>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelOverview.add(jLabelDescription, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanelOverview.add(jPanelSpace, gridBagConstraints);

        jSplitPane.setLeftComponent(jPanelOverview);

        jPanelLog.setLayout(new java.awt.GridBagLayout());

        jTextPaneLog.setEditable(false);
        jScrollPane.setViewportView(jTextPaneLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLog.add(jScrollPane, gridBagConstraints);

        jSplitPane.setRightComponent(jPanelLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSplitPane, gridBagConstraints);

        jPanelButton.setLayout(new java.awt.GridBagLayout());

        jButtonViewCertificates.setText(this.rb.getResourceString( "button.viewcert"));
        jButtonViewCertificates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewCertificatesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelButton.add(jButtonViewCertificates, gridBagConstraints);

        jButtonClose.setText(this.rb.getResourceString( "button.close"));
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelButton.add(jButtonClose, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanelButton, gridBagConstraints);

        setSize(new java.awt.Dimension(929, 656));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jButtonViewCertificatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewCertificatesActionPerformed
        this.viewCertificates();
    }//GEN-LAST:event_jButtonViewCertificatesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonViewCertificates;
    private javax.swing.JLabel jLabelCertificate;
    private javax.swing.JLabel jLabelCertificateState;
    private javax.swing.JLabel jLabelConnection;
    private javax.swing.JLabel jLabelConnectionState;
    private javax.swing.JLabel jLabelDescription;
    private javax.swing.JLabel jLabelHeader;
    private javax.swing.JLabel jLabelOFTPService;
    private javax.swing.JLabel jLabelOFTPServiceState;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelLog;
    private javax.swing.JPanel jPanelOverview;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTextPane jTextPaneLog;
    // End of variables declaration//GEN-END:variables
}
