//$Header: /as2/de/mendelson/util/security/cert/gui/JPanelCertificates.java 33    7/09/18 1:02p Heller $
package de.mendelson.util.security.cert.gui;

import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.AllowModificationCallback;
import de.mendelson.util.security.cert.CertificateInUseChecker;
import de.mendelson.util.security.cert.CertificateInUseInfo;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import de.mendelson.util.security.cert.TableModelCertificates;
import de.mendelson.util.tables.JTableColumnResizer;
import de.mendelson.util.tables.TableCellRendererDate;
import java.awt.Color;
import java.security.cert.CertPath;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Panel to configure the Certificates
 *
 * @author S.Heller
 * @version $Revision: 33 $
 */
public class JPanelCertificates extends JPanel implements ListSelectionListener, PopupMenuListener {

    private Logger logger = null;
    /**
     * Title used to render subdialogs
     */
    private String title = null;
    private JButton editButton = null;
    private JButton deleteButton = null;
    private JMenuItem itemEdit = null;
    private JMenuItem itemDelete = null;
    private CertificateManager manager = null;
    private String keystoreType;
    private MecResourceBundle rb = null;
    public static final ImageIcon ICON_CERTIFICATE_ROOT = TableModelCertificates.ICON_CERTIFICATE_ROOT;
    public static final ImageIcon ICON_CERTIFICATE_UNTRUSTED = new ImageIcon(JPanelCertificates.class.getResource(
            "/de/mendelson/util/security/cert/gui/cert_untrusted16x16.gif"));
    private final List<CertificateInUseChecker> inUseChecker = Collections.synchronizedList(new ArrayList<CertificateInUseChecker>());
    private List<AllowModificationCallback> allowModificationCallbackList = new ArrayList<AllowModificationCallback>();
    /**
     * Allows to set an external label to display the trust anchor information in
     */
    private JLabel jLabelTrustAnchorValueAlternate = null;
    private JLabel jLabelWarnings = null;

    /**
     * Creates new form JPanelPartnerConfig
     */
    public JPanelCertificates(Logger logger, ListSelectionListener additionalListener) {
        this.logger = logger;
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleCertificates.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        initComponents();
        //add row sorter
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(this.jTable.getModel());
        this.jTable.setRowSorter(sorter);
        this.jTable.getTableHeader().setReorderingAllowed(false);
        this.jTable.getColumnModel().getColumn(0).setMaxWidth(20);
        this.jTable.getColumnModel().getColumn(1).setMaxWidth(20);
        this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jTable.getSelectionModel().addListSelectionListener(additionalListener);
        this.jTable.getSelectionModel().addListSelectionListener(this);
        this.jTable.setDefaultRenderer(Date.class, new TableCellRendererDate(DateFormat.getDateInstance(DateFormat.SHORT)));
        this.jPopupMenu.setInvoker(this.jScrollPaneTable);
        this.jPopupMenu.addPopupMenuListener(this);
    }

    /**
     * Allows to setup an external alternate label where the trust label
     * information is displayed in. Calling this will invisible the local label
     *
     * @param jLabelTrustAnchorValueAlternate
     */
    public void setExternalDisplayComponents(JLabel jLabelTrustAnchorValueAlternate, JLabel jLabelWarnings) {
        this.jLabelTrustAnchor.setVisible(false);
        this.jLabelTrustAnchorValue.setVisible(false);
        this.jLabelTrustAnchorValueAlternate = jLabelTrustAnchorValueAlternate;
        this.jLabelWarnings = jLabelWarnings;
    }

    /**
     * Adds a callback that is called if a user tries to modify the
     * configuration A modification will be prevented if one of the callbacks
     * does not allow it
     */
    public void addAllowModificationCallback(AllowModificationCallback callback) {
        this.allowModificationCallbackList.add(callback);
    }

    /**
     * Checks if the operation is possible because the keystore is R/O and
     * displayes a message if not It's also possible to set the module into a
     * mode where modifications are not allowed - this will be displayed, too
     */
    private boolean isOperationAllowed(boolean silent) {
        for (AllowModificationCallback callback : this.allowModificationCallbackList) {
            boolean modificationAllowed = callback.allowModification(silent);
            if (!modificationAllowed) {
                return (false);
            }
        }
        boolean readWrite = true;
        readWrite = readWrite && this.manager.canWrite();
        if (!readWrite) {
            JOptionPane.showMessageDialog(this,
                    this.rb.getResourceString("keystore.readonly.message"),
                    this.rb.getResourceString("keystore.readonly.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        return (readWrite);
    }

    public void addKeystore(CertificateManager manager) {
        this.manager = manager;
        this.keystoreType = manager.getKeystoreType();
        this.refreshData();
        JTableColumnResizer.adjustColumnWidthByContent(this.jTable);
        if (this.jTable.getRowCount() > 0) {
            this.jTable.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    private boolean keystoreIsReadonly() {
        return (!this.manager.canWrite());
    }

    public void addCertificateInUseChecker(CertificateInUseChecker checker) {
        synchronized (this.inUseChecker) {
            this.inUseChecker.add(checker);
        }
    }

    public void setSelectionByAlias(String selectedAlias) {
        if (selectedAlias != null) {
            for (int i = 0; i < ((TableModelCertificates) this.jTable.getModel()).getRowCount(); i++) {
                KeystoreCertificate certificate = ((TableModelCertificates) this.jTable.getModel()).getParameter(i);
                if (certificate.getAlias().equals(selectedAlias)) {
                    this.jTable.getSelectionModel().setSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    /**
     * Returns a single certificate of a row of the embedded table
     */
    public KeystoreCertificate getSelectedCertificate() {
        int row = this.jTable.getSelectedRow();
        if (row < 0) {
            return (null);
        }
        return (((TableModelCertificates) this.jTable.getModel()).getParameter(row));
    }

    /**
     * Returns the actual selected row
     */
    public int getSelectedRow() {
        return (this.jTable.getSelectedRow());
    }

    public void refreshData() {
        //try to keep the mark
        int selectedRow = this.jTable.getSelectedRow();
        String selectedAlias = null;
        if (selectedRow >= 0) {
            selectedAlias = (((TableModelCertificates) this.jTable.getModel()).getParameter(selectedRow)).getAlias();
        }
        if (this.jCheckBoxShowCACertificates.isSelected()) {
            //show all certificates
            ((TableModelCertificates) this.jTable.getModel()).setNewData(this.manager.getKeyStoreCertificateList());
        } else {
            List<KeystoreCertificate> keystoreCertList = new Vector<KeystoreCertificate>();
            List<KeystoreCertificate> keystoreCertListAll = this.manager.getKeyStoreCertificateList();
            //do not show the CA certificates
            for (KeystoreCertificate cert : keystoreCertListAll) {
                if (!cert.isCACertificate()) {
                    keystoreCertList.add(cert);
                }
            }
            ((TableModelCertificates) this.jTable.getModel()).setNewData(keystoreCertList);
        }
        for (int i = 0, rowCount = this.jTable.getRowCount(); i < rowCount; i++) {
            KeystoreCertificate cert = ((TableModelCertificates) this.jTable.getModel()).getParameter(i);
            if (cert.getAlias().equals(selectedAlias)) {
                this.jTable.getSelectionModel().setSelectionInterval(i, i);
                break;
            }
        }
    }

    /**
     * Lets this gui refresh the table
     */
    public void certificateDeleted(int lastRow) {
        //last row? dec
        if (lastRow > this.jTable.getRowCount() - 1 && lastRow != 0) {
            lastRow--;
        }
        if (this.jTable.getRowCount() > 0) {
            this.jTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
        }
    }

    /**
     * Lets this gui refresh the table
     */
    public void certificateAdded(String newAlias) {
        if (newAlias == null) {
            return;
        }
        for (int i = 0, rowCount = this.jTable.getRowCount(); i < rowCount; i++) {
            KeystoreCertificate cert = ((TableModelCertificates) this.jTable.getModel()).getParameter(i);
            if (cert.getAlias().equals(newAlias)) {
                this.jTable.getSelectionModel().setSelectionInterval(i, i);
                break;
            }
        }
    }

    /**
     * Lets this gui refresh the table
     */
    public void certificateRenamedTo(String newAlias) {
        if (newAlias == null) {
            return;
        }
        for (int i = 0, rowCount = this.jTable.getRowCount(); i < rowCount; i++) {
            KeystoreCertificate cert = ((TableModelCertificates) this.jTable.getModel()).getParameter(i);
            if (cert.getAlias().equals(newAlias)) {
                this.jTable.getSelectionModel().setSelectionInterval(i, i);
                break;
            }
        }
    }

    /**
     * Allows the GUI to control the passed buttons
     */
    public void setButtons(JButton editButton, JButton deleteButton) {
        this.editButton = editButton;
        this.deleteButton = deleteButton;
        this.setButtonState();
    }

    public void setMenuItems(JMenuItem itemEdit, JMenuItem itemDelete) {
        this.itemEdit = itemEdit;
        this.itemDelete = itemDelete;
        this.setButtonState();
    }

    /**
     * Control the state of the panels buttons
     */
    private void setButtonState() {
        if (this.deleteButton != null) {
            this.deleteButton.setEnabled(this.jTable.getSelectedRow() >= 0);
        }
        if (this.editButton != null) {
            this.editButton.setEnabled(this.jTable.getSelectedRow() >= 0);
        }
        if (this.itemEdit != null) {
            this.itemEdit.setEnabled(this.jTable.getSelectedRow() >= 0);
        }
        if (this.itemDelete != null) {
            this.itemDelete.setEnabled(this.jTable.getSelectedRow() >= 0);
        }
    }

    /**
     * Makes this a listSelection Listener
     */
    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow >= 0) {
            KeystoreCertificate certificate = ((TableModelCertificates) this.jTable.getModel()).getParameter(selectedRow);
            String infoText = certificate.getInfo();
            String extensionText = certificate.getInfoExtension();
            this.jTextAreaInfo.setText(infoText);
            this.jTextAreaInfoExtension.setText(extensionText);
            this.jTreeTrustChain.buildTree(this.computeTrustChain(certificate.getAlias()));
            this.displayTrustAnchor();
            this.displayWarnings(certificate);
        }
        this.setButtonState();
    }

    /**
     * Displays warnings if a related label is set
     */
    private void displayWarnings(KeystoreCertificate certificate) {
        Color colorWarning = new Color(204, 0, 0);
        Color colorOk = new Color(0, 153, 0);
        if (this.jLabelWarnings == null) {
            return;
        }
        this.jLabelWarnings.setText("");
        this.jLabelWarnings.setForeground(colorOk);
        boolean problem = false;
        for (String fingerprint : KeystoreCertificate.TEST_KEYS_FINGERPRINTS_SHA1) {
            if (fingerprint.equalsIgnoreCase(certificate.getFingerPrintSHA1())) {
                this.jLabelWarnings.setForeground(colorWarning);
                this.jLabelWarnings.setText(this.rb.getResourceString("warning.testkey"));
                problem = true;
                break;
            }
        }
        if (!problem) {
            try {
                certificate.getX509Certificate().checkValidity();
                if (certificate.getIsKeyPair()) {
                    this.jLabelWarnings.setText(this.rb.getResourceString( "label.key.valid"));
                } else {
                    this.jLabelWarnings.setText(this.rb.getResourceString( "label.cert.valid"));
                }
            } catch (Exception e) {
                //there is a problem...turn label color to red
                this.jLabelWarnings.setForeground(colorWarning);
                if (certificate.getIsKeyPair()) {
                    this.jLabelWarnings.setText(this.rb.getResourceString( "label.key.invalid"));
                } else {
                    this.jLabelWarnings.setText(this.rb.getResourceString( "label.cert.invalid"));
                }
            }
        }
    }

    /**
     * Compute the whole trust chain for pkcs#7 export
     */
    private List<KeystoreCertificate> computeTrustChain(String alias) {
        KeystoreCertificate certificate = this.manager.getKeystoreCertificate(alias);
        PKIXCertPathBuilderResult result = certificate.getPKIXCertPathBuilderResult(this.manager.getKeystore(), this.manager.getX509CertificateList());
        List<KeystoreCertificate> list = new ArrayList<KeystoreCertificate>();
        //self signed?
        if (result == null) {
            //it's a self signed certificate: return it without any CA/intermediate certs
            list.add(certificate);
        } else {
            //trusted cert
            CertPath path = result.getCertPath();
            for (Object cert : path.getCertificates()) {
                X509Certificate workingCert = (X509Certificate) cert;
                for (KeystoreCertificate availableKeystoreCert : this.manager.getKeyStoreCertificateList()) {
                    if (workingCert.equals(availableKeystoreCert.getX509Certificate())) {
                        list.add(0, availableKeystoreCert);
                    }
                }
            }
            X509Certificate anchorCertX509 = list.get(0).getX509Certificate();
            boolean trustChainComplete = false;
            while (!trustChainComplete) {
                KeystoreCertificate keyCertAnchor = null;
                //find out the keystore cert of the anchor
                for (KeystoreCertificate keyCert : this.manager.getKeyStoreCertificateList()) {
                    if (keyCert.getX509Certificate().equals(anchorCertX509)) {
                        keyCertAnchor = keyCert;
                        break;
                    }
                }
                if (keyCertAnchor != null) {
                    //check if the anchor has another anchor as intermediates certificate may have the attribute "CA:true", too
                    result = keyCertAnchor.getPKIXCertPathBuilderResult(this.manager.getKeystore(), this.manager.getX509CertificateList());
                    if (result != null) {
                        anchorCertX509 = result.getTrustAnchor().getTrustedCert();
                        if (!keyCertAnchor.getX509Certificate().equals(anchorCertX509)) {
                            for (KeystoreCertificate availableKeystoreCert : this.manager.getKeyStoreCertificateList()) {
                                if (anchorCertX509.equals(availableKeystoreCert.getX509Certificate())) {
                                    list.add(0, availableKeystoreCert);
                                }
                            }
                        } else {
                            trustChainComplete = true;
                        }
                    } else {
                        trustChainComplete = true;
                    }
                } else {
                    trustChainComplete = true;
                }
            }
            //if a certificate is imported two times into the keystore it will occure two or more times in a row in this list and
            //this will confuse the cert path display
            // - the following code will remove the certificates if they are two times in a row in the list
            KeystoreCertificate selectedCertificate = null;
            for (KeystoreCertificate cert : list) {
                if (cert.getAlias().equals(alias) && cert.getFingerPrintSHA1().equals(list.get(list.size() - 1).getFingerPrintSHA1())) {
                    selectedCertificate = cert;
                }
            }
            KeystoreCertificate lastCheckedCert = null;
            boolean repeatLoop = true;
            while (repeatLoop && list.size() > 1) {
                int deleteIndex = -1;
                for (int i = 0; i < list.size(); i++) {
                    KeystoreCertificate singleCert = list.get(i);
                    if (lastCheckedCert == null) {
                        lastCheckedCert = singleCert;
                    } else {
                        if (lastCheckedCert.getFingerPrintSHA1().equals(singleCert.getFingerPrintSHA1())) {
                            deleteIndex = i;
                            lastCheckedCert = null;
                            break;
                        }
                        lastCheckedCert = singleCert;
                    }
                }
                if (deleteIndex != -1) {
                    list.remove(deleteIndex);
                } else {
                    repeatLoop = false;
                }
            }
            //ensure that the slectedCertificate is always the last one in the path - it might be the same cert with an other
            //alias, too after this delete algorithm
            if (selectedCertificate != null) {
                list.remove(list.size() - 1);
                list.add(selectedCertificate);
            }
        }
        return (list);
    }

    public void displayTrustAnchor() {
        JLabel usedDisplayLabel = this.jLabelTrustAnchorValue;
        if (this.jLabelTrustAnchorValueAlternate != null) {
            usedDisplayLabel = this.jLabelTrustAnchorValueAlternate;
        }
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                KeystoreCertificate certificate = ((TableModelCertificates) this.jTable.getModel()).getParameter(selectedRow);
                if (certificate.isRootCertificate()) {
                    usedDisplayLabel.setIcon(ICON_CERTIFICATE_ROOT);
                    usedDisplayLabel.setText("Root certificate");
                } else if (certificate.isSelfSigned()) {
                    //figure out the icon used to render the cert entry in the table
                    usedDisplayLabel.setIcon(((TableModelCertificates) this.jTable.getModel()).getIconForCertificate(certificate));
                    usedDisplayLabel.setText("Self signed");
                } else {
                    PKIXCertPathBuilderResult result = certificate.getPKIXCertPathBuilderResult(this.manager.getKeystore(), this.manager.getX509CertificateList());
                    if (result == null) {
                        usedDisplayLabel.setIcon(ICON_CERTIFICATE_UNTRUSTED);
                        usedDisplayLabel.setText("Untrusted");
                    } else {
                        TrustAnchor anchor = result.getTrustAnchor();
                        if (anchor == null) {
                            usedDisplayLabel.setIcon(ICON_CERTIFICATE_UNTRUSTED);
                            usedDisplayLabel.setText("Untrusted");
                        } else {
                            List<KeystoreCertificate> trustPath = this.computeTrustChain(certificate.getAlias());
                            //found a root in the cert path
                            usedDisplayLabel.setIcon(ICON_CERTIFICATE_ROOT);
                            usedDisplayLabel.setText(trustPath.get(0).getAlias());
                        }
                    }
                }
            } catch (Exception e) {
                usedDisplayLabel.setIcon(null);
                usedDisplayLabel.setText("--");
            }
        } else {
            usedDisplayLabel.setIcon(null);
            usedDisplayLabel.setText("--");
        }
    }

    public void performDeleteParameter() {
        try {
            KeystoreCertificate certificate = ((TableModelCertificates) this.jTable.getModel()).getParameter(this.jTable.getSelectedRow());
            this.manager.deleteKeystoreEntry(certificate.getAlias());
            this.manager.saveKeystore();
            int selectedRow = this.jTable.getSelectedRow();
            this.refreshData();
            if (this.jTable.getRowCount() - 1 >= selectedRow) {
                this.jTable.getSelectionModel().setSelectionInterval(
                        selectedRow, selectedRow);
            } else if (this.jTable.getRowCount() > 0) {
                this.jTable.getSelectionModel().setSelectionInterval(
                        this.jTable.getRowCount() - 1, this.jTable.getRowCount() - 1);
            }
        } catch (Throwable e) {
            this.logger.severe(e.getMessage());
        }
    }

    /**
     * Renames the selected alias
     */
    public void performEditParameter() {
        try {
            KeystoreCertificate certificate = ((TableModelCertificates) this.jTable.getModel()).getParameter(this.jTable.getSelectedRow());
            JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(
                    JFrame.class, this);
            JDialogRenameEntry dialog = new JDialogRenameEntry(parent, this.manager, certificate.getAlias(), this.keystoreType);
            dialog.setVisible(true);
            this.manager.saveKeystore();
            this.refreshData();
        } catch (Throwable e) {
            this.logger.severe(e.getMessage());
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void renameSelectedAlias() {
        KeystoreCertificate selectedCertificate = this.getSelectedCertificate();
        String oldAlias = selectedCertificate.getAlias();
        JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        JDialogRenameEntry dialog = new JDialogRenameEntry(parent, this.manager, oldAlias, this.keystoreType);
        dialog.setVisible(true);
        String newAlias = dialog.getNewAlias();
        dialog.dispose();
        this.refreshData();
        this.certificateRenamedTo(newAlias);
    }

    public void deleteSelectedCertificate() {
        KeystoreCertificate selectedCertificate = this.getSelectedCertificate();
        StringBuilder builder = new StringBuilder();
        synchronized (this.inUseChecker) {
            for (CertificateInUseChecker checker : this.inUseChecker) {
                for (CertificateInUseInfo singleInfo : checker.checkUsed(selectedCertificate)) {
                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    builder.append(singleInfo.getMessage());
                }
            }
        }
        if (builder.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    this.rb.getResourceString("cert.delete.impossible")
                    + "\n\n" + builder.toString(),
                    this.rb.getResourceString("title.cert.in.use"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        //ask the user if the cert should be really deleted, all data is lost
        int requestValue = JOptionPane.showConfirmDialog(
                this, this.rb.getResourceString("dialog.cert.delete.message", selectedCertificate.getAlias()),
                this.rb.getResourceString("dialog.cert.delete.title"),
                JOptionPane.YES_NO_OPTION);
        if (requestValue != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int selectedRow = this.getSelectedRow();
            this.manager.deleteKeystoreEntry(selectedCertificate.getAlias());
            this.manager.saveKeystore();
            this.refreshData();
            this.certificateDeleted(selectedRow);
        } catch (Throwable e) {
            this.logger.warning(e.getMessage());
        }
    }

    /**
     * Exports a selected certificate
     */
    public void exportSelectedCertificate() {
        KeystoreCertificate selectedCertificate = this.getSelectedCertificate();
        if (selectedCertificate != null) {
            JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
            JDialogExportCertificate dialog = new JDialogExportCertificate(parent, this.manager,
                    selectedCertificate.getAlias(), this.logger);
            dialog.setVisible(true);
        }
    }

    /**
     * Exports a key to a pkcs12 keystore
     */
    public void exportPKCS12Key() {
        KeystoreCertificate selectedCertificate = this.getSelectedCertificate();
        String preselectionAlias = selectedCertificate == null ? null : selectedCertificate.getAlias();
        try {
            JFrame parent = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
            JDialogExportKeyPKCS12 dialog = new JDialogExportKeyPKCS12(parent, this.logger, this.manager,
                    preselectionAlias);
            dialog.setVisible(true);
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemPopupExportKey = new javax.swing.JMenuItem();
        jMenuItemPopupExportCert = new javax.swing.JMenuItem();
        jMenuItemPopupRenameAlias = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemPopupDeleteEntry = new javax.swing.JMenuItem();
        jSplitPane = new javax.swing.JSplitPane();
        jScrollPaneTable = new javax.swing.JScrollPane();
        jTable = new de.mendelson.util.tables.JTableSortable();
        jTabbedPaneInfo = new javax.swing.JTabbedPane();
        jScrollPaneInfo = new javax.swing.JScrollPane();
        jTextAreaInfo = new javax.swing.JTextArea();
        jScrollPaneInfoExtension = new javax.swing.JScrollPane();
        jTextAreaInfoExtension = new javax.swing.JTextArea();
        jScrollPaneTrustchain = new javax.swing.JScrollPane();
        jTreeTrustChain = new de.mendelson.util.security.cert.gui.JTreeTrustChain();
        jCheckBoxShowCACertificates = new javax.swing.JCheckBox();
        jLabelTrustAnchor = new javax.swing.JLabel();
        jLabelTrustAnchorValue = new javax.swing.JLabel();

        jMenuItemPopupExportKey.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/key16x16.gif"))); // NOI18N
        jMenuItemPopupExportKey.setText(this.rb.getResourceString("label.key.export.pkcs12"));
        jMenuItemPopupExportKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupExportKeyActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupExportKey);

        jMenuItemPopupExportCert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/certificate16x16.gif"))); // NOI18N
        jMenuItemPopupExportCert.setText(this.rb.getResourceString("label.cert.export"));
        jMenuItemPopupExportCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupExportCertActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupExportCert);

        jMenuItemPopupRenameAlias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/edit_16x16.gif"))); // NOI18N
        jMenuItemPopupRenameAlias.setText(this.rb.getResourceString("button.edit"));
        jMenuItemPopupRenameAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupRenameAliasActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupRenameAlias);
        jPopupMenu.add(jSeparator1);

        jMenuItemPopupDeleteEntry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/delete_16x16.gif"))); // NOI18N
        jMenuItemPopupDeleteEntry.setText(this.rb.getResourceString( "button.delete"));
        jMenuItemPopupDeleteEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupDeleteEntryActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemPopupDeleteEntry);

        setLayout(new java.awt.GridBagLayout());

        jSplitPane.setDividerLocation(200);
        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTable.setModel(new TableModelCertificates());
        jTable.setShowHorizontalLines(false);
        jTable.setShowVerticalLines(false);
        jTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableMouseClicked(evt);
            }
        });
        jScrollPaneTable.setViewportView(jTable);

        jSplitPane.setLeftComponent(jScrollPaneTable);

        jTextAreaInfo.setEditable(false);
        jTextAreaInfo.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jTextAreaInfo.setLineWrap(true);
        jTextAreaInfo.setWrapStyleWord(true);
        jScrollPaneInfo.setViewportView(jTextAreaInfo);

        jTabbedPaneInfo.addTab(this.rb.getResourceString( "tab.info.basic" ), jScrollPaneInfo);

        jTextAreaInfoExtension.setEditable(false);
        jTextAreaInfoExtension.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jTextAreaInfoExtension.setLineWrap(true);
        jTextAreaInfoExtension.setWrapStyleWord(true);
        jScrollPaneInfoExtension.setViewportView(jTextAreaInfoExtension);

        jTabbedPaneInfo.addTab(this.rb.getResourceString( "tab.info.extension" ), jScrollPaneInfoExtension);

        jScrollPaneTrustchain.setViewportView(jTreeTrustChain);

        jTabbedPaneInfo.addTab(this.rb.getResourceString( "tab.info.trustchain" ), jScrollPaneTrustchain);

        jSplitPane.setRightComponent(jTabbedPaneInfo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane, gridBagConstraints);

        jCheckBoxShowCACertificates.setText(this.rb.getResourceString( "display.ca.certs"));
        jCheckBoxShowCACertificates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowCACertificatesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(jCheckBoxShowCACertificates, gridBagConstraints);

        jLabelTrustAnchor.setText(this.rb.getResourceString( "label.trustanchor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(jLabelTrustAnchor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelTrustAnchorValue, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void jCheckBoxShowCACertificatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowCACertificatesActionPerformed
    this.refreshData();
}//GEN-LAST:event_jCheckBoxShowCACertificatesActionPerformed

private void jTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableMouseClicked
    if (evt.isPopupTrigger() || evt.isMetaDown()) {
        if (this.jTable.getSelectedRowCount() == 0) {
            return;
        }
        this.jPopupMenu.show(evt.getComponent(), evt.getX(),
                evt.getY());
    }
}//GEN-LAST:event_jTableMouseClicked

private void jMenuItemPopupDeleteEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupDeleteEntryActionPerformed
    if (!this.isOperationAllowed(false)) {
        return;
    }
    this.deleteSelectedCertificate();
}//GEN-LAST:event_jMenuItemPopupDeleteEntryActionPerformed

private void jMenuItemPopupRenameAliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupRenameAliasActionPerformed
    if (!this.isOperationAllowed(false)) {
        return;
    }
    this.renameSelectedAlias();
}//GEN-LAST:event_jMenuItemPopupRenameAliasActionPerformed

private void jMenuItemPopupExportKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupExportKeyActionPerformed
    if (!this.isOperationAllowed(false)) {
        return;
    }
    this.exportPKCS12Key();
}//GEN-LAST:event_jMenuItemPopupExportKeyActionPerformed

private void jMenuItemPopupExportCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupExportCertActionPerformed
    if (!this.isOperationAllowed(false)) {
        return;
    }
    this.exportSelectedCertificate();
}//GEN-LAST:event_jMenuItemPopupExportCertActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxShowCACertificates;
    private javax.swing.JLabel jLabelTrustAnchor;
    private javax.swing.JLabel jLabelTrustAnchorValue;
    private javax.swing.JMenuItem jMenuItemPopupDeleteEntry;
    private javax.swing.JMenuItem jMenuItemPopupExportCert;
    private javax.swing.JMenuItem jMenuItemPopupExportKey;
    private javax.swing.JMenuItem jMenuItemPopupRenameAlias;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPaneInfo;
    private javax.swing.JScrollPane jScrollPaneInfoExtension;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JScrollPane jScrollPaneTrustchain;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPaneInfo;
    private de.mendelson.util.tables.JTableSortable jTable;
    private javax.swing.JTextArea jTextAreaInfo;
    private javax.swing.JTextArea jTextAreaInfoExtension;
    private de.mendelson.util.security.cert.gui.JTreeTrustChain jTreeTrustChain;
    // End of variables declaration//GEN-END:variables

    /**
     * Makes this a popup menu listener
     */
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (e.getSource() == this.jPopupMenu) {
            boolean operationAllowed = this.isOperationAllowed(true);
            KeystoreCertificate selectedCertificate = this.getSelectedCertificate();
            this.jMenuItemPopupExportCert.setEnabled(selectedCertificate != null);
            this.jMenuItemPopupExportKey.setEnabled(selectedCertificate != null
                    && selectedCertificate.getIsKeyPair());
            //later enhancement possible here
            this.jMenuItemPopupDeleteEntry.setEnabled(operationAllowed && !this.keystoreIsReadonly());
            this.jMenuItemPopupRenameAlias.setEnabled(operationAllowed && !this.keystoreIsReadonly());
        }
    }

    /**
     * Makes this a popup menu listener
     */
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    /**
     * Makes this a popup menu listener
     */
    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }
}
