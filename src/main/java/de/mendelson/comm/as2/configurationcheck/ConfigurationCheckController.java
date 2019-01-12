//$Header: /as2/de/mendelson/comm/as2/configurationcheck/ConfigurationCheckController.java 12    4.10.18 13:23 Heller $
package de.mendelson.comm.as2.configurationcheck;

import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.timing.CertificateExpireController;
import de.mendelson.comm.as2.message.MessageAccessDB;
import de.mendelson.comm.as2.partner.Partner;
import de.mendelson.comm.as2.partner.PartnerAccessDB;
import de.mendelson.util.AS2Tools;
import de.mendelson.util.security.cert.CertificateManager;
import de.mendelson.util.security.cert.KeystoreCertificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Checks several issues of the configuration
 *
 * @author S.Heller
 * @version $Revision: 12 $
 */
public class ConfigurationCheckController {

    private CertificateManager managerEncSign;
    private CertificateManager managerSSL;
    private ConfigurationCheckThread checkThread;
    private Connection configConnection;
    private Connection runtimeConnection;
    private PreferencesAS2 preferences = new PreferencesAS2();

    public ConfigurationCheckController(CertificateManager managerEncSign, CertificateManager managerSSL, Connection configConnection,
            Connection runtimeConnection) {
        this.configConnection = configConnection;
        this.runtimeConnection = runtimeConnection;
        this.managerEncSign = managerEncSign;
        this.managerSSL = managerSSL;
    }

    /**
     * Returns all available issues that are detected by the server control
     */
    public List<ConfigurationIssue> getIssues() {
        return (this.checkThread.getIssues());
    }

    /**
     * Runs the configuration checks once - outside the thread context
     */
    public List<ConfigurationIssue> runOnce() {
        ConfigurationCheckThread testThread = new ConfigurationCheckThread(this.configConnection, this.runtimeConnection);
        testThread.runAllChecks();
        return (this.getIssues());
    }

    /**
     * Starts the embedded task that guards the log
     */
    public void start() {
        this.checkThread = new ConfigurationCheckThread(this.configConnection, this.runtimeConnection);
        Executors.newSingleThreadExecutor().submit(this.checkThread);
    }

    public class ConfigurationCheckThread implements Runnable {

        private final List<ConfigurationIssue> issueList = Collections.synchronizedList(new ArrayList<ConfigurationIssue>());
        private Connection configConnection;
        private Connection runtimeConnection;
        private boolean stopRequested = false;
        //wait this time between checks, once a day
        private final long WAIT_TIME = TimeUnit.SECONDS.toMillis(30);

        public ConfigurationCheckThread(Connection configConnection, Connection runtimeConnection) {
            this.configConnection = configConnection;
            this.runtimeConnection = runtimeConnection;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Configuration check thread");
            while (!stopRequested) {
                synchronized (this.issueList) {
                    this.issueList.clear();
                    this.runAllChecks();
                }
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    //nop
                }
            }
        }

        public List<ConfigurationIssue> getIssues() {
            List<ConfigurationIssue> issues = new ArrayList<ConfigurationIssue>();
            synchronized (this.issueList) {
                issues.addAll(this.issueList);
            }
            return (issues);
        }
        
        public void runAllChecks() {
            this.checkCertificatesExpired();
            this.checkSSLKeystore();
            this.checkAutoDelete();
            this.checkCPUCores();
            this.checkHeapMemory();
            this.checkOutboundConnectionsAllowed();
            this.checkAllPartnersCertificatesAvailable();
            this.checkDataModel32bit();
        }

        /**
         * It is possible that a keystore has been modified by an external
         * program - or deleted?
         *
         */
        private void checkAllPartnersCertificatesAvailable() {
            PartnerAccessDB partnerAccess = new PartnerAccessDB(this.configConnection, this.runtimeConnection);
            List<Partner> partnerList = partnerAccess.getPartner();
            for (Partner partner : partnerList) {
                String cryptFingerprint = partner.getCryptFingerprintSHA1();
                String signFingerprint = partner.getSignFingerprintSHA1();
                KeystoreCertificate certEncrypt = managerEncSign.getKeystoreCertificateByFingerprintSHA1(cryptFingerprint);
                if (certEncrypt == null) {
                    ConfigurationIssue issue = null;
                    if (partner.isLocalStation()) {
                        issue = new ConfigurationIssue(ConfigurationIssue.KEY_MISSING_ENC_LOCAL_STATION);
                    } else {
                        issue = new ConfigurationIssue(ConfigurationIssue.CERTIFICATE_MISSING_ENC_REMOTE_PARTNER);
                    }
                    issue.setDetails(partner.getName());
                    synchronized (issueList) {
                        issueList.add(issue);
                    }
                }
                KeystoreCertificate certSign = managerEncSign.getKeystoreCertificateByFingerprintSHA1(signFingerprint);
                if (certSign == null) {
                    ConfigurationIssue issue = null;
                    if (partner.isLocalStation()) {
                        issue = new ConfigurationIssue(ConfigurationIssue.KEY_MISSING_SIGN_LOCAL_STATION);
                    } else {
                        issue = new ConfigurationIssue(ConfigurationIssue.CERTIFICATE_MISSING_SIGN_REMOTE_PARTNER);
                    }
                    issue.setDetails(partner.getName());
                    synchronized (this.issueList) {
                        this.issueList.add(issue);
                    }
                }

            }
        }

        private void checkCertificatesExpired() {
            List<KeystoreCertificate> encSignList = managerEncSign.getKeyStoreCertificateList();
            for (KeystoreCertificate cert : encSignList) {
                if (CertificateExpireController.getCertificateExpireDuration(cert) <= 0) {
                    ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.CERTIFICATE_EXPIRED_ENC_SIGN);
                    issue.setDetails(cert.getAlias());
                    synchronized (this.issueList) {
                        this.issueList.add(issue);
                    }
                }
            }
            List<KeystoreCertificate> sslList = managerSSL.getKeyStoreCertificateList();
            for (KeystoreCertificate cert : sslList) {
                if (CertificateExpireController.getCertificateExpireDuration(cert) <= 0) {
                    ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.CERTIFICATE_EXPIRED_SSL);
                    issue.setDetails(cert.getAlias());
                    synchronized (this.issueList) {
                        this.issueList.add(issue);
                    }
                }
            }
        }

        private void checkDataModel32bit() {
            String dataModel = "";
            try {
                dataModel = System.getProperty("sun.arch.data.model");
                int bits = Integer.parseInt(dataModel);
                if (bits == 32) {
                    ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.JVM_32_BIT);
                    synchronized (issueList) {
                        this.issueList.add(issue);
                    }
                }
            } catch (Throwable e) {
                //ignore this - it does work only on oracle VMs. If the property is not supported it will return "unknown" which could
                //not be parsed as an integer and will result in a NumberFormatException
            }
        }

        private void checkOutboundConnectionsAllowed() {
            int numberOfConnections = preferences.getInt(PreferencesAS2.MAX_OUTBOUND_CONNECTIONS);
            if (numberOfConnections == 0) {
                ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.NO_OUTBOUND_CONNECTIONS_ALLOWED);
                issue.setDetails("");
                synchronized (issueList) {
                    this.issueList.add(issue);
                }
            }
        }

        /**
         * Finds out some issues that could occure in the SSL configuration: no
         * SSL key set, multiple SSL keys, use of public test keys as SSL key
         */
        private void checkSSLKeystore() {
            List<KeystoreCertificate> sslList = managerSSL.getKeyStoreCertificateList();
            StringBuilder aliasList = new StringBuilder();
            int keyCount = 0;
            List<KeystoreCertificate> keystoreKeysList = new ArrayList<KeystoreCertificate>();
            for (KeystoreCertificate cert : sslList) {
                if (cert.getIsKeyPair()) {
                    if (aliasList.length() > 0) {
                        aliasList.append(", ");
                    }
                    aliasList.append(cert.getAlias());
                    keystoreKeysList.add(cert);
                    keyCount++;
                }
            }
            if (keyCount == 0) {
                ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.NO_KEY_IN_SSL_KEYSTORE);
                synchronized (this.issueList) {
                    this.issueList.add(issue);
                }
            } else if (keyCount > 1) {
                ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.MULTIPLE_KEYS_IN_SSL_KEYSTORE);
                issue.setDetails(aliasList.toString());
                synchronized (this.issueList) {
                    this.issueList.add(issue);
                }
            } else {
                KeystoreCertificate usedSSLKey = keystoreKeysList.get(0);
                String foundFingerprint = usedSSLKey.getFingerPrintSHA1();
                for (String testFingerprint : KeystoreCertificate.TEST_KEYS_FINGERPRINTS_SHA1) {
                    if (foundFingerprint.equalsIgnoreCase(testFingerprint)) {
                        ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.USE_OF_TEST_KEYS_IN_SSL);
                        issue.setDetails(usedSSLKey.getAlias());
                        synchronized (this.issueList) {
                            this.issueList.add(issue);
                        }
                    }
                }
            }
        }

        private void checkAutoDelete() {
            if (!preferences.getBoolean(PreferencesAS2.AUTO_MSG_DELETE)) {
                MessageAccessDB messageAccess = new MessageAccessDB(configConnection, runtimeConnection);
                int transmissionCount = messageAccess.getMessageCount();
                if (transmissionCount > 30000) {
                    ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.HUGE_AMOUNT_OF_TRANSACTIONS_NO_AUTO_DELETE);
                    issue.setDetails(String.valueOf(transmissionCount));
                    synchronized (this.issueList) {
                        this.issueList.add(issue);
                    }
                }
            }
        }

        private void checkCPUCores() {
            int cores = Runtime.getRuntime().availableProcessors();
            if (cores < 4) {
                ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.FEW_CPU_CORES);
                issue.setDetails(String.valueOf(cores));
                synchronized (this.issueList) {
                    this.issueList.add(issue);
                }
            }
        }

        private void checkHeapMemory() {
            long maxMemory = Runtime.getRuntime().maxMemory();
            if (maxMemory < 950000000L) {
                ConfigurationIssue issue = new ConfigurationIssue(ConfigurationIssue.LOW_MAX_HEAP_MEMORY);
                issue.setDetails(AS2Tools.getDataSizeDisplay(maxMemory));
                synchronized (this.issueList) {
                    this.issueList.add(issue);
                }
            }
        }
    }
}
