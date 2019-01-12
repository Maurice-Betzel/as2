//$Header: /mendelson_business_integration/de/mendelson/util/security/cert/gui/keygeneration/JDialogGenerateKey.java 14    11/24/17 10:03a He $
package de.mendelson.util.security.cert.gui.keygeneration;

import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.security.keygeneration.KeyGenerationValues;
import de.mendelson.util.security.keygeneration.KeyGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Dialog to work with certificates
 *
 * @author S.Heller
 * @version $Revision: 14 $
 */
public class JDialogGenerateKey extends JDialog {

    public static final String SIGNATUREALGORITHM_SHA256_WITH_RSA = "SHA256WithRSA";
    public static final String SIGNATUREALGORITHM_SHA1_WITH_RSA = "SHA1WithRSA";
    public static final String SIGNATUREALGORITHM_MD5_WITH_RSA = "MD5WithRSA";
    public static final String SIGNATUREALGORITHM_SHA256_WITH_ECDSA = "SHA256WithECDSA";

    private static final String KEY_SIZE_256 = "256";
    private static final String KEY_SIZE_384 = "384";
    private static final String KEY_SIZE_512 = "512";
    private static final String KEY_SIZE_1024 = "1024";
    private static final String KEY_SIZE_2048 = "2048";
    private static final String KEY_SIZE_4096 = "4096";

    /**
     * ResourceBundle to localize the GUI
     */
    private MecResourceBundle rb = null;
    private CertificateManager manager = null;
    private KeyGenerationValues values = new KeyGenerationValues();
    private String alias = "myalias";
    private List<GeneralName> namesList = new ArrayList<GeneralName>();

    /**
     * Creates new form JDialogPartnerConfig
     *
     * @param manager Manager that handles the certificates
     */
    public JDialogGenerateKey(JFrame parent, CertificateManager manager) {
        super(parent, true);
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundleGenerateKey.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.setTitle(this.rb.getResourceString("title"));
        initComponents();
        this.manager = manager;
        //populate combo boxes
        this.jComboBoxKeyType.removeAllItems();
        this.jComboBoxKeyType.addItem(KeyGenerator.KEYTYPE_RSA);
        this.jComboBoxKeyType.addItem(KeyGenerator.KEYTYPE_DSA);
        this.jComboBoxKeyType.addItem(KeyGenerator.KEYTYPE_ECDSA);
        this.jComboBoxKeyType.setSelectedItem(KeyGenerator.KEYTYPE_RSA);
        this.displayValues();
        this.getRootPane().setDefaultButton(this.jButtonOk);
        this.setViewMode();
    }

    private void setKeyRelatedValuesToCombobox() {
        String keyType = (String) this.jComboBoxKeyType.getSelectedItem();
        String keySizePreselection = (String) this.jComboBoxSize.getSelectedItem();
        String signatureAlgorithmPreselection = (String) this.jComboBoxSignature.getSelectedItem();
        if (keyType == null) {
            return;
        }
        this.jComboBoxSize.removeAllItems();
        this.jComboBoxSignature.removeAllItems();
        if (keyType.equals(KeyGenerator.KEYTYPE_ECDSA)) {
            this.jComboBoxSize.addItem(KEY_SIZE_256);
            this.jComboBoxSize.addItem(KEY_SIZE_384);
            this.jComboBoxSize.addItem(KEY_SIZE_512);
            this.jComboBoxSignature.addItem(SIGNATUREALGORITHM_SHA256_WITH_ECDSA);
        } else {
            this.jComboBoxSize.addItem(KEY_SIZE_1024);
            this.jComboBoxSize.addItem(KEY_SIZE_2048);
            this.jComboBoxSize.addItem(KEY_SIZE_4096);
            this.jComboBoxSignature.addItem(SIGNATUREALGORITHM_SHA1_WITH_RSA);
            this.jComboBoxSignature.addItem(SIGNATUREALGORITHM_MD5_WITH_RSA);
            this.jComboBoxSignature.addItem(SIGNATUREALGORITHM_SHA256_WITH_RSA);
        }
        this.jComboBoxSize.setSelectedItem(keySizePreselection);
        if (this.jComboBoxSize.getSelectedItem() == null) {
            this.jComboBoxSize.setSelectedIndex(0);
        }
        this.jComboBoxSignature.setSelectedItem(signatureAlgorithmPreselection);
        if (this.jComboBoxSignature.getSelectedItem() == null) {
            this.jComboBoxSignature.setSelectedIndex(0);
        }
    }

    /**
     * Stores the actual gui values in an object that could be accessed from
     * outside
     */
    private void captureGUIValues() {
        this.getValues().setCommonName(this.jTextFieldCommonName.getText().trim());
        this.getValues().setCountryCode(this.jTextFieldCountryCode.getText().trim());
        this.getValues().setEmailAddress(this.jTextFieldMailAddress.getText().trim());
        this.getValues().setKeySize(Integer.valueOf(this.jComboBoxSize.getSelectedItem().toString()));
        this.getValues().setKeyType(this.jComboBoxKeyType.getSelectedItem().toString());
        this.getValues().setKeyValidInDays(Integer.valueOf(this.jTextFieldValidity.getText().trim()));
        this.getValues().setLocalityName(this.jTextFieldLocality.getText().trim());
        this.getValues().setOrganisationName(this.jTextFieldOrganisationName.getText().trim());
        this.getValues().setOrganisationUnit(this.jTextFieldOrganisationUnit.getText().trim());
        this.getValues().setSignatureAlgorithm(this.signature2OID(this.jComboBoxSignature.getSelectedItem().toString()));
        this.getValues().setStateName(this.jTextFieldState.getText().trim());
        if (this.jCheckBoxPurposeSignEncrypt.isSelected() || this.jCheckBoxPurposeSSL.isSelected()) {
            this.getValues().setKeyExtension(new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        }
        if (this.jCheckBoxPurposeSSL.isSelected()) {
            Vector<KeyPurposeId> extKeyUsage = new Vector<KeyPurposeId>();
            extKeyUsage.add(KeyPurposeId.id_kp_serverAuth);
            extKeyUsage.add(KeyPurposeId.id_kp_clientAuth);
            this.getValues().setExtendedKeyExtension(new ExtendedKeyUsage(extKeyUsage));
        }
        //Subject Alternative Name (German: Alternativer Antragstellername)
        for (GeneralName generalName : this.namesList) {
            this.getValues().addSubjectAlternativeName(generalName);
        }
    }

    private void displayValues() {
        this.jTextFieldCommonName.setText(this.getValues().getCommonName());
        this.jTextFieldCountryCode.setText(this.getValues().getCountryCode());
        this.jTextFieldLocality.setText(this.getValues().getLocalityName());
        this.jTextFieldMailAddress.setText(this.getValues().getEmailAddress());
        this.jTextFieldOrganisationName.setText(this.getValues().getOrganisationName());
        this.jTextFieldOrganisationUnit.setText(this.getValues().getOrganisationUnit());
        this.jTextFieldState.setText(this.getValues().getStateName());
        this.jTextFieldValidity.setText(String.valueOf(this.getValues().getKeyValidInDays()));
        this.jComboBoxSize.setSelectedItem(String.valueOf(this.getValues().getKeySize()));
        this.jComboBoxKeyType.setSelectedItem(this.getValues().getKeyType());
        this.jComboBoxSignature.setSelectedItem(this.oid2Signature(this.getValues().getSignatureAlgorithm()));
    }

    private String signature2OID(String signature) {
        if (signature.equals(SIGNATUREALGORITHM_MD5_WITH_RSA)) {
            return (KeyGenerator.SIGNATUREALGORITHM_MD5_WITH_RSA_OID);
        } else if (signature.equals(SIGNATUREALGORITHM_SHA1_WITH_RSA)) {
            return (KeyGenerator.SIGNATUREALGORITHM_SHA1_WITH_RSA_OID);
        } else if (signature.equals(SIGNATUREALGORITHM_SHA256_WITH_RSA)) {
            return (KeyGenerator.SIGNATUREALGORITHM_SHA256_WITH_RSA_OID);
        } else if (signature.equals(SIGNATUREALGORITHM_SHA256_WITH_ECDSA)) {
            return (KeyGenerator.SIGNATUREALGORITHM_SHA256_WITH_ECDSA_OID);
        }
        throw new IllegalArgumentException("signature2OID: Unsupported signature algorithm " + signature);
    }

    private String oid2Signature(String oid) {
        if (oid.equals(KeyGenerator.SIGNATUREALGORITHM_MD5_WITH_RSA_OID)) {
            return (SIGNATUREALGORITHM_MD5_WITH_RSA);
        } else if (oid.equals(KeyGenerator.SIGNATUREALGORITHM_SHA1_WITH_RSA_OID)) {
            return (SIGNATUREALGORITHM_SHA1_WITH_RSA);
        } else if (oid.equals(KeyGenerator.SIGNATUREALGORITHM_SHA256_WITH_RSA_OID)) {
            return (SIGNATUREALGORITHM_SHA256_WITH_RSA);
        } else if (oid.equals(KeyGenerator.SIGNATUREALGORITHM_SHA256_WITH_ECDSA_OID)) {
            return (SIGNATUREALGORITHM_SHA256_WITH_ECDSA);
        }
        throw new IllegalArgumentException("signature2OID: Unsupported signature algorithm " + oid);
    }

    public String getAlias() {
        return (this.alias);
    }

    /**
     * Sets the ok and cancel buttons of this GUI
     */
    private void setButtonState() {
    }

    /**
     * Checks the settings of the key generation
     */
    private boolean ignoreSettingProblems() {
        StringBuilder warning = new StringBuilder();
        String domain = this.jTextFieldCommonName.getText().trim();
        //if the CN is a wildcard entry it may have the structure "*.domain.com". In this case
        //the domain check for existence has to be performed on the main domain
        if (domain.startsWith("*.")) {
            domain = domain.substring(2);
        }
        String mail = this.jTextFieldMailAddress.getText();
        //check if domain exists
        try {
            InetAddress address = InetAddress.getByName(domain);
        } catch (UnknownHostException e) {
            if (warning.length() > 0) {
                warning.append("\n\n");
            }
            warning.append(this.rb.getResourceString("warning.nonexisting.domain", domain));
        }
        //get the mail domain        
        int atIndex = mail.indexOf("@");
        if (atIndex < 0 || atIndex == mail.length() - 1) {
            if (warning.length() > 0) {
                warning.append("\n\n");
            }
            warning.append(this.rb.getResourceString("warning.invalid.mail", mail));
        } else {
            String mailDomain = mail.substring(atIndex + 1);
            if (!domain.endsWith(mailDomain)) {
                if (warning.length() > 0) {
                    warning.append("\n\n");
                }
                warning.append(this.rb.getResourceString("warning.mail.in.domain", domain));
            }
        }
        if (warning.length() > 0) {
            return (this.askUserToIgnoreSettingProblem(warning.toString()));
        } else {
            return (true);
        }
    }

    private boolean askUserToIgnoreSettingProblem(String warning) {
        String[] options = new String[]{
            this.rb.getResourceString("button.reedit"),
            this.rb.getResourceString("button.ignore"),};
        int requestValue = JOptionPane.showOptionDialog(this,
                warning,
                this.rb.getResourceString("warning.title"), JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null,
                options, options[0]);
        return (requestValue == 1);
    }

    private synchronized void setViewMode() {
        this.jTextFieldValidity.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jTextFieldValidity.setEditable(this.jToggleButtonExpert.isSelected());
        this.jComboBoxKeyType.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jComboBoxSignature.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jComboBoxSize.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jCheckBoxPurposeSSL.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jCheckBoxPurposeSignEncrypt.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jLabelPurpose.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jLabelSubjectAlternativeNames.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jTextFieldSubjectAlternativeNames.setEnabled(this.jToggleButtonExpert.isSelected());
        this.jButtonSubjectAlternativeNames.setEnabled(this.jToggleButtonExpert.isSelected());
    }

    private void editSubjectAlternativeNames() {
        JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        JDialogEditSubjectAlternativeNames dialog = new JDialogEditSubjectAlternativeNames(parentFrame, this.namesList);
        dialog.setVisible(true);
        //display names list in text field
        StringBuilder namesSerialized = new StringBuilder();
        for (GeneralName generalName : this.namesList) {
            if (namesSerialized.length() > 0) {
                namesSerialized.append("; ");
            }
            int tagNo = generalName.getTagNo();
            namesSerialized.append(TagNo.intValueToString(tagNo));
            namesSerialized.append("=");
            //IP addresses are sometimes stored as DEROctetString which would result in a single hex value on display
            // - this has to be decoded for the display
            if (generalName.getName() instanceof DEROctetString && tagNo == GeneralName.iPAddress) {
                DEROctetString str = (DEROctetString) generalName.getName();
                StringBuilder decStr = new StringBuilder();
                byte[] octets = str.getOctets();
                for (byte octet : octets) {
                    if (decStr.length() > 0) {
                        decStr.append(".");
                    }
                    decStr.append((int) (octet & 0xFF));
                }
                namesSerialized.append(decStr);
            } else {
                namesSerialized.append(generalName.getName().toString());
            }
        }
        this.jTextFieldSubjectAlternativeNames.setText(namesSerialized.toString());
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
        jPanelEditInner = new javax.swing.JPanel();
        jLabelIcon = new javax.swing.JLabel();
        jLabelOrganisationUnit = new javax.swing.JLabel();
        jComboBoxSize = new javax.swing.JComboBox();
        jComboBoxSignature = new javax.swing.JComboBox();
        jComboBoxKeyType = new javax.swing.JComboBox();
        jTextFieldValidity = new javax.swing.JTextField();
        jTextFieldMailAddress = new javax.swing.JTextField();
        jTextFieldState = new javax.swing.JTextField();
        jTextFieldOrganisationName = new javax.swing.JTextField();
        jTextFieldOrganisationUnit = new javax.swing.JTextField();
        jLabelOrganisationName = new javax.swing.JLabel();
        jLabelLocality = new javax.swing.JLabel();
        jLabelState = new javax.swing.JLabel();
        jLabelCountryCode = new javax.swing.JLabel();
        jLabelMailAddress = new javax.swing.JLabel();
        jLabelValidity = new javax.swing.JLabel();
        jLabelKeyType = new javax.swing.JLabel();
        jLabelSignature = new javax.swing.JLabel();
        jLabelSize = new javax.swing.JLabel();
        jLabelCommonName = new javax.swing.JLabel();
        jCheckBoxPurposeSignEncrypt = new javax.swing.JCheckBox();
        jCheckBoxPurposeSSL = new javax.swing.JCheckBox();
        jLabelPurpose = new javax.swing.JLabel();
        jPanelLocality = new javax.swing.JPanel();
        jTextFieldLocality = new javax.swing.JTextField();
        jLabelLocalityHint = new javax.swing.JLabel();
        jPanelCommonName = new javax.swing.JPanel();
        jTextFieldCommonName = new javax.swing.JTextField();
        jLabelCommonNameHint = new javax.swing.JLabel();
        jPanelCountryCode = new javax.swing.JPanel();
        jLabelCountryCodeHint = new javax.swing.JLabel();
        jTextFieldCountryCode = new javax.swing.JTextField();
        jPanelCountryCode1 = new javax.swing.JPanel();
        jTextFieldSubjectAlternativeNames = new javax.swing.JTextField();
        jButtonSubjectAlternativeNames = new javax.swing.JButton();
        jLabelSubjectAlternativeNames = new javax.swing.JLabel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jToolBar = new javax.swing.JToolBar();
        jToggleButtonBasic = new javax.swing.JToggleButton();
        jToggleButtonExpert = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelEdit.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelEdit.setLayout(new java.awt.GridBagLayout());

        jPanelEditInner.setLayout(new java.awt.GridBagLayout());

        jLabelIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/keygeneration/key32x32.gif"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        jPanelEditInner.add(jLabelIcon, gridBagConstraints);

        jLabelOrganisationUnit.setText(this.rb.getResourceString( "label.organisationunit"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelOrganisationUnit, gridBagConstraints);

        jComboBoxSize.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jComboBoxSize, gridBagConstraints);

        jComboBoxSignature.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jComboBoxSignature, gridBagConstraints);

        jComboBoxKeyType.setPreferredSize(new java.awt.Dimension(80, 20));
        jComboBoxKeyType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxKeyTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jComboBoxKeyType, gridBagConstraints);

        jTextFieldValidity.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldValidityKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jTextFieldValidity, gridBagConstraints);

        jTextFieldMailAddress.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldMailAddressKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jTextFieldMailAddress, gridBagConstraints);

        jTextFieldState.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldStateKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jTextFieldState, gridBagConstraints);

        jTextFieldOrganisationName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldOrganisationNameKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jTextFieldOrganisationName, gridBagConstraints);

        jTextFieldOrganisationUnit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldOrganisationUnitKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jTextFieldOrganisationUnit, gridBagConstraints);

        jLabelOrganisationName.setText(this.rb.getResourceString( "label.organisationname"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelOrganisationName, gridBagConstraints);

        jLabelLocality.setText(this.rb.getResourceString( "label.locality"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelLocality, gridBagConstraints);

        jLabelState.setText(this.rb.getResourceString( "label.state"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelState, gridBagConstraints);

        jLabelCountryCode.setText(this.rb.getResourceString( "label.countrycode"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelCountryCode, gridBagConstraints);

        jLabelMailAddress.setText(this.rb.getResourceString( "label.mailaddress"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelMailAddress, gridBagConstraints);

        jLabelValidity.setText(this.rb.getResourceString( "label.validity"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelValidity, gridBagConstraints);

        jLabelKeyType.setText(this.rb.getResourceString( "label.keytype"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelKeyType, gridBagConstraints);

        jLabelSignature.setText(this.rb.getResourceString( "label.signature"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelSignature, gridBagConstraints);

        jLabelSize.setText(this.rb.getResourceString( "label.size"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelSize, gridBagConstraints);

        jLabelCommonName.setText(this.rb.getResourceString( "label.commonname"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelCommonName, gridBagConstraints);

        jCheckBoxPurposeSignEncrypt.setText(this.rb.getResourceString( "label.purpose.encsign"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanelEditInner.add(jCheckBoxPurposeSignEncrypt, gridBagConstraints);

        jCheckBoxPurposeSSL.setText(this.rb.getResourceString( "label.purpose.ssl"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanelEditInner.add(jCheckBoxPurposeSSL, gridBagConstraints);

        jLabelPurpose.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPurpose.setText(this.rb.getResourceString( "label.purpose"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(30, 5, 5, 5);
        jPanelEditInner.add(jLabelPurpose, gridBagConstraints);

        jPanelLocality.setLayout(new java.awt.GridBagLayout());

        jTextFieldLocality.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldLocalityKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelLocality.add(jTextFieldLocality, gridBagConstraints);

        jLabelLocalityHint.setText(this.rb.getResourceString("label.locality.hint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelLocality.add(jLabelLocalityHint, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelEditInner.add(jPanelLocality, gridBagConstraints);

        jPanelCommonName.setLayout(new java.awt.GridBagLayout());

        jTextFieldCommonName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCommonNameKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelCommonName.add(jTextFieldCommonName, gridBagConstraints);

        jLabelCommonNameHint.setText(this.rb.getResourceString("label.commonname.hint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelCommonName.add(jLabelCommonNameHint, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelEditInner.add(jPanelCommonName, gridBagConstraints);

        jPanelCountryCode.setLayout(new java.awt.GridBagLayout());

        jLabelCountryCodeHint.setText(this.rb.getResourceString("label.countrycode.hint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelCountryCode.add(jLabelCountryCodeHint, gridBagConstraints);

        jTextFieldCountryCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCountryCodeKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelCountryCode.add(jTextFieldCountryCode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelEditInner.add(jPanelCountryCode, gridBagConstraints);

        jPanelCountryCode1.setLayout(new java.awt.GridBagLayout());

        jTextFieldSubjectAlternativeNames.setEditable(false);
        jTextFieldSubjectAlternativeNames.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldSubjectAlternativeNamesKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelCountryCode1.add(jTextFieldSubjectAlternativeNames, gridBagConstraints);

        jButtonSubjectAlternativeNames.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/util/security/cert/gui/keygeneration/edit_16x16.gif"))); // NOI18N
        jButtonSubjectAlternativeNames.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonSubjectAlternativeNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSubjectAlternativeNamesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelCountryCode1.add(jButtonSubjectAlternativeNames, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelEditInner.add(jPanelCountryCode1, gridBagConstraints);

        jLabelSubjectAlternativeNames.setText(this.rb.getResourceString( "label.subjectalternativenames"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanelEditInner.add(jLabelSubjectAlternativeNames, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanelEdit.add(jPanelEditInner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelButtons, gridBagConstraints);

        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);

        jToggleButtonBasic.setSelected(true);
        jToggleButtonBasic.setText(this.rb.getResourceString( "view.basic"));
        jToggleButtonBasic.setFocusable(false);
        jToggleButtonBasic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonBasic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonBasic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonBasicActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleButtonBasic);

        jToggleButtonExpert.setText(this.rb.getResourceString( "view.expert"));
        jToggleButtonExpert.setFocusable(false);
        jToggleButtonExpert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonExpert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonExpert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonExpertActionPerformed(evt);
            }
        });
        jToolBar.add(jToggleButtonExpert);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jToolBar, gridBagConstraints);

        setSize(new java.awt.Dimension(559, 718));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.values = null;
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        if (!this.ignoreSettingProblems()) {
            return;
        }
        this.captureGUIValues();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jTextFieldValidityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldValidityKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldValidityKeyReleased

    private void jTextFieldMailAddressKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMailAddressKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMailAddressKeyReleased

    private void jTextFieldCountryCodeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCountryCodeKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldCountryCodeKeyReleased

    private void jTextFieldStateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldStateKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldStateKeyReleased

    private void jTextFieldLocalityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLocalityKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldLocalityKeyReleased

    private void jTextFieldOrganisationNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldOrganisationNameKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldOrganisationNameKeyReleased

    private void jTextFieldOrganisationUnitKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldOrganisationUnitKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldOrganisationUnitKeyReleased

    private void jTextFieldCommonNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCommonNameKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldCommonNameKeyReleased

    private void jToggleButtonBasicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonBasicActionPerformed
        this.jToggleButtonExpert.setSelected(!this.jToggleButtonBasic.isSelected());
        this.setViewMode();
    }//GEN-LAST:event_jToggleButtonBasicActionPerformed

    private void jToggleButtonExpertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonExpertActionPerformed
        this.jToggleButtonBasic.setSelected(!this.jToggleButtonExpert.isSelected());
        this.setViewMode();
    }//GEN-LAST:event_jToggleButtonExpertActionPerformed

    private void jComboBoxKeyTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxKeyTypeActionPerformed
        this.setKeyRelatedValuesToCombobox();
    }//GEN-LAST:event_jComboBoxKeyTypeActionPerformed

    private void jTextFieldSubjectAlternativeNamesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSubjectAlternativeNamesKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldSubjectAlternativeNamesKeyReleased

    private void jButtonSubjectAlternativeNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSubjectAlternativeNamesActionPerformed
        this.editSubjectAlternativeNames();
    }//GEN-LAST:event_jButtonSubjectAlternativeNamesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JButton jButtonSubjectAlternativeNames;
    private javax.swing.JCheckBox jCheckBoxPurposeSSL;
    private javax.swing.JCheckBox jCheckBoxPurposeSignEncrypt;
    private javax.swing.JComboBox jComboBoxKeyType;
    private javax.swing.JComboBox jComboBoxSignature;
    private javax.swing.JComboBox jComboBoxSize;
    private javax.swing.JLabel jLabelCommonName;
    private javax.swing.JLabel jLabelCommonNameHint;
    private javax.swing.JLabel jLabelCountryCode;
    private javax.swing.JLabel jLabelCountryCodeHint;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JLabel jLabelKeyType;
    private javax.swing.JLabel jLabelLocality;
    private javax.swing.JLabel jLabelLocalityHint;
    private javax.swing.JLabel jLabelMailAddress;
    private javax.swing.JLabel jLabelOrganisationName;
    private javax.swing.JLabel jLabelOrganisationUnit;
    private javax.swing.JLabel jLabelPurpose;
    private javax.swing.JLabel jLabelSignature;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JLabel jLabelState;
    private javax.swing.JLabel jLabelSubjectAlternativeNames;
    private javax.swing.JLabel jLabelValidity;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelCommonName;
    private javax.swing.JPanel jPanelCountryCode;
    private javax.swing.JPanel jPanelCountryCode1;
    private javax.swing.JPanel jPanelEdit;
    private javax.swing.JPanel jPanelEditInner;
    private javax.swing.JPanel jPanelLocality;
    private javax.swing.JTextField jTextFieldCommonName;
    private javax.swing.JTextField jTextFieldCountryCode;
    private javax.swing.JTextField jTextFieldLocality;
    private javax.swing.JTextField jTextFieldMailAddress;
    private javax.swing.JTextField jTextFieldOrganisationName;
    private javax.swing.JTextField jTextFieldOrganisationUnit;
    private javax.swing.JTextField jTextFieldState;
    private javax.swing.JTextField jTextFieldSubjectAlternativeNames;
    private javax.swing.JTextField jTextFieldValidity;
    private javax.swing.JToggleButton jToggleButtonBasic;
    private javax.swing.JToggleButton jToggleButtonExpert;
    private javax.swing.JToolBar jToolBar;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the values
     */
    public KeyGenerationValues getValues() {
        return values;
    }
}
