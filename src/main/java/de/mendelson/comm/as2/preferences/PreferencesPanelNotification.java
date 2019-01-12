//$Header: /as2/de/mendelson/comm/as2/preferences/PreferencesPanelNotification.java 24    22.10.18 13:03 Heller $
package de.mendelson.comm.as2.preferences;

import de.mendelson.comm.as2.client.AS2StatusBar;
import de.mendelson.comm.as2.clientserver.message.PerformNotificationTestRequest;
import de.mendelson.util.systemevents.notification.NotificationData;
import de.mendelson.util.systemevents.notification.clientserver.NotificationGetRequest;
import de.mendelson.util.systemevents.notification.clientserver.NotificationGetResponse;
import de.mendelson.util.systemevents.notification.clientserver.NotificationSetMessage;
import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import de.mendelson.util.clientserver.messages.ClientServerResponse;
import de.mendelson.util.systemevents.notification.NotificationDataImplAS2;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software. Other product
 * and brand names are trademarks of their respective owners.
 */
/**
 * Panel to define the directory preferences
 *
 * @author S.Heller
 * @version: $Revision: 24 $
 */
public class PreferencesPanelNotification extends PreferencesPanel {

    /**
     * Localize the GUI
     */
    private MecResourceBundle rb = null;
    private PreferencesClient preferences;
    private BaseClient baseClient;
    private boolean inInit = false;
    private AS2StatusBar statusbar;

    /**
     * Creates new form PreferencesPanelDirectories
     */
    public PreferencesPanelNotification(BaseClient baseClient, AS2StatusBar statusbar) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePreferences.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle " + e.getClassName() + " not found.");
        }
        this.baseClient = baseClient;
        this.statusbar = statusbar;
        this.preferences = new PreferencesClient(baseClient);
        this.initComponents();
        this.inInit = true;
        this.jComboBoxSecurity.removeAllItems();
        this.jComboBoxSecurity.addItem(new SecurityEntry(NotificationData.SECURITY_PLAIN));
        this.jComboBoxSecurity.addItem(new SecurityEntry(NotificationData.SECURITY_START_SSL));
        this.jComboBoxSecurity.addItem(new SecurityEntry(NotificationData.SECURITY_SSL));
        this.inInit = false;
    }

    /**
     * Sets new preferences to this panel to changes/modify
     */
    @Override
    public void loadPreferences() {
        this.inInit = true;
        NotificationDataImplAS2 data 
                = (NotificationDataImplAS2)((NotificationGetResponse) 
                        this.baseClient.sendSync(new NotificationGetRequest())).getData();
        this.jTextFieldHost.setText(data.getMailServer());
        this.jTextFieldNotificationMail.setText(data.getNotificationMail());
        this.jTextFieldPort.setText(String.valueOf(data.getMailServerPort()));
        this.jTextFieldReplyTo.setText(data.getReplyTo());
        this.jCheckBoxNotifyCert.setSelected(data.notifyCertExpire());
        this.jCheckBoxNotifyTransactionError.setSelected(data.notifyTransactionError());
        this.jCheckBoxNotifyCEM.setSelected(data.notifyCEM());
        this.jCheckBoxNotifySystemFailure.setSelected(data.notifySystemFailure());
        this.jCheckBoxNotifyResend.setSelected(data.notifyResendDetected());
        this.jCheckBoxSMTPAuthentication.setSelected(data.usesSMTHAuth());
        if (data.getSMTPUser() != null) {
            this.jTextFieldUser.setText(data.getSMTPUser());
        } else {
            this.jTextFieldUser.setText("");
        }
        if (data.getSMTPPass() != null) {
            this.jPasswordFieldPass.setText(String.valueOf(data.getSMTPPass()));
        } else {
            this.jPasswordFieldPass.setText("");
        }
        this.jComboBoxSecurity.setSelectedItem(new SecurityEntry(data.getConnectionSecurity()));
        this.jTextFieldMaxMailsPerMin.setText(String.valueOf(data.getMaxNotificationsPerMin()));
        this.inInit = false;
        this.setButtonState();
    }

    private void setButtonState() {
        this.jTextFieldUser.setEnabled(this.jCheckBoxSMTPAuthentication.isSelected());
        this.jPasswordFieldPass.setEnabled(this.jCheckBoxSMTPAuthentication.isSelected());
    }

    /**
     * Captures the gui settings, creates a notification data object from these
     * settings and returns this
     */
    private NotificationDataImplAS2 captureGUIData() {
        NotificationDataImplAS2 data = new NotificationDataImplAS2();
        data.setMailServer(this.jTextFieldHost.getText());
        try {
            data.setMailServerPort(Integer.valueOf(this.jTextFieldPort.getText()).intValue());
        } catch (NumberFormatException e) {
            //if there is nonsense in this field just take the default value of the object
        }
        data.setNotifyCertExpire(this.jCheckBoxNotifyCert.isSelected());
        data.setNotifyTransactionError(this.jCheckBoxNotifyTransactionError.isSelected());
        data.setNotifyCEM(this.jCheckBoxNotifyCEM.isSelected());
        data.setNotifySystemFailure(this.jCheckBoxNotifySystemFailure.isSelected());
        data.setNotificationMail(this.jTextFieldNotificationMail.getText());
        data.setUsesSMTHAuth(this.jCheckBoxSMTPAuthentication.isSelected());
        data.setSMTPUser(this.jTextFieldUser.getText());
        data.setSMTPPass(this.jPasswordFieldPass.getPassword());
        data.setReplyTo(this.jTextFieldReplyTo.getText());
        data.setNotifyResendDetected(this.jCheckBoxNotifyResend.isSelected());
        data.setConnectionSecurity(((SecurityEntry) this.jComboBoxSecurity.getSelectedItem()).getValue());
        try {
            data.setMaxNotificationsPerMin(new Integer(this.jTextFieldMaxMailsPerMin.getText()));
        } catch (Exception e) {
            //nop, ignore
        }
        return (data);
    }

    /**
     * Capture the GUI values and store them in the database
     *
     */
    @Override
    public void savePreferences() {
        NotificationData data = this.captureGUIData();
        NotificationSetMessage message = new NotificationSetMessage();
        message.setData(data);
        this.baseClient.sendAsync(message);

    }

    private void sendTestMail() {
        final String uniqueId = this.getClass().getName() + ".sendTestMail." + System.currentTimeMillis();
        Runnable test = new Runnable() {

            @Override
            public void run() {
                try {
                    PreferencesPanelNotification.this.statusbar.startProgressIndeterminate(
                            PreferencesPanelNotification.this.rb.getResourceString("testmail"), uniqueId);
                    NotificationData data = PreferencesPanelNotification.this.captureGUIData();
                    PerformNotificationTestRequest message = new PerformNotificationTestRequest(data);
                    ClientServerResponse response = PreferencesPanelNotification.this.baseClient.sendSync(message);
                    PreferencesPanelNotification.this.statusbar.stopProgressIfExists(uniqueId);
                    if (response == null) {
                        JOptionPane.showMessageDialog(PreferencesPanelNotification.this,
                                PreferencesPanelNotification.this.rb.getResourceString("testmail.message.error", "Timeout"),
                                PreferencesPanelNotification.this.rb.getResourceString("testmail.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (response.getException() != null) {
                        JOptionPane.showMessageDialog(PreferencesPanelNotification.this,
                                PreferencesPanelNotification.this.rb.getResourceString("testmail.message.error", response.getException().getMessage()),
                                PreferencesPanelNotification.this.rb.getResourceString("testmail.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JOptionPane.showMessageDialog(PreferencesPanelNotification.this,
                            PreferencesPanelNotification.this.rb.getResourceString("testmail.message.success"),
                            PreferencesPanelNotification.this.rb.getResourceString("testmail.title"),
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    PreferencesPanelNotification.this.statusbar.stopProgressIfExists(uniqueId);
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(test);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelSpace = new javax.swing.JPanel();
        jCheckBoxNotifyCert = new javax.swing.JCheckBox();
        jCheckBoxNotifyTransactionError = new javax.swing.JCheckBox();
        jCheckBoxNotifyCEM = new javax.swing.JCheckBox();
        jLabelHost = new javax.swing.JLabel();
        jTextFieldHost = new javax.swing.JTextField();
        jLabelPort = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jLabelNotificationMail = new javax.swing.JLabel();
        jTextFieldNotificationMail = new javax.swing.JTextField();
        jLabelReplyTo = new javax.swing.JLabel();
        jTextFieldReplyTo = new javax.swing.JTextField();
        jButtonSendTestMail = new javax.swing.JButton();
        jPanelSep = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jCheckBoxSMTPAuthentication = new javax.swing.JCheckBox();
        jPasswordFieldPass = new javax.swing.JPasswordField();
        jTextFieldUser = new javax.swing.JTextField();
        jLabelUser = new javax.swing.JLabel();
        jLabelPass = new javax.swing.JLabel();
        jCheckBoxNotifySystemFailure = new javax.swing.JCheckBox();
        jCheckBoxNotifyResend = new javax.swing.JCheckBox();
        jLabelSecurity = new javax.swing.JLabel();
        jComboBoxSecurity = new javax.swing.JComboBox();
        jLabelMaxMailsPerMain = new javax.swing.JLabel();
        jTextFieldMaxMailsPerMin = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanelSpace, gridBagConstraints);

        jCheckBoxNotifyCert.setText(this.rb.getResourceString( "checkbox.notifycertexpire"));
        jCheckBoxNotifyCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyCertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyCert, gridBagConstraints);

        jCheckBoxNotifyTransactionError.setText(this.rb.getResourceString("checkbox.notifytransactionerror"));
        jCheckBoxNotifyTransactionError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyTransactionErrorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyTransactionError, gridBagConstraints);

        jCheckBoxNotifyCEM.setText(this.rb.getResourceString("checkbox.notifycem"));
        jCheckBoxNotifyCEM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyCEMActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        add(jCheckBoxNotifyCEM, gridBagConstraints);

        jLabelHost.setText(this.rb.getResourceString("label.mailhost"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelHost, gridBagConstraints);

        jTextFieldHost.setMinimumSize(new java.awt.Dimension(180, 20));
        jTextFieldHost.setPreferredSize(new java.awt.Dimension(180, 20));
        jTextFieldHost.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldHostKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        add(jTextFieldHost, gridBagConstraints);

        jLabelPort.setText(this.rb.getResourceString( "label.mailport"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelPort, gridBagConstraints);

        jTextFieldPort.setMinimumSize(new java.awt.Dimension(60, 20));
        jTextFieldPort.setPreferredSize(new java.awt.Dimension(60, 20));
        jTextFieldPort.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldPortKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldPort, gridBagConstraints);

        jLabelNotificationMail.setText(this.rb.getResourceString( "label.notificationmail"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelNotificationMail, gridBagConstraints);

        jTextFieldNotificationMail.setMinimumSize(new java.awt.Dimension(180, 20));
        jTextFieldNotificationMail.setPreferredSize(new java.awt.Dimension(180, 20));
        jTextFieldNotificationMail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNotificationMailKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        add(jTextFieldNotificationMail, gridBagConstraints);

        jLabelReplyTo.setText(this.rb.getResourceString( "label.replyto"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelReplyTo, gridBagConstraints);

        jTextFieldReplyTo.setMinimumSize(new java.awt.Dimension(180, 20));
        jTextFieldReplyTo.setPreferredSize(new java.awt.Dimension(180, 20));
        jTextFieldReplyTo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldReplyToKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        add(jTextFieldReplyTo, gridBagConstraints);

        jButtonSendTestMail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/mendelson/comm/as2/preferences/testconnection16x16.gif"))); // NOI18N
        jButtonSendTestMail.setText(this.rb.getResourceString("button.testmail"));
        jButtonSendTestMail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendTestMailActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        add(jButtonSendTestMail, gridBagConstraints);

        jPanelSep.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanelSep.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jPanelSep, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        add(jSeparator2, gridBagConstraints);

        jCheckBoxSMTPAuthentication.setText(this.rb.getResourceString( "label.smtpauthentication"));
        jCheckBoxSMTPAuthentication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSMTPAuthenticationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxSMTPAuthentication, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(jPasswordFieldPass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(jTextFieldUser, gridBagConstraints);

        jLabelUser.setText(this.rb.getResourceString( "label.smtpauthentication.user"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelUser, gridBagConstraints);

        jLabelPass.setText(this.rb.getResourceString( "label.smtpauthentication.pass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelPass, gridBagConstraints);

        jCheckBoxNotifySystemFailure.setText(this.rb.getResourceString( "checkbox.notifyfailure"));
        jCheckBoxNotifySystemFailure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifySystemFailureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifySystemFailure, gridBagConstraints);

        jCheckBoxNotifyResend.setText(this.rb.getResourceString( "checkbox.notifyresend"));
        jCheckBoxNotifyResend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNotifyResendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxNotifyResend, gridBagConstraints);

        jLabelSecurity.setText(this.rb.getResourceString( "label.security"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelSecurity, gridBagConstraints);

        jComboBoxSecurity.setMinimumSize(new java.awt.Dimension(100, 20));
        jComboBoxSecurity.setPreferredSize(new java.awt.Dimension(100, 20));
        jComboBoxSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSecurityActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        add(jComboBoxSecurity, gridBagConstraints);

        jLabelMaxMailsPerMain.setText(this.rb.getResourceString("label.maxmailspermin"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelMaxMailsPerMain, gridBagConstraints);

        jTextFieldMaxMailsPerMin.setMinimumSize(new java.awt.Dimension(40, 20));
        jTextFieldMaxMailsPerMin.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        add(jTextFieldMaxMailsPerMin, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSendTestMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendTestMailActionPerformed
        this.sendTestMail();
    }//GEN-LAST:event_jButtonSendTestMailActionPerformed

private void jTextFieldHostKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldHostKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldHostKeyReleased

private void jTextFieldPortKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldPortKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldPortKeyReleased

private void jTextFieldReplyToKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldReplyToKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldReplyToKeyReleased

private void jTextFieldNotificationMailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNotificationMailKeyReleased
    this.savePreferences();
}//GEN-LAST:event_jTextFieldNotificationMailKeyReleased

private void jCheckBoxNotifyCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyCertActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifyCertActionPerformed

private void jCheckBoxNotifyTransactionErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyTransactionErrorActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifyTransactionErrorActionPerformed

private void jCheckBoxNotifyCEMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyCEMActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifyCEMActionPerformed

private void jCheckBoxSMTPAuthenticationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSMTPAuthenticationActionPerformed
    this.setButtonState();
}//GEN-LAST:event_jCheckBoxSMTPAuthenticationActionPerformed

private void jCheckBoxNotifySystemFailureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifySystemFailureActionPerformed
    this.savePreferences();
}//GEN-LAST:event_jCheckBoxNotifySystemFailureActionPerformed

    private void jCheckBoxNotifyResendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNotifyResendActionPerformed
        this.savePreferences();
    }//GEN-LAST:event_jCheckBoxNotifyResendActionPerformed

    private void jComboBoxSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSecurityActionPerformed
        if (!this.inInit) {
            SecurityEntry selectedEntry = (SecurityEntry) this.jComboBoxSecurity.getSelectedItem();
            this.jTextFieldPort.setText(String.valueOf(selectedEntry.getDefaultPort()));
            this.savePreferences();
        }
    }//GEN-LAST:event_jComboBoxSecurityActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonSendTestMail;
    private javax.swing.JCheckBox jCheckBoxNotifyCEM;
    private javax.swing.JCheckBox jCheckBoxNotifyCert;
    private javax.swing.JCheckBox jCheckBoxNotifyResend;
    private javax.swing.JCheckBox jCheckBoxNotifySystemFailure;
    private javax.swing.JCheckBox jCheckBoxNotifyTransactionError;
    private javax.swing.JCheckBox jCheckBoxSMTPAuthentication;
    private javax.swing.JComboBox jComboBoxSecurity;
    private javax.swing.JLabel jLabelHost;
    private javax.swing.JLabel jLabelMaxMailsPerMain;
    private javax.swing.JLabel jLabelNotificationMail;
    private javax.swing.JLabel jLabelPass;
    private javax.swing.JLabel jLabelPort;
    private javax.swing.JLabel jLabelReplyTo;
    private javax.swing.JLabel jLabelSecurity;
    private javax.swing.JLabel jLabelUser;
    private javax.swing.JPanel jPanelSep;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JPasswordField jPasswordFieldPass;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextFieldHost;
    private javax.swing.JTextField jTextFieldMaxMailsPerMin;
    private javax.swing.JTextField jTextFieldNotificationMail;
    private javax.swing.JTextField jTextFieldPort;
    private javax.swing.JTextField jTextFieldReplyTo;
    private javax.swing.JTextField jTextFieldUser;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getIconResource() {
        return ("/de/mendelson/comm/as2/preferences/notification32x32.gif");
    }

    @Override
    public String getTabResource() {
        return ("tab.notification");
    }

    private static class SecurityEntry {

        private int value = NotificationData.SECURITY_PLAIN;

        public SecurityEntry(int value) {
            this.value = value;
        }

        public int getDefaultPort() {
            if (this.value == NotificationData.SECURITY_SSL) {
                return (465);
            } else {
                return (25);
            }
        }

        @Override
        public String toString() {
            if (this.getValue() == NotificationData.SECURITY_PLAIN) {
                return ("--");
            } else if (this.getValue() == NotificationData.SECURITY_START_SSL) {
                return ("Start SSL");
            } else {
                return ("SSL/TSL");
            }
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * Overwrite the equal method of object
         *
         * @param anObject object ot compare
         */
        @Override
        public boolean equals(Object anObject) {
            if (anObject == this) {
                return (true);
            }
            if (anObject != null && anObject instanceof SecurityEntry) {
                SecurityEntry entry = (SecurityEntry) anObject;
                return (entry.value == this.value);
            }
            return (false);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.value;
            return hash;
        }
    }
}
