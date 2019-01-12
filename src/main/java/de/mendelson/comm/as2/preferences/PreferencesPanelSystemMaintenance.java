//$Header: /as2/de/mendelson/comm/as2/preferences/PreferencesPanelSystemMaintenance.java 12    24.09.18 17:35 Heller $
package de.mendelson.comm.as2.preferences;

import de.mendelson.util.MecResourceBundle;
import de.mendelson.util.clientserver.BaseClient;
import de.mendelson.util.clientserver.clients.preferences.PreferencesClient;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 *Panel to define the inbox settings
 * @author S.Heller
 * @version: $Revision: 12 $
 */
public class PreferencesPanelSystemMaintenance extends PreferencesPanel {

    /**Localize the GUI*/
    private static MecResourceBundle rb = null;
    /**GUI prefs*/
    private PreferencesClient preferences;

    /** Creates new form PreferencesPanelDirectories */
    public PreferencesPanelSystemMaintenance(BaseClient baseClient) {
        //load resource bundle
        try {
            this.rb = (MecResourceBundle) ResourceBundle.getBundle(
                    ResourceBundlePreferences.class.getName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Oops..resource bundle "
                    + e.getClassName() + " not found.");
        }
        this.initComponents();
        this.preferences = new PreferencesClient(baseClient);
        if (this.preferences.getBoolean(PreferencesAS2.COMMUNITY_EDITION)) {
            this.jCheckBoxDeleteStatsOlderThan.setVisible(false);
            this.jTextFieldDeleteStatsOlderThan.setVisible(false);
            this.jLabelDays2.setVisible(false);
        }
        this.jComboBoxTimeUnit.addItem(new TimeUnitMaintenance(TimeUnitMaintenance.MULTIPLIER_DAY));
        this.jComboBoxTimeUnit.addItem(new TimeUnitMaintenance(TimeUnitMaintenance.MULTIPLIER_HOUR));
        this.jComboBoxTimeUnit.addItem(new TimeUnitMaintenance(TimeUnitMaintenance.MULTIPLIER_MINUTE));        
    }

    /**Sets new preferences to this panel to changes/modify
     */
    @Override
    public void loadPreferences(){
        this.jCheckBoxDeleteMsgOlderThan.setSelected(this.preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE));
        this.jTextFieldDeleteMsgOlderThan.setText(String.valueOf(this.preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN)));
        this.jComboBoxTimeUnit.setSelectedItem(new TimeUnitMaintenance(this.preferences.getInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S)));
        if (!this.preferences.getBoolean(PreferencesAS2.COMMUNITY_EDITION)) {
            this.jCheckBoxDeleteStatsOlderThan.setSelected(this.preferences.getBoolean(PreferencesAS2.AUTO_STATS_DELETE));
            this.jTextFieldDeleteStatsOlderThan.setText(String.valueOf(this.preferences.getInt(PreferencesAS2.AUTO_STATS_DELETE_OLDERTHAN)));
        }
        this.jComboBoxTimeUnit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                savePreferences();
            }
        });
    }

    /**Stores the GUI settings in the preferences
     */
    @Override
    public void savePreferences() {
        try {
            int olderThanFiles = Integer.valueOf(this.jTextFieldDeleteMsgOlderThan.getText()).intValue();
            //do not allow negative values or the 0
            if (olderThanFiles <= 0) {
                olderThanFiles = Integer.getInteger(this.preferences.getDefaultValue(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN)).intValue();
            }
            this.preferences.putInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN, olderThanFiles);
            this.preferences.putInt(PreferencesAS2.AUTO_MSG_DELETE_OLDERTHAN_MULTIPLIER_S, 
                    (int)((TimeUnitMaintenance)this.jComboBoxTimeUnit.getSelectedItem()).getMultiplier());
            this.preferences.putBoolean(PreferencesAS2.AUTO_MSG_DELETE, this.jCheckBoxDeleteMsgOlderThan.isSelected());
            //stats auto delete capabilites
            if (!this.preferences.getBoolean(PreferencesAS2.COMMUNITY_EDITION)) {
                int olderThanStats = Integer.valueOf(this.jTextFieldDeleteStatsOlderThan.getText()).intValue();
                if (olderThanStats <= 0) {
                    olderThanStats = Integer.getInteger(this.preferences.getDefaultValue(PreferencesAS2.AUTO_STATS_DELETE_OLDERTHAN)).intValue();
                }
                this.preferences.putInt(PreferencesAS2.AUTO_STATS_DELETE_OLDERTHAN, olderThanStats);
                this.preferences.putBoolean(PreferencesAS2.AUTO_STATS_DELETE, this.jCheckBoxDeleteStatsOlderThan.isSelected());
            }
        } catch (Exception nop) {
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelSpace = new javax.swing.JPanel();
        jCheckBoxDeleteMsgOlderThan = new javax.swing.JCheckBox();
        jTextFieldDeleteMsgOlderThan = new javax.swing.JTextField();
        jCheckBoxDeleteStatsOlderThan = new javax.swing.JCheckBox();
        jTextFieldDeleteStatsOlderThan = new javax.swing.JTextField();
        jLabelDays2 = new javax.swing.JLabel();
        jComboBoxTimeUnit = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelSpace, gridBagConstraints);

        jCheckBoxDeleteMsgOlderThan.setText(this.rb.getResourceString( "label.deletemsgolderthan"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 5, 5);
        add(jCheckBoxDeleteMsgOlderThan, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 5, 5);
        add(jTextFieldDeleteMsgOlderThan, gridBagConstraints);

        jCheckBoxDeleteStatsOlderThan.setText(this.rb.getResourceString( "label.deletestatsolderthan"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jCheckBoxDeleteStatsOlderThan, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jTextFieldDeleteStatsOlderThan, gridBagConstraints);

        jLabelDays2.setText(this.rb.getResourceString( "label.days" ));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabelDays2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(20, 5, 5, 5);
        add(jComboBoxTimeUnit, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxDeleteMsgOlderThan;
    private javax.swing.JCheckBox jCheckBoxDeleteStatsOlderThan;
    private javax.swing.JComboBox jComboBoxTimeUnit;
    private javax.swing.JLabel jLabelDays2;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JTextField jTextFieldDeleteMsgOlderThan;
    private javax.swing.JTextField jTextFieldDeleteStatsOlderThan;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getIconResource() {
        return ("/de/mendelson/comm/as2/preferences/maintenance32x32.gif");
    }

    @Override
    public String getTabResource() {
        return ("tab.maintenance");
    }
    
    public static class TimeUnitMaintenance{
        
        public static final long MULTIPLIER_DAY = TimeUnit.DAYS.toSeconds(1);
        public static final long MULTIPLIER_HOUR = TimeUnit.HOURS.toSeconds(1);
        public static final long MULTIPLIER_MINUTE = TimeUnit.MINUTES.toSeconds(1);
        
        private long multiplier = MULTIPLIER_DAY;
        
        public TimeUnitMaintenance( long multiplier ){
            this.multiplier = multiplier;
        }
        
        @Override
        public String toString(){
            if( this.getMultiplier() == MULTIPLIER_DAY){
                return( rb.getResourceString("maintenancemultiplier.day"));
            }else if( this.getMultiplier() == MULTIPLIER_HOUR){
                return( rb.getResourceString("maintenancemultiplier.hour"));
            }else if( this.getMultiplier() == MULTIPLIER_MINUTE){
                return( rb.getResourceString("maintenancemultiplier.minute"));
            }else return( "Unknown value " + this.getMultiplier());
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
            if (anObject != null && anObject instanceof TimeUnitMaintenance) {
                TimeUnitMaintenance entry = (TimeUnitMaintenance) anObject;
                return (entry.getMultiplier() == this.getMultiplier());
            }
            return (false);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (int) (this.getMultiplier() ^ (this.getMultiplier() >>> 32));
            return hash;
        }

        /**
         * @return the multiplier
         */
        public long getMultiplier() {
            return multiplier;
        }

        
        
    }
    
}
